package com.erp.finance;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for FinFixedAsset.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/finance/fixed_assets")
public class FinFixedAssetResource extends CrudResource<FinFixedAsset> {

    @Override
    protected Class<FinFixedAsset> entityType() {
        return FinFixedAsset.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "assetNumber", "name", "category" };
    }

    @Override
    protected String defaultSort() {
        return "assetNumber";
    }

}
