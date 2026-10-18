package com.erp.desktop.api;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Conversion helpers between the JSON wire format and what the JavaFX controls hold.
 */
public final class Values {
    private static final ThreadLocal<java.text.DecimalFormat> MONEY = ThreadLocal.withInitial(() ->
            new java.text.DecimalFormat("#,##0.00##", java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US)));

    private Values() {
    }

    /** Cell text for a grid column. */
    public static String display(JsonNode row, String field) {
        JsonNode value = row.get(field);
        if (value == null || value.isNull()) {
            return "";
        }
        if (value.isBoolean()) {
            return value.asBoolean() ? "Yes" : "No";
        }
        if (value.isNumber()) {
            String name = field.toLowerCase(java.util.Locale.ROOT);
            boolean money = name.endsWith("amount") || name.endsWith("balance") || name.endsWith("cost") || name.endsWith("price")
                    || name.equals("totalvalue") || name.equals("totaldebit") || name.equals("totalcredit") || name.equals("debit") || name.equals("credit") || name.equals("basicsalary");
            if (money) return MONEY.get().format(value.decimalValue());
            if (value.isFloatingPointNumber()) return value.decimalValue().stripTrailingZeros().toPlainString();
        }
        if (value.isTextual()) {
            String text = value.asText();
            // Instant values arrive as ISO-8601; trim to something readable.
            if (text.length() > 16 && text.indexOf(':') > 0 && text.endsWith("Z")) {
                return text.substring(0, 16).replace('T', ' ');
            }
            return text;
        }
        return value.asText();
    }

    /** Writes a form value into the payload using the field's declared kind. */
    public static void put(ObjectNode payload, EntityMeta.Field field, String raw) {
        String text = raw == null ? "" : raw.trim();
        if (field.kind == EntityMeta.Kind.PASSWORD) text = raw == null ? "" : raw;
        if (field.required && text.isBlank()) throw new IllegalArgumentException(field.label + " is required.");
        if (field.maxLength > 0 && text.length() > field.maxLength) throw new IllegalArgumentException(field.label + " must be at most " + field.maxLength + " characters.");
        if (text.isEmpty()) {
            payload.putNull(field.name);
            return;
        }
        switch (field.kind) {
            case INTEGER -> {
                try {
                    long number = field.javaType.equals("Integer") || field.javaType.equals("int")
                            ? Integer.parseInt(text) : Long.parseLong(text);
                    payload.put(field.name, number);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(field.label + " must be a whole number.");
                }
            }
            case DECIMAL -> {
                try {
                    payload.put(field.name, new BigDecimal(text));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(field.label + " must be a valid decimal number.");
                }
            }
            case BOOLEAN -> {
                if (!text.equals("true") && !text.equals("false"))
                    throw new IllegalArgumentException("Choose Yes or No for " + field.label + ".");
                payload.put(field.name, Boolean.parseBoolean(text));
            }
            case DATE -> {
                try {
                    if (field.javaType.equals("Instant")) java.time.Instant.parse(text);
                    else if (field.javaType.equals("LocalDateTime")) java.time.LocalDateTime.parse(text);
                    else java.time.LocalDate.parse(text);
                    payload.put(field.name, text);
                } catch (java.time.format.DateTimeParseException ex) { throw new IllegalArgumentException(field.label + " needs a valid ISO date or timestamp."); }
            }
            case ENUM -> {
                if (!field.enumValues.contains(text)) throw new IllegalArgumentException("Choose a valid " + field.label + ".");
                payload.put(field.name, text);
            }
            case STRING, PASSWORD -> payload.put(field.name, text);
        }
    }

    /** Current value of a field as text, for pre-filling the edit form. */
    public static String asText(JsonNode row, String field) {
        JsonNode value = row.get(field);
        if (value == null || value.isNull()) {
            return "";
        }
        return value.asText();
    }
}
