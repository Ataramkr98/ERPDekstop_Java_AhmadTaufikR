package com.erp.desktop.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Parsed form of the {@code GET /meta} descriptor that CrudResource publishes.
 *
 * <p>Because the server describes its own fields, the grid and the edit form are
 * built at runtime: adding a column to an entity needs no change here.
 */
public class EntityMeta {

    /** Server-reported field kinds. */
    public enum Kind {
        STRING, INTEGER, DECIMAL, BOOLEAN, DATE, ENUM, PASSWORD;

        public static Kind of(String raw) {
            if (raw == null) {
                return STRING;
            }
            return switch (raw.toLowerCase()) {
                case "integer" -> INTEGER;
                case "decimal" -> DECIMAL;
                case "boolean" -> BOOLEAN;
                case "date" -> DATE;
                case "enum" -> ENUM;
                case "password" -> PASSWORD;
                default -> STRING;
            };
        }
    }

    /** One editable/visible attribute. */
    public static class Field {
        public final String name;
        public final String label;
        public final Kind kind;
        public final boolean readOnly;
        public final List<String> enumValues;
        public boolean required;
        public int maxLength;
        public String javaType = "String";
        public String defaultValue = "";
        public String referencePath = "";
        public String referenceLabel = "";

        public Field(String name, String label, Kind kind, boolean readOnly, List<String> enumValues) {
            this.name = name;
            this.label = label;
            this.kind = kind;
            this.readOnly = readOnly;
            this.enumValues = enumValues == null ? List.of() : enumValues;
        }

        /** Fields like {@code companyId} are shown in the grid but never in the form. */
        public boolean isHiddenInForm() {
            return readOnly || name.equals("id") || name.equals("companyId") || name.equals("version");
        }
    }

    private final String entity;
    private final List<Field> fields;
    private final List<String> searchFields;
    private final boolean companyScoped;
    private boolean readOnly;
    private boolean importable = true;
    private String readOnlyReason = "Read-only records";
    public boolean readOnly() { return readOnly; }
    public boolean importable() { return importable && !readOnly; }
    public String readOnlyReason() { return readOnlyReason; }

    public EntityMeta(String entity, List<Field> fields, List<String> searchFields,
                      boolean companyScoped) {
        this.entity = entity;
        this.fields = fields;
        this.searchFields = searchFields;
        this.companyScoped = companyScoped;
    }

    public String entity() {
        return entity;
    }

    public List<Field> fields() {
        return fields;
    }

    public List<String> searchFields() {
        return searchFields;
    }

    public boolean companyScoped() {
        return companyScoped;
    }

    /** Fields shown as grid columns, sans the ones too wide/noisy to tabulate. */
    public List<Field> gridFields() {
        List<Field> out = new ArrayList<>();
        for (Field f : fields) {
            if (f.kind == Kind.PASSWORD || f.name.equals("companyId") || f.name.equals("version")) continue;
            if (f.name.equals("id")) {
                out.add(0, f);
            } else {
                out.add(f);
            }
        }
        List<String> priority = List.of("id", "code", "name", "username", "fullName", "title", "invoiceNumber", "orderNumber",
                "receiptNumber", "deliveryNumber", "entryNumber", "invoiceDate", "orderDate", "receiptDate", "deliveryDate",
                "postingDate", "dueDate", "status", "totalAmount", "balanceAmount", "totalDebit", "totalCredit", "totalValue", "currency");
        out.sort(java.util.Comparator.comparingInt(field -> {
            int rank = priority.indexOf(field.name);
            return rank < 0 ? priority.size() : rank;
        }));
        return out;
    }

    /** Parses the JSON returned by {@code GET /meta}. */
    public static EntityMeta parse(JsonNode node) {
        String entity = node.path("entity").asText("?");
        boolean companyScoped = node.path("companyScoped").asBoolean(true);

        List<String> search = new ArrayList<>();
        JsonNode searchNode = node.path("searchFields");
        if (searchNode.isArray()) {
            for (JsonNode s : searchNode) {
                search.add(s.asText());
            }
        }

        List<Field> fields = new ArrayList<>();
        JsonNode fieldNodes = node.path("fields");
        if (fieldNodes.isArray()) {
            for (JsonNode f : fieldNodes) {
                List<String> enumValues = new ArrayList<>();
                JsonNode en = f.path("enumValues");
                if (en.isArray()) {
                    for (JsonNode v : en) {
                        enumValues.add(v.asText());
                    }
                }
                Field descriptor = new Field(
                        f.path("name").asText(),
                        f.path("label").asText(f.path("name").asText()),
                        Kind.of(f.path("type").asText()),
                        f.path("readOnly").asBoolean(false),
                        enumValues);
                descriptor.required = f.path("required").asBoolean(false);
                descriptor.maxLength = f.path("maxLength").asInt(0);
                descriptor.javaType = f.path("javaType").asText("String");
                descriptor.defaultValue = f.path("defaultValue").asText("");
                descriptor.referencePath = f.path("referencePath").asText("");
                descriptor.referenceLabel = f.path("referenceLabel").asText("");
                fields.add(descriptor);
            }
        }
        EntityMeta result = new EntityMeta(entity, fields, search, companyScoped);
        result.readOnly = node.path("readOnly").asBoolean(false);
        result.importable = node.path("importable").asBoolean(true);
        result.readOnlyReason = node.path("readOnlyReason").asText("Read-only records");
        return result;
    }
}
