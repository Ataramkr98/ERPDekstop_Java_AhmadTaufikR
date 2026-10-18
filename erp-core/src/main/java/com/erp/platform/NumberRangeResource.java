package com.erp.platform;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for NumberRange.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/platform/number_ranges")
public class NumberRangeResource extends CrudResource<NumberRange> {

    @Override
    protected Class<NumberRange> entityType() {
        return NumberRange.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "documentType", "prefix" };
    }

    @Override
    protected String defaultSort() {
        return "documentType";
    }

}
