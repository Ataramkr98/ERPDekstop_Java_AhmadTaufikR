package com.erp.platform;

import java.time.Instant;
import java.util.*;
import com.erp.config.CurrentUser;
import com.erp.shared.ApiException;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Authenticated @Path("/api/overview") @Produces(MediaType.APPLICATION_JSON)
public class OverviewResource {
    @Inject EntityManager em;
    @Inject CurrentUser user;
    @GET public Map<String,Object> overview() {
        Long company = user.companyId();
        if (company == null) throw ApiException.forbidden("Sign in to an active company.");
        Map<String,Object> result = new LinkedHashMap<>();
        Company organization = Company.findById(company);
        result.put("company", organization == null ? "Your workspace" : organization.name);
        result.put("updatedAt", Instant.now().toString());
        result.put("customers", count("SalCustomer", company));
        result.put("items", count("InvItem", company));
        result.put("orders", count("SalOrder", company));
        result.put("invoices", count("SalInvoice", company));
        // Dashboard headline metrics. The JavaFX overview renders exactly these keys, so
        // a renamed field here would blank a metric there: keep them in sync.
        result.put("openOrders", countWhere("SalOrder", company,
                "e.status in ('DRAFT','CONFIRMED','PARTIALLY_DELIVERED','PARTIALLY_INVOICED')"));
        result.put("overdueInvoices", countWhere("SalInvoice", company,
                "e.status <> 'VOID' and (e.status = 'OVERDUE' or (e.dueDate < current_date and e.balanceAmount > 0))"));
        result.put("goodsReceiptCount", count("PurGoodsReceipt", company));
        result.put("recentInvoices", recent("SalInvoice", company, 5));
        result.put("deliveries", recent("SalDelivery", company, 5));
        result.put("recentGoodsReceipts", recent("PurGoodsReceipt", company, 5));
        List<AuditLog> recent = em.createQuery("select a from AuditLog a where a.companyId = :c and a.active = true order by a.occurredAt desc, a.id desc", AuditLog.class)
                .setParameter("c", company).setMaxResults(12).getResultList();
        result.put("activity", recent.stream().map(a -> {
            Map<String,Object> event = new LinkedHashMap<>();
            event.put("time", a.occurredAt); event.put("action", a.action); event.put("entity", a.entityType);
            event.put("record", a.entityId); event.put("user", a.username); return event;
        }).toList());
        return result;
    }
    private long count(String entity, Long company) {
        return em.createQuery("select count(e) from " + entity + " e where e.companyId = :c and e.active = true", Long.class).setParameter("c", company).getSingleResult();
    }

    private long countWhere(String entity, Long company, String predicate) {
        return em.createQuery("select count(e) from " + entity + " e where e.companyId = :c and e.active = true and " + predicate, Long.class)
                .setParameter("c", company).getSingleResult();
    }

    /**
     * Latest rows for one entity as plain maps. Plain maps (not entities) keep the
     * payload small and cycle-free, and keep the JavaFX sections decoupled from new
     * entity fields. Only entities carrying {@code companyId} are eligible, so a
     * missing column here is a programming error, not a silent cross-tenant leak.
     */
    private Map<String, Object> recent(String entity, Long company, int limit) {
        List<?> rows = em.createQuery("select e from " + entity + " e where e.companyId = :c and e.active = true order by e.id desc", Object.class)
                .setParameter("c", company).setMaxResults(limit).getResultList();
        List<Map<String, Object>> items = new java.util.ArrayList<>();
        for (Object row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            for (java.lang.reflect.Field field : com.erp.shared.CrudService.dataFields(row.getClass())) {
                try {
                    Object value = field.get(row);
                    if (value == null) continue;
                    if (value instanceof java.time.LocalDate date) item.put(field.getName(), date.toString());
                    else if (value instanceof java.time.Instant instant) item.put(field.getName(), instant.toString());
                    else if (value instanceof Enum<?> e) item.put(field.getName(), e.name());
                    else if (value instanceof Number || value instanceof Boolean || value instanceof String) item.put(field.getName(), value);
                } catch (IllegalAccessException ignored) {
                    // A field that cannot be read is skipped: the section still renders.
                }
            }
            items.add(item);
        }
        Map<String, Object> table = new LinkedHashMap<>();
        table.put("items", items);
        table.put("total", items.size());
        return table;
    }
}
