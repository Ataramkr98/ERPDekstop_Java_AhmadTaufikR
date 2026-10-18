package com.erp.desktop.view;

import java.util.List;
import com.erp.desktop.api.ApiClient;
import com.erp.desktop.api.EntityMeta;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;

/** Searchable related-record selection, through the same tenant-scoped list API. */
public final class ReferencePicker extends Dialog<JsonNode> {
    private int page;
    private final TableView<JsonNode> table = new TableView<>();
    private final Label status = Ui.status("Search by code, name or document number.");
    private final TextField search = new TextField();
    private final Button previous = Ui.button("Previous", () -> load(page - 1));
    private final Button next = Ui.button("Next", () -> load(page + 1));
    private final ApiClient api;
    private final EntityMeta.Field field;
    private final ButtonType choose = new ButtonType("Choose record", ButtonBar.ButtonData.OK_DONE);
    private int pages = 1;
    private Task<?> request;
    private boolean disposed;

    public ReferencePicker(ApiClient api, EntityMeta.Field field, Window owner) {
        this.api = api; this.field = field;
        if (owner != null) initOwner(owner);
        setTitle("Choose " + field.referenceLabel); setHeaderText(field.label);
        setResizable(true); Ui.theme(getDialogPane());
        search.setPromptText("Search " + field.referenceLabel.toLowerCase());
        search.setOnAction(e -> load(0));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        TableColumn<JsonNode,String> id = new TableColumn<>("ID");
        id.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().path("id").asText())); id.setMaxWidth(90);
        TableColumn<JsonNode,String> description = new TableColumn<>("Record");
        description.setCellValueFactory(v -> new SimpleStringProperty(describe(v.getValue())));
        table.getColumns().add(id); table.getColumns().add(description); table.setPrefHeight(320);
        table.setPlaceholder(Ui.emptyState("No matching records", "Try another search, or create the related record in its module."));
        getDialogPane().getButtonTypes().addAll(choose, ButtonType.CANCEL);
        getDialogPane().lookupButton(choose).getStyleClass().add("primary");
        getDialogPane().lookupButton(choose).disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                setResult(table.getSelectionModel().getSelectedItem()); close();
            }
        });
        VBox body = new VBox(8, new HBox(8, search, Ui.button("Search", () -> load(0))), table,
                new HBox(8, previous, next), status);
        getDialogPane().setContent(body); getDialogPane().setPrefWidth(620);
        setResultConverter(button -> button == choose ? table.getSelectionModel().getSelectedItem() : null);
        setOnHidden(e -> { disposed = true; if (request != null) request.cancel(); });
        load(0);
    }

    private void load(int target) {
        if (disposed || target < 0) return;
        if (request != null) request.cancel();
        String term = search.getText();
        previous.setDisable(true); next.setDisable(true); status.setText("Loading records…");
        request = Ui.async(() -> api.list(field.referencePath, term, target, 20, "id", false), result -> {
            if (disposed) return;
            page = result.path("page").asInt(); pages = Math.max(1, result.path("pages").asInt());
            table.getItems().clear(); result.path("items").forEach(table.getItems()::add);
            previous.setDisable(page == 0); next.setDisable(page + 1 >= pages);
            status.setText(result.path("total").asInt() + " matching records · page " + (page + 1) + " of " + pages);
        }, failure -> { if (!disposed) status.setText(Ui.message(failure)); });
    }

    public static String describe(JsonNode row) {
        String code = "", name = "";
        for (String key : List.of("code", "invoiceNumber", "receiptNumber", "orderNumber", "deliveryNumber", "entryNumber", "username"))
            if (row.hasNonNull(key)) { code = row.path(key).asText(); break; }
        for (String key : List.of("name", "fullName", "title", "description"))
            if (row.hasNonNull(key)) { name = row.path(key).asText(); break; }
        String label = code + (!code.isBlank() && !name.isBlank() ? " · " : "") + name;
        return label.isBlank() ? "Record #" + row.path("id").asText() : label;
    }
}
