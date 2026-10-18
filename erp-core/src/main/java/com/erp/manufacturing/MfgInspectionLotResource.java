package com.erp.manufacturing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for MfgInspectionLot.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/manufacturing/inspection_lots")
public class MfgInspectionLotResource extends CrudResource<MfgInspectionLot> {

    @Override
    protected Class<MfgInspectionLot> entityType() {
        return MfgInspectionLot.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "inspectionNumber" };
    }

    @Override
    protected String defaultSort() {
        return "inspectionDate";
    }

}
