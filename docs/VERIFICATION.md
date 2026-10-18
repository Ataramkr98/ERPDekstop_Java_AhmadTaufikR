# Implementation and verification report

Prepared 14 September 2026; final verification results added 15 September 2026. This report covers the source implementation, automated checks, Neon runtime verification and Windows packaging. See `INITIAL-AUDIT.md` for the findings recorded before application edits.

## Completed implementation

| Area | Changed files | Result and reason |
|---|---|---|
| Neon and launch | `RuntimeConfigSource.java`, both `application.properties`, `DesktopEnvironment.java`, `Bootstrap.java`, `scripts/*.ps1`, launch CMD files, POMs | Hosted PostgreSQL URI parsing, required TLS/channel binding, Windows account-encrypted credentials, per-user signing keys, loopback API, portable JDK discovery and native packaging. Source resources no longer contain shared private keys. |
| Authentication | `AuthResource.java`, `AppUser.java`, `AppUserResource.java`, `ApiClient.java` | Database-backed JWT login, persistent lockout counters, active-user checks, company restrictions, password changes and write-only password entry. Embedded Basic fallback removed. |
| Data integrity | `CrudResource.java`, `CrudService.java`, `RecordPolicy.java`, `ReferenceValidation.java`, `EntityValidation.java`, `ApiExceptionMapper.java` | Company-scoped IDs, administrator-only security settings, optimistic versions, typed validation, actionable errors, reference checks and protected generated records. |
| CSV and audit | `CsvSupport.java`, shared CRUD, `AuditService.java` | Complete bounded exports, field-name headers, strict CSV parsing, atomic imports, formula protection, secret exclusion and audit events committed with business changes. |
| Business actions | `SalesService.java`, `PurchasingService.java`, `InventoryService.java`, `JournalService.java`, `NumberRangeService.java`, `BusinessRecords.java`, action resources | Document and company locks, account/stock ownership checks, posting guards, receipt/shipment corrections, matching/hold handling and controlled journal reversal. |
| Desktop workspace | `DesktopApp.java`, `MainWindow.java`, `Catalog.java`, `EntityView.java`, `RecordDialog.java`, `EntityMeta.java`, `Values.java`, `Ui.java`, `styles/app.css` | Consistent navigation and theme, real record identity during sorting, typed scrolling forms, background requests, loading/error/empty states, search/filter/sort/page controls, shortcuts, CSV and print actions. |
| Overview and demonstration | `OverviewResource.java`, `StatusResource.java`, `DemoResource.java`, `OverviewView.java` | Live company counts, database status, recent audit events, quick navigation and idempotent DEMO records. No fabricated dashboard metrics. |
| Reproducibility | regression tests, `PortfolioCapture.java`, README, setup and architecture docs | Automated API/data/UI checks, actual JavaFX screenshots, documented launch workflow and explicit feature boundaries. |

The application keeps its three-module architecture and all 118 catalog entries. Historical Flyway migrations V1–V14 were preserved. Views remain programmatic JavaFX; no artificial FXML layer was introduced.

## Automated checks

Final results from the 15 September 2026 verification run (`mvn clean verify`, JDK 21.0.12.1, Maven 3.9.9, Windows): all three modules green in one reactor run.

- Core regression suite (`SharedRegressionTest`, 10 tests, 3.4 s): PostgreSQL URI conversion, channel binding, secret handling, CSV round-trip/BOM/multiline/formula cases, invalid values, metadata validation and tenant policy.
- Backend integration suite (`BackendWorkflowTest`, 7 tests, 332 s): all 118 metadata and list endpoints; required fields; create/update/stale versions/soft delete; audit history; 205-row export; atomic failed import; authentication and account lockout; role and company isolation; idempotent demo data; invoice posting/reversal; stock receipt posting and protected lines.
- JavaFX suite (`ViewRegressionTest`, 4 tests, 44 s): constructing every catalog view with shared styles, sorted record identity, invalid numeric input and scrolling date forms.

Backend tests run on an isolated H2 database in PostgreSQL compatibility mode with ephemeral signing keys. They do not run destructive regression operations against Neon. `NON_KEYWORDS=VALUE` is confined to the H2 test profile.

## Runtime and visual verification

Completed 15 September 2026 against the live Neon database (`ep-steep-snow-b3pyi3gc-pooler`, PostgreSQL 18.6):

- Standalone backend started from today's build (`erp-backend/target/quarkus-app/quarkus-run.jar`); Flyway validated schema v14 as up to date; the server listened on 127.0.0.1:8080; the runtime log contains no ERROR/FATAL lines; shutdown was clean.
- `PortfolioCapture` drove the real JavaFX client end to end: sign-in, blank-credential validation, overview, navigation to Invoices, the customer record form, the 1120 px compact layout and sign-out. It passed and wrote `sign-in.png`, `overview.png`, `invoices.png`, `customer-form.png` and `compact-window.png` to `docs/screenshots`.
- The idempotent demo seed was applied first, so the captures show live data: overview counts, a real audit-fed activity table and a draft invoice. All five PNGs were reviewed; no garbled text, broken layout or inconsistent styling was found.
- Those five PNGs show an **earlier UI iteration** and no longer match the current build; the current screenshots are the `demo-*.png` set described in the follow-up section at the end of this document.
- The desktop uber-jar was rebuilt today (`mvn -pl erp-desktop package`, 76 MB, exit 0) and is the jar the capture ran against. Full `Build-Portable.ps1` packaging was not re-run in this session; the most recent portable bundle under `dist/` predates today's run.

## Remaining boundaries

- Payroll calculation, MRP execution, scheduled report delivery, binary document storage, carrier integrations and offline synchronization remain record/definition surfaces without execution engines. They are not advertised as automated functionality.
- Inventory posting supports standard cost and moving average. FIFO/LIFO requests fail explicitly; cost-layer engines are not implemented.
- Some references still use numeric IDs in forms. Known catalog relationships are validated; this is not a comprehensive semantic validator for every domain relationship.
- CSV exports are limited to 5,000 matching rows with an explicit error above that limit. Imports create up to 1,000 records / 4 MB atomically; they do not update records by CSV ID.
- Print produces a selected-record summary through the system printer dialog. Long field values may be abbreviated. Physical printer output is not part of automated verification.
- This is a portfolio ERP foundation, not a certified accounting/payroll system. Granular enterprise permissions, jurisdiction-specific tax/accounting rules and comprehensive concurrency/load testing need further domain work.
- Native packaging produces a self-contained Windows application folder. It is not a signed installer; older distributions in `dist` are historical artifacts.

## Reproduce

```powershell
./mvnw.cmd clean verify
pwsh -NoProfile -File scripts/Configure-Neon.ps1
./run-desktop.cmd
pwsh -NoProfile -File scripts/Build-Portable.ps1 -SkipBuild
```

Use JDK 21 and PowerShell 7. The configured workstation already has a protected Neon profile. The initial application account on an empty database is `admin` / `admin`; use the top-bar password action to change it.

The PostgreSQL driver is pinned to 42.7.12 in the parent dependency management so both runtime hosts receive its channel-binding fixes. See the [pgJDBC release notes](https://jdbc.postgresql.org/changelogs/2026-06-29-42.7.12-release/) and [connection parameter documentation](https://jdbc.postgresql.org/documentation/use/).

## Follow-up verification — 6 October 2026

A second pass audited the portfolio demo end to end. The demo feature existed in source but had never been exercised against a running backend: its integration suite failed, and the record screens had never loaded a real page of data. Every finding below was reproduced, fixed in code, and re-verified.

### Defects found and fixed

| # | Symptom | Root cause | Fix |
|---|---|---|---|
| 1 | `POST /api/demo/session` answered 500 and every later demo assertion cascaded to 401/403 | The first call seeds the whole demo tenant inside a request-scoped JTA transaction. Seeding touches most entities, so on a cold JVM the class loading alone exceeded the 60 s JTA default; the Narayana reaper aborted the transaction (`ARJUNA012110`) and the commit failed with `ARJUNA016102`. | The two endpoints that (re)build the dataset open their transaction explicitly with a 300 s budget through `QuarkusTransaction.requiringNew().timeout(...)`. `jakarta.transaction.Transactional` has no per-method timeout, so the annotation could not express this. |
| 2 | `POST /api/demo/reset` answered 403 to a genuine demo session | `CurrentUser.isDemo()` read the custom `demo` claim with `Boolean.TRUE.equals(getClaim("demo"))`. SmallRye's `DefaultJWTCallerPrincipal.fixJoseTypes()` replaces every *custom* claim value with a JSON-P `JsonValue`, so the claim arrives as `JsonValue.TRUE`, never `java.lang.Boolean`. The numeric `companyId` claim escaped the same trap only because `JsonNumber` implements `Number`. | Type-tolerant claim readers (`CurrentUser.booleanClaim` / `textClaim`), also used by `AuthResource.me()`, which additionally stopped `fullName` serialising as a JSON-P wrapper. |
| 3 | Every record screen died on the FX thread with `ComboBox.disable: A bound value cannot be set`, leaving the list half-built | `buildColumns()` called `setDisable()` on the sort controls whose `disableProperty` was already bound to `busy`. | A `sortUnavailable` property bound together with `busy`. |
| 4 | No record grid ever showed a row | `loadMeta()` set `busy = true` and never cleared it on the success path, so `reload()` — which refuses to run while busy — never fired. | Release the flag before requesting the first page. |
| 5 | Sixteen-column grids elided every header to "..." | `CONSTRAINED_RESIZE_POLICY` (deprecated in JavaFX 20) squeezed the columns into the viewport. | The non-deprecated unconstrained policy plus per-column preferred widths; the grid scrolls horizontally when it must. |
| 6 | The status strip said "Loading..." forever after a successful load | `applyResult()` never replaced the text set before the request. | It now reports the loaded row count. |
| 7 | Toolbar actions were elided to "..." on a narrow window | The toolbar was an `HBox`, which cannot wrap, despite a comment claiming it did. | A `FlowPane`, which moves the overflow onto a second row. |
| 8 | The module sidebar looked empty | The tree root was shown but never expanded, so every division sat behind a collapsed disclosure arrow. | `setShowRoot(false)`, so the divisions are the visible top level. |
| 9 | The navigation was unreadable against the dark sidebar | The CSS targeted `.module-tree .tree-cell .label`, but a tree cell draws its own text through the skin (a `LabeledText` node, not a child `Label`), so the selector never matched and the text fell back to the dark theme colour. | Style the cell itself; also fixed the sidebar scrollbar and the muted-text contrast. |
| 10 | The demo notice rendered unstyled | `OverviewView` removed the notice's own style class immediately after adding it. | Removed the line. |
| 11 | `mvn clean verify` could not reach the desktop module | `PortfolioCapture` did not compile (`TreeView<String>` cast where `MainWindow` builds `TreeView<Object>`). | Corrected the cast; the harness also gained a demo walkthrough flow. |
| 12 | `invoicePostingAndSingleReversal` failed with 400 | The test fixture created an invoice without the header totals that posting validates against the line sum. | The fixture now states `subtotal` / `taxAmount` / `totalAmount`. |
| 13 | A stack trace printed on shutdown | The asynchronous connection probe could resolve after `stop()` had shut the worker pool down, and then tried to start Quarkus on it. | The shell marks itself as shutting down and late callbacks return early. |

### Tests added

- `DemoWorkspaceTest` — `/api/auth/me` must report `demo: true` with a readable `fullName` for a demo session and `demo: false` for a normal one (regression for defect 2); leaving the demo is acknowledged with 204 for a demo session and refused with 403 for a normal one.
- `ViewRegressionTest` — a record screen must finish loading metadata *and* its first page from a reachable endpoint (regression for defects 3 and 4; the earlier catalog test could not see them because its client points at a dead port); the module tree must expose its divisions without manual expansion (defect 8).

### Results

`mvn test`, JDK 21.0.12.1, Maven 3.9.9, Windows — all four suites green in one reactor run:

| Suite | Tests | Time |
|---|---|---|
| `SharedRegressionTest` | 11 | 1.1 s |
| `BackendWorkflowTest` | 9 | 161.7 s |
| `DemoWorkspaceTest` | 9 | 92.5 s |
| `ViewRegressionTest` | 7 | 19.9 s |

Live run: the standalone backend started with `-Dquarkus.profile=demo` against a dedicated `erp_demo` database. `GET /api/demo/status` → 200; `POST /api/demo/session` → 200 in 6.7 s once warm (against an aborted 60 s+ attempt before the fix); `GET /api/auth/me` → `{"demo":true,"fullName":"Demo Reviewer"}`; `POST /api/demo/reset` → 200 with `{"customers":6,"items":8,"orders":6,"invoices":5,...}`; re-opening the session is idempotent; `DELETE /api/demo/session` → 204.

Isolation was verified in the database rather than only in assertions: the demo tenant holds 6 customers / 8 items / 5 invoices and **0 rows belonging to another company**, while the pre-existing `erp` database still contains **0 DEMO rows** after the whole walkthrough.

Screenshots were captured from that running application by `PortfolioCapture`: `demo-sign-in.png`, `demo-overview.png`, `demo-invoices.png`, `demo-customers.png`, `demo-reset-confirm.png`, `demo-exit-confirm.png`, `demo-exited.png`, plus a refreshed normal-mode set (`sign-in.png`, `overview.png`, `invoices.png`, `customer-form.png`, `compact-window.png`). Every image was reviewed. The normal-mode set shows an empty `DEFAULT` company, which is what a fresh database contains.

### Still not verified in this session

- Packaging (`Build-Portable.ps1`, `jpackage`, the Inno Setup wrapper) was not re-run. `mvn package` succeeds and produces `erp-backend/target/quarkus-app` and the desktop uber-jar.
- The Neon profile was not used; the demo was pointed at a local database instead, which is the documented `ERP_DEMO_DATABASE_URL` path.
- Keyboard-only and screen-reader behaviour was reviewed in code (shortcuts are bound on the root pane and every control is focus-traversable) but not exercised with assistive tooling.

