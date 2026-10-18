# Portfolio engineering audit — 12 October 2026

Written before application-code changes. The repository contains three Maven modules, 118 registry-backed record pages, programmatic JavaFX views (no FXML), PostgreSQL migrations, demo data, and transactional inventory/accounting services. No `AGENTS.md` or Git checkout is present. Previous audit and verification reports describe an earlier build and are not evidence that the current desktop workflows work.

## Findings

| Severity | Existing problem and cause | Recommended solution | Affected files |
|---|---|---|---|
| High | JSON POST/PUT requests omit Content-Type. Real JSON-consuming endpoints can reject login and writes with HTTP 415; socket stubs do not check headers. | Explicit JSON media type and a transport regression check. | `ApiClient.java`, desktop tests |
| High | Posting, shipping, matching, hold release and reversal exist on the server but are unreachable from the desktop. The client's action URL also does not match those endpoints. | A typed action registry using verified routes, selection/status guards, confirmations, refresh and error feedback. | `ApiClient.java`, `EntityView.java`, new action descriptors |
| High | Generated/posted records cannot be inspected: Edit is enabled for generated records but returns without doing anything. | Separate View and Edit, a readable detail dialog, print and administrator history; disable illegal writes. | `EntityView.java`, new record detail view |
| High | Busy does not disable New, Edit, Delete, CSV or search. Selection listeners can enable writes during a request, and metadata failure leaves New active. | Bind every relevant action to busy, metadata availability, selection and document policy. | `EntityView.java` |
| High | Filter support is documented but no filter UI exists. Export ignores sort and filters. | Metadata-driven enum/boolean filters; one captured query for paging and export. | `EntityView.java`, `ApiClient.java` |
| High | Last-page correction excludes the one-page case. Deleting row 51 can leave an empty second page and incorrect counters. | Clamp to the last valid page whenever the requested page is out of range. | `EntityView.java`, desktop tests |
| Medium | Async tasks are submitted before callbacks are installed; discarded views still receive results and redundant runLater calls enlarge the stale-result window. | Install handlers before submission, return a cancellable task, cancel reads on disposal, guard all callbacks. | `Ui.java`, `EntityView.java`, `OverviewView.java` |
| Medium | There is no Overview navigation entry. Opening Overview also fails to dispose the previous screen, including after demo reset. | Permanent Overview control; central screen release and correct active-path reset. | `MainWindow.java` |
| Medium | Normal sessions have no permanent password-change action after the initial warning disappears. Hidden demo controls remain managed and reserve top-bar space. | Permanent account action, visible/managed binding and compact responsive top bar. | `MainWindow.java` |
| Medium | Overview uses a fixed HBox and unscrollable VBox. Four metrics and three recent-document groups overflow smaller windows. It omits returned audit activity, quick actions and refresh status. | Wrapping metrics, scrollable content, useful quick navigation, actual audit activity and live refresh/error state. | `OverviewView.java`, `MainWindow.java` |
| Medium | Forms require people to type related-record IDs. The server knows the target entities but does not expose that knowledge. New forms also discard entity defaults. | Publish reference/default metadata and add searchable, tenant-scoped record selection. Preserve constructor defaults. | `ReferenceValidation.java`, `CrudResource.java`, `EntityMeta.java`, `RecordDialog.java`, new reference picker |
| Medium | Numeric/boolean conversion is incomplete: integer overflow for Integer fields is not checked and any unexpected boolean becomes false. Validation only prints a general error. | Type-aware strict conversion and focus the failing field. | `Values.java`, `RecordDialog.java`, tests |
| Medium | Successful save/delete/import messages are immediately replaced by Loading and then record counts. Cancelled forms trigger unnecessary refreshes. | Refresh only after a saved result; preserve success feedback after refresh. | `EntityView.java`, `RecordDialog.java` |
| Medium | CSV chooser has no default selection, is unthemed, claims updates despite create-only import, and prevents export of generated records. Entire input files are read before checking bounds. | Separate import/export controls, explain atomic create-only import, permit read-only exports and check file size first. | `EntityView.java` |
| Medium | Pill helper adds one class containing a space; `.pill.demo` does not match. Stylesheet contains an unsupported font-variant property. Spacing and semantic colours are inconsistent. | Separate style classes, remove unsupported CSS, use shared semantic tokens and an 8px layout rhythm. Add consistent vector icons only to navigation/actions where useful. | `Ui.java`, `app.css`, new icon helper |
| Medium | Metadata authorization checks authentication but omits entity access policy, so non-administrators can retrieve administration descriptors. | Apply the same access rule as list/get before publishing descriptors. | `CrudResource.java`, backend tests |
| Medium | Dashboard overdue counts rely on an explicit OVERDUE status; past-due posted unpaid invoices are excluded unless manually updated. | Derive overdue from unpaid balance and due date as well as explicit status. | `OverviewResource.java`, backend tests |
| Medium | Shipping and standalone three-way matching omit a document-specific audit event. | Record business activity in the same transaction. | `SalesService.java`, `PurchasingActionsResource.java` |
| Low | README claims screens/features not implemented and repeats setup text. No current reproducible completion report or design component guide. | Update documentation to actual verified behavior and distinguish domain boundaries. | `README.md`, new final report and design-system guide |

## Existing behavior to preserve

### Additional finding during package-path review

The portable launcher selects `Main`, which did not configure the saved connection and signing keys. Its embedded startup also passed the `QuarkusApplication` interface to `Quarkus.run`, rather than a concrete CDI application. This is a high-severity startup defect outside the client-mode screenshot path. Correct `Main`, `DesktopEnvironment`, and `DesktopApp`; add a concrete embedded runtime and verify startup from the generated desktop runner. The native package should provide an explicit demo launcher with a separate demo key directory.

The embedded smoke check reached demo sign-in/exit successfully but exposed an interrupted graceful shutdown: `Ui.shutdownNow()` also interrupted the long-running Quarkus task. Give the service a separate lifecycle thread and request its exit before cancelling transient UI workers; verify that the embedded service stops without that error.

The native runtime smoke isolated another high-severity packaging issue: the reduced image omitted `jdk.zipfs`. SmallRye's `ClassPathUtils` requires the `jar` filesystem provider at static initialization and threw `NoSuchElementException`, before logging was fully active. JVM exception tracing and the bundled class bytecode verified the cause. Include `jdk.zipfs` explicitly and test both the bundled runtime and the actual native demo executable, rather than relying only on full-JDK checks.

The live PostgreSQL run also exposed a high-severity signing-key path defect: SmallRye's file loader strips `file:` and passes the rest directly to `FileInputStream`, without decoding `%20`. `Path.toUri()` therefore breaks key directories containing spaces. Use normalized absolute filenames in `RuntimeConfigSource`, `DesktopEnvironment`, and the test resource. A regression reads an actual file through the bundled JWT library from a directory with spaces.

The live posting scenario exposed unscoped demo invoice/delivery/receipt/vendor-invoice lines. Master records were scoped, but the direct line constructors omitted `companyId`; tenant checks correctly refused posting and reset could not remove those lines. Seed all four line types with the demo tenant, guard against future unscoped seeds, and repair legacy lines only through an explicitly demo-owned parent. Vendor-invoice seed lines also omitted their tax amounts. Add a regression that posts real seeded documents, matches a vendor invoice, ships received stock, checks audit, and verifies reset removes the old tenant's lines.

Shared endpoints bypassed company scoping even for demo credentials. Force demo reads/writes into their company, including shared-reference direct IDs. Company administration must require an administrator on the server as it already does in navigation. Currency writes require an administrator; metadata must mark currency pages read-only for ordinary users. Verify both shared-row isolation and role restrictions.

Final account review found that the demo principal (`demo`) could reach the password endpoint, which looked up accounts by username. A normal account with that username must remain untouched by demo sessions; reject demo password changes before account lookup and prove the normal credentials remain valid. Account CSV import also could not supply the transient password required for creation. Publish that capability as unavailable and direct administrators to the existing password form.

The collision regression also exposed that normal login could select the synthetic demo account before a normal account with the same name. Exclude the reserved demo tenant when selecting a normal identity, retain rejection of demo-only accounts, and constrain password lookup to the token's company.

Keep the three-module architecture, all registry pages, tenant-scoped JWT access, immutable historical migrations, optimistic versions, soft deletion, atomic CSV import, generated-record protection, demo isolation/reset and transactional stock/ledger posting. Tests use isolated H2 and ephemeral keys. Do not reset a configured production database or rewrite existing distributions in place.

## Domain boundaries

HR, manufacturing, analytics, documents and logistics include record/definition surfaces. Payroll execution, MRP optimization, report scheduling, binary document storage, carrier integrations and offline synchronization have no end-to-end engine or specified external contracts. They cannot honestly be certified as production capabilities. The completion work will make existing record workflows usable and complete the implemented sales/purchasing/accounting workflows, with explicit remaining-domain notes in the final report.

## Verification plan

1. Run the existing reactor tests as a baseline.
2. Add targeted regression tests for transport, busy/read-only rules, filters, page correction, navigation, references, metadata access, activity and overdue calculations.
3. Exercise real backend CRUD, atomic imports, tenant isolation and posting/reversal tests.
4. Render every catalog screen and the new dialogs under JavaFX; inspect normal/compact screenshots and exercise the demo against a dedicated local database.
5. Build runnable backend and desktop artifacts; report exact results, screenshots, and checks requiring a physical printer or assistive technology.
