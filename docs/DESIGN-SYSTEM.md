# ERP Workspace design system

The application is a dense operations workspace. Navigation, documents and forms take priority over decoration. JavaFX views share `styles/app.css`; dialogs attach that same stylesheet through `Ui.theme`. There is no FXML layer to maintain.

## Tokens

| Role | Token | Value / use |
|---|---|---|
| Background | `-erp-bg` | `#f5f6f8`, workspace canvas |
| Surface | `-erp-surface` | White, tables and panels |
| Alternate surface | `-erp-surface-alt` | `#fafbfc`, table headers and alternate rows |
| Primary | `-erp-accent` | `#23508f`, principal action and focus |
| Hover | `-erp-accent-hover` | `#1c4177` |
| Information | `-erp-accent-soft` | `#e8eff9`, selection and state labels |
| Primary text | `-erp-text` | `#1f2937` |
| Secondary text | `-erp-text-muted` | `#64748b` |
| Border | `-erp-border` / `-erp-border-strong` | Panel separation / input boundaries |
| Error | `-erp-danger` | `#b3261e`, failed operation and validation |
| Success | `-erp-success` | `#1f7a53`, completed writes |
| Warning | `-erp-warning` / `-erp-warning-soft` | `#7a4a00` / `#fdf1dc`, demo and warnings |
| Navigation | `-erp-nav-bg` / `-erp-nav-text` | Dark slate background with readable light text |

Typography uses the installed Segoe UI font: 13px body, 21px page titles, 12px panel labels and 28px live metrics. Major layout spacing is 8, 16 and 24px. Inputs/actions keep their border width when focused so keyboard traversal does not move adjacent controls. Table headers and rows are 40px high. Border radii stay within 4–8px. No gradients or external font download is required.

## Shared components

| Component | Behavior and states |
|---|---|
| Primary button | One principal action per record screen: New record. Disabled during loading or on generated pages. |
| Standard/subtle button | Supporting commands and navigation. Hover, keyboard focus, press and disabled states. |
| Danger button | Delete with a confirmation explaining soft deletion. Disabled for generated/posted rows. |
| Record grid | JSON row identity, server sort/filter/page, horizontal scrolling, tooltips, selection and loading/error/empty states. Enter or double-click opens View. |
| Filter bar | Enum/boolean filters from metadata; Clear filters also clears the search. Controls are disabled during requests. |
| Record form | Required labels, typed dates, enum/boolean inputs, constructor defaults, related-record picker, focused validation errors and one in-flight save. Long forms scroll. |
| Reference picker | Search and 20-row pages through the tenant-scoped API. Choose or double-click a record; Cancel leaves the form unchanged. |
| Details | Readable fields, including generated/posted records; scrollable values and a paginated print summary. |
| History | Administrator-only per-record audit list, pagination, actor/time/action and before/after snapshots. |
| Feedback | Persistent write confirmation after refresh, error text and retry; status reports actual service results. |
| Overview | Real company counts and documents, wrapping metrics/rows, quick navigation, audit events and a last-refresh status. Content scrolls at compact sizes. |
| Icons | Local monochrome vectors on a 16px coordinate system with a 1.5px rounded stroke. Text labels remain present. |

## Interaction and accessibility

Use Tab/Shift+Tab for standard controls. Ctrl+K focuses page search, Ctrl+F record search, Ctrl+N creates, Ctrl+E edits and F5 refreshes. Enter opens a selected row. Views cancel outstanding reads when disposed and never read control values from a worker thread. File and API I/O run through JavaFX Tasks; callbacks execute on the FX thread.

The embedded Quarkus service owns a separate lifecycle thread. Application exit requests graceful service shutdown before cancelling transient UI workers; the worker pool must never interrupt the service's shutdown sequence.

Text labels accompany icons and statuses, required-field labels target their input, and related-record selection can be completed with standard table/dialog keyboard controls. Physical printer output and assistive-technology behavior require separate device testing; this guide is not a WCAG certification.

## Extension rules

Add entity fields and pages through the shared registry/metadata before creating a bespoke screen. Keep business transactions in services, expose explicit REST actions, and add a desktop action descriptor only when that endpoint exists. Never fabricate overview totals or add a nonfunctional action. Preserve historical migrations and company authorization. Use semantic CSS tokens for new colors and `Ui` helpers for shared controls.
