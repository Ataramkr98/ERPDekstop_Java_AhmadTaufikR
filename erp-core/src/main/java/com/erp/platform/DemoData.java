package com.erp.platform;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.erp.finance.AccountType;
import com.erp.finance.FinAccount;
import com.erp.finance.FinCostCenter;
import com.erp.finance.JournalEntry;
import com.erp.finance.PeriodStatus;
import com.erp.finance.PostingPeriod;
import com.erp.hr.EmployeeStatus;
import com.erp.hr.HrDepartment;
import com.erp.hr.HrEmployee;
import com.erp.inventory.InvItem;
import com.erp.inventory.InvLocation;
import com.erp.inventory.InvWarehouse;
import com.erp.inventory.ItemType;
import com.erp.inventory.LocationType;
import com.erp.inventory.ValuationMethod;
import com.erp.purchasing.GoodsReceiptStatus;
import com.erp.purchasing.MatchStatus;
import com.erp.purchasing.PoStatus;
import com.erp.purchasing.PurGoodsReceipt;
import com.erp.purchasing.PurGoodsReceiptLine;
import com.erp.purchasing.PurInvoice;
import com.erp.purchasing.PurInvoiceLine;
import com.erp.purchasing.PurOrder;
import com.erp.purchasing.PurVendor;
import com.erp.purchasing.VendorInvoiceStatus;
import com.erp.purchasing.VendorType;
import com.erp.project.PjmProject;
import com.erp.project.PjmTask;
import com.erp.project.ProjectStatus;
import com.erp.project.ProjectType;
import com.erp.project.TaskStatus;
import com.erp.sales.CustomerType;
import com.erp.sales.DeliveryStatus;
import com.erp.sales.InvoiceStatus;
import com.erp.sales.SalCustomer;
import com.erp.sales.SalDelivery;
import com.erp.sales.SalDeliveryLine;
import com.erp.sales.SalInvoice;
import com.erp.sales.SalInvoiceLine;
import com.erp.sales.SalOrder;
import com.erp.sales.SalesOrderStatus;
import com.erp.service.SvcTicket;
import com.erp.service.TicketPriority;
import com.erp.service.TicketStatus;
import com.erp.shared.ApiException;
import com.erp.shared.BaseEntity;
import com.erp.shared.CrudService;
import com.erp.shared.EntityValidation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Builds the synthetic dataset behind the portfolio demo.
 *
 * <p>Every row is written with {@code companyId} pointing at the demo tenant, so the
 * demo session cannot see or touch a real company even though both live in the same
 * schema: all reads and writes in the app are scoped by the {@code companyId} JWT
 * claim and re-checked by {@code RecordPolicy.owned}.
 *
 * <p>All names are fictional. Nothing here reads a customer file, a payment detail or
 * any other real personal data, and no outbound integration is invoked.
 *
 * <p>Seeding is idempotent: {@link #ensureWorkspace()} is keyed on a natural code so it
 * can be called on every demo start, and {@link #resetTenant(Long)} deletes the demo
 * tenant so the next call rebuilds the exact same starting point.
 */
@ApplicationScoped
public class DemoData {

    /** The one-off demo tenant. Kept in a static so every seeding entry point agrees. */
    public static final String COMPANY_CODE = "DEMO";

    @Inject
    EntityManager em;

    @Inject
    AuditService audit;

    // ------------------------------------------------------------------ master data

    /**
     * Creates (or repairs) the demo tenant and its master data. Runs without a JWT, so
     * it never depends on {@code CurrentUser}: every row carries an explicit
     * {@code companyId}.
     */
    @Transactional
    public Map<String, Object> ensureWorkspace() {
        Company company = company();
        Long c = company.id;
        repairLineOwnership(c);
        String currency = "USD";

        seedCurrencies(c);
        seedAccounts(c, currency);
        seedPeriods(c);

        InvWarehouse warehouse = one(InvWarehouse.class, c, "code", "MAIN",
                w -> { w.code = "MAIN"; w.name = "Central warehouse"; w.city = "Rotterdam"; w.countryCode = "NL"; w.defaultWarehouse = Boolean.TRUE; });
        one(InvLocation.class, c, "code", "MAIN-A1",
                l -> { l.code = "MAIN-A1"; l.name = "Aisle 1 / Rack A"; l.warehouseId = warehouse.id; l.locationType = LocationType.RACK; l.aisle = "A"; l.rack = "01"; l.pickable = Boolean.TRUE; l.receivable = Boolean.TRUE; });

        FinCostCenter costCenter = one(FinCostCenter.class, c, "code", "CC-OPS",
                x -> { x.code = "CC-OPS"; x.name = "Operations"; x.description = "Warehouse and logistics cost centre"; });
        HrDepartment department = one(HrDepartment.class, c, "code", "OPS", d -> { d.code = "OPS"; d.name = "Operations"; d.costCenterId = costCenter.id; });
        one(HrEmployee.class, c, "employeeNumber", "E-1001",
                e -> { e.employeeNumber = "E-1001"; e.firstName = "Ravi"; e.lastName = "Menon"; e.fullName = "Ravi Menon"; e.email = "ravi.menon@example.invalid"; e.hireDate = LocalDate.of(2021, 3, 1); e.departmentId = department.id; e.status = EmployeeStatus.ACTIVE; e.basicSalary = new BigDecimal("6200.00"); });
        one(HrEmployee.class, c, "employeeNumber", "E-1002",
                e -> { e.employeeNumber = "E-1002"; e.firstName = "Sofia"; e.lastName = "Bauer"; e.fullName = "Sofia Bauer"; e.email = "sofia.bauer@example.invalid"; e.hireDate = LocalDate.of(2019, 9, 16); e.departmentId = department.id; e.status = EmployeeStatus.ACTIVE; e.basicSalary = new BigDecimal("6200.00"); });
        one(HrEmployee.class, c, "employeeNumber", "E-1003",
                e -> { e.employeeNumber = "E-1003"; e.firstName = "Tomas"; e.lastName = "de Vries"; e.fullName = "Tomas de Vries"; e.email = "tomas.devries@example.invalid"; e.hireDate = LocalDate.of(2023, 1, 9); e.departmentId = department.id; e.status = EmployeeStatus.ON_LEAVE; e.basicSalary = new BigDecimal("3400.00"); });

        // Catalogue: a mix of stockable and service items, priced and costed, so the
        // inventory screens and the costing logic have something to work with.
        InvItem desk = one(InvItem.class, c, "code", "ART-DESK",
                i -> item(i, "ART-DESK", "Height-adjustable desk", "Furniture", "80.00", "549.00", true, true));
        InvItem chair = one(InvItem.class, c, "code", "ART-CHAIR",
                i -> item(i, "ART-CHAIR", "Ergonomic task chair", "Furniture", "185.00", "395.00", true, true));
        InvItem monitor = one(InvItem.class, c, "code", "TFT-27",
                i -> item(i, "TFT-27", "27-inch QHD monitor", "Electronics", "198.50", "329.00", true, true));
        InvItem cable = one(InvItem.class, c, "code", "ACC-CABLE",
                i -> item(i, "ACC-CABLE", "USB-C cable 2 m", "Accessories", "6.40", "19.00", true, true));
        InvItem service = one(InvItem.class, c, "code", "SVC-INSTALL",
                i -> { item(i, "SVC-INSTALL", "On-site installation (per day)", "Services", "0.00", "450.00", false, true); i.itemType = ItemType.SERVICE; i.isPurchasable = false; });
        one(InvItem.class, c, "code", "ACC-LAMP", i -> item(i, "ACC-LAMP", "Monitor arm", "Accessories", "42.00", "89.00", true, true));
        one(InvItem.class, c, "code", "TFT-34", i -> item(i, "TFT-34", "34-inch ultrawide monitor", "Electronics", "329.00", "549.00", true, true));
        one(InvItem.class, c, "code", "ACC-HUB", i -> item(i, "ACC-HUB", "USB-C dock", "Accessories", "88.00", "149.00", true, true));

        SalCustomer northstar = one(SalCustomer.class, c, "code", "C-1001",
                x -> customer(x, "C-1001", "Northstar Studio", "COMPANY", "30", "NL", "Rotterdam"));
        SalCustomer fieldwork = one(SalCustomer.class, c, "code", "C-1002",
                x -> customer(x, "C-1002", "Fieldwork Supply", "COMPANY", "14", "NL", "Utrecht"));
        SalCustomer atlas = one(SalCustomer.class, c, "code", "C-1003",
                x -> customer(x, "C-1003", "Atlas Office Group", "COMPANY", "45", "BE", "Antwerp"));
        SalCustomer harbor = one(SalCustomer.class, c, "code", "C-1004",
                x -> customer(x, "C-1004", "Harborline Retail", "COMPANY", "30", "NL", "Amsterdam"));
        SalCustomer merk = one(SalCustomer.class, c, "code", "C-1005",
                x -> customer(x, "C-1005", "Merk Trading", "COMPANY", "21", "DE", "Duisburg"));
        SalCustomer delft = one(SalCustomer.class, c, "code", "C-1006",
                x -> customer(x, "C-1006", "Delft Academic Labs", "GOVERNMENT", "60", "NL", "Delft"));

        PurVendor nordic = one(PurVendor.class, c, "code", "V-2001",
                v -> vendor(v, "V-2001", "Nordic Components AB", "SUPPLIER", "30", "SE"));
        PurVendor bv = one(PurVendor.class, c, "code", "V-2002",
                v -> vendor(v, "V-2002", "Bureau Vertrieb GmbH", "SUPPLIER", "21", "DE"));
        PurVendor pack = one(PurVendor.class, c, "code", "V-2003",
                v -> vendor(v, "V-2003", "PackRight B.V.", "SUPPLIER", "14", "NL"));
        PurVendor consult = one(PurVendor.class, c, "code", "V-2004",
                v -> { vendor(v, "V-2004", "Consult & Build", "SUBCONTRACTOR", "30", "NL"); v.leadTimeDays = 45; });

        // -------------------------------------------------------------- documents
        LocalDate today = LocalDate.now();
        String prefix = today.getYear() + "-";

        SalOrder so1 = order(c, prefix + "SO-0001", northstar, warehouse, today.minusDays(12), today.plusDays(4),
                SalesOrderStatus.DELIVERED, "549.00", lines(new Object[][] {
                        {desk, "1", "549.00", "0"}, {service, "1", "450.00", "0"} }));
        SalOrder so2 = order(c, prefix + "SO-0002", fieldwork, warehouse, today.minusDays(9), today.plusDays(1),
                SalesOrderStatus.PARTIALLY_DELIVERED, "1504.00", lines(new Object[][] {
                        {chair, "2", "395.00", "0"}, {monitor, "2", "329.00", "0"}, {cable, "4", "19.00", "0"} }));
        SalOrder so3 = order(c, prefix + "SO-0003", atlas, warehouse, today.minusDays(5), today.plusDays(9),
                SalesOrderStatus.CONFIRMED, "1644.00", lines(new Object[][] {
                        {desk, "2", "549.00", "0"}, {chair, "1", "395.00", "0"}, {desk, "0", "0", "0"} }));
        order(c, prefix + "SO-0004", harbor, warehouse, today.minusDays(2), today.plusDays(14),
                SalesOrderStatus.DRAFT, "686.00", lines(new Object[][] {
                        {monitor, "1", "329.00", "0"}, {desk, "0", "0", "0"} }));
        order(c, prefix + "SO-0005", merk, warehouse, today, today.plusDays(21),
                SalesOrderStatus.DRAFT, "327.00", lines(new Object[][] {{cable, "3", "19.00", "0"}}));
        order(c, prefix + "SO-0006", delft, warehouse, today.minusDays(1), today.plusDays(30),
                SalesOrderStatus.CONFIRMED, "1098.00", lines(new Object[][] {{desk, "2", "549.00", "0"}}));

        delivery(c, prefix + "DN-0001", so1, northstar, warehouse, today.minusDays(10), DeliveryStatus.DELIVERED, desk);
        delivery(c, prefix + "DN-0002", so2, fieldwork, warehouse, today.minusDays(6), DeliveryStatus.PICKING, chair);
        delivery(c, prefix + "DN-0003", so3, atlas, warehouse, today, DeliveryStatus.PENDING, desk);

        invoice(c, prefix + "INV-0001", northstar, so1, today.minusDays(10), today.plusDays(20), InvoiceStatus.POSTED, "999.00", "0.00", new Object[][] {{desk, "1", "549.00", "0"}, {service, "1", "450.00", "0"}});
        invoice(c, prefix + "INV-0002", fieldwork, so2, today.minusDays(6), today.plusDays(8), InvoiceStatus.POSTED, "1504.00", "322.86",
                new Object[][] {{chair, "2", "395.00", "0"}, {monitor, "2", "329.00", "0"}, {cable, "4", "19.00", "0"}});
        invoice(c, prefix + "INV-0003", atlas, so3, today.minusDays(3), today.plusDays(27), InvoiceStatus.DRAFT, "1493.00", "0.00",
                new Object[][] {{desk, "2", "549.00", "0"}, {chair, "1", "395.00", "0"}});
        invoice(c, prefix + "INV-0004", harbor, null, today, today.plusDays(30), InvoiceStatus.DRAFT, "329.00", "0.00",
                new Object[][] {{monitor, "1", "329.00", "0"}});
        invoice(c, prefix + "INV-0005", merk, null, today.minusDays(40), today.minusDays(10), InvoiceStatus.OVERDUE, "627.00", "0.00",
                new Object[][] {{desk, "1", "549.00", "0"}, {cable, "4", "19.00", "0"}});

        PurOrder po1 = purchaseOrder(c, prefix + "PO-0001", nordic, warehouse, today.minusDays(14), PoStatus.PARTIALLY_RECEIVED, "1191.00",
                new Object[][] {{monitor, "6", "198.50"}});
        purchaseOrder(c, prefix + "PO-0002", bv, warehouse, today.minusDays(7), PoStatus.APPROVED, "1092.00",
                new Object[][] {{chair, "4", "185.00", "0"}, {desk, "0", "0", "0"}});
        purchaseOrder(c, prefix + "PO-0003", pack, warehouse, today.minusDays(2), PoStatus.DRAFT, "256.00",
                new Object[][] {{cable, "40", "6.40"}});

        goodsReceipt(c, prefix + "GR-0001", nordic, po1, warehouse, today.minusDays(12), GoodsReceiptStatus.POSTED, new Object[][] {{monitor, "6", "198.50", "1191.00"}});
        goodsReceipt(c, prefix + "GR-0002", pack, null, warehouse, today.minusDays(3), GoodsReceiptStatus.DRAFT, new Object[][] {{cable, "40", "6.40", "256.00"}});

        vendorInvoice(c, prefix + "VIN-0001", nordic, po1, today.minusDays(11), VendorInvoiceStatus.MATCHED, "1396.07", new Object[][] {{monitor, "6", "198.50", "205.07"}});
        vendorInvoice(c, prefix + "VIN-0002", pack, null, today.minusDays(2), VendorInvoiceStatus.DRAFT, "300.16", new Object[][] {{cable, "40", "6.40", "44.16"}});

        // Projects and service give the sidebar modules something honest to show.
        PjmProject rollout = one(PjmProject.class, c, "code", "P-3001",
                p -> { p.code = "P-3001"; p.name = "Amsterdam office refit"; p.description = "Fit-out for the Amsterdam branch"; p.startDate = today.minusDays(30); p.endDate = today.plusDays(45); p.status = ProjectStatus.ACTIVE; p.projectType = ProjectType.INTERNAL; p.budgetAmount = new BigDecimal("85000.00"); p.completionPercent = new BigDecimal("35"); p.ragStatus = "A"; });
        PjmProject portal = one(PjmProject.class, c, "code", "P-3002",
                p -> { p.code = "P-3002"; p.name = "Customer portal v2"; p.description = "Self-service ordering portal"; p.startDate = today.minusDays(10); p.endDate = today.plusDays(80); p.status = ProjectStatus.ACTIVE; p.projectType = ProjectType.EXTERNAL_FIXED_PRICE; p.budgetAmount = new BigDecimal("42000.00"); p.completionPercent = new BigDecimal("12"); p.ragStatus = "G"; });
        one(PjmTask.class, c, "wbsCode", "P-3001.10", t -> { t.wbsCode = "P-3001.10"; t.name = "Demolition and disposal"; t.projectId = rollout.id; t.status = TaskStatus.COMPLETED; t.plannedHours = new BigDecimal("40"); t.completionPercent = new BigDecimal("100"); });
        one(PjmTask.class, c, "wbsCode", "P-3001.20", t -> { t.wbsCode = "P-3001.20"; t.name = "Electrical first fix"; t.projectId = rollout.id; t.status = TaskStatus.IN_PROGRESS; t.plannedHours = new BigDecimal("120"); t.completionPercent = new BigDecimal("40"); });
        one(PjmTask.class, c, "wbsCode", "P-3002.10", t -> { t.wbsCode = "P-3002.10"; t.name = "Authentication service"; t.projectId = portal.id; t.status = TaskStatus.IN_PROGRESS; t.plannedHours = new BigDecimal("80"); t.completionPercent = new BigDecimal("10"); });

        one(SvcTicket.class, c, "ticketNumber", "T-4001",
                t -> { t.ticketNumber = "T-4001"; t.subject = "Damaged monitor on arrival"; t.description = "Two TFT-27 arrived with a cracked bezel."; t.customerId = fieldwork.id; t.priority = TicketPriority.HIGH; t.status = TicketStatus.OPEN; t.openedAt = Instant.now().minusSeconds(86400L * 3); });
        one(SvcTicket.class, c, "ticketNumber", "T-4002",
                t -> { t.ticketNumber = "T-4002"; t.subject = "Request for installation visit"; t.description = "Customer asked for on-site assembly of 6 desks."; t.customerId = atlas.id; t.priority = TicketPriority.MEDIUM; t.status = TicketStatus.PENDING; t.openedAt = Instant.now().minusSeconds(86400L); });
        one(SvcTicket.class, c, "ticketNumber", "T-4003",
                t -> { t.ticketNumber = "T-4003"; t.subject = "Invoice copy requested"; t.description = "Customer asked for a PDF copy of an invoice for their procurement file."; t.customerId = harbor.id; t.priority = TicketPriority.LOW; t.status = TicketStatus.RESOLVED; t.openedAt = Instant.now().minusSeconds(86400L * 9); t.resolvedAt = Instant.now().minusSeconds(86400L * 7); });

        audit.custom("DEMO_SEED", "Company", c, "Demo workspace prepared.");
        return summary(c);
    }

    // ------------------------------------------------------------------ tenant reset

    /**
     * Removes every row that belongs to the demo tenant so the next
     * {@link #ensureWorkspace()} rebuilds the identical starting dataset.
     *
     * <p>Safe because every table in the schema carries {@code company_id} (verified:
     * only {@code flyway_schema_history} does not) and the schema declares no foreign
     * key constraints, so a per-table tenant delete cannot orphan or block another
     * table. The delete still runs in one transaction, so a failure rolls the whole
     * reset back rather than leaving a half-empty tenant.
     */
    @Transactional
    public void resetTenant(Long demoCompanyId) {
        if (demoCompanyId == null) {
            throw ApiException.badRequest("The demo tenant is not initialised.");
        }
        repairLineOwnership(demoCompanyId);
        List<String> tables = em.createNativeQuery(
                "select table_name from information_schema.tables "
                        + "where table_schema = current_schema() and table_type = 'BASE TABLE' "
                        + "and table_name <> 'flyway_schema_history' order by table_name",
                String.class).getResultList();
        int removed = 0;
        for (String table : tables) {
            if (!table.matches("[a-z_][a-z0-9_]*")) {
                throw ApiException.badRequest("Unexpected table name in the demo schema.");
            }
            removed += em.createNativeQuery("delete from " + table + " where company_id = ?1")
                    .setParameter(1, demoCompanyId).executeUpdate();
        }
        em.createNativeQuery("delete from company where id = ?1").setParameter(1, demoCompanyId).executeUpdate();
        audit.custom("DEMO_RESET", "Company", demoCompanyId, "Demo tenant cleared (" + removed + " rows).");
    }

    public Map<String, Object> summary(Long demoCompanyId) {
        Map<String, Object> out = new LinkedHashMap<>();
        Company company = Company.findById(demoCompanyId);
        out.put("company", company == null ? COMPANY_CODE : company.name);
        out.put("companyId", demoCompanyId);
        out.put("customers", count(SalCustomer.class, demoCompanyId));
        out.put("items", count(InvItem.class, demoCompanyId));
        out.put("orders", count(SalOrder.class, demoCompanyId));
        out.put("invoices", count(SalInvoice.class, demoCompanyId));
        out.put("deliveries", count(SalDelivery.class, demoCompanyId));
        out.put("goodsReceipts", count(PurGoodsReceipt.class, demoCompanyId));
        out.put("purchaseOrders", count(PurOrder.class, demoCompanyId));
        out.put("journalEntries", count(JournalEntry.class, demoCompanyId));
        return out;
    }

    private long count(Class<?> type, Long companyId) {
        return em.createQuery("select count(e) from " + CrudService.entityName(type)
                + " e where e.companyId = :c", Long.class).setParameter("c", companyId).getSingleResult();
    }

    /** The demo tenant, created on first use. */
    @Transactional
    public Company company() {
        List<Company> found = em.createQuery("select c from Company c where c.code = :code", Company.class)
                .setParameter("code", COMPANY_CODE).getResultList();
        if (!found.isEmpty()) {
            Company existing = found.getFirst();
            if (existing.companyId == null) {
                existing.companyId = existing.id;
                em.merge(existing);
            }
            return existing;
        }
        Company company = new Company();
        company.code = COMPANY_CODE;
        company.name = "Demo Workspace";
        company.legalName = "Demo Workspace BV";
        company.taxId = "NL000000000B01";
        company.baseCurrency = "USD";
        company.reportingCurrency = "USD";
        company.countryCode = "NL";
        company.localeTag = "en-US";
        company.timezone = "Europe/Amsterdam";
        company.addressLine1 = "Demo Street 1";
        company.city = "Rotterdam";
        company.countryCode = "NL";
        company.email = "demo@example.invalid";
        company.fiscalYearStartMonth = 1;
        company.enabled = Boolean.TRUE;
        persist(company);
        company.companyId = company.id;
        em.merge(company);
        return company;
    }

    // ------------------------------------------------------------------ helpers

    private void seedCurrencies(Long c) {
        one(Currency.class, c, "code", "USD", x -> { x.code = "USD"; x.name = "US dollar"; x.symbol = "$"; x.minorUnits = 2; x.enabled = Boolean.TRUE; });
        one(Currency.class, c, "code", "EUR", x -> { x.code = "EUR"; x.name = "Euro"; x.symbol = "€"; x.minorUnits = 2; x.enabled = Boolean.TRUE; });
    }

    private void seedAccounts(Long c, String currency) {
        String[][] accounts = {
                {"1000", "Cash and bank", "ASSET", "1"},
                {"1100", "Trade receivables", "ASSET", null},
                {"1200", "Accounts receivable", "ASSET", "1100"},
                {"1300", "Inventory", "ASSET", null},
                {"1400", "Goods received not invoiced", "ASSET", null},
                {"1500", "Input VAT", "ASSET", null},
                {"2000", "Trade payables", "LIABILITY", null},
                {"2100", "Accounts payable", "LIABILITY", "2000"},
                {"2200", "Tax payable", "LIABILITY", null},
                {"3000", "Equity", "EQUITY", null},
                {"4000", "Sales revenue", "REVENUE", null},
                {"5000", "Cost of goods sold", "EXPENSE", null},
                {"6000", "Operating expenses", "EXPENSE", null}};
        for (String[] a : accounts) {
            one(FinAccount.class, c, "code", a[0], x -> {
                x.code = a[0];
                x.name = a[1];
                x.accountType = AccountType.valueOf(a[2]);
                x.currency = currency;
                x.isGroup = "1".equals(a[3]);
                x.level = 1;
                x.reconciliationAccount = "1100".equals(a[0]) || "2000".equals(a[0]) || "1000".equals(a[0]);
                x.cashFlowRelevant = "1000".equals(a[0]);
            });
        }
    }

    private void seedPeriods(Long c) {
        LocalDate today = LocalDate.now();
        for (int back = 2; back >= 0; back--) {
            YearMonth ym = YearMonth.from(today.minusMonths(back));
            String label = ym.getYear() + "-" + String.format("%02d", ym.getMonthValue());
            one(PostingPeriod.class, c, "name", label, p -> {
                p.fiscalYear = ym.getYear();
                p.periodNo = ym.getMonthValue();
                p.name = label;
                p.startDate = ym.atDay(1);
                p.endDate = ym.atEndOfMonth();
                p.status = PeriodStatus.OPEN;
            });
        }
    }

    private void item(InvItem i, String code, String name, String category, String cost, String price,
                      boolean stockable, boolean saleable) {
        i.code = code;
        i.name = name;
        i.category = category;
        i.itemType = stockable ? ItemType.STOCK : ItemType.SERVICE;
        i.uom = "EA";
        i.standardCost = new BigDecimal(cost);
        i.unitPrice = new BigDecimal(price);
        i.valuationMethod = ValuationMethod.STANDARD;
        i.isStockable = stockable;
        i.isPurchasable = true;
        i.isSaleable = saleable;
        i.reorderPoint = new BigDecimal("5");
        i.safetyStock = new BigDecimal("2");
        i.leadTimeDays = stockable ? 14 : 0;
        i.taxCode = "STD";
    }

    private void customer(SalCustomer x, String code, String name, String type, String terms, String country, String city) {
        x.code = code;
        x.name = name;
        x.customerType = CustomerType.valueOf(type);
        x.email = ("orders@" + code.toLowerCase() + ".example.invalid");
        x.phone = "+31 10 000 0000";
        x.website = "https://" + code.toLowerCase() + ".example.invalid";
        x.countryCode = country;
        x.city = city;
        x.currency = "USD";
        x.paymentTermDays = Integer.parseInt(terms);
        x.creditLimit = new BigDecimal("25000.00");
        x.currentBalance = BigDecimal.ZERO;
        x.taxExempt = Boolean.FALSE;
        x.isOnCreditHold = Boolean.FALSE;
        x.addressLine1 = name + " 1";
        x.postalCode = "3000 AA";
    }

    private void vendor(PurVendor v, String code, String name, String type, String terms, String country) {
        v.code = code;
        v.name = name;
        v.vendorType = VendorType.valueOf(type);
        v.email = ("sales@" + code.toLowerCase() + ".example.invalid");
        v.phone = "+49 30 000 0000";
        v.countryCode = country;
        v.city = "Hamburg";
        v.currency = "USD";
        v.paymentTermDays = Integer.parseInt(terms);
        v.rating = 4;
        v.bankName = "Example Bank";
        v.bankAccountHolder = name;
        // Deliberately not a real account number: this dataset must never move money.
        v.bankAccountNumber = "DEMO00000000";
        v.leadTimeDays = 10;
    }

    private SalOrder order(Long c, String number, SalCustomer customer, InvWarehouse warehouse,
                           LocalDate date, LocalDate promised, SalesOrderStatus status,
                           String total, Object[][] lines) {
        return one(SalOrder.class, c, "orderNumber", number, o -> {
            o.orderNumber = number;
            o.customerId = customer.id;
            o.orderDate = date;
            o.requestedDate = promised;
            o.promisedDate = promised;
            o.status = status;
            o.currency = "USD";
            o.subtotal = new BigDecimal(total);
            o.discountAmount = BigDecimal.ZERO;
            o.taxAmount = BigDecimal.ZERO;
            o.totalAmount = new BigDecimal(total);
            o.warehouseId = warehouse.id;
            o.customerReference = customer.code + "-PO";
            o.creditChecked = Boolean.TRUE;
            o.notes = "Demo dataset - " + status.name().toLowerCase().replace('_', ' ') + " order";
        });
    }

    private Object[][] lines(Object[][] rows) {
        // A {item, qty 0, price 0} row is a placeholder that should never survive;
        // drop it here so the intent stays readable at the call sites above.
        return java.util.Arrays.stream(rows)
                .filter(r -> new BigDecimal(r[1].toString()).signum() != 0
                        || new BigDecimal(r[2].toString()).signum() != 0)
                .toArray(Object[][]::new);
    }

    private void delivery(Long c, String number, SalOrder order, SalCustomer customer, InvWarehouse warehouse,
                          LocalDate date, DeliveryStatus status, InvItem item) {
        SalDelivery delivery = one(SalDelivery.class, c, "deliveryNumber", number, d -> {
            d.deliveryNumber = number;
            d.orderId = order.id;
            d.customerId = customer.id;
            d.deliveryDate = date;
            d.status = status;
            d.warehouseId = warehouse.id;
            d.trackingNumber = "TRK" + number.replace("-", "");
            d.notes = "Demo dataset delivery";
        });
        if (em.createQuery("select count(l) from SalDeliveryLine l where l.deliveryId = :id and l.active = true", Long.class)
                .setParameter("id", delivery.id).getSingleResult() == 0) {
            SalDeliveryLine line = new SalDeliveryLine();
            line.companyId = c;
            line.deliveryId = delivery.id;
            line.lineNo = 10;
            line.itemId = item.id;
            line.quantity = BigDecimal.ONE;
            line.warehouseId = warehouse.id;
            line.uom = "EA";
            persist(line);
        }
    }

    private void invoice(Long c, String number, SalCustomer customer, SalOrder order, LocalDate date,
                         LocalDate due, InvoiceStatus status, String net, String tax, Object[][] lines) {
        SalInvoice invoice = one(SalInvoice.class, c, "invoiceNumber", number, i -> {
            i.invoiceNumber = number;
            i.customerId = customer.id;
            i.orderId = order == null ? null : order.id;
            i.invoiceDate = date;
            i.dueDate = due;
            i.status = status;
            i.currency = "USD";
            i.subtotal = new BigDecimal(net);
            i.discountAmount = BigDecimal.ZERO;
            i.taxAmount = new BigDecimal(tax);
            i.totalAmount = new BigDecimal(net).add(new BigDecimal(tax));
            i.balanceAmount = status == InvoiceStatus.OVERDUE ? i.totalAmount : (status == InvoiceStatus.POSTED ? i.totalAmount : BigDecimal.ZERO);
            i.paidAmount = BigDecimal.ZERO;
            i.notes = "Demo dataset invoice (" + status.name().toLowerCase() + ")";
        });
        if (em.createQuery("select count(l) from SalInvoiceLine l where l.invoiceId = :id and l.active = true", Long.class)
                .setParameter("id", invoice.id).getSingleResult() == 0) {
            int lineNo = 10;
            for (Object[] row : lines) {
                SalInvoiceLine line = new SalInvoiceLine();
                line.companyId = c;
                line.invoiceId = invoice.id;
                line.lineNo = lineNo;
                line.itemId = ((InvItem) row[0]).id;
                line.description = ((InvItem) row[0]).name;
                line.quantity = new BigDecimal(row[1].toString());
                line.uom = "EA";
                line.unitPrice = new BigDecimal(row[2].toString());
                line.discountPercent = BigDecimal.ZERO;
                line.lineTotal = new BigDecimal(row[2].toString()).multiply(new BigDecimal(row[1].toString()));
                persist(line);
                lineNo += 10;
            }
        }
    }

    private PurOrder purchaseOrder(Long c, String number, PurVendor vendor, InvWarehouse warehouse,
                                   LocalDate date, PoStatus status, String total, Object[][] lines) {
        return one(PurOrder.class, c, "orderNumber", number, o -> {
            o.orderNumber = number;
            o.vendorId = vendor.id;
            o.orderDate = date;
            o.requiredDate = date.plusDays(14);
            o.promisedDate = date.plusDays(10);
            o.status = status;
            o.currency = "USD";
            o.subtotal = new BigDecimal(total);
            o.taxAmount = BigDecimal.ZERO;
            o.totalAmount = new BigDecimal(total);
            o.warehouseId = warehouse.id;
            o.revision = 1;
            o.paymentTerms = "Net 30";
            o.notes = "Demo dataset purchase order";
        });
    }

    private void goodsReceipt(Long c, String number, PurVendor vendor, PurOrder order, InvWarehouse warehouse,
                              LocalDate date, GoodsReceiptStatus status, Object[][] lines) {
        PurGoodsReceipt receipt = one(PurGoodsReceipt.class, c, "receiptNumber", number, r -> {
            r.receiptNumber = number;
            r.vendorId = vendor.id;
            r.orderId = order == null ? null : order.id;
            r.receiptDate = date;
            r.status = status;
            r.warehouseId = warehouse.id;
            r.deliveryNoteNumber = "DN-" + number.substring(3);
            r.totalValue = new BigDecimal(lines[0][3].toString());
            r.notes = "Demo dataset goods receipt";
        });
        if (em.createQuery("select count(l) from PurGoodsReceiptLine l where l.receiptId = :id and l.active = true", Long.class)
                .setParameter("id", receipt.id).getSingleResult() == 0) {
            int lineNo = 10;
            for (Object[] row : lines) {
                PurGoodsReceiptLine line = new PurGoodsReceiptLine();
                line.companyId = c;
                line.receiptId = receipt.id;
                line.lineNo = lineNo;
                line.itemId = ((InvItem) row[0]).id;
                line.quantity = new BigDecimal(row[1].toString());
                line.orderedQuantity = new BigDecimal(row[1].toString());
                line.uom = "EA";
                line.unitCost = new BigDecimal(row[2].toString());
                line.lineTotal = new BigDecimal(row[3].toString());
                persist(line);
                lineNo += 10;
            }
        }
    }

    private void vendorInvoice(Long c, String number, PurVendor vendor, PurOrder order, LocalDate date,
                               VendorInvoiceStatus status, String total, Object[][] lines) {
        PurInvoice invoice = one(PurInvoice.class, c, "invoiceNumber", number, i -> {
            i.invoiceNumber = number;
            i.vendorInvoiceNumber = "EXT-" + number;
            i.vendorId = vendor.id;
            i.orderId = order == null ? null : order.id;
            i.invoiceDate = date;
            i.dueDate = date.plusDays(30);
            i.status = status;
            i.matchStatus = status == VendorInvoiceStatus.MATCHED ? MatchStatus.MATCHED : MatchStatus.PENDING;
            i.currency = "USD";
            i.subtotal = new BigDecimal(lines[0][2].toString()).multiply(new BigDecimal(lines[0][1].toString()));
            i.taxAmount = new BigDecimal(total).subtract(i.subtotal);
            i.totalAmount = new BigDecimal(total);
            i.balanceAmount = new BigDecimal(total);
            i.paidAmount = BigDecimal.ZERO;
            i.notes = "Demo dataset vendor invoice";
        });
        if (em.createQuery("select count(l) from PurInvoiceLine l where l.invoiceId = :id and l.active = true", Long.class)
                .setParameter("id", invoice.id).getSingleResult() == 0) {
            int lineNo = 10;
            for (Object[] row : lines) {
                PurInvoiceLine line = new PurInvoiceLine();
                line.companyId = c;
                line.invoiceId = invoice.id;
                line.lineNo = lineNo;
                line.itemId = ((InvItem) row[0]).id;
                line.description = ((InvItem) row[0]).name;
                line.quantity = new BigDecimal(row[1].toString());
                line.uom = "EA";
                line.unitPrice = new BigDecimal(row[2].toString());
                line.taxAmount = new BigDecimal(row[3].toString());
                line.lineTotal = new BigDecimal(row[2].toString()).multiply(new BigDecimal(row[1].toString()));
                persist(line);
                lineNo += 10;
            }
        }
    }

    /**
     * Returns the existing row for this natural key, or creates it. This is what makes
     * the demo reproducible: the same code always means the same record, so re-running
     * the seed never duplicates rows.
     */
    private <T extends BaseEntity> T one(Class<T> type, Long companyId, String field, String key, Consumer<T> init) {
        List<T> found = em.createQuery("select e from " + CrudService.entityName(type)
                        + " e where e.companyId = :c and e." + field + " = :key", type)
                .setParameter("c", companyId).setParameter("key", key).getResultList();
        if (!found.isEmpty()) {
            return found.getFirst();
        }
        try {
            T entity = type.getDeclaredConstructor().newInstance();
            entity.companyId = companyId;
            init.accept(entity);
            EntityValidation.validate(entity);
            em.persist(entity);
            return entity;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Cannot instantiate " + type.getName(), ex);
        }
    }

    private void persist(BaseEntity entity) {
        if (!(entity instanceof Company) && entity.companyId == null)
            throw new IllegalStateException("Demo records must carry an explicit company ID.");
        EntityValidation.validate(entity);
        em.persist(entity);
    }

    /** Repairs only unscoped children whose parent is proven to belong to this demo company. */
    private void repairLineOwnership(Long company) {
        for (String[] relation : new String[][] {
                {"SalDeliveryLine", "deliveryId", "SalDelivery"}, {"SalInvoiceLine", "invoiceId", "SalInvoice"},
                {"PurGoodsReceiptLine", "receiptId", "PurGoodsReceipt"}, {"PurInvoiceLine", "invoiceId", "PurInvoice"}}) {
            em.createQuery("update " + relation[0] + " l set l.companyId = :c where l.companyId is null and l."
                    + relation[1] + " in (select p.id from " + relation[2] + " p where p.companyId = :c)")
                    .setParameter("c", company).executeUpdate();
        }
    }
}
