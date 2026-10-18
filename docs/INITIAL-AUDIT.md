# Initial engineering audit — 14 September 2026

This report was written before application edits. Scope: the three Maven modules, the JavaFX startup/login/navigation and metadata-driven records UI, all entity/resource definitions, shared persistence/security/CSV, the five business services, migrations and Windows packaging. The workspace has no Git repository. Existing generated distributions are stale build artifacts, not the source of truth.

## Findings and planned corrections

| Severity | Finding / cause | Correction | Affected files |
|---|---|---|---|
| Critical | Table sorting moves visible strings independently of `rawRows`; editing/deleting by selection index can mutate another record. | Bind rows directly to JSON record objects and use server sorting. | `EntityView.java` |
| Critical | Login falls back to an embedded admin/admin identity, bypassing disabled accounts and database identity. Shared signing private keys ship in resources. | Require database JWT login, disable embedded Basic authentication, generate per-install signing material outside source. | `DesktopApp.java`, `ApiClient.java`, both application configurations, security bootstrap |
| Critical | Direct get/update/delete use unscoped IDs; login accepts an arbitrary company ID; role management is authenticated but unrestricted. | Enforce tenant ownership and administrator access for administration operations; constrain company selection. | `CrudResource.java`, `AuthResource.java`, platform resources, business services |
| High | Password hashes and security internals appear in metadata, JSON, CSV and snapshots. | Exclude secrets, add a write-only password workflow and protect audit records. | `AppUser.java`, `AppUserResource.java`, `CrudResource.java`, `CsvSupport.java`, `AuditService.java` |
| High | Forms omit optimistic-lock version, send invalid numbers as null, permit repeated saves, and can extend beyond the window. | Preserve versions, validate before sending, disable duplicate submissions, add scrolling and date controls. | `RecordDialog.java`, `EntityMeta.java`, `Values.java`, shared update path |
| High | Generic CRUD can bypass posted-document invariants and directly alter stock balances and journals. | Guard generated records and posted documents; expose explicit business actions. | `CrudResource.java`, record policy, services, desktop actions |
| High | CSV export silently caps at 200 despite requesting 5,000; labels/BOM cannot round-trip; imports swallow persistence failures inside one transaction and skip audit hooks. | Bounded complete export with explicit oversize failure, field-name headers, strict parser and atomic validated import. | `CrudResource.java`, `CrudService.java`, `CsvSupport.java`, `ApiClient.java`, `EntityView.java` |
| High | Failed login counters roll back when a runtime exception is thrown; role list may be immutable; soft-deleted users can log in. | Commit failure responses, serialize per-account attempts, use mutable role lists and enforce active status. | `AuthResource.java` |
| High | Enhanced entity fields are discovered but are not made reflectively accessible; export/audit can silently omit values. Audit commits independently from failed business writes. | Make persistent fields accessible, exclude enhancement/transient internals, commit audit with the business transaction. | `CrudService.java`, `AuditService.java` |
| High | Shipping assigns warehouse ID as location ID; posting/reversal concurrency and tenant checks are incomplete. | Correct references, lock documents, validate quantities and account ownership, prevent repeat reversal. | `SalesService.java`, `PurchasingService.java`, `InventoryService.java`, `JournalService.java`, `NumberRangeService.java` |
| High | Backend launcher forcibly sets localhost credentials and starts local PostgreSQL. Neon documentation embeds credentials and uses libpq options in JDBC. | Shared environment-driven Neon setup, protected local credentials, correct JDBC flags, no local server in Neon mode. | `run-backend.cmd`, both application configurations, `Bootstrap.java`, `NEON-SETUP.md`, launch scripts |
| Medium | Busy indicators do not prevent overlapping requests; abandoned views can publish stale status; export reads controls from a worker and writes files on the FX thread. | Managed background jobs, captured inputs, view disposal, disabled busy actions, background file I/O. | `EntityView.java`, `RecordDialog.java`, `MainWindow.java`, `DesktopApp.java` |
| Medium | Default controls, repeated inline styles, doubled table headers, no page title, cramped toolbar and weak empty/error states. | Shared CSS, clear titles, wrapping action bars, deliberate typography and status treatments. | JavaFX views, new stylesheet and reusable UI helpers |
| Medium | No working overview, desktop import, print, enum filters, keyboard shortcuts or business-action buttons; audit history exists only as a generic entity. | Real-data overview, recent audit events, quick navigation, import/export/print and supported posting actions. | desktop views, overview endpoint |
| Medium | `findAll` ignores requested size, inactive filtering is ignored, Instant coercion returns LocalDateTime; validation/constraint errors become generic 500s. | Respect query options, strict conversions and typed 400/409 errors. | `CrudService.java`, `CrudQuery.java`, `ApiExceptionMapper.java` |
| Medium | No source tests or reproducible portfolio verification guide. Maven launcher pins a machine-specific JDK. | Add focused regression and integration coverage, portable JDK discovery, reproducible run/build instructions. | POMs, tests, launch scripts, README |

## Existing functionality to preserve

All catalog entities remain reachable. Keep the three-module architecture, JavaFX programmatic views (no FXML exists), Flyway migrations V1–V14, search/pagination, soft deletion, typed enum fields, JWT sessions and transactional sales/purchasing/ledger services. Do not rewrite historical migrations against an existing database.

## Feature boundaries discovered

Many modules are record-management surfaces, not complete domain engines. Payroll calculation, MRP execution, FIFO/LIFO cost layers, scheduled report delivery, offline sync, binary document storage and external carrier integrations have no implementing service. They must not be represented as working automated capabilities. Existing record screens will be made usable; supported business actions will be tested explicitly. Demo records must be opt-in and clearly identified, with no fabricated dashboard totals.

## Verification plan

1. Establish build baseline and repair toolchain issues if necessary.
2. Unit regression tests for CSV, value validation, metadata, access policy and database URL conversion.
3. Backend integration tests against an isolated disposable test database/schema; exercise login, CRUD, versions, tenancy, imports, export bounds, audit and posting.
4. Read-only Neon connectivity/schema inspection before applying additive application migrations. Never reset/clean an existing database.
5. JavaFX smoke harness to construct all catalog views, exercise dialogs and capture rendered UI; report separately what is automated versus manually inspected.
6. Rebuild runnable artifacts and document actual results and remaining limitations.
