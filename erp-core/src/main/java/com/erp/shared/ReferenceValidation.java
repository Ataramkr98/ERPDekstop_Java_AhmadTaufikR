package com.erp.shared;

import java.lang.reflect.Field;
import java.util.*;
import com.erp.platform.ModuleRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

/** Resolves known FK fields and locks parent documents before editing their lines. */
@ApplicationScoped
public class ReferenceValidation {
    @Inject EntityManager em;
    private static final Map<String,String> ALIASES = Map.ofEntries(
            Map.entry("receipt","PurGoodsReceipt"), Map.entry("receiptLine","PurGoodsReceiptLine"),
            Map.entry("user","AppUser"), Map.entry("period","PostingPeriod"), Map.entry("defaultCompany","Company"),
            Map.entry("defaultWarehouse","InvWarehouse"), Map.entry("destWarehouse","InvWarehouse"), Map.entry("destLocation","InvLocation"),
            Map.entry("receivableAccount","FinAccount"), Map.entry("revenueAccount","FinAccount"), Map.entry("expenseAccount","FinAccount"),
            Map.entry("inventoryAccount","FinAccount"), Map.entry("payableAccount","FinAccount"), Map.entry("journalEntry","JournalEntry"));
    public void validate(BaseEntity entity) {
        if (entity.companyId == null) return;
        for (Field field : CrudService.dataFields(entity.getClass())) {
            if (!field.getName().endsWith("Id") || field.getName().equals("companyId") || RecordPolicy.sensitive(field.getName())) continue;
            try {
                Object raw = field.get(entity); if (!(raw instanceof Long id)) continue;
                Class<? extends BaseEntity> type = resolve(entity.getClass(), field.getName()); if (type == null) continue;
                BaseEntity related = em.find(type,id,LockModeType.PESSIMISTIC_WRITE);
                if (related == null || !Boolean.TRUE.equals(related.active)) throw ApiException.badRequest(field.getName() + " does not reference an active record.");
                if (related.companyId != null) RecordPolicy.owned(related,entity.companyId);
                if (entity.getClass().getSimpleName().endsWith("Line") && Set.of("invoiceId","deliveryId","receiptId").contains(field.getName())) RecordPolicy.editable(related);
            } catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }
        }
    }
    public static Optional<ModuleRegistry.Entry> target(Class<?> owner, String field) {
        if (!field.endsWith("Id") || field.equals("companyId")) return Optional.empty();
        String stem=field.substring(0,field.length()-2), alias=ALIASES.get(stem);
        String module=owner.getPackageName().substring(owner.getPackageName().lastIndexOf('.')+1);
        List<ModuleRegistry.Entry> candidates=ModuleRegistry.ENTRIES.stream().filter(entry -> alias != null ? entry.entity().equals(alias) :
                entry.entity().replaceFirst("^(Fin|Sal|Pur|Inv|Mfg|Crm|Pjm|Svc|Log|Dms|Bi|Hr)","").equalsIgnoreCase(stem)).toList();
        ModuleRegistry.Entry match=candidates.stream().filter(e->e.division().equals(module)).findFirst().orElse(candidates.size()==1?candidates.getFirst():null);
        return Optional.ofNullable(match);
    }
    private Class<? extends BaseEntity> resolve(Class<?> owner, String field) {
        var match = target(owner, field).orElse(null);
        if (match == null) return null;
        try { return Class.forName("com.erp."+match.division()+"."+match.entity()).asSubclass(BaseEntity.class); }
        catch(ClassNotFoundException ex) { throw new IllegalStateException(ex); }
    }
}
