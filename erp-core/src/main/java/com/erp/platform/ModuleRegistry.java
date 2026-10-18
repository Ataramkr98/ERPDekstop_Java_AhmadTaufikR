package com.erp.platform;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;

/**
 * Catalog of every entity in the platform, grouped by division.
 *
 * <p>The desktop shell builds its navigation, its command palette index and its
 * "go to record" search from this one endpoint, so a new entity appears in the
 * UI as soon as it exists in the domain with no UI code written for it.
 *
 * <p>Generated from .tools/codegen - do not edit by hand.
 */
@ApplicationScoped
@Path("/api/modules")
public class ModuleRegistry {

    /** One entity exposed in the UI. */
    public record Entry(String division, String divisionLabel, String label,
                        String path, String entity, String search) {}

    public static final List<Entry> ENTRIES = List.of(
        new Entry("finance", "Finance", "Accounts", "/api/finance/accounts", "FinAccount", "code name"),
        new Entry("finance", "Finance", "Posting Periods", "/api/finance/posting-periods", "PostingPeriod", "name"),
        new Entry("finance", "Finance", "Journal Entries", "/api/finance/journal-entries", "JournalEntry", "entryNumber description reference"),
        new Entry("finance", "Finance", "Journal Lines", "/api/finance/journal-lines", "JournalLine", "description"),
        new Entry("finance", "Finance", "Tax Codes", "/api/finance/tax_codes", "FinTaxCode", "code name jurisdiction"),
        new Entry("finance", "Finance", "Exchange Rates", "/api/finance/exchange-rates", "FinExchangeRate", "fromCurrency toCurrency"),
        new Entry("finance", "Finance", "Bank Accounts", "/api/finance/bank_accounts", "FinBankAccount", "code name accountNumber"),
        new Entry("finance", "Finance", "Bank Statements", "/api/finance/bank_statements", "FinBankStatement", "statementRef"),
        new Entry("finance", "Finance", "Bank Statement Lines", "/api/finance/bank_statement_lines", "FinBankStatementLine", "description reference counterparty"),
        new Entry("finance", "Finance", "Fixed Assets", "/api/finance/fixed_assets", "FinFixedAsset", "assetNumber name category"),
        new Entry("finance", "Finance", "Depreciation Runs", "/api/finance/depreciation-runs", "FinDepreciationRun", ""),
        new Entry("finance", "Finance", "Cost Centers", "/api/finance/cost-centers", "FinCostCenter", "code name"),
        new Entry("finance", "Finance", "Profit Centers", "/api/finance/profit-centers", "FinProfitCenter", "code name"),
        new Entry("finance", "Finance", "Budget Plans", "/api/finance/budgets", "FinBudgetPlan", "code name"),
        new Entry("sales", "Sales", "Customers", "/api/sales/customers", "SalCustomer", "code name email taxId"),
        new Entry("sales", "Sales", "Price Lists", "/api/sales/price-lists", "SalPriceList", "code name"),
        new Entry("sales", "Sales", "Price List Lines", "/api/sales/price-list-lines", "SalPriceListLine", ""),
        new Entry("sales", "Sales", "Quotations", "/api/sales/quotations", "SalQuotation", "quoteNumber notes"),
        new Entry("sales", "Sales", "Quotation Lines", "/api/sales/quotation_lines", "SalQuotationLine", "description"),
        new Entry("sales", "Sales", "Orders", "/api/sales/orders", "SalOrder", "orderNumber customerReference notes"),
        new Entry("sales", "Sales", "Order Lines", "/api/sales/order-lines", "SalOrderLine", "description"),
        new Entry("sales", "Sales", "Deliveries", "/api/sales/deliveries", "SalDelivery", "deliveryNumber trackingNumber"),
        new Entry("sales", "Sales", "Delivery Lines", "/api/sales/delivery_lines", "SalDeliveryLine", ""),
        new Entry("sales", "Sales", "Invoices", "/api/sales/invoices", "SalInvoice", "invoiceNumber notes"),
        new Entry("sales", "Sales", "Invoice Lines", "/api/sales/invoice_lines", "SalInvoiceLine", "description"),
        new Entry("sales", "Sales", "RMAs", "/api/sales/r_m_as", "SalRMA", "rmaNumber reasonCode"),
        new Entry("sales", "Sales", "Commissions", "/api/sales/commissions", "SalCommission", ""),
        new Entry("purchasing", "Purchasing", "Vendors", "/api/purchasing/vendors", "PurVendor", "code name email taxId"),
        new Entry("purchasing", "Purchasing", "Requisitions", "/api/purchasing/requisitions", "PurRequisition", "requisitionNumber justification"),
        new Entry("purchasing", "Purchasing", "Requisition Lines", "/api/purchasing/requisition_lines", "PurRequisitionLine", "description"),
        new Entry("purchasing", "Purchasing", "Orders", "/api/purchasing/orders", "PurOrder", "orderNumber notes"),
        new Entry("purchasing", "Purchasing", "Order Lines", "/api/purchasing/order-lines", "PurOrderLine", "description"),
        new Entry("purchasing", "Purchasing", "Goods Receipts", "/api/purchasing/goods-receipts", "PurGoodsReceipt", "receiptNumber deliveryNoteNumber"),
        new Entry("purchasing", "Purchasing", "Goods Receipt Lines", "/api/purchasing/goods_receipt_lines", "PurGoodsReceiptLine", ""),
        new Entry("purchasing", "Purchasing", "Invoices", "/api/purchasing/invoices", "PurInvoice", "invoiceNumber vendorInvoiceNumber"),
        new Entry("purchasing", "Purchasing", "Invoice Lines", "/api/purchasing/invoice_lines", "PurInvoiceLine", "description"),
        new Entry("purchasing", "Purchasing", "Three Way Matches", "/api/purchasing/three-way-matches", "PurThreeWayMatch", "notes"),
        new Entry("inventory", "Inventory", "Items", "/api/inventory/items", "InvItem", "code name barcode category"),
        new Entry("inventory", "Inventory", "Item Conversions", "/api/inventory/item-conversions", "InvItemConversion", "uom"),
        new Entry("inventory", "Inventory", "Warehouses", "/api/inventory/warehouses", "InvWarehouse", "code name"),
        new Entry("inventory", "Inventory", "Locations", "/api/inventory/locations", "InvLocation", "code name"),
        new Entry("inventory", "Inventory", "Stocks", "/api/inventory/stocks", "InvStock", ""),
        new Entry("inventory", "Inventory", "Movements", "/api/inventory/movements", "InvMovement", "movementNumber reasonCode notes"),
        new Entry("inventory", "Inventory", "Lots", "/api/inventory/lots", "InvLot", "lotNumber supplierLotNumber"),
        new Entry("inventory", "Inventory", "Serials", "/api/inventory/serials", "InvSerial", "serialNumber"),
        new Entry("inventory", "Inventory", "Cycle Counts", "/api/inventory/cycle-counts", "InvCycleCount", "countNumber"),
        new Entry("inventory", "Inventory", "Cycle Count Lines", "/api/inventory/cycle_count_lines", "InvCycleCountLine", "notes"),
        new Entry("inventory", "Inventory", "Reorder Rules", "/api/inventory/reorder-rules", "InvReorderRule", ""),
        new Entry("inventory", "Inventory", "Adjustments", "/api/inventory/adjustments", "InvAdjustment", "adjustmentNumber reasonCode"),
        new Entry("manufacturing", "Production", "Boms", "/api/manufacturing/boms", "MfgBom", "bomCode revision"),
        new Entry("manufacturing", "Production", "Bom Lines", "/api/manufacturing/bom_lines", "MfgBomLine", "notes"),
        new Entry("manufacturing", "Production", "Work Centers", "/api/manufacturing/work-centers", "MfgWorkCenter", "code name"),
        new Entry("manufacturing", "Production", "Routings", "/api/manufacturing/routings", "MfgRouting", "routingCode"),
        new Entry("manufacturing", "Production", "Operations", "/api/manufacturing/operations", "MfgOperation", "description"),
        new Entry("manufacturing", "Production", "Work Orders", "/api/manufacturing/work-orders", "MfgWorkOrder", "workOrderNumber notes"),
        new Entry("manufacturing", "Production", "Work Order Operations", "/api/manufacturing/work_order_operations", "MfgWorkOrderOperation", "description"),
        new Entry("manufacturing", "Production", "Material Issues", "/api/manufacturing/material_issues", "MfgMaterialIssue", ""),
        new Entry("manufacturing", "Production", "Mrp Runs", "/api/manufacturing/mrp-runs", "MfgMrpRun", "status"),
        new Entry("manufacturing", "Production", "Planned Orders", "/api/manufacturing/planned-orders", "MfgPlannedOrder", "notes"),
        new Entry("manufacturing", "Production", "Inspection Lots", "/api/manufacturing/inspection_lots", "MfgInspectionLot", "inspectionNumber"),
        new Entry("manufacturing", "Production", "Non Conformances", "/api/manufacturing/non_conformances", "MfgNonConformance", "ncrNumber defectCode"),
        new Entry("crm", "CRM", "Leads", "/api/crm/leads", "CrmLead", "leadNumber company contactName email"),
        new Entry("crm", "CRM", "Accounts", "/api/crm/accounts", "CrmAccount", "code name industry"),
        new Entry("crm", "CRM", "Contacts", "/api/crm/contacts", "CrmContact", "firstName lastName email"),
        new Entry("crm", "CRM", "Opportunities", "/api/crm/opportunities", "CrmOpportunity", "opportunityNumber name competitor"),
        new Entry("crm", "CRM", "Activities", "/api/crm/activities", "CrmActivity", "subject description"),
        new Entry("crm", "CRM", "Campaigns", "/api/crm/campaigns", "CrmCampaign", "code name"),
        new Entry("hr", "People", "Departments", "/api/hr/departments", "HrDepartment", "code name"),
        new Entry("hr", "People", "Positions", "/api/hr/positions", "HrPosition", "code title"),
        new Entry("hr", "People", "Employees", "/api/hr/employees", "HrEmployee", "employeeNumber fullName firstName lastName email"),
        new Entry("hr", "People", "Contracts", "/api/hr/contracts", "HrContract", "contractNumber"),
        new Entry("hr", "People", "Payroll Runs", "/api/hr/payroll-runs", "HrPayrollRun", "periodLabel"),
        new Entry("hr", "People", "Payslips", "/api/hr/payslips", "HrPayslip", ""),
        new Entry("hr", "People", "Leave Types", "/api/hr/leave-types", "HrLeaveType", "code name"),
        new Entry("hr", "People", "Leave Requests", "/api/hr/leave-requests", "HrLeaveRequest", "reason"),
        new Entry("hr", "People", "Timesheets", "/api/hr/timesheets", "HrTimesheet", ""),
        new Entry("hr", "People", "Timesheet Lines", "/api/hr/timesheet_lines", "HrTimesheetLine", "notes"),
        new Entry("hr", "People", "Attendances", "/api/hr/attendances", "HrAttendance", "status"),
        new Entry("hr", "People", "Job Requisitions", "/api/hr/job-requisitions", "HrJobRequisition", "requisitionNumber title"),
        new Entry("hr", "People", "Candidates", "/api/hr/candidates", "HrCandidate", "firstName lastName email"),
        new Entry("project", "Projects", "Projects", "/api/project/projects", "PjmProject", "code name"),
        new Entry("project", "Projects", "Tasks", "/api/project/tasks", "PjmTask", "wbsCode name"),
        new Entry("project", "Projects", "Expenses", "/api/project/expenses", "PjmExpense", "description category"),
        new Entry("project", "Projects", "Milestones", "/api/project/milestones", "PjmMilestone", "name"),
        new Entry("project", "Projects", "Resource Plans", "/api/project/resource-plans", "PjmResourcePlan", "role"),
        new Entry("project", "Projects", "Status Reports", "/api/project/status-reports", "PjmStatusReport", "risksAndIssues"),
        new Entry("service", "Service", "Tickets", "/api/service/tickets", "SvcTicket", "ticketNumber subject description"),
        new Entry("service", "Service", "Sla Policies", "/api/service/sla-policies", "SvcSlaPolicy", "code name"),
        new Entry("service", "Service", "Knowledge Articles", "/api/service/kb-articles", "SvcKnowledgeArticle", "articleNumber title body"),
        new Entry("service", "Service", "Warranties", "/api/service/warranties", "SvcWarranty", "warrantyNumber"),
        new Entry("service", "Service", "Warranty Claims", "/api/service/warranty-claims", "SvcWarrantyClaim", "claimNumber defectCode"),
        new Entry("service", "Service", "Service Contracts", "/api/service/service_contracts", "SvcServiceContract", "contractNumber name"),
        new Entry("service", "Service", "Surveys", "/api/service/surveys", "SvcSurvey", "comment"),
        new Entry("logistics", "Logistics", "Carriers", "/api/logistics/carriers", "LogCarrier", "code name"),
        new Entry("logistics", "Logistics", "Shipments", "/api/logistics/shipments", "LogShipment", "shipmentNumber trackingNumber billOfLadingNumber"),
        new Entry("logistics", "Logistics", "Shipment Lines", "/api/logistics/shipment_lines", "LogShipmentLine", "description"),
        new Entry("logistics", "Logistics", "Routes", "/api/logistics/routes", "LogRoute", "routeNumber vehiclePlate"),
        new Entry("logistics", "Logistics", "Route Stops", "/api/logistics/route_stops", "LogRouteStop", "city"),
        new Entry("logistics", "Logistics", "Freight Costs", "/api/logistics/freight-costs", "LogFreightCost", "costType"),
        new Entry("logistics", "Logistics", "Proof Of Deliveries", "/api/logistics/proof-of-delivery", "LogProofOfDelivery", "receivedBy"),
        new Entry("dms", "Documents", "Folders", "/api/dms/folders", "DmsFolder", "name"),
        new Entry("dms", "Documents", "Documents", "/api/dms/documents", "DmsDocument", "documentNumber name fileName searchText"),
        new Entry("dms", "Documents", "Document Versions", "/api/dms/document_versions", "DmsDocumentVersion", "fileName"),
        new Entry("bi", "Reports & BI", "Kpis", "/api/bi/kpis", "BiKpi", "code name category"),
        new Entry("bi", "Reports & BI", "Kpi Values", "/api/bi/kpi-values", "BiKpiValue", ""),
        new Entry("bi", "Reports & BI", "Dashboards", "/api/bi/dashboards", "BiDashboard", "code name roleCode"),
        new Entry("bi", "Reports & BI", "Tiles", "/api/bi/tiles", "BiTile", "title"),
        new Entry("bi", "Reports & BI", "Reports", "/api/bi/reports", "BiReport", "code name category"),
        new Entry("bi", "Reports & BI", "Report Schedules", "/api/bi/report-schedules", "BiReportSchedule", "name"),
        new Entry("platform", "Administration", "Companies", "/api/platform/companies", "Company", "code name taxId"),
        new Entry("platform", "Administration", "App Users", "/api/platform/app_users", "AppUser", "username fullName email"),
        new Entry("platform", "Administration", "Roles", "/api/platform/roles", "Role", "code name"),
        new Entry("platform", "Administration", "User Roles", "/api/platform/user_roles", "UserRole", "roleCode"),
        new Entry("platform", "Administration", "Permissions", "/api/platform/permissions", "Permission", "code module"),
        new Entry("platform", "Administration", "Audit Logs", "/api/platform/audit_logs", "AuditLog", "username entityType action"),
        new Entry("platform", "Administration", "Number Ranges", "/api/platform/number_ranges", "NumberRange", "documentType prefix"),
        new Entry("platform", "Administration", "System Configs", "/api/platform/system_configs", "SystemConfig", "configKey scope"),
        new Entry("platform", "Administration", "Currencies", "/api/platform/currencies", "Currency", "code name"));

    @GET
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public List<Entry> list() {
        return ENTRIES;
    }
}
