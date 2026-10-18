package com.erp.inventory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import com.erp.config.CurrentUser;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import com.erp.shared.ApiException;

/**
 * Stock ledger.
 *
 * <p>Every physical or logical change to stock goes through {@link #record}. That
 * keeps one invariant in one place: {@code InvStock.quantityOnHand} is always the sum
 * of the movements, and available-to-promise is always on-hand minus reserved.
 *
 * <p>Valuation is applied on inbound movements only. Outbound movements are costed at
 * the current moving average (or standard cost), which is what makes the COGS journal
 * on a delivery reconcile with the inventory account.
 */
@ApplicationScoped
public class InventoryService {

    @Inject
    EntityManager em;
    @Inject com.erp.shared.BusinessRecords records;
    @Inject com.erp.platform.AuditService audit;

    /** Input for a single stock movement. */
    public static class MovementRequest {
        public Long itemId;
        public Long warehouseId;
        public Long locationId;
        public Long destWarehouseId;
        public Long destLocationId;
        public Long lotId;
        public String serialNumber;
        public BigDecimal quantity;
        public String uom;
        public BigDecimal unitCost;
        public LocalDate movementDate;
        public String sourceDocumentType;
        public Long sourceDocumentId;
        public String reasonCode;
        public String notes;

        public MovementRequest() {
        }

        public MovementRequest(MovementType type, Long itemId, Long warehouseId, BigDecimal quantity) {
            this.type = type;
            this.itemId = itemId;
            this.warehouseId = warehouseId;
            this.quantity = quantity;
        }

        public MovementType type;
    }

    /**
     * Records a movement and updates the stock balance in one transaction.
     *
     * @return the persisted movement, whose {@code totalCost} the caller can post
     */
    @Transactional
    public InvMovement record(MovementRequest request) {
        if (request == null || request.itemId == null) {
            throw ApiException.badRequest("A stock movement needs an item.");
        }
        if (request.quantity == null || request.quantity.signum() == 0) {
            throw ApiException.badRequest("A stock movement needs a non-zero quantity.");
        }
        if (request.warehouseId == null) {
            throw ApiException.badRequest("A stock movement needs a warehouse.");
        }

        InvItem item = records.lock(InvItem.class, request.itemId);
        records.lock(InvWarehouse.class, request.warehouseId);
        if (request.locationId != null) {
            InvLocation location = records.lock(InvLocation.class, request.locationId);
            if (!request.warehouseId.equals(location.warehouseId)) throw ApiException.badRequest("Location does not belong to this warehouse.");
        }
        if (request.unitCost != null && request.unitCost.signum() < 0) throw ApiException.badRequest("Unit cost cannot be negative.");
        if (item == null) {
            throw ApiException.notFound("Item " + request.itemId + " was not found.");
        }
        if (!Boolean.TRUE.equals(item.isStockable) && request.type != MovementType.ADJUSTMENT) {
            throw ApiException.badRequest(item.code + " is not a stock item, so it cannot be moved in stock.");
        }

        LocalDate date = request.movementDate == null ? LocalDate.now() : request.movementDate;
        InvStock stock = balance(request.itemId, request.warehouseId, request.locationId, request.lotId);

        BigDecimal quantity = request.quantity;
        BigDecimal unitCost = request.unitCost;
        BigDecimal receiptCost = unitCost == null || unitCost.signum() == 0 ? nz(item.standardCost) : unitCost;

        // Outbound movements are costed at the current book value; inbound at the
        // supplied cost, falling back to the item's standard cost.
        if (quantity.signum() < 0) {
            BigDecimal available = nz(stock.quantityOnHand).subtract(nz(stock.quantityReserved));
            if (available.add(quantity).signum() < 0) {
                throw ApiException.badRequest("Not enough stock of " + item.code + ". Only "
                        + available.stripTrailingZeros().toPlainString() + " is on hand.");
            }
            if (unitCost == null || unitCost.signum() == 0) {
                unitCost = nz(stock.unitCost).signum() > 0 ? stock.unitCost : nz(item.standardCost);
            }
        } else {
            if (unitCost == null || unitCost.signum() == 0) {
                unitCost = nz(item.standardCost).signum() > 0 ? item.standardCost : nz(stock.unitCost);
            }
            unitCost = applyValuation(item, stock, quantity, unitCost);
        }

        BigDecimal totalCost = quantity.abs().multiply(quantity.signum() > 0 && item.valuationMethod == ValuationMethod.MOVING_AVERAGE ? receiptCost : unitCost).setScale(4, RoundingMode.HALF_UP);

        stock.quantityOnHand = nz(stock.quantityOnHand).add(quantity);
        stock.quantityAvailable = stock.quantityOnHand.subtract(nz(stock.quantityReserved));
        stock.unitCost = unitCost;
        stock.totalValue = stock.quantityOnHand.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);
        stock.lastMovementDate = date;
        em.merge(stock);

        InvMovement movement = new InvMovement();
        movement.movementType = request.type == null ? MovementType.ADJUSTMENT : request.type;
        movement.itemId = request.itemId;
        movement.movementDate = date;
        movement.quantity = quantity;
        movement.uom = request.uom != null ? request.uom : item.uom;
        movement.warehouseId = request.warehouseId;
        movement.locationId = request.locationId;
        movement.destWarehouseId = request.destWarehouseId;
        movement.destLocationId = request.destLocationId;
        movement.lotId = request.lotId;
        movement.serialNumber = request.serialNumber;
        movement.unitCost = unitCost;
        movement.totalCost = totalCost;
        movement.sourceDocumentType = request.sourceDocumentType;
        movement.sourceDocumentId = request.sourceDocumentId;
        movement.reasonCode = request.reasonCode;
        movement.notes = request.notes;
        movement.companyId = CurrentUser.companyIdOrNull();
        em.persist(movement);
        return movement;
    }

    /** Available to promise for an item across every warehouse, per warehouse detail. */
    public List<InvStock> stockForItem(Long itemId) {
        return em.createQuery(
                "select s from InvStock s where s.itemId = :i and s.active = true and s.companyId = :c order by s.warehouseId",
                InvStock.class)
                .setParameter("i", itemId)
                .setParameter("c", CurrentUser.companyIdOrNull())
                .getResultList();
    }

    private InvStock balance(Long itemId, Long warehouseId, Long locationId, Long lotId) {
        List<InvStock> found = em.createQuery(
                "select s from InvStock s where s.active = true and s.itemId = :i and s.warehouseId = :w "
                        + "and coalesce(s.locationId, 0) = coalesce(:l, 0) "
                        + "and coalesce(s.lotId, 0) = coalesce(:lot, 0)",
                InvStock.class)
                .setParameter("i", itemId)
                .setParameter("w", warehouseId)
                .setParameter("l", locationId)
                .setParameter("lot", lotId)
                .getResultList();
        if (!found.isEmpty()) {
            return found.get(0);
        }
        InvStock stock = new InvStock();
        stock.itemId = itemId;
        stock.warehouseId = warehouseId;
        stock.locationId = locationId;
        stock.lotId = lotId;
        stock.quantityOnHand = BigDecimal.ZERO;
        stock.quantityReserved = BigDecimal.ZERO;
        stock.quantityAvailable = BigDecimal.ZERO;
        stock.quantityInTransit = BigDecimal.ZERO;
        stock.unitCost = BigDecimal.ZERO;
        stock.totalValue = BigDecimal.ZERO;
        stock.companyId = CurrentUser.companyIdOrNull();
        em.persist(stock);
        return stock;
    }

    /**
     * Recomputes the book cost for an inbound movement according to the item's
     * valuation method (M-INV-007).
     */
    private BigDecimal applyValuation(InvItem item, InvStock stock, BigDecimal incoming, BigDecimal unitCost) {
        if (item.valuationMethod == null || item.valuationMethod == ValuationMethod.STANDARD) {
            return nz(item.standardCost).signum() > 0 ? item.standardCost : unitCost;
        }
        if (item.valuationMethod != ValuationMethod.MOVING_AVERAGE) {
            throw ApiException.badRequest("FIFO/LIFO cost layers are not configured. Choose STANDARD or MOVING_AVERAGE valuation.");
        }
        BigDecimal onHand = nz(stock.quantityOnHand);
        BigDecimal newQty = onHand.add(incoming);
        if (newQty.signum() <= 0) {
            return unitCost;
        }
        BigDecimal existingValue = onHand.max(BigDecimal.ZERO).multiply(nz(stock.unitCost));
        BigDecimal incomingValue = incoming.multiply(unitCost);
        return existingValue.add(incomingValue)
                .divide(newQty, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
