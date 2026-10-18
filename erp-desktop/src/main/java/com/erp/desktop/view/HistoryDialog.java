package com.erp.desktop.view;

import java.util.Map;
import com.erp.desktop.api.ApiClient;
import com.erp.desktop.api.Values;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;

/** Administrator audit inspection, paged and scoped to the selected entity and ID. */
public final class HistoryDialog extends Dialog<Void> {
    private int page;
    private final TableView<JsonNode> table = new TableView<>();
    private final TextArea snapshot = new TextArea();
    private final Label status = Ui.status("Loading history…");
    private final Button previous = Ui.button("Previous", () -> load(page - 1));
    private final Button next = Ui.button("Next", () -> load(page + 1));
    private final ApiClient api;
    private final Map<String,String> filters;
    private Task<?> task;
    private boolean disposed;

    public HistoryDialog(ApiClient api, String entity, String id, Window owner) {
        this.api = api; filters = Map.of("entityType", entity, "entityId", id);
        if (owner != null) initOwner(owner);
        setTitle("Activity history · #" + id); setHeaderText(entity); setResizable(true); Ui.theme(getDialogPane());
        for (String field : new String[]{"occurredAt", "action", "username"}) {
            TableColumn<JsonNode,String> column = new TableColumn<>(switch(field) { case "occurredAt" -> "Time (UTC)"; case "action" -> "Action"; default -> "User"; });
            column.setCellValueFactory(row -> new SimpleStringProperty(Values.display(row.getValue(), field)));
            table.getColumns().add(column);
        }
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN); table.setPrefHeight(240);
        table.setPlaceholder(Ui.emptyState("No recorded activity", "Changes made through the application are recorded here."));
        snapshot.setEditable(false); snapshot.setWrapText(true); snapshot.setPrefRowCount(7);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, row) -> snapshot.setText(row == null ? "" :
                "Before\n" + row.path("beforeValue").asText("—") + "\n\nAfter\n" + row.path("afterValue").asText("—")));
        getDialogPane().setContent(new VBox(8, table, new HBox(8, previous, next), status, snapshot));
        getDialogPane().setPrefWidth(740); getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        setOnHidden(e -> { disposed = true; if (task != null) task.cancel(); });
        load(0);
    }
    private void load(int target) {
        if (disposed || target < 0) return;
        previous.setDisable(true); next.setDisable(true); status.setText("Loading history…");
        task = Ui.async(() -> api.list("/api/platform/audit_logs", "", target, 50, "occurredAt", true, filters), result -> {
            if (disposed) return;
            page = result.path("page").asInt(); table.getItems().clear(); result.path("items").forEach(table.getItems()::add);
            table.getSelectionModel().selectFirst();
            previous.setDisable(page == 0); next.setDisable(page + 1 >= Math.max(1, result.path("pages").asInt()));
            status.setText(result.path("total").asInt() + " events · page " + (page + 1));
        }, error -> { if (!disposed) status.setText(Ui.message(error)); });
    }
}
