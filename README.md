# ERP Workspace

A Java 21 desktop ERP with JavaFX views, a Quarkus business core and Neon PostgreSQL. The desktop talks to a loopback REST server in the same process; the backend can also run separately.

## What this is, and the problem it solves

Most portfolio projects stop at a CRUD demo. This one is an attempt at the harder part of an
ERP: one generic, schema-driven record engine that carries ~118 record surfaces across 13
divisions, plus the explicit business workflows that make an ERP more than a database — posting
a goods receipt or a customer invoice writes the stock movements and the balanced journal
entries in the same transaction, refuses to post twice, and freezes the generated records so
they cannot be edited by hand.

It also solves a second, practical problem: **a reviewer cannot evaluate an ERP without a login.**
The application therefore ships a *demo mode* — a credential-free session in its own isolated
tenant with a realistic synthetic dataset, which can be reset and left at any time. See
[Demo mode](#demo-mode-portfolio-review) below.

**Technology.** Java 21 · JavaFX 21 (programmatic views, no FXML) · Quarkus 3.15 LTS · Hibernate
ORM / Panache · Flyway migrations · PostgreSQL (Neon) · SmallRye JWT (RS256) · Maven multi-module.
Three modules: `erp-core` (domain, REST, security, business services), `erp-backend` (standalone
Quarkus host + API tests) and `erp-desktop` (JavaFX shell, navigation, shared record forms, REST
client).

## Run on Windows

### Prerequisites

| Requirement | Notes |
|---|---|
| JDK 21 or later | `JAVA_HOME` is respected; the launcher also discovers a JDK from `java` on `PATH`. |
| PowerShell 7 **or** Windows PowerShell 5.1 | The `.cmd` launchers pick whichever is installed. |
| Maven 3.9+ | Optional — the repository ships a portable Maven in `.tools/maven` and `mvnw.cmd` uses it. |
| PostgreSQL reachable from the machine | Either a local server or a hosted Neon URL. Demo mode uses the same database unless you point it elsewhere (see below). |

```powershell
./mvnw.cmd clean verify
pwsh -NoProfile -File scripts/Configure-Neon.ps1
./run-desktop.cmd
```

For the portfolio demo walkthrough (no credentials typed by the reviewer):

```powershell
./run-demo.cmd
```

`run-desktop.cmd -Build` and `run-demo.cmd -Build` build before launching; `run-backend.cmd` starts the standalone backend on `127.0.0.1:8080`. Use `ERP_PORT` to choose a different local port. `run-demo.cmd` passes `-Dquarkus.profile=demo`, which loads `application-demo.properties` from both the backend and the desktop module.

`Configure-Neon.ps1` asks for the PostgreSQL URI without echoing it, then protects it with Windows account encryption. The launcher loads the profile under the current Windows account.

The first empty database is initialized by Flyway V1–V15 and contains the application account **admin / admin**. Change this password from the top bar. Database credentials and application accounts are separate. See [Neon setup](NEON-SETUP.md) for environment configuration.

## Demo mode (portfolio review)

`run-demo.cmd` starts the **real application** — JavaFX desktop with the Quarkus core in the same JVM and the real PostgreSQL system of record — under the Quarkus `demo` profile. It is not a mock or a scripted walkthrough.

What a reviewer can do:

1. On the sign-in screen, press **Coba Demo**. No username or password is required; the session is minted by `POST /api/demo/session`.
2. A **Mode Demo** badge in the top bar marks the whole session. The workspace opens on the overview with live counts and recent documents from the demo tenant.
3. Navigate the module tree (one click), search and sort record lists, open records, and run create/edit/delete flows. All writes go through the normal REST API, validation, audit and optimistic locking.
4. Press **Reset Data Demo** in the top bar (with confirmation) to restore the seeded sample dataset exactly.
5. Press **Keluar Demo** to leave. The session token is dropped, so nothing from the demo carries into a normal sign-in.

Isolation from production, enforced in code rather than by convention:

- Every demo row carries the demo tenant (`companyId` of company code `DEMO`). Reads and writes are scoped by the JWT `companyId` claim and re-checked by `RecordPolicy`, so a demo session cannot see or alter another company's rows. `DemoWorkspaceTest` asserts this, including that reset never touches another tenant.
- Shared administration/reference routes are tenant-scoped for demo sessions too. Account, role and company administration is read-only in demo mode; legacy accounts owned by the demo company cannot use normal sign-in to obtain an unrestricted session. The company code `DEMO` is reserved for the portfolio workspace.
- The demo endpoints answer **404 unless `erp.demo.enabled=true`**, and `DemoSettings` forces the flag back to `false` whenever the profile starts with `prod` or `live` — a stray system property cannot enable credential-free sign-in in production.
- Demo sessions use their own signing keys (`%LOCALAPPDATA%\ERP-Desktop\demo-keys`), separate from normal sessions.
- To keep demo data off a production database, set `ERP_DEMO_DATABASE_URL` (or `ERP_DEMO_JDBC_URL`) before `run-demo.cmd`; otherwise the demo shares the configured database and is isolated by tenant only. The application has no payment, messaging or webhook integrations, so the demo cannot trigger any external side effect.

## Screenshots

The current portfolio acceptance captures live in `docs/screenshots/portfolio`: overview, customers, invoices, forms, reference selection, record details/history, posting confirmation and compact layouts. `PortfolioScenario` rejects blank screenshots and exercises the actual JavaFX controls against PostgreSQL. The older screenshots in the parent directory belong to previous verification runs.

To reproduce the portfolio walkthrough with a disposable loopback PostgreSQL cluster and separate signing keys:

```powershell
./mvnw.cmd verify
pwsh -NoProfile -File scripts/Verify-Portfolio.ps1 -JavaHome "$env:JAVA_HOME"
# Also exercise the desktop's embedded Quarkus startup and demo sign-in/exit:
pwsh -NoProfile -File scripts/Verify-Portfolio.ps1 -JavaHome "$env:JAVA_HOME" -Embedded
# Check a generated app image's actual demo launcher:
pwsh -NoProfile -File scripts/Verify-Portfolio.ps1 -JavaHome "$env:JAVA_HOME" -NativeDemoLauncher '<absolute bundle path>/ERP-Demo.exe'
```

`Verify-Portfolio.ps1` requires local PostgreSQL binaries (use `-PostgresBin` for a different installation). It uses `.local/portfolio-qa`, chooses free ports, creates its own database, and stops its services in `finally`. The acceptance scenario resets only that disposable demo tenant. It never reads the saved Neon credential.

`erp-desktop`'s `PortfolioCapture` also runs against a backend that is already running:

```powershell
# terminal 1 - the backend the capture talks to
java -Dquarkus.profile=demo -jar erp-backend/target/quarkus-app/quarkus-run.jar

# terminal 2 - the demo walkthrough
$env:ERP_CAPTURE = "demo"; java -cp <desktop classpath> com.erp.desktop.PortfolioCapture
```

## Demonstration workflow (normal mode)

1. Sign in as **admin / admin** (change it from the top bar on a real deployment).
2. Open Sales → Customers. Search, sort, edit a record, and export/import a CSV. Imports create new records, so change unique codes before importing an exported file.
3. Open Purchasing → Goods Receipts and post a draft receipt. Check Inventory → Stocks and Finance → Journal Entries for the resulting stock and balanced journal.
4. Open Sales → Invoices and post a draft invoice. Review its journal and the customer's updated balance.
5. Return to the overview to see counts and recent activity sourced from the database.

The record workspace provides typed forms, validation, scrolling, search, enum filters, server sorting, 50-row pagination, optimistic concurrency checks, soft deletion, CSV import/export, printable record summaries and audit history. Generated stock, movement, matching and ledger records are protected from direct editing. Administration screens require an administrator role.

Keyboard shortcuts: **Ctrl+K** find a page, **Ctrl+F** search records, **Ctrl+N** create a record, **Ctrl+E** edit, **F5** refresh, **Enter/double-click** view a selected record. Overview remains reachable from the sidebar. Generated and posted records support View/export/print; illegal writes are disabled. Supported posting, shipping, matching, hold release and reversal actions appear on their document pages with status checks and confirmations.

CSV exports contain all matching rows up to 5,000; larger results require narrower filters and are never silently truncated. Imports accept up to 1,000 rows / 4 MB and commit atomically. Print opens the system printer dialog for a summary of the selected record.

Create application accounts through the account form, which requires a password; account CSV import is disabled. Demo sessions cannot change account passwords.

## Modules and boundaries

The navigation registry exposes the existing finance, sales, purchasing, inventory, manufacturing, CRM, HR, projects, service, logistics, documents, analytics and administration record surfaces. Explicit business workflows include receipt posting, shipment posting, customer/vendor invoice posting, three-way matching, hold release and journal reversal.

The application is a portfolio ERP foundation. Payroll calculation, MRP planning, scheduled report delivery, binary document storage, external integrations and offline synchronization are not implemented execution engines. Their existing screens manage records and definitions. Inventory posting supports standard cost and moving average; FIFO/LIFO posting is explicitly rejected. Full jurisdiction-specific accounting and granular enterprise permission administration require further domain work.

## Project structure

| Module | Responsibility |
|---|---|
| `erp-core` | Entities, migrations, REST, security, validation, audit and business services |
| `erp-backend` | Standalone Quarkus host and isolated API integration tests |
| `erp-desktop` | JavaFX lifecycle, navigation, shared record forms, overview and REST client |
| `scripts` | Maven, encrypted Neon configuration and launchers |

Historical migrations are immutable. Runtime credentials and signing keys stay outside the source tree. The API binds to loopback and requires JWT authentication for business records. H2 tests use an isolated in-memory database and ephemeral signing keys, not Neon.

See the [completion report](docs/PORTFOLIO-COMPLETION-2026-10-12.md), [current audit](docs/PORTFOLIO-AUDIT-2026-10-12.md), [design system](docs/DESIGN-SYSTEM.md), [architecture](docs/ARCHITECTURE.md), and [historical verification report](docs/VERIFICATION.md).

## Native Windows package

Run `pwsh -NoProfile -File scripts/Build-Portable.ps1` to verify and package self-contained `ERP-Workspace.exe` and `ERP-Demo.exe` launchers in a timestamped `dist/portfolio-...` folder. The package includes its Java runtime, reads the saved Windows-encrypted Neon profile, and omits bundled PostgreSQL. The demo launcher uses the demo profile and separate signing keys; `ERP_DEMO_DATABASE_URL` can point it at a dedicated database. On another computer, run the included `Configure-Neon.ps1` once. This is an application folder, not a signed installer.
