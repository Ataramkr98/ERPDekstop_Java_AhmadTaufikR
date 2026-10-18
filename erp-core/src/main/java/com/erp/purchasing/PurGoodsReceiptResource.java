package com.erp.purchasing;

import com.erp.shared.CrudResource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Path;

/**
 * REST surface for PurGoodsReceipt.
 *
 * <p>Standard CRUD, search, filter, sort, paging, CSV import and export, and the
 * field descriptor the UI renders from are all inherited from {@link CrudResource}.
 */
@Authenticated
@Path("/api/purchasing/goods-receipts")
public class PurGoodsReceiptResource extends CrudResource<PurGoodsReceipt> {

    @Override
    protected Class<PurGoodsReceipt> entityType() {
        return PurGoodsReceipt.class;
    }

    @Override
    protected String[] searchFields() {
        return new String[] { "receiptNumber", "deliveryNoteNumber" };
    }

    @Override
    protected String defaultSort() {
        return "receiptDate";
    }

}
