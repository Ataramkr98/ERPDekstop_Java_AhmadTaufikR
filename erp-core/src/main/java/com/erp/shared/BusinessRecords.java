package com.erp.shared;

import com.erp.config.CurrentUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

/** Locks a document before checking and changing its state. */
@ApplicationScoped
public class BusinessRecords {
    @Inject EntityManager em;
    @Inject CurrentUser user;
    public <T extends BaseEntity> T lock(Class<T> type, Long id) {
        if (user.companyId() == null) throw ApiException.forbidden("Sign in to an active company.");
        if (type != com.erp.platform.Company.class) em.find(com.erp.platform.Company.class, user.companyId(), LockModeType.PESSIMISTIC_WRITE);
        if (id == null) throw ApiException.badRequest(type.getSimpleName() + " reference is required.");
        T entity = em.find(type, id, LockModeType.PESSIMISTIC_WRITE);
        if (entity == null || !Boolean.TRUE.equals(entity.active)) throw ApiException.notFound(type.getSimpleName() + " was not found.");
        RecordPolicy.owned(entity, user.companyId());
        return entity;
    }
}
