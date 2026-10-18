package com.erp.shared;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

/**
 * One generic persistence engine behind every entity in the platform.
 *
 * <p>Every one of the ~80 entities across the 13 divisions gets list / get / create /
 * update / soft-delete, free-text search, per-field filtering with range operators,
 * sorting and CSV export from this class. Module-specific behaviour hooks in through
 * {@link CrudResource#beforeSave} etc, never by duplicating persistence code.
 *
 * <p>Filter fields are validated against the entity's real declared fields, so a client
 * cannot inject JPQL through a query parameter.
 */
@ApplicationScoped
public class CrudService {

    @Inject
    EntityManager em;

    // ------------------------------------------------------------------ read

    public <T extends BaseEntity> PageResult<T> search(Class<T> cls, CrudQuery q, String[] searchFields) {
        Where where = buildWhere(cls, q, searchFields);

        String countJpql = "select count(e) from " + entityName(cls) + " e" + where.clause;
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        where.bind(countQuery);
        long total = countQuery.getSingleResult();

        String orderBy = orderBy(cls, q);
        String jpql = "select e from " + entityName(cls) + " e" + where.clause + orderBy;
        TypedQuery<T> query = em.createQuery(jpql, cls);
        where.bind(query);
        long offset = (long) q.page() * q.size();
        if (offset > Integer.MAX_VALUE) throw ApiException.badRequest("Page number is too large.");
        query.setFirstResult((int) offset);
        query.setMaxResults(q.size());

        return new PageResult<>(query.getResultList(), total, q.page(), q.size());
    }

    public <T extends BaseEntity> List<T> findAll(Class<T> cls, CrudQuery q, String[] searchFields) {
        Where where = buildWhere(cls, q, searchFields);
        TypedQuery<T> query = em.createQuery(
                "select e from " + entityName(cls) + " e" + where.clause + orderBy(cls, q), cls);
        where.bind(query);
        query.setMaxResults(q.size());
        return query.getResultList();
    }

    public <T extends BaseEntity> T get(Class<T> cls, Long id) {
        if (id == null) throw ApiException.badRequest("A record ID is required.");
        T found = em.find(cls, id);
        if (found == null || Boolean.FALSE.equals(found.active)) {
            throw ApiException.notFound(cls.getSimpleName() + " " + id + " was not found.");
        }
        return found;
    }

    public <T extends BaseEntity> T findByIdOrNull(Class<T> cls, Long id) {
        return em.find(cls, id);
    }

    // ----------------------------------------------------------------- write

    public <T extends BaseEntity> T save(T entity) {
        if (entity.id == null) {
            em.persist(entity);
            return entity;
        }
        return em.merge(entity);
    }

    /** NFR-DR-04: business records are tombstoned, never physically removed here. */
    public <T extends BaseEntity> void softDelete(T entity) {
        entity.active = Boolean.FALSE;
        entity.updatedAt = java.time.Instant.now();
        em.merge(entity);
    }

    // --------------------------------------------------------------- helpers

    public static String entityName(Class<?> cls) {
        Entity ann = cls.getAnnotation(Entity.class);
        if (ann != null && ann.name() != null && !ann.name().isBlank()) {
            return ann.name();
        }
        return cls.getSimpleName();
    }

    public static List<Field> dataFields(Class<?> cls) {
        List<Field> out = new ArrayList<>();
        for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                int mods = f.getModifiers();
                if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
                    continue;
                }
                if (f.isSynthetic() || f.getName().startsWith("$") || f.isAnnotationPresent(jakarta.persistence.Transient.class)) {
                    continue;
                }
                // Quarkus/Hibernate bytecode enhancement rewrites public entity fields to
                // {@code protected} (it generates getter/setter pairs plus $$_hibernate_*
                // accessors for dirty tracking and lazy loading). We therefore accept both
                // public and protected columns, otherwise every enhanced entity would expose
                // only {@code id} through the schema-driven UI, the search/filter engine and
                // CSV export (the Audit Logs "Could not read field metadata" symptom).
                if (!Modifier.isPublic(mods) && !Modifier.isProtected(mods)) {
                    continue;
                }
                f.trySetAccessible();
                out.add(f);
            }
        }
        return out;
    }

    public static boolean hasField(Class<?> cls, String name) {
        return dataFields(cls).stream().anyMatch(f -> f.getName().equals(name));
    }

    // ---------------------------------------------------------------- where

    private <T extends BaseEntity> Where buildWhere(Class<T> cls, CrudQuery q, String[] searchFields) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> params = new LinkedHashMap<>();
        List<String> conditions = new ArrayList<>();

        if (!q.includeInactive()) conditions.add("e.active = true");

        if (q.companyId() != null && hasField(cls, "companyId")) {
            conditions.add("e.companyId = :__companyId");
            params.put("__companyId", q.companyId());
        }

        if (q.search() != null && searchFields != null && searchFields.length > 0) {
            List<String> parts = new ArrayList<>();
            for (String f : searchFields) {
                if (hasField(cls, f) && fieldType(cls, f) == String.class && !RecordPolicy.sensitive(f)) {
                    parts.add("lower(coalesce(e." + f + ",'')) like :__q");
                }
            }
            if (!parts.isEmpty()) {
                conditions.add("(" + String.join(" or ", parts) + ")");
                params.put("__q", "%" + q.search().toLowerCase(java.util.Locale.ROOT) + "%");
            }
        }

        int i = 0;
        for (Map.Entry<String, String> entry : q.filters().entrySet()) {
            String raw = entry.getKey();
            String value = entry.getValue();
            if (value == null || value.isBlank()) {
                continue;
            }
            String operator = "=";
            String field = raw;
            for (String suffix : new String[] { "__gte", "__lte", "__gt", "__lt", "__ne", "__in" }) {
                if (raw.endsWith(suffix)) {
                    field = raw.substring(0, raw.length() - suffix.length());
                    operator = switch (suffix) {
                        case "__gte" -> ">=";
                        case "__lte" -> "<=";
                        case "__gt" -> ">";
                        case "__lt" -> "<";
                        case "__ne" -> "<>";
                        default -> "in";
                    };
                    break;
                }
            }
            if (!hasField(cls, field) || RecordPolicy.sensitive(field)) {
                // Unknown or non-public field: ignore rather than fail the whole query.
                continue;
            }
            String param = "__f" + (i++);
            Class<?> type = fieldType(cls, field);

            if ("in".equals(operator)) {
                List<Object> values = new ArrayList<>();
                for (String v : value.split(",")) {
                    values.add(coerce(v.trim(), type));
                }
                if (values.isEmpty()) {
                    continue;
                }
                conditions.add("e." + field + " in (:" + param + ")");
                params.put(param, values);
            } else {
                Object coerced = coerce(value, type);
                if (coerced == null) {
                    continue;
                }
                conditions.add("e." + field + " " + operator + " :" + param);
                params.put(param, coerced);
            }
        }

        if (!conditions.isEmpty()) {
            sb.append(" where ").append(String.join(" and ", conditions));
        }
        return new Where(sb.toString(), params);
    }

    private <T extends BaseEntity> String orderBy(Class<T> cls, CrudQuery q) {
        String sort = q.sort();
        if (!hasField(cls, sort)) {
            sort = "id";
        }
        return " order by e." + sort + (q.descending() ? " desc" : " asc") + ", e.id desc";
    }

    private static Class<?> fieldType(Class<?> cls, String name) {
        return dataFields(cls).stream()
                .filter(f -> f.getName().equals(name))
                .map(Field::getType)
                .findFirst()
                .orElse(String.class);
    }

    /** Coerce a string filter value into the field's Java type. Returns null when impossible. */
    static Object coerce(String value, Class<?> type) {
        if (type == null || value == null) {
            return null;
        }
        try {
            if (type == String.class) {
                return value;
            }
            if (type == Long.class || type == long.class) {
                return Long.valueOf(value);
            }
            if (type == Integer.class || type == int.class) {
                return Integer.valueOf(value);
            }
            if (type == Boolean.class || type == boolean.class) {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) return null;
                return Boolean.valueOf(value);
            }
            if (type == java.math.BigDecimal.class) {
                return new java.math.BigDecimal(value);
            }
            if (type == LocalDate.class) {
                return LocalDate.parse(value);
            }
            if (type == java.time.Instant.class) return java.time.Instant.parse(value);
            if (type == LocalDateTime.class) {
                return LocalDateTime.parse(value);
            }
            if (type.isEnum()) {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                Object enumValue = Enum.valueOf((Class<Enum>) type, value);
                return enumValue;
            }
        } catch (RuntimeException e) {
            return null;
        }
        return value;
    }

    /** A rendered where clause plus its bound parameters. */
    private record Where(String clause, Map<String, Object> params) {
        void bind(jakarta.persistence.Query query) {
            params.forEach(query::setParameter);
        }
    }
}
