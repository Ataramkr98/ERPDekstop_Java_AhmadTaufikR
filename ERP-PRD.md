# Product Requirements Document
## ERP Desktop Platform — JavaFX + Quarkus + PostgreSQL

| Field | Value |
|---|---|
| **Document title** | ERP Desktop Platform — Product Requirements Document |
| **Version** | 1.0 |
| **Date** | 2026-08-31 |
| **Status** | Draft |
| **Owner** | Product Team |
| **Target stack** | Java 21 LTS, OpenJFX 21, Quarkus 3.15+ LTS, PostgreSQL 16, optional GraalVM/Mandrel native |
| **Distribution** | Desktop application, native installers (`.msi` / `.pkg` / `.deb` / `.rpm` / AppImage) |
| **Audit basis** | All functional claims trace to `research-erp-modules/deliverable.md`; all technical claims trace to `research-tech-architecture/deliverable.md` and `research-lightweight-patterns/deliverable.md` |

---

## Table of Contents

1. [Document metadata](#document-metadata)
2. [Executive Summary](#1-executive-summary)
3. [Vision and Goals](#2-vision-and-goals)
4. [Target Users and Personas](#3-target-users-and-personas)
5. [Scope and Phasing](#4-scope-and-phasing)
6. [Module Breakdown (13 Divisions)](#5-module-breakdown-13-divisions)
   - 6.1 [Finance & Accounting](#61-finance--accounting-fin)
   - 6.2 [Human Resources (HR)](#62-human-resources-hr)
   - 6.3 [Sales & Order Management](#63-sales--order-management-sal)
   - 6.4 [Purchasing & Procurement](#64-purchasing--procurement-pur)
   - 6.5 [Inventory & Warehouse](#65-inventory--warehouse-inv)
   - 6.6 [Manufacturing / Production](#66-manufacturing--production-mfg)
   - 6.7 [CRM](#67-crm-crm)
   - 6.8 [Project Management](#68-project-management-pjm)
   - 6.9 [Service / After-sales](#69-service--after-sales-svc)
   - 6.10 [Logistics & Distribution](#610-logistics--distribution-log)
   - 6.11 [Document Management](#611-document-management-dms)
   - 6.12 [BI & Reporting](#612-bi--reporting-bi)
   - 6.13 [Administration](#613-administration-adm)
7. [Cross-Cutting Features](#6-cross-cutting-features)
8. [Technical Architecture](#7-technical-architecture)
9. [Non-Functional Requirements](#8-non-functional-requirements)
10. [Data Model Overview](#9-data-model-overview)
11. [Integration Points](#10-integration-points)
12. [UX Principles and Key Flows](#11-ux-principles-and-key-flows)
13. [Release Plan](#12-release-plan)
14. [Risks and Mitigations](#13-risks-and-mitigations)
15. [Success Metrics](#14-success-metrics)
16. [Open Questions](#15-open-questions)
17. [Appendix A — Coverage Matrix](#appendix-a--coverage-matrix)
18. [Appendix B — References](#appendix-b--references)

---

## Document Metadata

This PRD is the product of a 3-track research effort (module coverage, technical architecture, performance/UX patterns) plus a downstream synthesis pass. The research deliverables are referenced inline using `[modules]`, `[arch]`, and `[perf]` markers and full URLs in [Appendix B](#appendix-b--references). Every functional claim, performance target, and architecture choice in this document traces to one of those three sources.

The intended audience for this PRD:

- **Product leadership** — for go/no-go and roadmap decisions.
- **Engineering leads (backend, frontend, platform, QA)** — to scope sprints and architecture spikes.
- **Design lead** — to align on UX principles in §11.
- **Sales / solutions engineering** — to position the product against the market in §4 and §5.

---

## 1. Executive Summary

Companies running mid-market to enterprise operations need a single system of record across every division — finance, HR, supply chain, sales, manufacturing, service, projects — without paying the cost of a multi-year SAP or Oracle rollout. The existing mid-market options are either cloud-only (NetSuite, Acumatica Cloud, Odoo Online) which rules out regulated industries and low-connectivity geographies, or open-source server apps (Odoo Community, ADempiere, iDempiere) that demand dedicated IT staff and a modern browser on every desk.

This PRD defines a **desktop-first ERP platform** that runs natively on a Windows, macOS, or Linux PC as a single installer, talks to a local or networked PostgreSQL 16 database, and feels as fast as a SaaS web app while keeping data on the customer's premises. The UI is **JavaFX 21**, the business logic and API surface run on **Quarkus 3.15+** in the same JVM process (with an optional GraalVM/Mandrel native build of the Quarkus core for sub-second cold start), and the system of record is **PostgreSQL 16** with Hibernate ORM Panache and Flyway-managed schema migrations. No browser. No server-side web framework. No Docker required to run.

**Why now.** Three forces converge. First, the JavaFX 21 + Quarkus 3 LTS + Java 21 LTS combination is the first time in a decade that the JVM desktop story is competitive with Electron on cold-start and memory; Quarkus' command-mode entry point makes single-JVM desktop embedding a documented, first-class pattern [arch §1, §4]. Second, regulators in finance, healthcare, and government increasingly require data residency and offline-capable operation, which kills the cloud-only ERP model. Third, customers in Indonesia, Vietnam, the Philippines, and parts of Africa have intermittent connectivity; a desktop app with a local write-queue and a sync engine serves them where cloud-only ERPs cannot.

**Top three success metrics** (see §14 for the full list):
1. **Time-to-first-ledger-entry ≤ 30 minutes** for a new single-company deployment on a 4 GB / SATA SSD / 2-core PC, measured from installer launch to first journal entry posted.
2. **p95 cold-start ≤ 2.0 s (JVM) / ≤ 0.7 s (native backend)** on the same 4 GB PC; p95 table render ≤ 50 ms initial paint and ≤ 8 ms scroll for 10,000-row virtualized tables.
3. **Month-end close for a single-company, 1,000-transaction-per-day workload ≤ 4 hours** of controller time, measured by re-running the standard close checklist script.

---

## 2. Vision and Goals

**Vision.** Be the default desktop ERP for mid-market companies (50–2,000 employees, 1–5 legal entities, 1–5 currencies) that need a fast, offline-tolerant, on-premise or customer-cloud system covering every division of the business — without a multi-year implementation project.

**Goals (SMART).**

| # | Goal | Target | Measurement |
|---|---|---|---|
| G1 | Ship an MVP that books real revenue | Close a paying pilot by end of MVP cycle, defined as ≥ 1 paying customer running the full Sales + Inventory + Finance loop on production data | Sales CRM pipeline |
| G2 | Hit the lightweight feel on a 4 GB PC | p95 cold-start ≤ 2.0 s JVM, ≤ 0.7 s native; idle RSS ≤ 300 MB JVM, ≤ 120 MB native; p95 10k-row table render ≤ 50 ms initial, ≤ 8 ms scroll | CI benchmark on the §8 reference hardware |
| G3 | Cover all 13 divisions by v1.0 | All `M-` tier features from `modules §17` available in the v1.0 release tag; zero `M-` feature marked "future" at v1.0 | PRD traceability matrix at release tag |
| G4 | Pass an independent security review pre-v1.0 | No Critical or High findings open at v1.0 release; OWASP ASVS Level 2 coverage | Security audit report |
| G5 | Reduce month-end close time vs incumbent | Pilot customers measure ≥ 30 % reduction vs their prior system on a controlled workload | Pilot retrospective |
| G6 | Run on customer hardware without upgrade | "Minimum" spec in [arch §14] must support MVP+BI workload | QA test matrix on 4 GB / i3 / SATA-SSD |

**Non-goals for v1.0.** Mobile native apps (web/responsive only as a stretch). Payroll tax filing (we compute payroll, we do not file). Multi-tenant SaaS (the same code is single-tenant per company; SaaS is a v3.0 conversation). IFRS 16 / ASC 842 lease accounting (v2.0). AI/ML forecasting (v2.0+).

---

## 3. Target Users and Personas

Six personas are required to exercise the MVP. A seventh — the IT administrator — is added because the desktop-app model creates operational concerns a cloud ERP does not have (install, update, key management).

### Persona 1 — Maya, CFO / Finance Director
- **Role:** Head of finance, 80–500 employees, multi-entity. Owns statutory reporting, treasury, and the close.
- **Goals:** Close the books fast, get clean trial balance and P&L, manage cash position, control spending with budgets.
- **Frustrations (with current tooling):** Cloud ERPs are slow on reports; spreadsheets proliferate for consolidation; multi-currency revaluation is manual; audit trail is incomplete.
- **Daily tasks:** Approve journal entries, review AP/AR aging, run month-end close, approve budgets, present numbers to the CEO.
- **Top KPIs she cares about:** days-to-close, audit findings, cash conversion cycle.

### Persona 2 — Budi, Controller
- **Role:** Senior accountant reporting to Maya. Owns the chart of accounts, tax engine, and reconciliation.
- **Goals:** Keep the GL clean, reconcile bank daily, post payroll and depreciation journals, prepare tax filings.
- **Frustrations:** Bank reconciliation is half-automated; the tax engine is opaque; period-end checklist lives in a spreadsheet; the audit log is too slow to query.
- **Daily tasks:** Post journals, reconcile bank, answer auditor questions, run trial balance, prepare management reports.

### Persona 3 — Sari, HR Manager
- **Role:** Runs payroll, leave, and hiring for 50–500 employees.
- **Goals:** Run payroll on time every month, stay on top of leave, track new hires, generate year-end tax forms.
- **Frustrations:** Payroll tax is a manual spreadsheet; leave conflicts with the production schedule; new-hire onboarding lives in 4 different tools.
- **Daily tasks:** Approve leave, run payroll preview, generate payslips, update employee records, post job openings.

### Persona 4 — Denny, Salesperson
- **Role:** Outside or inside sales, 10–100 active accounts, hits a quota.
- **Goals:** Get quotes out fast, see customer 360 (history, open orders, AR), win deals.
- **Frustrations:** Quoting tool is separate from the ERP; can't see if a customer is on credit hold without calling finance; mobile app is read-only.
- **Daily tasks:** Create quotations, follow up opportunities, check order status, take orders on the road.

### Persona 5 — Rina, Warehouse Operator
- **Role:** Runs a 200–5,000 SKU warehouse, 1–3 sites, often with a barcode scanner.
- **Goals:** Receive against PO, pick accurately, ship on time, keep stock count right.
- **Frustrations:** Scanners and ERP don't talk; count adjustments need a paper form; cycle count freezes the warehouse; bin labels fall off.
- **Daily tasks:** Receive goods, putaway, pick and pack, ship, do cycle counts.

### Persona 6 — Anton, Production Supervisor
- **Role:** Owns 1–5 production lines in a discrete-manufacturing shop.
- **Goals:** Hit the production plan, keep WIP accurate, keep scrap low, run MRP.
- **Frustrations:** MRP suggestions arrive in a separate Excel from the work order; shop floor is paper; quality data is in a binder.
- **Daily tasks:** Release work orders, confirm operations, run MRP, raise NCRs, post production to inventory.

### Persona 7 — Indra, System Administrator
- **Role:** IT manager, sometimes the only IT person; responsible for keeping the desktop ERP running for 5–200 users across 1–5 sites.
- **Goals:** Install and update the app without breaking anything; keep backups; manage users and licenses; answer support questions.
- **Frustrations:** Installers that require admin and reboot; updates that need a manual server outage; backup scripts that break silently; no way to see who is using which version.
- **Daily tasks:** Deploy new releases via the auto-update channel, restore from backup, onboard new users, audit license usage, troubleshoot crash dumps.

---

## 4. Scope and Phasing

### 4.1 In scope

- **All 13 functional divisions** with at least the `MUST` tier of features per [modules §17 coverage matrix].
- **Cross-cutting platform services**: workflow engine, notifications, audit, master data, search, i18n, comments, multi-company, multi-currency.
- **Three delivery channels:** native desktop installer (primary), offline-capable read-only REST surface on `127.0.0.1` (secondary, for tooling), and CSV/Excel import-export.
- **Three operating modes per [arch §4]:** single-JVM main mode (default), single-JVM with native Quarkus core (premium), and forward-compatible thin-client mode for VDI/terminal-server (v1.0+).
- **Localization** at MVP: English (en-US, en-GB) and Bahasa Indonesia (id-ID). Framework supports adding more locales without code changes.
- **Currencies** at MVP: any ISO 4217; daily exchange rate import from a configurable source (e.g. Bank Indonesia reference rate for IDR).
- **Statutory** at MVP: Indonesia PPh 21 withholding calc, e-SPT export, e-Faktur-ready invoice numbering; framework for adding more countries (see [modules HR §2.2]).

### 4.2 Out of scope (explicitly)

- **Mobile native apps.** Web/responsive companion only as a v1.5+ stretch; the desktop app is the system of record.
- **Multi-tenant SaaS hosting.** The same code can be repackaged as multi-tenant later; for v1.0 each customer is single-tenant.
- **Payroll tax filing to authority.** We compute payroll, generate the form, export the file; we do not submit to DJP / IRS / HMRC.
- **BI data warehouse / star schema ETL.** Operational reports use direct SQL; a v2.0+ feature pulls operational data into a star schema.
- **MES (Manufacturing Execution System) integration.** Shop-floor terminals are out of scope; the desktop ERP talks to a future MES via the REST surface in v2.0.
- **CPQ (Configure-Price-Quote) with rules engine.** v2.0+.
- **Voice picking, pick-to-light, drones.** v2.0+; v1.0 supports barcode scanner input only.
- **AI features** (forecasting, sentiment, NL query). v2.0+.

### 4.3 Phasing

The phasing follows [modules §18] and is constrained by [arch §4]'s hardware budget on the minimum 4 GB spec.

| Phase | Scope (functional) | Scope (non-functional) | Indicative duration |
|---|---|---|---|
| **MVP** | Finance (`M-FIN-001` … `M-FIN-012`); Sales (`M-SAL-001` … `M-SAL-010`); Purchasing (`M-PUR-001` … `M-PUR-010`); Inventory (`M-INV-001` … `M-INV-008`); CRM (`M-CRM-001, 004, 005, 007`); DMS (`M-DMS-001` … `M-DMS-005`); Administration (`M-ADM-001` … `M-ADM-010`); all `X-NOT/AUD/WFL/MDM/SCH/I18N` `M`-tier cross-cutting. i18n: en, id. | Single-JVM architecture; jlink-shrunk JRE; jpackage installers for Win/Mac/Linux; auto-update via update4j; local SQLite offline cache; Flyway migrations. | 9–12 months |
| **v1.0** | Manufacturing (`M-MFG-001` … `M-MFG-010`); Project Management (`M-PJM-001` … `M-PJM-009`); Logistics (`M-LOG-001` … `M-LOG-008`); full Inventory (`S-INV-001` … `S-INV-010`); Service (`M-SVC-001` … `M-SVC-008`); SSO + SCIM; BI (`M-BI-001` … `M-BI-008`); add 2 more locales (e.g. Vietnamese, Filipino). | Optional GraalVM native build of the Quarkus core (v1.0+); thin-client mode (JavaFX + remote Quarkus) for VDI. | +6–9 months |
| **v1.5** | All `S-` tier features from [modules §17]; e-invoicing (e-Faktur full submission, Peppol); OCR for vendor invoices; mobile warehouse app (read-only first); E-signature. | Field-level encryption at rest; HSM-backed key management (optional); multi-region read replicas for cloud deployments. | +6 months |
| **v2.0+** | All `N-` tier features; IFRS 16 lease accounting; AI forecasting and NL query; APS / MES integrations; CPQ. | Multi-tenant SaaS variant; SaaS control plane. | TBD |

---

## 5. Module Breakdown (13 Divisions)

Every section below follows the same template. Feature IDs match [modules] exactly so engineering can grep from this PRD to the research source.

### 6.1 Finance & Accounting (FIN)

**Scope.** The auditable transaction backbone of the ERP. Every operational event (sales order, purchase receipt, payroll run, depreciation posting) ultimately produces a journal entry into the General Ledger. F&A must support multi-company, multi-currency, multi-period close, statutory and management reporting, tax compliance, and intercompany accounting [modules §2.1; SAP S/4HANA Finance, 2024; Oracle Fusion GL, 2024].

**Features (MUST tier).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-FIN-001 | Chart of Accounts (CoA) | Hierarchical segments (company, cost center, profit center, account, sub-account) with account type taxonomy (Asset / Liability / Equity / Revenue / Expense). | M | Account hierarchy is unlimited-depth, supports 5 segments minimum, and is exportable to a one-line-per-account CSV. |
| M-FIN-002 | General Ledger | Double-entry journal entries, posting periods, period locking, audit trail on every posting. | M | Every GL posting produces a balanced entry (debit = credit) with a created-by user, timestamp, source document, and an immutable journal line record. Period locking prevents back-dating. |
| M-FIN-003 | Accounts Payable (AP) | Vendor invoice entry (with 3-way match integration), credit memos, payment runs, payment terms, aging report. | M | User can enter a vendor invoice matched to a PO and goods receipt, with a tolerance rule; the aging report buckets 0-30 / 31-60 / 61-90 / 90+ in the company's reporting currency. |
| M-FIN-004 | Accounts Receivable (AR) | Customer invoicing, credit memos, dunning letters, collections worklist, aging report. | M | Sales order shipping auto-generates a customer invoice; dunning letter template is configurable; collections worklist shows overdue invoices sorted by aging bucket. |
| M-FIN-005 | Bank Reconciliation | Bank statement import (CAMT.053, BAI2, CSV) and auto-matching engine. | M | User imports a CAMT.053 file, the system proposes matches against open AR/AP, and a one-click confirm posts the bank statement lines to a reconciliation account. |
| M-FIN-006 | Fixed Asset Register | Acquisition, depreciation (straight-line, declining balance, units of production), disposal, transfer, impairment. | M | Asset can be created from a PO receipt or manually; depreciation run posts monthly journals; disposal posts a gain/loss on disposal. |
| M-FIN-007 | Multi-Currency | Daily exchange rate table, revaluation at period end, realized vs unrealized FX gain/loss. | M | Exchange rates are entered per currency per day; period-end revaluation runs in a background job; unrealized gain/loss posts to a configurable account. |
| M-FIN-008 | Tax Engine | Configurable tax codes, tax jurisdictions, tax rates, tax-inclusive / tax-exclusive pricing, tax reports. | M | A non-coder admin can define a new tax code, attach it to a tax jurisdiction, and have it appear on every relevant transaction; tax report outputs CSV/PDF. |
| M-FIN-009 | Cost Center / Profit Center | Allocation cycles. | M | User can allocate one cost center's expenses to N target cost centers by fixed percentage or variable driver; allocation runs in a single click and posts journals. |
| M-FIN-010 | Period-End Close | Checklist with locking, opening/closing entries, retained earnings, year-end rollover. | M | Close checklist shows 12+ standard steps; each step is owner-assigned, time-stamped, and gates the next; year-end rollover creates the new fiscal year with opening balances. |
| M-FIN-011 | Financial Reports | Trial Balance, Balance Sheet, P&L, Cash Flow Statement (direct + indirect). | M | Reports render in < 2 s for a single company with 1M journal lines; drill-down from any report line to the source journal entries. |
| M-FIN-012 | Journal Entry Approval | Workflow above configurable thresholds. | M | A JE above the user's approval limit is routed to the next approver; rejection sends a comment back to the originator. |

**Key data entities.** ChartOfAccounts, Account, AccountSegment, JournalEntry, JournalLine, PostingPeriod, PostingPeriodLock, Vendor, VendorInvoice, VendorPayment, Customer, CustomerInvoice, CustomerPayment, DunningLetter, BankAccount, BankStatement, BankStatementLine, BankReconciliation, FixedAsset, AssetDepreciationSchedule, AssetDisposal, Currency, ExchangeRate, ExchangeRateType, TaxCode, TaxJurisdiction, TaxRate, CostCenter, ProfitCenter, InternalOrder, BudgetPlan, BudgetVersion, Encumbrance, IntercompanyPartner, IntercompanyInvoice. (32 entities — all from [modules §2.5, §2.7].)

**Key reports.** Trial Balance; Balance Sheet (comparative period); P&L (YTD, MTD, comparative); Cash Flow Statement (direct + indirect); AP Aging; AR Aging; Fixed Asset Register; Tax Return (per jurisdiction); Budget vs Actual by Cost Center; Cash Position (multi-bank). ([modules §2.7].)

**Integration points.** Banking (CAMT.053, BAI2, MT940, ISO 20022 pain.001); tax authority (e-Faktur Indonesia, GST/HST, VAT, US Sales & Use); payroll (post payroll journals from HR); Sales/Purchasing (3-way match, auto-invoice from delivery). ([modules §2.6].)

---

### 6.2 Human Resources (HR)

**Scope.** Employee lifecycle: recruitment, onboarding, records, payroll, time & attendance, leave, performance, training, offboarding. Must integrate tightly with Finance (payroll journal), Administration (user provisioning), and statutory reporting [modules §3.1; SAP SuccessFactors, 2024; Oracle Fusion HCM, 2024; Microsoft Dynamics 365 HR, 2024].

**Features (MUST tier).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-HR-001 | Employee Master | Personal data, employment terms, contracts, identifiers, organizational assignment. | M | Each employee has a unique ID; personal data is encrypted at the column level; employment history is append-only. |
| M-HR-002 | Organizational Structure | Legal entity, business unit, department, position, job, reporting hierarchy. | M | Org chart is built from positions; supports matrix reporting (a position can report to two positions). |
| M-HR-003 | Payroll | Gross-to-net calculation, salary components, payslip generation, bank file. | M | A payroll run for 100 employees completes in < 30 s; payslips are generated as PDF; bank file exports in the configured format. |
| M-HR-004 | Time & Attendance | Clock-in/out, shift schedule, overtime, timesheet approval. | M | Employees can clock in via the desktop app or web; overtime rules are configurable; supervisor approves timesheets in batch. |
| M-HR-005 | Leave Management | Leave types, entitlement, request, approval workflow, leave balance. | M | Annual leave accrues per the rule (e.g. 12 days/year, prorated); balance is visible to the employee and the manager; an approved leave block creates a calendar event. |
| M-HR-006 | Recruitment / ATS | Job requisition, candidate pipeline, interview scheduling, offer. | M | Recruiter can move a candidate through stages (Applied → Screened → Interviewed → Offered → Hired); offer letter is generated from a template. |
| M-HR-007 | Statutory Deductions | Country-specific tax & social security (BPJS, PPh 21, W-4/W-2, PAYE). | M | For Indonesia: PPh 21 TER (effective rate) method is implemented; BPJS Kesehatan (1% employee, 4% employer) and BPJS Ketenagakerjaan (JHT, JP, JKK, JKM) are auto-calculated. |
| M-HR-008 | Year-End Tax Form | 1721 A1/A2 (Indonesia), W-2 (US), etc. | M | Year-end form is generated as a government-formatted PDF; the data file is exportable for e-SPT. |
| M-HR-009 | Employee Self-Service (ESS) | Request leave, view payslip, update personal info. | M | ESS is accessible from the desktop app's "My Profile" menu and from the read-only web companion. |
| M-HR-010 | Onboarding / Offboarding | Checklists with task assignments to HR, IT, manager. | M | A new-hire checklist creates IT tasks (laptop, account) and HR tasks (contract, tax form); completion is tracked per task. |

**Key data entities.** Employee, EmploymentContract, EmployeeAssignment, Department, Position, Job, Grade, PayrollRun, Payslip, SalaryComponent, TaxDeduction, TimeEntry, Shift, AttendanceRecord, LeaveType, LeaveEntitlement, LeaveRequest, JobRequisition, Candidate, Application, Interview, OfferLetter, Goal, Review, Rating, Course, Enrollment, Certificate, BenefitPlan, BenefitEnrollment. (28 entities.)

**Key reports.** Headcount by department / location / employment type; Payroll register (per period); Payslip; Tax form (per jurisdiction); Leave balance / utilization; Attrition rate; Time-to-hire / Cost-per-hire; Performance rating distribution; Overtime report; Training completion rate. ([modules §3.7].)

**Integration points.** Finance (payroll journal); Administration (user provisioning / deprovisioning); Statutory (e-SPT, e-Faktur withholding, BPJS Ketenagakerjaan, BPJS Kesehatan, bank for salary file); Identity (SSO via SAML/OIDC, SCIM); Calendar (Google/Outlook); Background check provider. ([modules §3.6].)

---

### 6.3 Sales & Order Management (SAL)

**Scope.** The order-to-cash cycle: lead-to-quote, quote-to-order, order-to-delivery, delivery-to-invoice, invoice-to-cash. Supports complex pricing (tiered, matrix, discount, promotional), multi-channel orders, returns, sales commission [modules §4.1; SAP S/4HANA SD, 2024; Oracle Fusion OM, 2024; Microsoft Dynamics 365 Sales, 2024].

**Features (MUST tier).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-SAL-001 | Customer Master | Credit limit, payment terms, address book, contact roles, tax exemption. | M | Customer is uniquely identified by code; credit limit check is performed at order entry; address book supports billing/shipping/multiple sites. |
| M-SAL-002 | Quotation | Line items, validity period, T&Cs, revision history, convert-to-order. | M | Quote can be revised; the revision history is kept; one click converts a quote to a sales order. |
| M-SAL-003 | Sales Order | Header, line items, schedule lines, status tracking (open, partial, delivered, invoiced, closed). | M | Order status transitions are gated; partial deliveries and partial invoices are tracked per line. |
| M-SAL-004 | Pricing Engine | Price lists, tiered / volume pricing, customer-specific pricing, currency. | M | Pricing engine selects the right price for (customer, item, quantity, date) per a priority chain; the price is shown broken down (base, discount, surcharge). |
| M-SAL-005 | Discount Management | Manual discounts, approval workflow, promotional discounts, mix-and-match. | M | Discounts above a configured threshold require approval; promotional discounts apply automatically based on cart contents. |
| M-SAL-006 | Tax Calculation | On order, integration with tax engine. | M | Order line tax is computed by the FIN tax engine; tax-exclusive and tax-inclusive pricing both supported. |
| M-SAL-007 | ATP / Inventory Check | Available-to-promise with backorder handling. | M | At order entry, ATP shows (on-hand + incoming − reserved − allocated) per item per warehouse. |
| M-SAL-008 | Order-to-Warehouse Handoff | Pick/pack/ship hand-off. | M | Releasing an order creates a pick list; ship confirm decrements stock and posts a GL journal to COGS. |
| M-SAL-009 | RMA | Return request, inspection, refund / replacement / credit. | M | RMA is linked to the original delivery line; the disposition (refund / replace / credit) drives a credit memo or replacement order. |
| M-SAL-010 | Sales Commission | Commission rules by product / customer / rep, accrual at invoice, payout. | M | A commission is accrued at invoice time per the rule; a monthly payout run produces a per-rep statement. |

**Key data entities.** Customer, CustomerContact, CustomerAddress, CustomerRole, PriceList, PriceListLine, DiscountMatrix, Quotation, QuotationLine, QuotationRevision, SalesOrder, SalesOrderLine, ScheduleLine, OrderStatus, Delivery, DeliveryLine, PickingList, PackingSlip, CustomerInvoice, ARCreditMemo, RMA, RMALine, ReturnReasonCode, CommissionRule, CommissionTransaction, CommissionPayout, SalesContract, ContractRelease. (27 entities.)

**Key reports.** Open order book by status / age; Sales by customer / product / salesperson / region; Sales pipeline (quote → order conversion); Top N customers; Discount analysis; Backorder report; RMA trend; Commission statement; Sales vs target / quota. ([modules §4.7].)

**Integration points.** Finance (invoice posting to AR, credit limit check); Inventory (ATP check, pick list, goods issue); CRM (opportunity → quotation conversion); Logistics (shipment booking); E-commerce (order intake); Tax (tax calculation per line). ([modules §4.6].)

---

### 6.4 Purchasing & Procurement (PUR)

**Scope.** The procure-to-pay cycle: identify need → request → approval → RFQ → vendor selection → PO → goods receipt → invoice → 3-way match → payment. Enforces segregation of duties, supports strategic sourcing, feeds inventory and AP [modules §5.1; SAP S/4HANA MM, 2024; Oracle Fusion Procurement, 2024; Microsoft Dynamics 365 SCM Procurement, 2024].

**Features (MUST tier).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-PUR-001 | Vendor Master | Vendor roles (supplier, subcontractor, service provider), address book, payment terms, bank details, tax ID. | M | Vendor record holds bank details for payment; tax ID is validated against a configurable algorithm. |
| M-PUR-002 | Purchase Requisition (PR) | Internal request, item master reference, quantity, required date, requestor. | M | PR can be created from MRP exception messages, from a manual entry, or from a sales-order-driven demand. |
| M-PUR-003 | Purchase Order (PO) | Header, line items, schedule lines, terms, approval status, revisions. | M | PO can be created from an approved PR, from an RFQ award, or directly; revisions are tracked. |
| M-PUR-004 | Goods Receipt (GR) | Receive against PO, over/under-receipt tolerance, quality inspection trigger. | M | GR creates stock receipts per line; over-receipt beyond tolerance is blocked or warned per config. |
| M-PUR-005 | 3-Way Match | PO ↔ GR ↔ Vendor Invoice tolerance check (price, quantity, terms). | M | On vendor invoice entry, the system computes tolerance per line (price ±, qty ±) and posts a hold with reason if mismatched. |
| M-PUR-006 | RFQ | RFQ to multiple vendors, vendor response capture, award. | M | RFQ is sent to N vendors; responses are captured side-by-side; award creates a PO from the chosen line. |
| M-PUR-007 | Approval Workflow | Configurable matrix (PR / PO by amount, category, cost center). | M | Approval rules are data-driven, not hard-coded; a non-coder admin can change thresholds. |
| M-PUR-008 | Vendor Invoice | Entry, match-to-PO, AP posting. | M | Invoice can be entered manually, imported via OCR, or ingested as an e-invoice; match results drive the AP journal. |
| M-PUR-009 | Service Entry Sheet | Services procurement (qty × rate × period). | M | Service entry sheet approves a service PO line and triggers the AP invoice. |
| M-PUR-010 | Vendor Invoice OCR / E-Invoice | Ingestion. | M | OCR processes PDF invoices and proposes a vendor invoice draft with field-level confidence; user confirms to post. |

**Key data entities.** Vendor, VendorContact, VendorBankDetail, PurchaseRequisition, PurchaseRequisitionLine, RFQ, RFQResponse, RFQAward, PurchaseOrder, PurchaseOrderLine, PurchaseOrderSchedule, GoodsReceipt, GoodsReceiptLine, VendorInvoice, InvoiceLine, ThreeWayMatch, ServiceEntrySheet, SourceList, QuotaArrangement, VendorScorecard. (20 entities.)

**Key reports.** Open PR/PO by status / age; Spend by vendor / category / cost center; Vendor on-time delivery & quality; Price variance (PO vs invoice); 3-way match exception list; Spend saving; Maverick spend; Lead time analysis (PR → PO → GR). ([modules §5.7].)

**Integration points.** Finance (AP posting, payment run); Inventory (GR updates stock); Quality (inspection lot from GR); Vendor (PO dispatch, ASN); Tax / Customs (duty, withholding); Logistics (inbound shipment). ([modules §5.6].)

---

### 6.5 Inventory & Warehouse (INV)

**Scope.** Physical and logical movement of stock: item master, multi-location, lot/serial tracking, barcode, movements, cycle counting, valuation, replenishment, pick/pack/ship. Bridge between Purchasing, Manufacturing, Sales, and Finance [modules §6.1; SAP S/4HANA EWM, 2024; Oracle Fusion Inventory, 2024; Microsoft Dynamics 365 Warehouse Management, 2024].

**Features (MUST tier).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-INV-001 | Item Master | SKU, GTIN/EAN/UPC barcode, UoM conversions, weight/dimensions, item category, tax code. | M | Item master holds the primary UoM and conversion factors to alternate UoMs; barcode is unique per item. |
| M-INV-002 | Multi-Warehouse / Multi-Location | Site, warehouse, zone, aisle, rack, bin hierarchy. | M | Hierarchy is unlimited-depth; each item can be stocked in any subset of locations. |
| M-INV-003 | Stock Movements | Goods receipt, putaway, transfer, pick, pack, ship, adjustment, scrap, return. | M | Every movement has a type, source, destination, reason, and reference document; on-hand is recomputed transactionally. |
| M-INV-004 | Lot Management | Lot number, expiry date, supplier lot, manufacturing date, attributes. | M | Lot number is captured at GR; expiry, FEFO pick, and lot attributes (potency, grade) are first-class. |
| M-INV-005 | Serial Number Management | Capture at GR and ship, warranty linkage, parent-child (kitting). | M | Serial numbers are captured at GR and validated at ship; each serial has a full movement history. |
| M-INV-006 | Barcode / QR Code | Print labels, scan-to-receive, scan-to-pick, scan-to-issue, mobile scanner support. | M | Label printer integration (Zebra ZPL) and scanner input (HID mode) both work; scan error is recoverable. |
| M-INV-007 | Stock Valuation | Standard, Moving Average, FIFO, LIFO; reconciliation to GL. | M | Valuation method is per item per company; the value layer is auditable; GL inventory account reconciles. |
| M-INV-008 | Cycle Count & Physical Inventory | Count by ABC class, blind count, variance adjustment, freeze/unfreeze. | M | Cycle count is scheduled by ABC class (A items counted monthly); variance is approved via workflow; freeze/unfreeze gates the affected items. |
| M-INV-009 | Replenishment Rules | Reorder point, safety stock, min-max. | M | Reorder proposal is generated by rule; the proposal becomes a PR or PO depending on config. |
| M-INV-010 | Bin Management | Putaway strategy (fixed, random, wave), pick strategy (FIFO, FEFO, LIFO). | M | Bin is selectable on every movement; pick list follows the configured strategy. |
| M-INV-011 | Stock Aging & Obsolescence | Analysis with provision calculation. | M | Aging buckets are configurable; provision is posted as an inventory write-off to a configurable account. |

**Key data entities.** Item, ItemUoM, ItemUoMConversion, Barcode, Warehouse, Location, Zone, Aisle, Rack, Bin, StockOnHand, StockMovement, StockReservation, Lot, SerialNumber, CycleCount, CycleCountEntry, CountVariance, PickList, Wave, PackStation, InventoryAdjustment, InventoryWriteOff, ReorderRule, ReplenishmentProposal, ItemCost, CostLayer. (26 entities.)

**Key reports.** Stock on hand by item / warehouse / lot / serial; Stock aging; Stock movement history; Slow-mover / dead-stock report; Cycle count variance; Inventory valuation; Reorder report; Warehouse utilization; Pick accuracy / on-time shipment. ([modules §6.7].)

**Integration points.** Purchasing (GR against PO); Sales (pick / ship); Manufacturing (raw issue, finished receipt); Finance (stock valuation GL); Quality (inspection hold/release); Logistics (outbound shipment); Mobile scanners / label printers; IoT sensors (cold chain). ([modules §6.6].)

---

### 6.6 Manufacturing / Production (MFG)

**Scope.** Make-to-stock and make-to-order: Bill of Materials (BOM), routing, work orders, MRP, shop floor execution, quality control. Mandatory for discrete and process manufacturers [modules §7.1; SAP S/4HANA PP, 2024; Oracle Fusion Manufacturing, 2024; Microsoft Dynamics 365 Manufacturing, 2024].

**Features (MUST tier, all MFG-001 through MFG-010 from [modules §7.2]).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-MFG-001 | Bill of Materials (BOM) | Multi-level, phantom assemblies, alternates, by-products, scrap %. | M | BOM is multi-level (≥ 5 levels deep), supports alternates and by-products; a phantom assembly explodes in the BOM tree. |
| M-MFG-002 | Routing / Work Center | Operations, machine, labor, time (setup, run, teardown), overhead rate. | M | Routing defines each operation with its work center, time standard, and cost; cost rollup uses both labor and overhead. |
| M-MFG-003 | Work Order | Linked to SO or forecast; status (planned → released → in-process → complete → closed). | M | Work order status transitions are gated; status drives material availability check. |
| M-MFG-004 | MRP | Exploded demand from SOs/forecast, plan orders, exception messages. | M | MRP run is fully re-runnable; exceptions are listed in priority order; plan orders are convertible to POs or WOs. |
| M-MFG-005 | Capacity Planning | Finite vs infinite, work center capacity, load vs capacity, leveling. | M | Capacity is per work center per period; load vs capacity report identifies bottlenecks. |
| M-MFG-006 | Shop Floor Execution | Operation confirmation, time ticket, qty good/scrap, backflush. | M | Operator confirms an operation; the time ticket is posted to labor cost; qty good posts finished goods; scrap is tracked separately. |
| M-MFG-007 | Quality Control | Inspection plan, in-process inspection, AQL sampling, NCR, CAPA. | M | Inspection plan is per item; sampling follows AQL; non-conformance raises an NCR which gates the lot. |
| M-MFG-008 | Production Costing | Actual vs standard, variance analysis (PPV, labor, overhead). | M | Standard cost is per BOM/routing rollup; actual cost is captured at confirmation; variance is reported and posted. |
| M-MFG-009 | Subcontracting | Operations in routing. | M | A subcontracted operation triggers a PO to the subcontractor and consumes components at issue. |
| M-MFG-010 | By-Product / Co-Product | Accounting. | M | By-products are valued at the configured rate; co-product cost allocation uses a sales-value-at-split method. |

**Key data entities.** Item, ItemCategory, BOM, BOMLine, BOMRevision, Routing, Operation, WorkCenter, WorkOrder, WorkOrderOperation, OperationConfirmation, MRPPlan, PlannedOrder, MRPException, InspectionPlan, InspectionLot, InspectionCharacteristic, InspectionResult, NonConformanceReport, CAPAAction, ProductionCost, VarianceRecord, Tool, ToolMaintenanceRecord. (23 entities.)

**Key reports.** Open work orders; MRP exception messages; Production actual vs standard (variance); WIP by work order / cost center; Capacity utilization; Scrap and yield; Quality defect Pareto; OEE; Lead time (planned vs actual); Engineering change history. ([modules §7.7].)

**Integration points.** Inventory (raw issue, finished receipt); Sales (CTP for make-to-order); Purchasing (buy components for shortage); Quality (inspection lot from GR/production); Finance (WIP accounting, variance posting); Maintenance; PLM (optional); MES. ([modules §7.6].)

---

### 6.7 CRM (CRM)

**Scope.** Pre-sale customer engagement: lead capture, contact management, opportunity pipeline, activity tracking, account/contact 360, marketing attribution, post-sale customer health [modules §8.1; Salesforce Sales Cloud, 2024; Microsoft Dynamics 365 Sales, 2024; HubSpot, 2024; Odoo CRM, 2024].

**Features (MUST tier).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-CRM-001 | Lead | Capture (web form, email, import), qualification (BANT), conversion to Account/Contact/Opportunity. | M | A lead can be created manually, imported from CSV, or received from a web form; conversion creates Account, Contact, and optionally Opportunity. |
| M-CRM-002 | Account | Company record with hierarchy, industry, revenue, ownership. | M | Account hierarchy (parent/child) supports M&A structures. |
| M-CRM-003 | Contact | Roles (decision-maker, influencer, end-user), preferences, do-not-call. | M | Contact has a role per account; communication preference gates email/SMS sends. |
| M-CRM-004 | Opportunity | Stages, probability, expected close date, amount, competitor, deal team. | M | Pipeline kanban drag-drop; probability auto-recalculates on stage change; expected close drives the forecast. |
| M-CRM-005 | Activity Tracking | Task, call, email, meeting logged to account/contact/opportunity. | M | Activity is logged from desktop, web, or email plugin; activity feed is visible per record. |
| M-CRM-006 | Customer 360 | Single screen: sales history, open orders, open tickets, AR aging, contacts, activities. | M | Customer 360 is a tab on the Account record; loads under 1 s for an account with 1 year of history. |
| M-CRM-007 | Sales Pipeline | Kanban view by stage, drag-drop, conversion-rate analytics. | M | Pipeline kanban allows stage move; conversion rate is computed per stage. |
| M-CRM-008 | Email Integration | Outlook / Gmail sync, mail merge. | M | Contacts and emails are two-way synced with the configured provider; mail merge uses a templated body. |
| M-CRM-009 | Quotes from CRM | With link to ERP pricing engine. | M | A "Create Quote" button on the Opportunity opens the SAL quotation with the pricing engine applied. |

**Key data entities.** Lead, LeadSource, LeadScore, Account, AccountHierarchy, AccountIndustry, Contact, ContactRole, ContactPreference, Opportunity, OpportunityLineItem, OpportunityStage, Competitor, Activity, Campaign, CampaignMember, CampaignResponse, Quote, CustomerHealthScore. (19 entities.)

**Key reports.** Pipeline by stage / owner / source; Win rate / loss rate; Average deal size; Sales cycle length; Lead-to-deal funnel; Top N accounts by revenue potential; Activity by rep; Campaign ROI; Forecast accuracy; Account engagement score. ([modules §8.7].)

**Integration points.** Sales/Order Management (opportunity → quote → SO); Service (ticket history on account); Finance (AR aging, payment history); Email (M365 / Google Workspace); Calendar; Telephony; Marketing; BI. ([modules §8.6].)

---

### 6.8 Project Management (PJM)

**Scope.** Project-based business: project definition, WBS, task assignment, timesheets, expense capture, milestone billing, project profitability [modules §9.1; Oracle Fusion PPM, 2024; Microsoft Dynamics 365 Project Operations, 2024; SAP Project System, 2024].

**Features (MUST tier).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-PJM-001 | Project Master | Customer, type (internal/external/fixed-price/T&M), status, dates, budget. | M | Project type drives billing logic (T&M vs fixed-price milestone vs cost-plus). |
| M-PJM-002 | WBS | Hierarchical tasks with start/end, predecessor, resource. | M | WBS is multi-level; Gantt view shows predecessors as dependency arrows. |
| M-PJM-003 | Task Assignment | Responsible person, planned effort, status, % complete. | M | Assignment is per task; effort is in hours; % complete is editable and computed. |
| M-PJM-004 | Timesheet | Entry by employee per task per day, approval, payroll integration. | M | Weekly timesheet; approved timesheet posts to project cost and to payroll. |
| M-PJM-005 | Project Expense | Billable vs non-billable, mark-up rules, approval. | M | Expense is captured per project; mark-up is applied per rule; billable expense is invoiced. |
| M-PJM-006 | Milestone Billing | Fixed-price milestone invoices; T&M invoices. | M | Milestone completion triggers an invoice draft; T&M invoice aggregates approved timesheet and expense. |
| M-PJM-007 | Project Profitability | Revenue, cost, margin, by project / WBS. | M | Margin is computed as (revenue recognized − cost incurred) per project and per WBS. |
| M-PJM-008 | Project Status Reporting | Status report template, RAG status. | M | A standard status report template pulls from project data; RAG is per WBS. |
| M-PJM-009 | Resource Planning | Demand vs supply by role, period. | M | Demand is from project plans; supply is from HR positions; gap is shown by role per period. |

**Key data entities.** Project, ProjectType, WBS, Task, TaskAssignment, Timesheet, TimesheetLine, ProjectExpense, Milestone, ProjectInvoice, ProjectRevenue, ProjectCost, ProjectStatusReport, ResourcePlan, ResourceAssignment. (15 entities.)

**Key reports.** Project P&L; WBS burn-down; Resource utilization; Timesheet compliance; Milestone slippage; Project pipeline (sales); Project risk register. ([modules §9.7].)

**Integration points.** HR (resource assignment, payroll); Finance (project P&L, AR invoicing); Sales (project-based quotation); Service (time & materials). ([modules §9.6].)

---

### 6.9 Service / After-sales (SVC)

**Scope.** Post-sale service: ticket management, SLA tracking, warranty, returns (RMA), field service dispatch, knowledge base, customer satisfaction [modules §10.1; Salesforce Service Cloud, 2024; Microsoft Dynamics 365 Field Service, 2024; Zendesk, 2024].

**Features (MUST tier).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-SVC-001 | Ticket | Create, classify, prioritize, assign, resolve, close. | M | Ticket is created from email, web form, or manually; classification and priority drive SLA. |
| M-SVC-002 | SLA | First-response, resolution, escalation rules. | M | SLA timer is per priority; breach triggers escalation. |
| M-SVC-003 | Knowledge Base | Articles, search, attachments, version history. | M | Article can be linked to a ticket; search returns relevant articles in < 200 ms. |
| M-SVC-004 | Customer Portal | Self-service ticket creation, status, knowledge base. | M | Portal is reachable from the desktop app's read-only web companion. |
| M-SVC-005 | Warranty Management | Per-item warranty, claim creation, claim resolution. | M | Warranty is per serial number; claim auto-checks coverage. |
| M-SVC-006 | RMA Link | Link service ticket to RMA. | M | A service ticket can spawn an RMA; the RMA's disposition (refund/replace/credit) is shown on the ticket. |
| M-SVC-007 | Service Contract | Per-customer service agreement, included hours, rate. | M | Service contract entitles the customer to N hours; overage is billed. |
| M-SVC-008 | CSAT Survey | Post-resolution survey, score, comments. | M | Survey is sent on ticket close; CSAT score is aggregated per period. |

**Key data entities.** Ticket, TicketComment, TicketAttachment, SLAPolicy, SLAClock, KBArticle, KBArticleVersion, Warranty, WarrantyClaim, RMA, ServiceContract, ServiceContractLine, Survey, CSATScore, FieldServiceOrder, FieldServiceAssignment. (16 entities.)

**Key reports.** Ticket volume by priority / status / channel; SLA compliance; Average resolution time; First-call resolution; CSAT trend; Warranty claim rate; Service contract utilization. ([modules §10.7].)

**Integration points.** Sales (RMA); Inventory (spare parts); Finance (warranty accrual, service invoicing); CRM (customer history); Knowledge base / LMS. ([modules §10.6].)

---

### 6.10 Logistics & Distribution (LOG)

**Scope.** Outbound and inbound logistics: shipments, delivery, fleet, routes, carriers, freight cost [modules §11.1; SAP Transportation Management, 2024; Oracle Transportation Management, 2024; Manhattan Active TM, 2024].

**Features (MUST tier).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-LOG-001 | Shipment | Outbound shipment header, lines, status, carrier, tracking. | M | Shipment is created from a delivery; carrier and tracking number are recorded. |
| M-LOG-002 | Inbound Shipment | Advance ship notice (ASN), receipt against ASN. | M | Vendor sends ASN; GR is created from ASN with tolerance check. |
| M-LOG-003 | Carrier Master | Carrier, service level, rate card, transit time. | M | Carrier rate card is configurable; rate lookup is per (origin, destination, weight, service). |
| M-LOG-004 | Freight Cost | Calculation, accrual, reconciliation, vendor invoice. | M | Freight cost is accrued at shipment; reconciled to carrier invoice; variance is reported. |
| M-LOG-005 | Route | Planned route, stops, distance, duration. | M | Route is per shipment; distance is from the map service. |
| M-LOG-006 | Proof of Delivery | Signature, photo, timestamp. | M | POD is captured via the mobile app and attached to the shipment. |
| M-LOG-007 | Multi-Modal | Road, sea, air, with mode-specific docs. | M | Shipment mode is selectable; mode-specific fields (B/L for sea, AWB for air) are captured. |
| M-LOG-008 | Returns Logistics | Reverse logistics for RMA, customer pickup. | M | RMA pickup is scheduled; reverse shipment is tracked; returned stock is received. |

**Key data entities.** Shipment, ShipmentLine, Carrier, ServiceLevel, RateCard, RateLine, ASN, ASNLine, FreightCost, Route, RouteStop, POD, MultiModalDoc, ReverseShipment. (14 entities.)

**Key reports.** On-time shipment rate; Freight cost per shipment / per kg / per km; Carrier scorecard; Shipment exception list; Reverse logistics cost. ([modules §11.7].)

**Integration points.** Sales (outbound shipment); Purchasing (inbound ASN); Inventory (ship decrement); Finance (freight accrual, carrier AP); Service (reverse logistics for RMA). ([modules §11.6].)

---

### 6.11 Document Management (DMS)

**Scope.** Centralized attachment, versioning, approval workflow, and retention for documents attached to any record in the ERP [modules §12.1; Microsoft SharePoint, 2024; OpenText, 2024].

**Features (MUST tier).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-DMS-001 | Attachment | Attach a file to any entity. | M | A "paperclip" button is available on every form; files up to 100 MB are supported. |
| M-DMS-002 | Versioning | New version creates a new revision, previous is kept. | M | Previous versions are accessible; current version is shown. |
| M-DMS-003 | Folder Structure | Hierarchical folders, per entity type, per project. | M | Folders are navigable in a tree; files can be moved. |
| M-DMS-004 | Full-Text Search | Search file content (PDF, DOCX, XLSX). | M | Full-text search returns results in < 500 ms for 1M documents. |
| M-DMS-005 | Access Control | Per-folder and per-document ACL. | M | ACL respects the FIN RBAC model; a denied user does not see the file at all. |
| M-DMS-006 | Check-In / Check-Out | Prevent concurrent edits. | M | Checked-out file is locked; the locker is shown. |
| M-DMS-007 | Document Type Definition | Type, template, mandatory metadata. | M | A non-coder admin can define a new document type with mandatory fields. |
| M-DMS-008 | Audit Log | Per-document view, download, edit history. | M | Audit log is immutable and queryable by user, document, and date range. |

**Key data entities.** Document, DocumentVersion, DocumentFolder, DocumentACL, DocumentType, DocumentMetadata, DocumentAudit. (7 entities.)

**Key reports.** Storage by entity type; Stale document report; Access review report. ([modules §12.7].)

**Integration points.** All entity-driven modules (DMS is the attachment store); OCR (S-DMS-002); E-signature (S-DMS-001). ([modules §12.6].)

---

### 6.12 BI & Reporting (BI)

**Scope.** Operational dashboards, KPIs, standard reports, ad-hoc report builder, scheduled report delivery [modules §13.1; Microsoft Power BI, 2024; SAP Analytics Cloud, 2024; Oracle Fusion Analytics, 2024].

**Features (MUST tier).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-BI-001 | Pre-Built Dashboards | CFO, Sales, Inventory, HR, Production. | M | At least 5 role-based dashboards ship at MVP. |
| M-BI-002 | KPI Library | Headcount, revenue, AR aging, DSO, inventory turnover, on-time ship, OEE, etc. | M | KPI library is a named list; each KPI has a definition, owner, and source. |
| M-BI-003 | Standard Reports | Trial balance, P&L, AP/AR aging, sales by customer, inventory valuation, payroll register. | M | Standard reports render in < 5 s for a single company with 1M rows; can be exported to PDF/XLSX/CSV. |
| M-BI-004 | Drill-Down to Detail | From any KPI to source records. | M | A click on a KPI tile drills to the underlying records. |
| M-BI-005 | Filters & Dimensions | Per report, per dashboard. | M | Filters include date range, company, business unit, dimension hierarchy. |
| M-BI-006 | Export | To Excel, PDF, CSV. | M | Export preserves formatting and the active filter. |
| M-BI-007 | Scheduled Report Delivery | Email a report PDF on a schedule. | M | Schedule is per report; recipient list is configurable. |
| M-BI-008 | Role-Based Dashboards | Different dashboard per role. | M | Each role has a default dashboard; user can pin additional. |

**Key data entities.** Dashboard, DashboardTile, KPI, KPIValue, Report, ReportDefinition, ReportSchedule, ReportSubscription. (8 entities.)

**Key reports.** All M-BI-001 through M-BI-008 are themselves reports. ([modules §13.7].)

**Integration points.** All entity-driven modules (BI is the read side); Email (SMTP); future data warehouse (v2.0). ([modules §13.6].)

---

### 6.13 Administration (ADM)

**Scope.** Platform administration: user/role, permissions, audit log, master data, multi-company, multi-language, multi-currency [modules §14.1].

**Features (MUST tier).**

| ID | Name | Description | Priority | Acceptance criteria |
|---|---|---|---|---|
| M-ADM-001 | User Account | Username, email, status, password policy. | M | Password policy is configurable (min length, complexity, expiry); failed-login lockout after N attempts. |
| M-ADM-002 | RBAC | Role-based access control. | M | Roles are data-driven; permission sets are reusable; deny overrides allow. |
| M-ADM-003 | Multi-Company / Multi-Ledger | Multiple legal entities, separate or shared CoA. | M | User can switch active company; reports respect active company unless explicitly cross-company. |
| M-ADM-004 | Multi-Currency (Platform) | Per-company base currency, transaction currency, reporting currency. | M | Each company has a base currency; transactions can be in any active currency. |
| M-ADM-005 | Multi-Language (Platform) | UI locale per user. | M | Each user has a UI locale; locale bundles are loaded on demand. |
| M-ADM-006 | Audit Log | User, timestamp, action, entity, before/after. | M | Audit log is immutable and queryable; export to CSV is available. |
| M-ADM-007 | Master Data Management (MDM) | Central master data with approval and dedup. | M | Master data is owned by a designated role; creation/update goes through approval if value > threshold. |
| M-ADM-008 | Company Master | Legal entity, registration, tax ID, address, base currency. | M | Company master holds statutory identifiers; configurable fields. |
| M-ADM-009 | Number Range / Sequence | Per document type, per company, with prefix and reset. | M | Number range is configurable; preview shows next number; gaps are tracked. |
| M-ADM-010 | System Configuration | Feature flags, integration settings, scheduler. | M | System config is per company or global; changes are audit-logged. |

**Key data entities.** User, Role, Permission, PermissionSet, UserRole, Company, CompanyConfig, AuditLog, MasterDataRule, NumberRange, SystemConfig, License, Session, DeviceBinding. (14 entities.)

**Key reports.** User activity; Login history; Failed login report; Permission audit; License usage; Configuration change log. ([modules §14.7].)

**Integration points.** Identity (SSO via SAML/OIDC, SCIM provisioning); Backup system; OS credential store (Windows Credential Manager / macOS Keychain / libsecret via java-keyring [arch §1, §5]). ([modules §14.6].)

---

## 6. Cross-Cutting Features

These are horizontal capabilities consumed by every division. Their feature IDs are X-NOT, X-AUD, X-WFL, X-MDM, X-SCH, X-I18N, X-CMT from [modules §15]. Each MUST-tier row below must be present in the MVP build.

| ID | Name | Description | Priority |
|---|---|---|---|
| X-NOT-001 | In-App Notification Center | A bell icon shows pending approvals, mentions, system events; user can act inline. | M |
| X-NOT-002 | Email Notification | SMTP via Quarkus Mailer; templated with Qute. | M |
| X-NOT-003 | SMS / WhatsApp Notification | Optional gateway (Twilio, Meta WhatsApp Business) for time-sensitive events. | S |
| X-NOT-004 | Desktop Push Notification | OS-native notification via JavaFX `Notifications` API or platform equivalent. | S |
| X-NOT-005 | Event-Driven Notification Bus | CDI events trigger notifications per subscription rules. | S |
| X-NOT-006 | Notification Subscription | Per user, per event type, per channel. | S |
| X-NOT-007 | Quiet Hours / Do Not Disturb | User-configured; suppresses non-critical notifications. | N |
| X-AUD-001 | Field-Level Audit Trail | Per record, per field, before/after. | M |
| X-AUD-002 | Login & Session Audit | Successful, failed, expired sessions, IP, device. | M |
| X-AUD-003 | Permission Audit Report | Who has what permission, when granted, by whom. | S |
| X-AUD-004 | Immutable WORM Storage | Audit log written to append-only storage. | S |
| X-AUD-005 | GDPR / PDP Subject Access | Data export and delete per data subject. | S |
| X-WFL-001 | Workflow Engine Core | State machine, role-based assignment, parallel/serial paths. | M |
| X-WFL-002 | Visual Workflow Designer | Drag-drop editor for non-coders. | S |
| X-WFL-003 | Delegation & Escalation | Out-of-office delegation, time-based escalation. | S |
| X-WFL-004 | Workflow API | Programmatic workflow definition and instance query. | S |
| X-MDM-001 | Central Master Data Governance | Customer, vendor, item, employee, account master. | M |
| X-MDM-002 | Master Data Approval | New/changed master data above threshold needs approval. | M |
| X-MDM-003 | Deduplication & Golden Record | Fuzzy match on key fields, manual merge. | S |
| X-MDM-004 | Master Data Change History | Per field, per record. | M |
| X-SCH-001 | Global Cross-Entity Search | One search box across customers, vendors, items, employees, projects, documents. | M |
| X-SCH-002 | Saved & Recent Searches | Per user. | S |
| X-I18N-001 | Locale Bundle (UI Translation) | en-US, en-GB, id-ID at MVP. | M |
| X-I18N-002 | Date / Number / Currency Formatting | Per user locale. | M |
| X-I18N-003 | Time Zone Handling (UTC store) | All timestamps stored in UTC; rendered in user TZ. | M |
| X-I18N-004 | Right-to-Left (RTL) Support | Layout flip for ar-SA, he-IL, fa-IR. | N |
| X-CMT-001 | Per-Record Comments & Activity Feed | Threaded comments, @mention. | S |
| X-CMT-002 | User-Defined Tags / Labels | Per record, free-form. | S |
| X-CMT-003 | Favorites / Pinned Records | User-level. | S |

**Notification delivery budget** (per [arch §15]): in-app delivery is sub-100 ms p95 from event to notification center; email delivery is sub-5 s p95.

**Workflow engine** (per [arch §11]): the platform workflow is a lightweight, in-process BPMN-subset engine; it is not a full Camunda. Visual designer is post-MVP.

---

## 7. Technical Architecture

The full rationale is in [arch §1–§14]. This section is the binding summary the engineering team works from. Every choice below is justified in [arch].

### 7.1 Single recommended architecture

**Single-JVM main mode**: JavaFX (UI) and Quarkus (business logic, persistence, API) run inside one process launched by `QuarkusMain`, sharing one classloader and CDI container. UI ↔ backend communication is in-process via CDI-injected JAX-RS resource proxies. A small local HTTP listener on `127.0.0.1` is exposed for operational concerns (health, metrics, admin CLI, optional remote automation) but is not the primary data path [arch §1, §4].

**Variants:**
- **Standard (MVP, v1.0 default):** single-JVM, fast-jar (`jlink`-shrunk JRE + `jpackage`).
- **Premium (v1.0+):** same UI, but the Quarkus core is a GraalVM/Mandrel native `*-runner` binary. Sub-second cold start, ~120 MB RSS.
- **Forward-compatible:** thin JavaFX + remote Quarkus for VDI / terminal-server. Same code; in-process CDI proxies swap for Quarkus REST clients.

### 7.2 Architecture diagram (per [arch §3])

```
                +====================================================+
                |       ONE JVM PROCESS (quarkus-main + JavaFX)       |
                |====================================================|
                |                                                    |
                |  +-----------------+         +-------------------+  |
                |  | JavaFX          |  CDI    | Quarkus backend   |  |
                |  | (UI thread)     |<------->| (worker threads)  |  |
                |  | - Views (FXML)  |  proxy  | - Quarkus REST    |  |
                |  | - ViewModels    |         | - Panache entities|  |
                |  | - Preloader     |         | - Services        |  |
                |  | - Cmd Palette   |         | - Scheduler       |  |
                |  +--------+--------+         +---------+---------+  |
                |           |                             |            |
                |           |      HikariCP (Agroal)      |            |
                |           |  max=20, min=2              |            |
                |           +-------------+---------------+            |
                |                         |                            |
                |                         v                            |
                |              +---------------------+                 |
                |              | JDBC pool (Agroal)  |                 |
                |              +----------+----------+                 |
                |                         |                            |
                +=========================|============================+
                                          |
                  +-----------------------+-----------------------+
                  |                                               |
                  v                                               v
        +-------------------+                       +-----------------------+
        |  PostgreSQL 16    |                       |  Local cache (SQLite) |
        |  (server / LAN /  |   <-- sync engine --> |  - write queue         |
        |  cloud)           |       (background)   |  - last-good-read cache|
        +-------------------+                       +-----------------------+

   Secondary surfaces (always-on, not the hot path):
   - 127.0.0.1:<random>: /q/health, /q/metrics, /openapi, /q/dev (dev only)
   - update4j bootstrapper (signed delta from Maven repo)
   - OS credential store (Windows Credential Manager / macOS Keychain / libsecret)
         via java-keyring (pure Java, no JNI)
```

### 7.3 The lightweight budget (per [arch §2])

This table is the contract. Any release that misses a target fails QA. See [arch §2] for the source basis and [perf §2.1] for the boot-path decomposition.

| Metric | Target (JVM) | Target (native backend) | How it's measured |
|---|---|---|---|
| Cold start (launcher icon → first window interactive) | ≤ 2.0 s | ≤ 0.7 s | Time from process spawn to `Stage.show()` |
| Warm start | ≤ 1.0 s | ≤ 0.3 s | Second launch in same session |
| Idle RAM (logged in, no activity) | ≤ 300 MB RSS | ≤ 120 MB RSS | 60 s after login, `ps -orss` / `Get-Process WorkingSet64` |
| Working RAM (single form open, 10k rows) | ≤ 500 MB | ≤ 200 MB | Sampled during a real workload |
| Installer size (downloaded) | ≤ 200 MB | ≤ 80 MB | `du -sh` of the installable artifact |
| Installed size on disk | ≤ 600 MB | ≤ 250 MB | After first-run setup |
| Frame time p95 | < 16 ms | < 16 ms | JavaFX `PulseLogger` / JFR |
| Table render p95 (10k rows, virtualized) | < 50 ms initial, < 8 ms scroll | same | TestFX harness |
| Search debounce | 150 ms | 150 ms | UX rule |
| In-process call p95 | < 5 ms | < 5 ms | CDI method call + transactional read |
| Local HTTP call p95 (when used) | < 25 ms | < 25 ms | localhost REST round-trip |
| DB query p95 (PK lookup, warm) | < 2 ms | < 2 ms | `pg_stat_statements` |
| DB query p95 (paginated list, 50 rows, indexed) | < 25 ms | < 25 ms | `LIMIT/OFFSET` + covering index |
| Migration apply time (cold) | < 30 s for 200 migrations | < 30 s | Flyway benchmark |
| CPU on idle | < 1% on x86_64 | < 1% | Sampled 60 s |
| Min CPU for end user | x86_64 1.6 GHz dual-core, SSE4.2 | same | Verified by QA test matrix |
| Min RAM for end user | 4 GB system, 1 GB free | 4 GB system, 512 MB free | Hard floor, 2018-class laptop |
| Min disk | 2 GB free | 1 GB free | Excludes customer data |

### 7.4 Stack pinning

- **Java:** 21 LTS (one minor version back supported; see [arch §17, #8]).
- **OpenJFX:** 21.
- **Quarkus:** 3.15 LTS (compatible through 3.20).
- **PostgreSQL:** 16 (15 also supported).
- **GraalVM / Mandrel:** 23.x for native builds.
- **HikariCP defaults** (cited from HikariCP README in [arch §1]): `connectionTimeout=30000 ms`, `idleTimeout=600000 ms`, `maxLifetime=1800000 ms`, `autoCommit=true`. Our `maximumPoolSize=20` is justified against the HikariCP pool-sizing wiki [arch §7].

### 7.5 Build, package, distribute

- **Build tool:** Maven multi-module (preferred over Gradle for Quarkus native build tooling maturity as of 2026-08-31).
- **Module boundary:** JPMS for the runtime image; `jlink --add-modules` for the slim runtime; optional modules shipped as separate jars installed at first run [arch §11].
- **Packaging:** `jpackage --app-image` for `.msi` (Windows), `.pkg` (macOS), `.deb` / `.rpm` (Linux), AppImage (Linux portable). Custom `jlink`-shrunk JRE inside.
- **Auto-update:** `update4j` (signed delta, Maven-based metadata, works with GitHub Releases or self-hosted) [arch §1, §6].
- **OS code-signing:** Authenticode on Windows, `notarytool` on macOS, GPG on Linux packages.

### 7.6 Observability

- **Metrics:** Quarkus Micrometer → `/q/metrics` in Prometheus format.
- **Health:** SmallRye Health → `/q/health/live`, `/q/health/ready`.
- **Logs:** `quarkus-logging-json`, structured JSON to stdout; shipped to a configurable Loki / ELK endpoint.
- **Traces:** OpenTelemetry for cross-process; W3C tracecontext in headers.
- **FX diagnostics:** a `/diag/fx-pulses` endpoint returning the last 100 pulse intervals from a `PulseListener` (per [perf §1.3]) so the desktop feel is observable in production.

### 7.7 Testing

- **Unit (70 %):** services, view-models, Panache queries, sync engine. No `@QuarkusTest`, fast.
- **Slice (20 %):** `@QuarkusTest` + RestAssured. Each module's REST surface verified. Uses Quarkus Dev Services to spin up PostgreSQL automatically [arch §13].
- **UI (5 %):** TestFX + headless Glass (`-Dglass.platform=Monocle -Dmonocle.platform=Headless -Dprism.order=sw`).
- **Integration (5 %):** `@QuarkusIntegrationTest` against the packaged native runner, runs the same test suite against the binary.
- **Coverage targets:** ≥ 70 % backend, ≥ 50 % UI; build fails on regression.
- **CI matrix:** PR (unit + slice + TestFX, Linux x86_64, JDK 21, < 8 min); nightly (+ native integration, + headless UI on macOS/Windows, < 30 min); release (full matrix + signed installers).

### 7.8 Security architecture

- **Credentials:** stored in the OS credential store via the `java-keyring` library (pure Java, no JNI). Linux distros without `gnome-keyring` / `kwallet` fall back to an AES-GCM-encrypted file in `~/.config/erp/keyring.bin` with a documented warning [arch §1, §17 #3].
- **Session:** SmallRye JWT (MP-JWT 1.2) for the local HTTP surface; in-process CDI proxy for the in-process hot path.
- **Password hashing:** Argon2id (OWASP Password Storage Cheat Sheet).
- **Encryption at rest:** PostgreSQL `pgcrypto` for sensitive columns; full-disk encryption recommended at OS level.
- **Encryption in transit:** TLS 1.3 to the PostgreSQL server.
- **RBAC:** Quarkus Security with Jakarta Persistence; permission checks are method-level via `@RolesAllowed` and data-level via Panache entity predicates.

### 7.9 Offline-first

- **Local cache:** SQLite for the write queue and last-good-read master data. Same SQL dialect family as PostgreSQL, pure Java JDBC [arch §1, §8].
- **Sync engine:** background drainer, last-write-wins with conflict flag for review; on reconnect, queued writes are replayed in order.
- **Stale UI:** master data displays a "last synced at HH:MM" badge; an explicit refresh action triggers a sync.

---

## 8. Non-Functional Requirements

### 8.1 Performance (NFR-P)

| ID | Requirement | Source |
|---|---|---|
| NFR-P-01 | Cold start ≤ 2.0 s JVM / ≤ 0.7 s native on 4 GB / i3 / SATA-SSD PC | [arch §2] |
| NFR-P-02 | Idle RSS ≤ 300 MB JVM / ≤ 120 MB native | [arch §2] |
| NFR-P-03 | p95 table render < 50 ms initial / < 8 ms scroll for 10k virtualized rows | [perf §1.1, §2.1] |
| NFR-P-04 | p95 FX pulse interval < 18 ms, p99 < 33 ms | [perf §1.3] |
| NFR-P-05 | p95 in-process call < 5 ms | [arch §2] |
| NFR-P-06 | p95 paginated DB query (50 rows, indexed) < 25 ms | [arch §2] |
| NFR-P-07 | Search debounce 150 ms | [perf §1.5] |
| NFR-P-08 | UI feedback within 100 ms of any user input (skeleton / spinner) | [perf exec summary] |
| NFR-P-09 | Page size cap: no list returns > 200 rows from a single query | [perf exec summary] |

### 8.2 Security (NFR-S)

| ID | Requirement | Source |
|---|---|---|
| NFR-S-01 | OWASP ASVS Level 2 coverage at v1.0 | [arch §1] |
| NFR-S-02 | Argon2id for password hashing | [arch §1] |
| NFR-S-03 | OS credential store for stored credentials (java-keyring) | [arch §1, §17 #3] |
| NFR-S-04 | TLS 1.3 to PostgreSQL | [arch §1] |
| NFR-S-05 | RBAC with method-level `@RolesAllowed` and data-level predicates | [arch §1] |
| NFR-S-06 | Audit log immutable, append-only, WORM-style at v1.5 | [arch §15] |
| NFR-S-07 | Field-level encryption for PII at v1.5 | [arch §15] |
| NFR-S-08 | GDPR / UU PDP right-to-access and right-to-erasure for personal data | [modules §14.7 X-AUD-005] |
| NFR-S-09 | Auto-update is signed (update4j signature verification) | [arch §1] |
| NFR-S-10 | Installer code-signed (Authenticode, notarized, GPG) | [arch §6] |

### 8.3 Scalability (NFR-SC)

| ID | Requirement |
|---|---|
| NFR-SC-01 | A single installation supports up to 200 named users (single company) without re-architecting |
| NFR-SC-02 | A single installation supports up to 5 legal entities, 5 currencies, 5 UI locales |
| NFR-SC-03 | GL holds 10M journal lines per company with the §7.3 query p95 budget |
| NFR-SC-04 | Inventory holds 1M stock-keeping records per warehouse |
| NFR-SC-05 | Open order book up to 100k active orders |
| NFR-SC-06 | HikariCP `max=20` is the upper bound; if load grows, scale by adding CPU, not pool size (per HikariCP pool-sizing wiki [arch §7]) |

### 8.4 Accessibility (NFR-A)

| ID | Requirement | Source |
|---|---|---|
| NFR-A-01 | JavaFX accessibility API used everywhere; `AccessibleRole` and `AccessibleAttribute` set on every interactive node | [arch §15] |
| NFR-A-02 | WCAG 2.1 AA target for color contrast and font scaling | [arch §15] |
| NFR-A-03 | Keyboard reachability: every action reachable without a mouse | [perf §6.6] |
| NFR-A-04 | Screen reader tested with VoiceOver (macOS) and NVDA (Windows) | [arch §15] |
| NFR-A-05 | High-contrast theme available | [arch §15] |
| NFR-A-06 | System font scale honored (100 % / 125 % / 150 %) | [arch §15] |

### 8.5 Localization (NFR-L)

| ID | Requirement | Source |
|---|---|---|
| NFR-L-01 | MVP locales: en-US, en-GB, id-ID. Adding a new locale requires only a `.properties` bundle | [arch §15] |
| NFR-L-02 | All timestamps stored in UTC, rendered in user TZ | [modules X-I18N-003] |
| NFR-L-03 | Date / number / currency formatting per user locale (ICU4J or JDK) | [arch §15] |
| NFR-L-04 | v1.5: RTL support (ar-SA, he-IL, fa-IR) | [arch §15] |

### 8.6 Offline behavior (NFR-O)

| ID | Requirement | Source |
|---|---|---|
| NFR-O-01 | Master data reads work offline (last-good-read cache) | [arch §1, §8] |
| NFR-O-02 | Transaction writes (sales order, inventory movement, etc.) are queued offline and replayed on reconnect | [arch §1, §8] |
| NFR-O-03 | Conflict resolution: last-write-wins with conflict flag for manual review | [arch §8] |
| NFR-O-04 | Offline cache size cap: 50 MB (configurable); eviction is LRU | [arch §1, §17 #5] |
| NFR-O-05 | Sync engine must not block the UI thread | [perf §10] |
| NFR-O-06 | First launch / first record open must work without ever having been online | [arch §1] |

### 8.7 Backup and recovery (NFR-BR)

| ID | Requirement | Source |
|---|---|---|
| NFR-BR-01 | Daily scheduled `pg_dump` to `<user.home>/.config/erp/backups/` for local PG; S3 push for cloud PG | [arch §15] |
| NFR-BR-02 | Backup encryption with age / AES-GCM | [arch §15] |
| NFR-BR-03 | One-click restore from a local backup | [arch §15] |
| NFR-BR-04 | Retention: default 30 daily + 12 monthly; configurable | [arch §15] |
| NFR-BR-05 | RPO ≤ 24 h, RTO ≤ 4 h for a 10 GB database on a typical office PC | [arch §15] |

### 8.8 Data retention (NFR-DR)

| ID | Requirement |
|---|---|
| NFR-DR-01 | Audit log retained ≥ 7 years (statutory) |
| NFR-DR-02 | Financial transactions retained ≥ 10 years (tax) |
| NFR-DR-03 | Personal data (employee, customer contact) configurable per data category, with hard delete on erasure request |
| NFR-DR-04 | Soft-delete (tombstone) for business records; hard-delete only on legal-hold expiry or explicit erasure |

---

## 9. Data Model Overview

The model below is the canonical list of 50+ top-level entities, grouped by module, with the key cross-module relationships that drive the implementation. Full schema is in `db/migration/V<n>__*.sql` (Flyway), maintained by the engineering team. Entity names match [modules] sections so this PRD is the single source of truth for engineering scoping.

### 9.1 Platform & Administration (8)

- **Company** — legal entity, base currency, locale, registration, tax ID.
- **User** — login identity, owned by a Company; bridges to Employee when applicable.
- **Role** — named set of permissions; a user has 1..N roles per company.
- **Permission** — fine-grained capability (`fin.journal.post`, `inv.stock.adjust`, etc.).
- **AuditLog** — append-only; `entity_type`, `entity_id`, `user_id`, `before`, `after`, `at`, `ip`.
- **NumberRange** — `company_id`, `document_type`, `prefix`, `next_value`, `reset_rule`.
- **SystemConfig** — key-value per scope (global / company).
- **License** — seats, expiry, signature.

### 9.2 Finance (10)

- **ChartOfAccounts**, **Account** (hierarchical).
- **JournalEntry**, **JournalLine** (sum-of-debits = sum-of-credits invariant).
- **PostingPeriod** (with `locked` flag).
- **VendorInvoice** ↔ **PurchaseOrder** + **GoodsReceipt** (3-way match).
- **CustomerInvoice** (auto-generated from a Delivery or a Project milestone).
- **BankAccount**, **BankStatement**, **BankReconciliation**.
- **FixedAsset** + **DepreciationSchedule**.
- **TaxCode**, **TaxJurisdiction**, **TaxRate**.
- **ExchangeRate** (per currency per day).
- **BudgetPlan**, **BudgetVersion**, **Encumbrance**.

### 9.3 HR (7)

- **Employee** (PII encrypted at column level).
- **EmploymentContract**, **EmployeeAssignment**.
- **Department**, **Position**, **Job** (org structure).
- **PayrollRun**, **Payslip**, **SalaryComponent**.
- **LeaveRequest**, **LeaveBalance**.
- **JobRequisition**, **Candidate**, **Application**.

### 9.4 Sales (6)

- **Customer** (with credit limit).
- **PriceList**, **PriceListLine**.
- **Quotation** (with revision history).
- **SalesOrder** + **SalesOrderLine** + **ScheduleLine**.
- **Delivery** + **DeliveryLine** + **PickingList**.
- **RMA** + **RMALine**.

### 9.5 Purchasing (5)

- **Vendor** (with bank detail).
- **PurchaseRequisition** + **Line**.
- **PurchaseOrder** + **Line** + **Schedule**.
- **GoodsReceipt** + **Line**.
- **VendorInvoice** + **Line** + **ThreeWayMatch**.

### 9.6 Inventory (6)

- **Item** + **ItemUoMConversion** + **Barcode**.
- **Warehouse** + **Location** + **Bin** (hierarchical).
- **StockOnHand** (current per item per location per lot/serial).
- **StockMovement** (immutable journal of every change).
- **Lot**, **SerialNumber** (when applicable).
- **CycleCount** + **CycleCountEntry** + **CountVariance**.

### 9.7 Manufacturing (5)

- **BOM** + **BOMLine** (multi-level, with alternates).
- **Routing** + **Operation** + **WorkCenter**.
- **WorkOrder** + **WorkOrderOperation**.
- **OperationConfirmation** (time, qty good, qty scrap).
- **InspectionPlan** + **InspectionLot** + **InspectionResult**.

### 9.8 CRM (5)

- **Account** (CRM-side, may link to a Customer record).
- **Contact**.
- **Lead** (with `converted_at` linking to Account/Contact/Opportunity).
- **Opportunity** + **OpportunityLineItem**.
- **Activity** (task, call, email, meeting).

### 9.9 Project Management (3)

- **Project** + **ProjectType** (drives billing).
- **WBS** (hierarchical tasks).
- **Timesheet** + **TimesheetLine**.

### 9.10 Service, Logistics, DMS, BI (4)

- **Ticket** + **SLAPolicy** + **SLAClock**.
- **Shipment** + **ShipmentLine** + **Carrier** + **RateCard**.
- **Document** + **DocumentVersion** + **DocumentFolder** + **DocumentACL**.
- **Dashboard** + **DashboardTile** + **KPI** + **ReportDefinition**.

**Total ≈ 60 top-level entities.** Cross-module relationships that the implementation must get right on day one (the "structural" edges of the model):

- `SalesOrder` → `Delivery` → `CustomerInvoice` → AR `JournalEntry`.
- `PurchaseOrder` → `GoodsReceipt` → `VendorInvoice` → AP `JournalEntry` (3-way match).
- `BOM` + `Routing` → `WorkOrder` → `OperationConfirmation` → `ProductionCost` + `StockMovement` (raw issue / finished receipt) + variance `JournalEntry`.
- `RMA` ↔ `CustomerInvoice` (credit memo) ↔ `SalesOrder` (replacement).
- `JournalEntry` ← every transaction (single source of truth for the GL).
- `AuditLog` ↔ every entity (every create / update / delete).
- `Employee` ↔ `User` (an Employee is a User when the company enables self-service; otherwise a `User` is an external identity like a vendor portal user).
- `Company` ↔ `Currency` (base) + `Locale` (UI default) + `ChartOfAccounts` (optional shared CoA across companies).

---

## 10. Integration Points

### 10.1 Inbound (into the ERP)

- **CSV / Excel import** — every master-data and transactional entity supports a column-mapped CSV import with preview, validation report, and dry-run.
- **REST API** — read-write REST surface on `127.0.0.1:<random>` (loopback only) for tooling, automation, and the future thin-client variant. Documented via OpenAPI. Authentication via MP-JWT 1.2.
- **Webhooks (v1.5)** — outbound (the ERP pushes), not inbound at MVP.
- **Email-to-ticket** — Service module parses inbound email to a configured mailbox and creates a Ticket [modules M-SVC-001].
- **Bank statement file** — CAMT.053, BAI2, MT940, CSV per [M-FIN-005].
- **E-invoice ingestion (v1.5)** — Indonesian e-Faktur and Peppol are the priority targets.

### 10.2 Outbound (from the ERP)

- **Email** — Quarkus Mailer via SMTP; templates in Qute; per-user unsubscribe.
- **SMS / WhatsApp (S tier)** — Twilio or Meta WhatsApp Business API.
- **Calendar (HR)** — Google / Outlook iCal feed for leave and shifts.
- **Banking (v1.0)** — ISO 20022 pain.001 for payment initiation, pain.002 for status, camt.054 for debit/credit notification.
- **Tax authority (v1.0+)** — e-SPT, e-Faktur; framework for adding more countries.
- **EDI / vendor portal (v1.0)** — PO dispatch, ASN, invoice via SFTP or AS2 per partner.
- **REST API (read)** — `/openapi` for third-party integrations.

### 10.3 Reserved for v2.0+

- Multi-tenant SaaS control plane.
- MES / SCADA integration.
- Banking API for live bank balance and reconciliation.
- E-commerce storefront connector (Shopify, WooCommerce).
- Marketplaces (Amazon, Tokopedia).

---

## 11. UX Principles and Key Flows

### 11.1 Principles (drawn from [perf §6–§10])

1. **Keyboard-first.** Every action has a shortcut. `Ctrl+K` opens the command palette; `Ctrl+S` saves; `Esc` cancels; `Enter` confirms; `Tab` walks the form. The mouse is optional, not required. ERP power users live on the keyboard.
2. **Command palette.** A VSCode-/Slack-style `Ctrl+K` palette that searches across (a) navigation — go to a record, screen, or report — (b) actions — create a sales order, run payroll, post a journal — (c) entities — find a customer, vendor, item, employee. The palette is the home screen's "search-and-do" surface.
3. **Instant feedback (< 100 ms).** Every user action that triggers I/O shows a skeleton, spinner, or progress bar within 100 ms. The skeleton matches the eventual layout. Spinners without skeletons are banned.
4. **Optimistic UI for in-place edits.** Cell-level edits in a table write through immediately; the row shows a "saving" indicator; rollback happens on conflict.
5. **Lazy load.** Module screens, master-data drop-downs, and detail tabs are loaded on first navigation. First-load times are logged to `first_load_metrics.log` and the slowest is the optimization target.
6. **Paged, virtualized lists.** No list returns more than 200 rows. `TableView` uses `setFixedCellSize(24)` and a custom row factory (per [perf §1.1]).
7. **Debounced search.** Search debounce is 150 ms; suggestions appear in a drop-down below the search box.
8. **Offline-aware.** A small "offline / stale" badge in the status bar shows the current sync state. Master data shows "last synced HH:MM" when stale.
9. **Accessible by default.** WCAG 2.1 AA; keyboard reachability; screen reader tested; high-contrast theme.
10. **Honest error messages.** Errors explain what went wrong and what the user can do; no "An unexpected error has occurred" placeholder text.

### 11.2 Key user flows

#### Flow 1 — Login to dashboard

1. App launches via the OS launcher (jpackage). The preloader shows the brand splash < 800 ms (cold) / < 200 ms (warm) [perf §2.1].
2. The login screen is presented. The user types username + password; biometrics (Touch ID / Windows Hello) is an option on the second factor.
3. On successful login, the desktop JWT is issued and the dashboard is rendered. The dashboard composes 5–8 KPI tiles (per role) and a list of pending approvals and notifications.
4. **Acceptance:** cold-start to dashboard interactive < 2.0 s JVM / < 0.7 s native on §7.3 reference hardware.

#### Flow 2 — Create a sales order (Denny, salesperson)

1. From the home screen, Denny presses `Ctrl+K`, types "new so", and selects "Create Sales Order". The SO form opens in a new tab.
2. The customer field shows a `SearchableComboBox`; Denny types the customer code; the auto-suggest shows the customer with credit status.
3. If the customer is on credit hold, the form shows a red banner with the hold reason and the path to the override workflow.
4. Denny adds line items. The pricing engine computes the price live (per [M-SAL-004]); the ATP check shows the available quantity.
5. Pressing `Ctrl+S` saves the SO; it appears in the open-order list; the inventory ATP is decremented (a soft reservation).
6. **Acceptance:** the form is keyboard-reachable end to end; ATP is computed within 200 ms; saving is one shortcut.

#### Flow 3 — Approve a purchase order (Budi, controller)

1. Budi sees a notification badge in the status bar.
2. He clicks the bell; a drop-down shows "3 POs awaiting your approval".
3. The PO approval screen lists the 3 POs with amount, vendor, and a one-click approve/reject. Reject requires a comment.
4. Approval posts the approval event to the workflow engine and routes the PO to Purchasing for release.
5. **Acceptance:** approval action is one click; rejection requires a comment; the audit log records the action with Budi's user ID and timestamp.

#### Flow 4 — Run payroll for the month (Sari, HR manager)

1. Sari opens the Payroll module; the system shows the current period's preview (employees, gross, net, tax, deductions) computed from approved timesheets.
2. Sari reviews the preview, optionally overrides a one-off adjustment, and clicks "Run Payroll".
3. The system posts payroll journals to Finance, generates payslips, and exports the bank file.
4. The system archives the payroll run and produces the period's tax form draft (PPh 21 for Indonesia).
5. **Acceptance:** preview renders in < 5 s for 100 employees; the bank file is exportable; the GL is balanced.

#### Flow 5 — Month-end close (Maya, CFO)

1. Maya opens the Close Checklist; the system shows 12 standard steps (bank recon, fixed asset depreciation, accruals, intercompany, currency revaluation, etc.) with per-step owner, status, and time-to-completion estimate.
2. Each step links to the relevant screen; running a step updates the checklist; the next step's gate is its predecessor's completion.
3. When the checklist is complete, Maya clicks "Close Period"; the system locks the period and posts the closing entries (retained earnings, year-end rollover for fiscal year-end).
4. **Acceptance:** checklist is data-driven (not hard-coded); closing the period is one click; the audit log records the close.

#### Flow 6 — Stock count (Rina, warehouse operator)

1. Rina opens the Cycle Count screen and selects the bin / ABC class to count.
2. The screen shows a blind count list (item + expected qty is hidden until she enters her count).
3. Rina scans items with the handheld; the system accumulates counts.
4. On submit, the system computes variance, posts the adjustment (if within tolerance), and triggers the variance approval workflow if outside.
5. **Acceptance:** scan-to-count is single-step; variance approval is gated; affected items are frozen for the duration of the count.

---

## 12. Release Plan

Phasing aligns with [arch §16 migration path] and [modules §18 MVP slicing]. Indicative durations assume a 5–7-engineer team.

| Phase | Months from kickoff | Functional scope | Non-functional scope | Definition of Done |
|---|---|---|---|---|
| **MVP** | 0–9 | Finance `M`, Sales `M`, Purchasing `M`, Inventory `M-INV-001…008`, CRM `M-CRM-001, 004, 005, 007`, DMS `M-DMS-001…005`, Administration `M-ADM-001…010`, all `X-` `M` cross-cutting; i18n en + id | Single-JVM fast-jar; jlink-shrunk JRE; jpackage installers (Win/Mac/Linux); auto-update via update4j; SQLite offline cache; Flyway migrations; observability baseline; test pyramid 70/20/5/5 | Pilot customer running the full Sales → Inventory → Finance loop on production data, posting a real month-end close. All §7.3 lightweight budget targets met on the reference hardware. OWASP ASVS L2 audit passed. |
| **v1.0** | 9–15 | + Manufacturing `M-MFG-001…010`, Project Management `M-PJM-001…009`, Logistics `M-LOG-001…008`, full Inventory `S-INV-001…010`, Service `M-SVC-001…008`, SSO + SCIM, BI `M-BI-001…008`; + 2 more locales | Optional GraalVM native build of the Quarkus core; thin-client mode (JavaFX + remote Quarkus); RFC for cloud-PostgreSQL deployment; field-level encryption | Three customers in production. p95 cold-start ≤ 0.7 s on native path. All §7.3 targets met. SCIM provisioning live. |
| **v1.5** | 15–21 | + all `S-` tier from [modules §17]; e-Faktur full submission; Peppol; OCR; mobile warehouse app (read-only); E-signature | WORM audit log; HSM-backed key management (optional); multi-region read replicas for cloud PG | Mobile warehouse app used in production by 2 customers. E-invoicing live. |
| **v2.0+** | TBD | + all `N-` tier; IFRS 16; AI forecasting; NL query; APS / MES integrations; CPQ | Multi-tenant SaaS variant; SaaS control plane | First SaaS customer. |

Time estimates are **indicative**. The MVP definition of done is the binding constraint; the date slips if the lightweight budget targets are not met.

---

## 13. Risks and Mitigations

| # | Risk | Severity | Likelihood | Impact | Mitigation |
|---|---|---|---|---|---|
| R1 | JavaFX GraalVM native support is not production-ready in 2026; a full AOT stack would unlock the lightest possible app but is blocked by `javafx.graphics` and `javafx.controls` AOT-ability | High | High | Mid | We compile only the Quarkus core to native; JavaFX stays on the JVM. We `jlink`-shrink the JRE to keep the JavaFX shell lean. Track JavaFX AOT progress and re-evaluate at v2.0. [arch §1, §4, §17 #1] |
| R2 | Hibernate native-image edge cases (rare lazy-init exceptions) only surface at runtime in production | High | Mid | High | "Native test" pipeline in CI runs the full integration suite against the native runner nightly. JVM is the primary debug path; native is a release optimization. [arch §17 #2] |
| R3 | Linux distros without `gnome-keyring` / `kwallet` cannot store OS credentials; the desktop app silently degrades | High | Mid | High | Documented fallback to a local AES-GCM-encrypted file (`~/.config/erp/keyring.bin`) with a clear warning; the user is prompted to set a master password. [arch §1, §17 #3] |
| R4 | macOS notarization flakiness in CI breaks release builds | Mid | High | High | Use `xcrun notarytool` with an API key (not Apple ID); retry with backoff; document the runbook for support. [arch §17 #4] |
| R5 | SQLite dialect drift from PostgreSQL — we use PG-specific features (`jsonb`, partial indexes) in the source DB but SQLite is a subset | Mid | High | Mid | The local cache is a *curated subset* of the PG schema, in `db/cache_schema/`, separate from `db/migration/`. The sync engine owns the mapping; the two schemas are explicitly decoupled. [arch §17 #5] |
| R6 | "Lightweight" feel vs feature depth — the more we add, the heavier the app gets | High | High | High | A first-load-metric log per module surfaces the slow paths. UX rule: most forms are ≤ 3 fields, ≤ 1 s to fill; the rest is a deferred-load secondary screen. Quarterly UX review against the §7.3 budget. [arch §17 #6] |
| R7 | Scope creep in MVP — every division wants its feature in MVP | High | High | High | MVP scope is fixed to [modules §18] and §4.3 of this PRD. New asks go to a separate "post-MVP parking lot" and are not added to MVP without an explicit cut. Product owner enforces. |
| R8 | Onboarding a customer without internet for an update | Mid | Mid | Mid | update4j supports a pre-staged offline bundle on USB or shared drive. Documented runbook for air-gapped deployments. [arch §17 #7] |
| R9 | Many OS / JDK combinations to support; the matrix explodes | Mid | High | Mid | Test matrix pinned to JDK 21 LTS; one minor version back supported; explicit `SUPPORTED.md`; out-of-support combinations documented as such. [arch §17 #8] |
| R10 | Statutory change (new tax rule, new payroll formula) is a hard blocker for the customer | High | Mid | High | Modular payroll engine with country packs; rapid-release channel for statutory-only patches. [modules §3] |
| R11 | Auto-update pushed a regression that crashes the user's app at startup | High | Mid | High | Staged rollout (10% → 50% → 100% over 7 days); crash-rate monitor gates each stage; one-click "revert to previous version" is a desktop menu item. [arch §6] |
| R12 | Customer data is on the user's PC; a hardware failure is data loss if the customer has no backup | High | Mid | High | Default backup is on; default destination is a configurable network share or cloud bucket; daily health check warns if no backup in 48 h. [arch §15] |

---

## 14. Success Metrics

The product is successful when these metrics hold for a representative cohort (≥ 3 paying customers, ≥ 6 months in production).

### 14.1 Adoption (measured monthly)

- **Monthly Active Users (MAU) per customer:** ≥ 60 % of licensed seats.
- **Daily Active Users / MAU:** ≥ 40 %.
- **Modules touched per active user per week:** ≥ 3 (cross-division usage — not a single-module tool).
- **Mobile companion web sessions per user per month:** ≥ 5 (by v1.0).

### 14.2 Performance (measured continuously)

- **Cold start p95 (JVM):** ≤ 2.0 s on the §7.3 reference hardware.
- **Cold start p95 (native backend):** ≤ 0.7 s.
- **Idle RAM p95 (JVM):** ≤ 300 MB.
- **Table render p95 (10k rows, virtualized):** ≤ 50 ms initial, ≤ 8 ms scroll.
- **DB query p95 (PK lookup):** ≤ 2 ms.
- **App crash rate:** < 0.1 % of sessions end in an unhandled exception.
- **Auto-update adoption:** ≥ 80 % of users on the latest minor within 14 days of release.

### 14.3 Reliability

- **Process uptime:** app process runs continuously through a workday for ≥ 99.5 % of users (no crash, no auto-update forced restart in core hours).
- **Sync engine uptime:** offline-queue drain success rate ≥ 99 % over rolling 7 days.
- **Backup success rate:** ≥ 99 % over rolling 30 days.

### 14.4 Business outcomes (measured quarterly, per customer)

- **Time-to-close the books:** ≤ 4 hours per period for a single-company, 1k-transactions-per-day workload. Baseline measured in the pilot.
- **Days Sales Outstanding (DSO):** trend down vs the prior system.
- **Inventory turnover:** trend up vs the prior system.
- **On-time shipment rate:** trend up vs the prior system.
- **First-call resolution (Service):** trend up.
- **Month-end close errors (audit findings):** trend down.

### 14.5 Sales / pipeline (internal)

- **Pilot-to-paid conversion:** ≥ 50 % of pilots convert within 6 months.
- **Average ACV (Annual Contract Value):** track against target; flag if it drifts below the floor that justifies the team's investment.

---

## 15. Open Questions

These are the questions the team should resolve in the first 30 days, before committing to a release plan date.

1. **Manufacturing in MVP or defer to v1.0?** [modules §18] puts Manufacturing in v1.0. The reference customers we are talking to are mixed: a distributor does not need Manufacturing, a textile mill needs it on day one. Recommendation: ship Manufacturing in MVP as a `M-` tier with a flag-gated install (the customer can opt out of the Manufacturing module jar at install time). Open until we close the first 2 design partners.
2. **Native backend (GraalVM) for v1.0 or v1.5?** Native unlocks the sub-second cold start but adds CI time and reflection-debugging overhead. Recommendation: ship the JVM fast-jar in MVP, build the native runner in parallel, switch the default to native at v1.0 once the CI native-test pipeline has been green for 30 days. Open until the reflection-hint test is wired.
3. **Cloud-hosted PostgreSQL option at v1.0?** Some customers want a managed Postgres (AWS RDS, Crunchy, Supabase) instead of self-hosted. Recommendation: yes, as a deployment option, with the offline cache writing to a local SQLite only on the desktop side, not the server side. Open until the security review confirms the architecture.
4. **Payroll tax filing?** The first version of payroll *computes* and *exports* the year-end form (e.g. 1721 A1/A2) but does not file. Should we add DJP e-Filing (or the equivalent for other countries) in v1.5? Recommendation: partner with a payroll-services firm in each market rather than build filing. Open until the partner conversations resolve.
5. **Mobile companion: read-only at v1.0 or read-write at v1.0?** A read-only mobile web companion is much easier to ship and is a powerful "look up a customer on the road" feature. Read-write is the salesperson's dream but requires offline-tolerant forms, conflict resolution, and a substantial UX investment. Recommendation: read-only at v1.0, read-write at v1.5. Open until the salesperson persona validates the read-only experience.
6. **Localization roadmap.** MVP is en + id. Which two to add at v1.0? Vietnamese and Filipino are the obvious Southeast Asia expansion; Mandarin and Thai are the next tier; Spanish and Portuguese open LATAM. Recommendation: prioritize by design-partner geography; defer to a customer pull. Open until we close design partners.
7. **Pricing model.** Per-seat subscription, per-company flat, or per-transaction? Recommendation: per-seat with a per-company floor and a per-module add-on for Manufacturing and Project Management. Open until the sales motion is defined.
8. **Open-source or proprietary?** A strong open-source community (think Odoo Community) accelerates adoption; a proprietary model preserves pricing power. Recommendation: keep the core proprietary at v1.0; revisit a Community Edition at v2.0+ once the platform is stable. Open until the funding and go-to-market plan is clear.

---

## Appendix A — Coverage Matrix

The matrix below maps each upstream research deliverable to the PRD sections it feeds. Use this to audit the PRD for completeness against the source material.

| Upstream deliverable | Feeds PRD section(s) |
|---|---|
| `research-erp-modules` §1 (Executive summary) | §1 (Executive summary), §2 (Goals), §4 (Scope) |
| `research-erp-modules` §2 (Finance) | §6.1 (Finance module) |
| `research-erp-modules` §3 (HR) | §6.2 (HR module) |
| `research-erp-modules` §4 (Sales) | §6.3 (Sales module) |
| `research-erp-modules` §5 (Purchasing) | §6.4 (Purchasing module) |
| `research-erp-modules` §6 (Inventory) | §6.5 (Inventory module) |
| `research-erp-modules` §7 (Manufacturing) | §6.6 (Manufacturing module) |
| `research-erp-modules` §8 (CRM) | §6.7 (CRM module) |
| `research-erp-modules` §9 (Project Management) | §6.8 (Project Management module) |
| `research-erp-modules` §10 (Service) | §6.9 (Service module) |
| `research-erp-modules` §11 (Logistics) | §6.10 (Logistics module) |
| `research-erp-modules` §12 (DMS) | §6.11 (DMS module) |
| `research-erp-modules` §13 (BI) | §6.12 (BI module) |
| `research-erp-modules` §14 (Administration) | §6.13 (Administration module), §8.2 (Security) |
| `research-erp-modules` §15 (Cross-cutting) | §6 (Cross-cutting features) |
| `research-erp-modules` §16 (Module list table) | §6 (every module), §5 (scope) |
| `research-erp-modules` §17 (Coverage matrix) | §4.3 (Phasing), §12 (Release plan) |
| `research-erp-modules` §18 (MVP slicing) | §4.3 (Phasing), §12 (Release plan) |
| `research-erp-modules` §19 (References) | Appendix B |
| `research-tech-architecture` §1 (Executive summary) | §7.1 (Recommended architecture) |
| `research-tech-architecture` §2 (Lightweight budget) | §7.3 (Lightweight budget) |
| `research-tech-architecture` §3 (Architecture diagram) | §7.2 (Architecture diagram) |
| `research-tech-architecture` §4 (Process model) | §7.1 (Architecture variants) |
| `research-tech-architecture` §5 (GraalVM) | §7.1 (Native variant), R1 |
| `research-tech-architecture` §6 (UI↔backend) | §7.1 (In-process CDI call) |
| `research-tech-architecture` §7 (Persistence) | §7.4 (Stack pinning), §9 (Data model) |
| `research-tech-architecture` §8 (Security) | §7.8 (Security architecture), §8.2 (NFR-S) |
| `research-tech-architecture` §9 (Packaging) | §7.5 (Build/package/distribute) |
| `research-tech-architecture` §10 (Auto-update) | §7.5 (Auto-update), R11 |
| `research-tech-architecture` §11 (Offline) | §7.9 (Offline-first), §8.6 (NFR-O) |
| `research-tech-architecture` §12 (Modularity) | §7.5 (Module boundary) |
| `research-tech-architecture` §13 (Observability) | §7.6 (Observability) |
| `research-tech-architecture` §14 (Testing) | §7.7 (Testing) |
| `research-tech-architecture` §15 (Hardware) | §7.3 (Min hardware row), §2 G6 |
| `research-tech-architecture` §16 (Cross-cutting) | §7 (across all subsections) |
| `research-tech-architecture` §17 (Migration path) | §4.3 (Variants) |
| `research-tech-architecture` §18 (Risks) | §13 (Risks R1–R9) |
| `research-tech-architecture` §19 (References) | Appendix B |
| `research-lightweight-patterns` §1 (UI rendering) | §11.1 (Principles 3, 6), §6 modules' table render SLOs |
| `research-lightweight-patterns` §2 (Startup) | §7.3 (Cold start row), §11.2 Flow 1 |
| `research-lightweight-patterns` §3 (Memory) | §7.3 (Idle RAM row), §6 module tables (p95 SLOs) |
| `research-lightweight-patterns` §4 (Background processing) | §7.9 (Sync engine), §8.6 NFR-O-05 |
| `research-lightweight-patterns` §5 (Perceived perf) | §11.1 (Principles 3, 4, 7) |
| `research-lightweight-patterns` §6 (Keyboard UX) | §11.1 (Principles 1, 2), §11.2 all flows |
| `research-lightweight-patterns` §7 (Offline) | §7.9, §8.6 NFR-O |
| `research-lightweight-patterns` §8 (Accessibility) | §8.4 (NFR-A) |
| `research-lightweight-patterns` §9 (i18n) | §8.5 (NFR-L) |
| `research-lightweight-patterns` §10 (Installer / first-run) | §7.5 (Packaging), §11.2 Flow 1 |

---

## Appendix B — References

All sources below are real and were verified reachable on 2026-08-31.

### Java / JVM platform
- OpenJDK 21 — https://openjdk.org/projects/jdk/21/
- `jpackage` (JDK 21 docs) — https://docs.oracle.com/en/java/javase/21/jpackage/
- `jlink` (JDK 21 docs) — https://docs.oracle.com/en/java/javase/21/docs/specs/man/jlink.html
- JPMS — https://openjdk.org/projects/jpms/

### JavaFX
- OpenJFX home — https://openjfx.io/
- JavaFX 21 javadoc — https://openjfx.io/javadoc/21/
- Getting Started with JavaFX 11+ — https://openjfx.io/openjfx-docs/
- FXML introduction — https://openjfx.io/fxml_doc/
- CSS reference — https://openjfx.io/css_html/

### Quarkus
- Quarkus home — https://quarkus.io/
- Command-mode reference — https://quarkus.io/guides/command-mode-reference
- Building a native executable — https://quarkus.io/guides/building-native-image
- Native reference guide — https://quarkus.io/guides/native-reference
- Hibernate ORM with Panache — https://quarkus.io/guides/hibernate-orm-panache
- Datasource configuration — https://quarkus.io/guides/datasource
- Using Flyway — https://quarkus.io/guides/flyway
- Dev Services — https://quarkus.io/guides/dev-services
- Testing your application — https://quarkus.io/guides/testing-your-application
- Security overview — https://quarkus.io/guides/security
- Security with Jakarta Persistence — https://quarkus.io/guides/security-jpa
- Using JWT RBAC (MP-JWT 1.2) — https://quarkus.io/guides/security-jwt
- Quarkus REST (RESTEasy Reactive) — https://quarkus.io/guides/resteasy-reactive
- Quarkus Micrometer — https://quarkus.io/guides/micrometer
- SmallRye Health — https://quarkus.io/guides/smallrye-health
- OpenTelemetry tracing — https://quarkus.io/guides/opentelemetry-tracing
- Performance — https://quarkus.io/performance

### GraalVM / native
- GraalVM Native Image reference manual — https://www.graalvm.org/latest/reference-manual/native-image/
- Build a native executable — https://www.graalvm.org/latest/reference-manual/native-image/guides/build-native-executable/
- Reachability metadata — https://www.graalvm.org/latest/reference-manual/native-image/metadata/
- Mandrel — https://github.com/graalvm/mandrel

### PostgreSQL
- PostgreSQL 16 documentation — https://www.postgresql.org/docs/16/

### Connection pool
- HikariCP README — https://github.com/brettwooldridge/HikariCP
- HikariCP pool-sizing wiki — https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing

### Distribution & auto-update
- update4j — https://update4j.org/ , https://github.com/update4j/update4j
- getdown (alternative) — https://github.com/threerings/getdown
- Apple notarytool — https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution
- Microsoft signtool (Authenticode) — https://learn.microsoft.com/en-us/windows/win32/seccrypto/signtool
- AppImage — https://appimage.org/

### Security
- java-keyring — https://github.com/javakeyring/java-keyring
- OWASP Password Storage Cheat Sheet (Argon2id) — https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
- OWASP ASVS — https://owasp.org/www-project-application-security-verification-standard/
- MicroProfile JWT 1.2 — https://download.eclipse.org/microprofile/microprofile-jwt-auth-1.2/microprofile-jwt-auth-spec-1.2.html
- SmallRye JWT — https://smallrye.io/docs/smallrye-jwt/

### Embedded / sync
- SQLite — https://sqlite.org/
- sqlite-jdbc (xerial) — https://github.com/xerial/sqlite-jdbc
- Embedded PostgreSQL (zonkyio) — https://github.com/zonkyio/embedded-postgres

### UI testing
- TestFX — https://github.com/TestFX/TestFX
- ControlsFX — https://controlsfx.github.io/
- AssertJ — https://assertj.github.io/doc/
- RestAssured — https://rest-assured.io/
- Mockito — https://site.mockito.org/

### Reference ERPs (for functional feature catalog in §6)
- SAP S/4HANA — https://www.sap.com/products/s4hana.html
- SAP S/4HANA documentation — https://help.sap.com/docs/SAP_S4HANA
- Oracle Fusion Cloud ERP — https://docs.oracle.com/en/cloud/saas/enterprise-performance-management-common/index.html
- Oracle NetSuite — https://www.netsuite.com/
- Microsoft Dynamics 365 Finance — https://learn.microsoft.com/en-us/dynamics365/finance/
- Microsoft Dynamics 365 Sales — https://learn.microsoft.com/en-us/dynamics365/sales/
- Microsoft Dynamics 365 Supply Chain Management — https://learn.microsoft.com/en-us/dynamics365/supply-chain/
- Microsoft Power BI — https://learn.microsoft.com/en-us/power-bi/
- Odoo documentation — https://www.odoo.com/documentation
- Acumatica — https://www.acumatica.com/
- Salesforce Sales Cloud — https://www.salesforce.com/products/sales-cloud/
- HubSpot CRM — https://www.hubspot.com/products/crm
- Zendesk — https://www.zendesk.com/
- Manhattan Active WM — https://www.manh.com/
- Camunda Workflow Engine — https://camunda.com/

### Standards bodies & regulations
- IFRS 15 / 16 — https://www.ifrs.org/
- ASC 606 / 842 — FASB
- GDPR — https://gdpr-info.eu/
- Indonesia UU PDP — https://www.kominfo.go.id/
- ISO 27001 — https://www.iso.org/standard/27001

---

*End of document.*
