package com.erp.shared;

import java.lang.reflect.Field;
import jakarta.persistence.Column;

/** Enforces schema constraints before persistence, with usable field messages. */
public final class EntityValidation {
    private EntityValidation() {}
    public static void validate(BaseEntity entity) {
        for (Field f : CrudService.dataFields(entity.getClass())) {
            if (!RecordPolicy.writable(f)) continue;
            Column column = f.getAnnotation(Column.class);
            if (column == null) continue;
            try {
                Object value = f.get(entity);
                String label = f.getName().replaceAll("([a-z])([A-Z])", "$1 $2");
                if (!column.nullable() && (value == null || value instanceof String s && s.isBlank()))
                    throw ApiException.badRequest(label + " is required.");
                if (value instanceof String s && s.length() > column.length())
                    throw ApiException.badRequest(label + " must be at most " + column.length() + " characters.");
                if (value instanceof java.math.BigDecimal n && column.precision() > 0
                        && (n.scale() > column.scale() || n.precision() - n.scale() > column.precision() - column.scale()))
                    throw ApiException.badRequest(label + " exceeds the allowed precision (" + column.precision() + ", " + column.scale() + ").");
            } catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }
        }
    }
}
