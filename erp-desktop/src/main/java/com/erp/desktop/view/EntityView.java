package com.erp.desktop.view;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.erp.desktop.Catalog;
import com.erp.desktop.api.*;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

/** Shared record workspace. All API and file operations run away from the FX thread. */
public class EntityView implements ActiveScreen {
    public static final int PAGE_SIZE = 50;
    private final ApiClient api;
    private final Catalog.Entry entry;
    private final TableView<JsonNode> table = new TableView<>();
    private final TextField search = new TextField();
    private final Label status = Ui.status("Loading fields…");
    private final Label pageInfo = Ui.muted("");
    private final SimpleBooleanProperty busy = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty unavailable = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty readOnly = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty importable = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty locked = new SimpleBooleanProperty(false);
    private final SimpleIntegerProperty page = new SimpleIntegerProperty(0);
    private final SimpleIntegerProperty pages = new SimpleIntegerProperty(1);
    private final Button previous = Ui.button("Previous", () -> turn(-1));
    private final Button next = Ui.button("Next", () -> turn(1));
    private final Button newButton = Ui.primary("New record", this::create);
    private final Button viewButton = Ui.button("View", this::viewSelected);
    private final Button editButton = Ui.button("Edit", this::editSelected);
    private final Button deleteButton = Ui.danger("Delete", this::deleteSelected);
    private final ComboBox<String> sortField = new ComboBox<>();
    private final ToggleButton descending = Ui.toggle("Descending", false);
    private final FlowPane filtersPane = new FlowPane(8, 8);
    private final FlowPane actionsPane = new FlowPane(8, 8);
    private final Map<String,ComboBox<String>> filters = new LinkedHashMap<>();
    private final Map<Button,RecordActions.Action> businessButtons = new LinkedHashMap<>();
    private final VBox root = new VBox(8);
    private EntityMeta meta;
    private Task<?> readTask;
    private boolean disposed;
    private boolean configuring;
    private String successMessage;

    public EntityView(ApiClient api, Catalog.Entry entry) { this.api = api; this.entry = entry; build(); loadMeta(); }
    public Node root() { return root; }
    public void refresh() { if (meta == null) loadMeta(); else reload(); }
    public void focusSearch() { search.requestFocus(); search.selectAll(); }
    public void createRecord() { create(); }
    public void editRecord() { editSelected(); }

    private void build() {
        search.setId("entity-search"); search.setPromptText("Search " + entry.label().toLowerCase());
        search.setMinWidth(200); search.setPrefWidth(240); search.setOnAction(e -> firstPage());
        search.disableProperty().bind(busy.or(unavailable));
        sortField.setId("sort-field"); sortField.setPrefWidth(160); sortField.setPromptText("Sort by");
        sortField.disableProperty().bind(busy.or(unavailable));
        sortField.setConverter(new StringConverter<>() {
            public String toString(String name) { return labelFor(name); }
            public String fromString(String label) { return label; }
        });
        sortField.valueProperty().addListener((observable, old, value) -> { if (!configuring) firstPage(); });
        descending.disableProperty().bind(busy.or(unavailable)); descending.setOnAction(e -> firstPage());
        newButton.setId("new-record"); newButton.setGraphic(Icons.of("add"));
        newButton.disableProperty().bind(busy.or(unavailable).or(readOnly));
        var missingSelection = table.getSelectionModel().selectedItemProperty().isNull();
        viewButton.setId("view-record"); viewButton.disableProperty().bind(busy.or(unavailable).or(missingSelection));
        editButton.setId("edit-record"); editButton.disableProperty().bind(busy.or(unavailable).or(missingSelection).or(readOnly).or(locked));
        deleteButton.setId("delete-record"); deleteButton.disableProperty().bind(editButton.disableProperty());
        Button export = Ui.button("Export CSV…", this::exportCsv); export.setId("export-csv"); export.disableProperty().bind(busy.or(unavailable));
        Button importButton = Ui.button("Import CSV…", this::importCsv); importButton.setId("import-csv"); importButton.disableProperty().bind(busy.or(unavailable).or(importable.not()));
        importButton.setTooltip(new Tooltip("Create business records from CSV. Accounts require the account form."));
        Button refresh = Ui.subtle("Refresh", this::refresh); refresh.setId("refresh-records"); refresh.disableProperty().bind(busy);
        FlowPane toolbar = new FlowPane(8, 8, search, sortField, descending, refresh); toolbar.setId("entity-toolbar"); toolbar.setAlignment(Pos.CENTER_LEFT);
        FlowPane commands = new FlowPane(8, 8, newButton, viewButton, editButton, deleteButton, importButton, export);
        Button print = Ui.button("Print…", () -> withSelected(row -> RecordDetails.print(entry, meta, row, window())));
        print.disableProperty().bind(viewButton.disableProperty()); HBox inspection = Ui.row(print); commands.getChildren().add(inspection);
        if (api.roles().contains("ADMIN")) {
            Button history = Ui.button("History", () -> withSelected(row -> new HistoryDialog(api, meta.entity(), row.path("id").asText(), window()).show()));
            history.setId("record-history"); history.disableProperty().bind(viewButton.disableProperty()); inspection.getChildren().add(history);
        }
        filtersPane.setId("entity-filters"); filtersPane.setVisible(false); filtersPane.managedProperty().bind(filtersPane.visibleProperty());
        actionsPane.setId("business-actions"); actionsPane.setVisible(false); actionsPane.managedProperty().bind(actionsPane.visibleProperty());
        table.setId("records-table"); table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY); table.setPlaceholder(Ui.emptyState("Loading records…", ""));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, row) -> { locked.set(RecordActions.locked(entry.entity(), row)); updateActions(); });
        table.setRowFactory(view -> {
            TableRow<JsonNode> row = new TableRow<>();
            row.setOnMouseClicked(event -> { if (event.getClickCount() == 2 && !row.isEmpty()) viewSelected(); }); return row;
        });
        table.setOnKeyPressed(event -> { if (event.getCode() == javafx.scene.input.KeyCode.ENTER) { viewSelected(); event.consume(); } }); table.setSortPolicy(view -> false);
        previous.disableProperty().bind(busy.or(page.lessThan(1))); next.disableProperty().bind(busy.or(page.greaterThanOrEqualTo(pages.subtract(1))));
        HBox pager = Ui.row(pageInfo, previous, next); pager.setId("entity-pager"); HBox.setHgrow(pageInfo, Priority.ALWAYS);
        status.setId("record-status"); root.getStyleClass().add("entity-view"); root.setPadding(new Insets(24));
        root.getChildren().addAll(Ui.title(entry.label()), toolbar, filtersPane, commands, actionsPane, table, pager, status); VBox.setVgrow(table, Priority.ALWAYS);
        busy.addListener((obs, old, value) -> updateActions());
    }

    private void loadMeta() {
        if (disposed) return;
        busy.set(true); status.setText("Loading fields…");
        readTask = Ui.async(() -> api.meta(entry.path()), json -> {
            if (disposed) return;
            configuring = true; meta = EntityMeta.parse(json); readOnly.set(meta.readOnly()); importable.set(meta.importable()); buildColumns(); buildFilters(); buildActions();
            configuring = false; unavailable.set(false); busy.set(false); reload();
        }, error -> { if (!disposed) { busy.set(false); fail(error, this::loadMeta); } });
    }

    private void buildColumns() {
        table.getColumns().clear(); List<String> sorts = new ArrayList<>();
        for (EntityMeta.Field field : meta.gridFields()) {
            TableColumn<JsonNode,String> column = new TableColumn<>(field.label);
            column.setCellValueFactory(row -> new SimpleStringProperty(Values.display(row.getValue(), field.name)));
            column.setSortable(false); column.setPrefWidth(field.name.equals("id") ? 64 : switch(field.kind) {
                case DECIMAL -> 128; case INTEGER -> 104; case DATE -> 152; case BOOLEAN -> 96;
                default -> Math.max(144, Math.min(280, field.label.length() * 8 + 64));
            });
            column.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String text, boolean empty) {
                    super.updateItem(text, empty); setText(empty ? null : text); getStyleClass().removeAll("cell-number", "cell-date", "cell-status");
                    if (!empty) {
                        if (field.kind == EntityMeta.Kind.DECIMAL || field.kind == EntityMeta.Kind.INTEGER) getStyleClass().add("cell-number");
                        if (field.kind == EntityMeta.Kind.DATE) getStyleClass().add("cell-date");
                        if (field.name.equals("status")) getStyleClass().add("cell-status");
                    }
                    setTooltip(empty || text == null || text.isBlank() ? null : new Tooltip(text));
                }
            });
            table.getColumns().add(column); sorts.add(field.name);
        }
        sortField.getItems().setAll(sorts);
        sortField.setValue(sorts.stream().filter(s -> List.of("code", "invoiceNumber", "orderNumber", "receiptNumber", "name").contains(s))
                .findFirst().orElse(sorts.isEmpty() ? "id" : sorts.getFirst()));
    }

    private void buildFilters() {
        filters.clear(); filtersPane.getChildren().clear();
        for (EntityMeta.Field field : meta.fields()) {
            if (field.kind != EntityMeta.Kind.ENUM && field.kind != EntityMeta.Kind.BOOLEAN) continue;
            ComboBox<String> combo = new ComboBox<>(); combo.getItems().add("");
            combo.getItems().addAll(field.kind == EntityMeta.Kind.BOOLEAN ? List.of("true", "false") : field.enumValues);
            combo.setValue(""); combo.setPrefWidth(184); combo.setId("filter-" + field.name);
            combo.setConverter(new StringConverter<>() {
                public String toString(String value) { return value == null || value.isBlank() ? "All · " + field.label
                        : value.equals("true") ? "Yes · " + field.label : value.equals("false") ? "No · " + field.label : value.replace('_', ' '); }
                public String fromString(String value) { return value; }
            });
            combo.disableProperty().bind(busy); combo.valueProperty().addListener((observable, old, value) -> { if (!configuring) firstPage(); }); filters.put(field.name, combo); filtersPane.getChildren().add(combo);
        }
        if (!filters.isEmpty()) {
            Button clear = Ui.subtle("Clear filters", () -> { configuring = true; search.clear(); filters.values().forEach(combo -> combo.setValue("")); configuring = false; firstPage(); });
            clear.disableProperty().bind(busy); filtersPane.getChildren().add(clear);
        }
        filtersPane.setVisible(!filters.isEmpty());
    }
    private Map<String,String> queryFilters() {
        Map<String,String> values = new LinkedHashMap<>(); filters.forEach((name, control) -> {
            if (control.getValue() != null && !control.getValue().isBlank()) values.put(name, control.getValue());
        }); return Map.copyOf(values);
    }
    private String labelFor(String name) { return name == null ? "Sort by" : meta == null ? name : meta.fields().stream().filter(f -> f.name.equals(name)).map(f -> f.label).findFirst().orElse(name); }
    private void firstPage() { if (!busy.get() && !disposed) { page.set(0); reload(); } }
    private void turn(int delta) { if (!busy.get() && page.get() + delta >= 0 && page.get() + delta < pages.get()) { page.set(page.get() + delta); reload(); } }
    private void reload() {
        if (disposed || busy.get() || meta == null) return;
        String term = search.getText(), sort = sortField.getValue(); int requested = page.get(); boolean desc = descending.isSelected();
        Map<String,String> values = queryFilters(); busy.set(true); status.setText("Loading records…");
        readTask = Ui.async(() -> api.list(entry.path(), term, requested, PAGE_SIZE, sort, desc, values), result -> {
            if (disposed) return;
            busy.set(false);
            int count = result.path("total").asInt(), serverPages = Math.max(1, result.path("pages").asInt(1)), serverPage = result.path("page").asInt();
            pages.set(serverPages); if (serverPage >= serverPages) { page.set(serverPages - 1); reload(); return; }
            page.set(serverPage); table.getItems().clear(); result.path("items").forEach(table.getItems()::add);
            int from = count == 0 ? 0 : page.get() * PAGE_SIZE + 1, to = Math.min(count, (page.get() + 1) * PAGE_SIZE);
            pageInfo.setText(count == 0 ? "No records" : from + "–" + to + " of " + count + " · page " + (page.get() + 1) + " of " + pages.get());
            status.getStyleClass().removeAll("error", "success");
            status.setText(successMessage == null ? count + " records" + (meta.readOnly() ? " · " + meta.readOnlyReason() : " · Select a record to view or edit") : successMessage);
            if (successMessage != null) status.getStyleClass().add("success"); successMessage = null;
            table.setPlaceholder(Ui.emptyState("No matching " + entry.label().toLowerCase(), term.isBlank() && values.isEmpty() ? meta.readOnly()
                    ? meta.readOnlyReason() + ". You can view, export or print existing records." : meta.importable()
                    ? "Create a record or import a CSV to get started." : "Create a record with the account form to get started." : "Adjust the search or clear the filters to see more records."));
        }, error -> { if (!disposed) { busy.set(false); successMessage = null; fail(error, this::reload); } });
    }
    private void fail(Throwable error, Runnable retry) {
        status.getStyleClass().remove("success"); if (!status.getStyleClass().contains("error")) status.getStyleClass().add("error");
        status.setText(Ui.message(error)); if (table.getItems().isEmpty()) table.setPlaceholder(Ui.errorState(Ui.message(error), retry));
    }
    private void withSelected(java.util.function.Consumer<JsonNode> operation) {
        JsonNode row = table.getSelectionModel().getSelectedItem(); if (disposed || busy.get() || meta == null || row == null) return;
        String id = row.path("id").asText(); busy.set(true); status.setText("Opening record…");
        readTask = Ui.async(() -> api.get(entry.path() + "/" + id), current -> {
            if (disposed) return; busy.set(false); status.setText("Opened " + ReferencePicker.describe(current)); operation.accept(current);
        }, error -> { if (!disposed) { busy.set(false); fail(error, this::reload); } });
    }
    private void viewSelected() { withSelected(row -> RecordDetails.show(entry, meta, row, window())); }
    private void create() { if (!newButton.isDisabled() && !disposed) showForm(null, null); }
    private void editSelected() {
        if (editButton.isDisabled()) return;
        withSelected(row -> { if (RecordActions.locked(entry.entity(), row)) { status.setText("This document is posted. Use View to inspect it."); reload(); }
            else showForm(row.path("id").asText(), row); });
    }
    private void showForm(String id, JsonNode row) {
        RecordDialog dialog = new RecordDialog(api, entry, meta, id, row); if (window() != null) dialog.initOwner(window());
        dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
        dialog.setOnHidden(event -> { if (!disposed && dialog.getResult() != null) { successMessage = "Saved " + ReferencePicker.describe(dialog.getResult()) + "."; reload(); } }); dialog.show();
    }
    private void deleteSelected() {
        if (deleteButton.isDisabled() || disposed) return;
        JsonNode selected = table.getSelectionModel().getSelectedItem(); String name = ReferencePicker.describe(selected);
        if (!ConfirmDialog.confirm("Delete " + name + "?", "This removes the record from active lists and preserves its audit history. Posted documents cannot be deleted.", "Delete")) return;
        String id = selected.path("id").asText(); busy.set(true); status.setText("Deleting record…");
        Ui.async(() -> { api.delete(entry.path() + "/" + id); return true; }, done -> { if (!disposed) { busy.set(false); successMessage = "Deleted " + name + "."; reload(); } },
                error -> { if (!disposed) { busy.set(false); fail(error, this::reload); } });
    }
    private void buildActions() {
        actionsPane.getChildren().clear(); businessButtons.clear();
        for (var action : RecordActions.forEntity(entry.entity())) {
            if (action.adminOnly() && !api.roles().contains("ADMIN")) continue;
            Button button = Ui.button(action.label(), () -> runAction(action)); button.setTooltip(new Tooltip(action.description())); businessButtons.put(button, action); actionsPane.getChildren().add(button);
        }
        actionsPane.setVisible(!businessButtons.isEmpty()); updateActions();
    }
    private void updateActions() { businessButtons.forEach((button, action) -> button.setDisable(busy.get() || !action.available(table.getSelectionModel().getSelectedItem(), api.roles()))); }
    private void runAction(RecordActions.Action action) {
        JsonNode row = table.getSelectionModel().getSelectedItem(); if (disposed || busy.get() || !action.available(row, api.roles())) return;
        if (!ConfirmDialog.confirm(action.label() + " · " + ReferencePicker.describe(row) + "?", action.description(), action.label())) return;
        String route = action.path(row.path("id").asText()); busy.set(true); status.setText(action.label() + "…");
        Ui.async(() -> api.businessAction(route), result -> { if (!disposed) {
            busy.set(false); successMessage = result.path("status").asText().equals("ON_HOLD") ? "Invoice placed on hold: " + result.path("holdReason").asText()
                    : action.label() + " completed" + (result.has("matchStatus") ? " · " + result.path("matchStatus").asText() : "") + "."; reload();
        } }, error -> { if (!disposed) { busy.set(false); fail(error, this::reload); } });
    }
    private FileChooser csvChooser(String title) { FileChooser chooser = new FileChooser(); chooser.setTitle(title); chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv")); return chooser; }
    private void importCsv() {
        if (disposed || busy.get() || meta == null || !meta.importable()) return;
        if (!ConfirmDialog.confirm("Import " + entry.label() + "?", "Import creates new records; it does not update existing ones. Use exported field names, change duplicate unique codes, and keep the file below 4 MB / 1,000 rows. Every row must pass validation or nothing is imported.", "Choose CSV")) return;
        File file = csvChooser("Import " + entry.label()).showOpenDialog(window()); if (file == null) return;
        busy.set(true); status.setText("Importing records…");
        Ui.async(() -> { if (Files.size(file.toPath()) > 4_000_000) throw new IllegalArgumentException("CSV must be smaller than 4 MB."); return api.importCsv(entry.path(), Files.readString(file.toPath())); },
                result -> { if (!disposed) { busy.set(false); successMessage = "Imported " + result.path("created").asInt() + " new records."; page.set(0); reload(); } },
                error -> { if (!disposed) { busy.set(false); fail(error, this::reload); } });
    }
    private void exportCsv() {
        if (disposed || busy.get() || meta == null) return;
        FileChooser chooser = csvChooser("Export " + entry.label()); chooser.setInitialFileName(entry.entity() + ".csv"); File file = chooser.showSaveDialog(window()); if (file == null) return;
        String term = search.getText(), sort = sortField.getValue(); boolean desc = descending.isSelected(); Map<String,String> values = queryFilters(); busy.set(true); status.setText("Exporting matching records…");
        Ui.async(() -> { String csv = api.exportCsv(entry.path(), term, sort, desc, values); Files.writeString(file.toPath(), csv); return true; },
                done -> { if (!disposed) { busy.set(false); status.getStyleClass().remove("error"); status.setText("Exported matching records to " + file.getName() + "."); } },
                error -> { if (!disposed) { busy.set(false); fail(error, this::reload); } });
    }
    private Window window() { return root.getScene() == null ? null : root.getScene().getWindow(); }
    public void dispose() { disposed = true; if (readTask != null) readTask.cancel(); table.getItems().clear(); }
}
