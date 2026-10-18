package com.erp.purchasing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PurGoodsReceiptLine.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/purchasing/goods_receipt_lines")
public class PurGoodsReceiptLineResource extends CrudResource<PurGoodsReceiptLine> {

    @Override
    protected Class<PurGoodsReceiptLine> entityType() {
        return PurGoodsReceiptLine.class;
    }

    @Override
    protected String defaultSort() {
        return "lineNo";
    }

}
