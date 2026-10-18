package com.erp.desktop.view;

import java.util.List;
import java.util.function.Consumer;
import com.erp.desktop.api.ApiClient;
import com.erp.desktop.api.Values;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Live company overview, with wrapping summaries and scrollable document/activity lists. */
public class OverviewView implements AutoCloseable {
    private final ApiClient api;
    private final Consumer<String> navigate;
    private final VBox root = new VBox(16);
    private final FlowPane metrics = new FlowPane(16, 16);
    private final VBox documents = new VBox(16);
    private final VBox events = new VBox(8);
    private final Label status = Ui.status("Loading workspace…");
    private final Button refresh = Ui.button("Refresh", this::refresh);
    private Task<?> request;
    private boolean disposed;

    public OverviewView(ApiClient api) { this(api, path -> { }); }
    public OverviewView(ApiClient api, Consumer<String> navigate) { this.api = api; this.navigate = navigate; build(); refresh(); }
    public Node root() { return root; }

    private void build() {
        root.getStyleClass().add("overview"); root.setPadding(new Insets(24));
        Label heading = Ui.title("Overview"); HBox.setHgrow(heading, Priority.ALWAYS);
        root.getChildren().add(new HBox(16, heading, refresh));
        Label company = Ui.muted(api.companyName().isBlank() ? "Your company workspace" : api.companyName());
        company.setWrapText(true); root.getChildren().add(company);
        VBox content = new VBox(16);
        if (api.isDemo()) {
            Label notice = Ui.label("Demo workspace · Synthetic records in an isolated company. Reset data or leave the demo from the top bar.", "overview-notice");
            notice.setWrapText(true); notice.setMaxWidth(Double.MAX_VALUE); content.getChildren().add(notice);
        }
        metrics.setId("metrics"); documents.setId("activity"); events.setId("recent-activity");
        metrics.widthProperty().addListener((observable, old, width) -> fitMetrics());
        FlowPane shortcuts = new FlowPane(8, 8);
        for (String[] item : new String[][] {{"Customers", "/api/sales/customers"}, {"Sales invoices", "/api/sales/invoices"},
                {"Goods receipts", "/api/purchasing/goods-receipts"}, {"Journal entries", "/api/finance/journal-entries"}}) {
            Button button = Ui.button(item[0], () -> navigate.accept(item[1])); button.setGraphic(Icons.of("document")); shortcuts.getChildren().add(button);
        }
        content.getChildren().addAll(metrics, new VBox(8, Ui.label("Quick actions", "panel-title"), shortcuts), documents,
                new VBox(8, Ui.label("Recent activity", "panel-title"), events));
        ScrollPane scroll = new ScrollPane(content); scroll.setFitToWidth(true); scroll.setId("overview-scroll");
        root.getChildren().addAll(scroll, status); VBox.setVgrow(scroll, Priority.ALWAYS);
    }
    public void refresh() {
        if (disposed || refresh.isDisabled()) return;
        refresh.setDisable(true); status.setText("Loading workspace…");
        request = Ui.async(api::overview, data -> {
            if (disposed) return; refresh.setDisable(false); render(data);
            status.getStyleClass().remove("error"); status.setText("Connected · Updated " + Values.display(data, "updatedAt") + " UTC");
        }, error -> {
            if (disposed) return; refresh.setDisable(false); status.setText(Ui.message(error));
            if (!status.getStyleClass().contains("error")) status.getStyleClass().add("error");
            if (metrics.getChildren().isEmpty()) metrics.getChildren().setAll(Ui.errorState(Ui.message(error), this::refresh));
        });
    }
    private void render(JsonNode data) {
        metrics.getChildren().setAll(metric("Customers", data.path("customers"), "Master data"), metric("Open orders", data.path("openOrders"), "Sales pipeline"),
                metric("Overdue invoices", data.path("overdueInvoices"), "Unpaid past due"), metric("Goods receipts", data.path("goodsReceiptCount"), "Purchasing"));
        fitMetrics();
        documents.getChildren().setAll(section("Recent invoices", data.path("recentInvoices"), "invoiceNumber", "invoiceDate", "/api/sales/invoices"),
                section("Recent deliveries", data.path("deliveries"), "deliveryNumber", "deliveryDate", "/api/sales/deliveries"),
                section("Recent goods receipts", data.path("recentGoodsReceipts"), "receiptNumber", "receiptDate", "/api/purchasing/goods-receipts"));
        events.getChildren().clear();
        if (data.path("activity").isEmpty()) events.getChildren().add(Ui.muted("No activity recorded yet. Changes made in this workspace appear here."));
        else for (JsonNode event : data.path("activity")) {
            Label detail = new Label(event.path("action").asText() + " · " + event.path("entity").asText() + " #" + event.path("record").asText()
                    + " · " + event.path("user").asText() + " · " + Values.display(event, "time"));
            detail.setWrapText(true); events.getChildren().add(detail);
        }
    }
    private Node metric(String title, JsonNode value, String hint) {
        VBox box = new VBox(8, Ui.label(String.format("%,d", value.asLong()), "metric-value"), new Label(title), Ui.muted(hint));
        box.getStyleClass().add("metric"); box.setPrefWidth(168); box.setMinWidth(152); return box;
    }
    private void fitMetrics() {
        double width = metrics.getWidth(); if (width <= 0) return;
        int columns = width >= 720 ? 4 : 2;
        double cardWidth = Math.floor((width - 16 * (columns - 1)) / columns);
        metrics.getChildren().stream().filter(Region.class::isInstance).map(Region.class::cast).forEach(card -> card.setPrefWidth(cardWidth));
    }
    private Node section(String title, JsonNode data, String number, String date, String path) {
        VBox rows = new VBox(8); Label heading = Ui.label(title, "panel-title"); HBox.setHgrow(heading, Priority.ALWAYS);
        rows.getChildren().add(new HBox(8, heading, Ui.subtle("Open list", () -> navigate.accept(path))));
        if (data.path("items").isEmpty()) rows.getChildren().add(Ui.muted("No documents yet."));
        else for (JsonNode row : data.path("items")) {
            FlowPane fields = new FlowPane(16, 8); fields.getStyleClass().add("document-row");
            Label key = Ui.label(row.path(number).asText("#" + row.path("id").asText()), "cell-key"); key.setMinWidth(184);
            Label state = Ui.pill(row.path("status").asText("—").replace('_', ' '), "record-state"); state.setMinWidth(120);
            Label money = Ui.muted(amount(row)); money.setMinWidth(104);
            fields.getChildren().addAll(key, state, money, Ui.muted(row.path(date).asText("—"))); rows.getChildren().add(fields);
        }
        rows.getStyleClass().add("activity-section"); return rows;
    }
    private static String amount(JsonNode row) {
        for (String field : List.of("totalAmount", "totalValue", "amount")) if (row.path(field).isNumber())
            return (row.path("currency").asText("") + " " + String.format("%,.2f", row.path(field).decimalValue())).trim();
        return "—";
    }
    public void dispose() { disposed = true; if (request != null) request.cancel(); }
    public void close() { dispose(); }
}
