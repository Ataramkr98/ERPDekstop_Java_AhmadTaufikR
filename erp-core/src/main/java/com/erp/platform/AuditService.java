package com.erp.platform;

import java.util.Map;

import com.erp.config.CurrentUser;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Append-only audit trail (PRD X-AUD-001, M-ADM-006, NFR-S-06).
 *
 * <p>Every create/update/delete through {@code CrudResource} lands here with the
 * before and after snapshot, so the audit log answers "who changed what, when".
 */
@ApplicationScoped
public class AuditService {

    @Inject
    CurrentUser currentUser;

    @Transactional
    public void log(String action, String entityType, Long entityId, String before, String after) {
        AuditLog entry = new AuditLog();
        entry.action = action;
        entry.entityType = entityType;
        entry.entityId = entityId;
        entry.beforeValue = truncate(before);
        entry.afterValue = truncate(after);
        entry.username = currentUser.username();
        entry.companyId = currentUser.companyId();
        entry.occurredAt = java.time.Instant.now();
        entry.persist();
    }

    public void created(Object entity) {
        if (entity == null) {
            return;
        }
        log("CREATE", entity.getClass().getSimpleName(), idOf(entity), null, Snapshot.of(entity));
    }

    public void updated(String entityType, Long id, String before, String after) {
        log("UPDATE", entityType, id, before, after);
    }

    public void deleted(String entityType, Long id, String before) {
        log("DELETE", entityType, id, before, null);
    }

    public void custom(String action, String entityType, Long entityId, String detail) {
        log(action, entityType, entityId, null, detail);
    }

    private static Long idOf(Object entity) {
        try {
            Object v = entity.getClass().getField("id").get(entity);
            return v instanceof Number n ? n.longValue() : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 8000 ? s.substring(0, 8000) + "…(truncated)" : s;
    }

    /** Renders an entity as a stable field/value map for the before/after columns. */
    public static final class Snapshot {
        public static String of(Object entity) {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            for (java.lang.reflect.Field f : com.erp.shared.CrudService.dataFields(entity.getClass())) {
                if (com.erp.shared.RecordPolicy.sensitive(f.getName()) || f.getName().equals("version") || f.getName().equals("updatedAt")
                        || f.getName().equals("updatedBy")) {
                    continue;
                }
                try {
                    map.put(f.getName(), f.get(entity));
                } catch (ReflectiveOperationException ignored) {
                    // Field inaccessible: skip it rather than failing the business write.
                }
            }
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(map);
            } catch (Exception e) {
                return map.toString();
            }
        }
    }
}
