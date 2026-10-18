package com.erp.shared;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal RFC-4180 reader/writer. Fields are quoted when they contain a separator,
 * quote or newline; embedded quotes are doubled.
 */
public final class CsvSupport {

    private CsvSupport() {
    }

    public static List<List<String>> parse(String text) {
        List<List<String>> rows = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return rows;
        }
        if (text.startsWith("\uFEFF")) text = text.substring(1);
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        cell.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                row.add(cell.toString());
                cell.setLength(0);
            } else if (c == '\n') {
                row.add(cell.toString());
                cell.setLength(0);
                rows.add(row);
                row = new ArrayList<>();
            } else if (c != '\r') {
                cell.append(c);
            }
        }
        if (inQuotes) throw ApiException.badRequest("CSV contains an unclosed quoted field.");
        if (!cell.isEmpty() || !row.isEmpty()) {
            row.add(cell.toString());
            rows.add(row);
        }
        return rows;
    }

    public static String render(List<String> header, List<List<Object>> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter w = new PrintWriter(new java.io.OutputStreamWriter(out, StandardCharsets.UTF_8));
        w.print('\uFEFF'); // BOM so Excel opens UTF-8 correctly
        writeRow(w, header.stream().map(o -> (Object) o).toList());
        for (List<Object> r : rows) {
            writeRow(w, r);
        }
        w.flush();
        return out.toString(StandardCharsets.UTF_8);
    }

    private static void writeRow(PrintWriter w, List<Object> cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(cells.get(i)));
        }
        w.print(sb);
        w.print("\r\n");
    }

    private static String escape(Object value) {
        if (value == null) {
            return "";
        }
        String s = String.valueOf(value);
        if (value instanceof String && s.stripLeading().matches("^[=+@-].*")) s = "'" + s;
        if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }

    /** Exported columns: every public, non-audit field of the entity, id first. */
    public static List<Field> exportFields(Class<?> cls) {
        List<Field> all = CrudService.dataFields(cls);
        List<Field> out = new ArrayList<>();
        for (String preferred : new String[] { "id", "code", "name" }) {
            all.stream().filter(f -> f.getName().equals(preferred)).findFirst().ifPresent(out::add);
        }
        for (Field f : all) {
            if (!out.contains(f) && !RecordPolicy.sensitive(f.getName())) {
                out.add(f);
            }
        }
        return out;
    }
}
