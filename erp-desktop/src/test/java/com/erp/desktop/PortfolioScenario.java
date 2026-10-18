package com.erp.desktop;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import com.erp.desktop.api.*;
import com.erp.desktop.view.*;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.*;

/** Real-database UI acceptance scenario. Run only against a disposable demo database. */
public final class PortfolioScenario {
    private static Stage stage;
    private static MainWindow window;
    private static final Path OUT = Path.of("docs", "screenshots", "portfolio");
    private static ApiClient api;

    public static void main(String[] args) throws Exception {
        api = new ApiClient(System.getenv().getOrDefault("ERP_API_URL", "http://127.0.0.1:8080"));
        api.startDemo(); api.resetDemo(); Files.createDirectories(OUT);
        CountDownLatch ready = new CountDownLatch(1); Platform.startup(ready::countDown);
        if (!ready.await(20, TimeUnit.SECONDS)) throw new IllegalStateException("JavaFX did not start");
        Platform.setImplicitExit(false);
        try {
            fx(() -> {
                stage = new Stage(); window = new MainWindow(api, () -> { });
                Ui.theme(window.root()); stage.setScene(new Scene(window.root(), 1280, 800));
                stage.setTitle("ERP Portfolio QA"); stage.show();
            });
            await(() -> hasLabel("Recent invoices") && animationsFinished()); capture("overview.png");
            fx(() -> open("SalCustomer")); await(PortfolioScenario::gridReady);
            capture("customers.png");
            fx(() -> button("New record").fire()); await(() -> dialog() != null);
            fx(() -> dialogButton("Save record").fire());
            await(() -> dialog() != null && dialog().lookupAll(".error").stream().map(Label.class::cast).anyMatch(l -> l.getText().contains("required")));
            captureDialog("customer-validation-form.png");
            String code = "PORT-" + System.currentTimeMillis();
            fx(() -> { ((TextField)dialog().lookup("#field-code")).setText(code); ((TextField)dialog().lookup("#field-name")).setText("Portfolio acceptance customer"); });
            fx(() -> dialogButton("Save record").fire()); await(() -> dialog() == null && gridReady());
            search(code); await(() -> gridReady() && records().getItems().size() == 1);
            JsonNode created = api.list("/api/sales/customers", code, 0, 50, "id", false).path("items").get(0);
            require(created != null, "UI create did not persist");
            fx(() -> { records().getSelectionModel().selectFirst(); button("Edit").fire(); }); await(() -> dialog() != null);
            fx(() -> { ((TextField)dialog().lookup("#field-name")).setText("Updated acceptance customer"); dialogButton("Save record").fire(); });
            await(() -> dialog() == null && gridReady());
            require(api.get("/api/sales/customers/" + created.path("id").asText()).path("name").asText().equals("Updated acceptance customer"), "UI edit did not persist");
            fx(() -> { records().getSelectionModel().selectFirst(); button("History").fire(); });
            await(() -> dialog() != null && dialog().lookupAll(".table-view").stream().map(TableView.class::cast).anyMatch(t -> t.getItems().size() >= 2));
            captureDialog("record-history.png"); closeDialog();
            fx(() -> button("View").fire()); await(() -> dialog() != null); captureDialog("record-details.png"); closeDialog();
            fx(() -> open("SalInvoice")); await(PortfolioScenario::gridReady); capture("invoices.png");
            fx(() -> button("New record").fire()); await(() -> dialog() != null);
            require(fxValue(() -> ((ComboBox<?>)dialog().lookup("#field-status")).getValue().equals("DRAFT")), "Invoice form must default to DRAFT");
            Platform.runLater(() -> dialog().lookupAll(".button").stream().map(Button.class::cast).filter(b -> b.getText().equals("Choose…")).findFirst().orElseThrow().fire());
            await(() -> Window.getWindows().stream().filter(Window::isShowing).count() >= 3);
            await(() -> dialog() != null && dialog().lookupAll(".table-view").stream().map(TableView.class::cast).anyMatch(t -> !t.getItems().isEmpty()));
            captureDialog("related-record-picker.png");
            fx(() -> ((TableView<?>)dialog().lookup(".table-view")).getSelectionModel().selectFirst());
            fx(() -> dialogButton("Choose record").fire());
            await(() -> dialog() != null && dialog().lookup("#field-customerId") instanceof TextField input && !input.getText().isBlank());
            captureDialog("invoice-form.png"); closeDialog();
            fx(() -> ((ComboBox<String>)root().lookup("#filter-status")).setValue("DRAFT")); await(PortfolioScenario::gridReady);
            require(fxValue(() -> !records().getItems().isEmpty()), "Demo needs a draft invoice");
            JsonNode draft = fxValue(() -> records().getItems().getFirst());
            fx(() -> records().getSelectionModel().selectFirst());
            Platform.runLater(() -> button("Post invoice").fire()); await(() -> dialog() != null);
            captureDialog("post-invoice-confirmation.png"); fx(() -> dialogButton("Post invoice").fire());
            await(() -> dialog() == null && gridReady() && root().lookup("#record-status") instanceof Label message
                    && (message.getText().startsWith("Post invoice completed") || message.getStyleClass().contains("error")));
            require(api.get("/api/sales/invoices/" + draft.path("id").asText()).path("status").asText().equals("POSTED"),
                    "UI posting failed: " + fxValue(() -> ((Label)root().lookup("#record-status")).getText()));
            fx(() -> ((ComboBox<String>)root().lookup("#filter-status")).setValue("POSTED")); await(PortfolioScenario::gridReady);
            fx(() -> records().getSelectionModel().selectFirst());
            require(fxValue(() -> button("Edit").isDisabled() && !button("View").isDisabled()), "Posted records must be readable and locked");
            capture("posted-invoices.png");
            fx(() -> button("Overview").fire()); await(() -> hasLabel("Recent invoices") && animationsFinished());
            capture("overview-after-posting.png");
            fx(() -> { stage.setWidth(1020); stage.setHeight(640); }); Thread.sleep(400); capture("compact-overview.png");
            fx(() -> open("SalInvoice")); await(PortfolioScenario::gridReady); capture("compact-invoices.png");
            String exported = api.exportCsv("/api/sales/customers", code);
            require(exported.contains("Updated acceptance customer"), "CSV export did not contain the edited customer");
            api.importCsv("/api/sales/customers", "code,name\n" + code + "-IMPORT,Imported acceptance customer\n");
            require(api.list("/api/sales/customers", code + "-IMPORT", 0, 50, "id", false).path("total").asInt() == 1, "CSV import did not persist");
            System.out.println("PASS: live PostgreSQL overview/navigation, required validation, UI create/edit, history, details, references, defaults, filters, UI invoice posting, posted locks, compact layouts, CSV import/export.");
            if ("true".equalsIgnoreCase(System.getenv("ERP_QA_KEEP_OPEN"))) Thread.sleep(180_000);
        } finally { fx(() -> { if (window != null) window.dispose(); if (stage != null) stage.close(); }); Ui.shutdown(); Platform.exit(); }
    }
    private static void require(boolean condition, String text) { if (!condition) throw new IllegalStateException(text); }
    private static Parent root() { return stage.getScene().getRoot(); }
    @SuppressWarnings("unchecked") private static TableView<JsonNode> records() { return (TableView<JsonNode>)root().lookup("#records-table"); }
    private static boolean gridReady() { Node export = root().lookup("#export-csv"); return export != null && !export.isDisabled() && animationsFinished(); }
    private static boolean animationsFinished() {
        Node holder = root().lookup("#page-holder"); return root().getOpacity() >= .99 && (!(holder instanceof javafx.scene.layout.VBox box) || box.getChildren().getFirst().getOpacity() >= .99);
    }
    private static boolean hasLabel(String text) { return root().lookupAll(".label").stream().map(Label.class::cast).anyMatch(l -> l.getText() != null && l.getText().contains(text)); }
    private static Button button(String text) { return root().lookupAll(".button").stream().map(Button.class::cast).filter(b -> b.getText().equals(text)).findFirst().orElseThrow(); }
    private static DialogPane dialog() {
        List<Window> windows = new ArrayList<>(Window.getWindows()); Collections.reverse(windows);
        return windows.stream().filter(w -> w.isShowing() && w.getScene() != null && w.getScene().getRoot() instanceof DialogPane).map(w -> (DialogPane)w.getScene().getRoot()).findFirst().orElse(null);
    }
    private static Button dialogButton(String text) { return dialog().lookupAll(".button").stream().map(Button.class::cast).filter(b -> b.getText().equals(text)).findFirst().orElseThrow(); }
    private static void closeDialog() throws Exception { fx(() -> dialog().getButtonTypes().stream().filter(b -> b.getButtonData().isCancelButton()).findFirst().map(dialog()::lookupButton).map(Button.class::cast).orElseThrow().fire()); await(() -> dialog() == null); }
    private static void search(String text) throws Exception { fx(() -> { TextField search = (TextField)root().lookup("#entity-search"); search.setText(text); search.fireEvent(new javafx.event.ActionEvent()); }); }
    @SuppressWarnings("unchecked") private static void open(String entity) {
        var entry = com.erp.platform.ModuleRegistry.ENTRIES.stream().filter(e -> e.entity().equals(entity)).findFirst().orElseThrow();
        TreeView<Object> tree = (TreeView<Object>)root().lookup("#module-tree");
        var division = tree.getRoot().getChildren().stream().filter(n -> n.getValue().equals(entry.divisionLabel())).findFirst().orElseThrow();
        tree.getSelectionModel().select(division.getChildren().stream().filter(n -> n.getValue().toString().equals(entry.label())).findFirst().orElseThrow());
    }
    private static void fx(Runnable action) throws Exception { fxValue(() -> { action.run(); return true; }); }
    private static <T> T fxValue(Callable<T> action) throws Exception { FutureTask<T> task = new FutureTask<>(action); Platform.runLater(task); return task.get(60, TimeUnit.SECONDS); }
    private static void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(90);
        while (System.nanoTime() < deadline) { if (fxValue(condition::getAsBoolean)) return; Thread.sleep(100); }
        throw new IllegalStateException("The expected UI state did not appear. " + fxValue(() -> root().lookup("#record-status") instanceof Label label ? label.getText() : ""));
    }
    private static void capture(String name) throws Exception { fx(() -> write(root(), name)); }
    private static void captureDialog(String name) throws Exception { Thread.sleep(300); fx(() -> write(dialog(), name)); }
    private static void write(Parent root, String name) {
        root.applyCss(); root.layout(); WritableImage source = root.snapshot(null, null);
        BufferedImage output = new BufferedImage((int)source.getWidth(), (int)source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < output.getHeight(); y++) for (int x = 0; x < output.getWidth(); x++) {
            int color = source.getPixelReader().getArgb(x, y); output.setRGB(x, y, color); if (x % 8 == 0 && y % 8 == 0) colors.add(color);
        }
        require(colors.size() > 8, "Blank screenshot: " + name);
        try { ImageIO.write(output, "png", OUT.resolve(name).toFile()); } catch (Exception error) { throw new IllegalStateException(error); }
    }
}
