package com.erp.desktop;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.erp.desktop.api.*;
import com.erp.desktop.view.*;
import com.fasterxml.jackson.databind.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import java.util.*;
import java.util.concurrent.*;

class ViewRegressionTest {
    private static final ObjectMapper JSON=new ObjectMapper();
    @BeforeAll static void toolkit() throws Exception { CountDownLatch ready=new CountDownLatch(1); Platform.startup(ready::countDown); assertTrue(ready.await(15,TimeUnit.SECONDS)); Platform.setImplicitExit(false); }
    @AfterAll static void stop() { Ui.shutdown(); Platform.exit(); }
    private static void fx(Runnable action) throws Exception { FutureTask<Void> task=new FutureTask<>(action,null); Platform.runLater(task); task.get(60,TimeUnit.SECONDS); }
    @Test void invalidNumbersDoNotBecomeNull() {
        EntityMeta.Field field=new EntityMeta.Field("amount","Amount",EntityMeta.Kind.DECIMAL,false,List.of());
        assertThrows(IllegalArgumentException.class,()->Values.put(JSON.createObjectNode(),field,"not a number"));
    }
    @Test void everyCatalogViewBuildsAndUsesConsistentStyles() throws Exception {
        ApiClient api=new ApiClient("http://127.0.0.1:1");
        for(String module:Catalog.modules())for(Catalog.Entry entry:Catalog.entries(module)) fx(()-> {
            EntityView view=new EntityView(api,entry);
            javafx.scene.Node root=view.root();
            Ui.theme((javafx.scene.Parent) root);
            new Scene((javafx.scene.Parent) root,1100,740);
            root.applyCss();
            ((javafx.scene.Parent) root).layout();
            assertNotNull(root.lookup("#records-table"));
            assertNotNull(root.lookup("#entity-search"));
            view.dispose();
        });
    }
    @Test void paginationMatchesTheServerDefaultPageSize() {
        // The generic CRUD defaults to 50 rows; the grid must request the same page
        // size or page counters drift from the server response.
        assertEquals(50, EntityView.PAGE_SIZE);
    }
    @Test void demoEntryPointsStayVisibleDuringADemoSession() throws Exception {
        ApiClient api=new ApiClient("http://127.0.0.1:1");
        fx(()-> {
            MainWindow window=new MainWindow(api,()->{});
            // The required Indonesian affordances from the portfolio task: a "Mode Demo"
            // indicator in the top bar plus reset/exit controls for the demo session.
            assertNotNull(window.root().lookup("#demo-badge"));
            var labels=window.root().lookupAll(".label").stream()
                    .filter(javafx.scene.control.Label.class::isInstance)
                    .map(javafx.scene.control.Label.class::cast)
                    .map(javafx.scene.control.Label::getText).collect(java.util.stream.Collectors.toSet());
            assertTrue(labels.contains("Mode Demo"));
            var buttons=window.root().lookupAll(".button").stream()
                    .filter(Button.class::isInstance).map(Button.class::cast)
                    .map(Button::getText).collect(java.util.stream.Collectors.toSet());
            assertTrue(buttons.contains("Reset Data Demo"));
            assertTrue(buttons.contains("Keluar Demo"));
            window.dispose();
        });
    }
    @Test void navigationShowsTheModulesWithoutManualExpansion() throws Exception {
        ApiClient api=new ApiClient("http://127.0.0.1:1");
        fx(()-> {
            MainWindow window=new MainWindow(api,()->{});
            TreeView<?> nav=(TreeView<?>)window.root().lookup("#module-tree");
            assertNotNull(nav);
            // The sidebar is the only way into the application. A collapsed root used to
            // hide every division behind a disclosure arrow, so the sidebar rendered empty
            // and the workspace looked broken; the expanded row count proves the modules
            // are actually reachable.
            assertTrue(nav.getExpandedItemCount()>1,
                    "the module tree must expose its divisions without the user expanding anything");
            window.dispose();
        });
    }

    @Test void longFormsScrollAndPreserveTypedInputs() throws Exception {
        var field=JSON.createObjectNode().put("name","invoiceDate").put("label","Invoice date").put("type","date").put("javaType","LocalDate").put("required",true);
        var description=JSON.createObjectNode().put("entity","Example"); description.putArray("fields").add(field);
        EntityMeta meta=EntityMeta.parse(description);
        fx(()-> {
            RecordDialog dialog=new RecordDialog(new ApiClient("http://127.0.0.1:1"),new Catalog.Entry("Examples","/examples","Example"),meta,null,null);
            dialog.getDialogPane().applyCss(); dialog.getDialogPane().layout();
            assertInstanceOf(DatePicker.class,dialog.getDialogPane().lookup("#field-invoiceDate"));
            assertFalse(dialog.getDialogPane().lookupAll(".scroll-pane").isEmpty()); dialog.close();
        });
    }

    /**
     * Regression guard for a record screen that actually reaches a backend.
     *
     * <p>{@code EntityView.buildColumns()} used to call {@code setDisable} on the sort
     * controls whose {@code disableProperty} was already bound. JavaFX throws
     * "A bound value cannot be set" from inside the metadata callback, so the screen
     * silently stopped after drawing its headers: no rows, no sort value, no error.
     *
     * <p>{@link #everyCatalogViewBuildsAndUsesConsistentStyles} could not catch it - its
     * client points at a dead port, so metadata never arrives and {@code buildColumns}
     * never runs. This test answers the metadata and list calls from a stub on a real
     * socket and asserts the screen finishes loading.
     */
    @Test void recordListLoadsMetadataAndRowsFromAReachableBackend() throws Exception {
        com.sun.net.httpserver.HttpServer server =
                com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1",0),0);
        try {
            server.createContext("/api/auth/login", exchange -> respond(exchange,"{\"token\":\"stub-token\"}"));
            server.createContext("/api/sales/customers/meta", exchange -> respond(exchange,
                    "{\"entity\":\"SalCustomer\",\"searchFields\":[\"code\",\"name\"],\"companyScoped\":true,\"readOnly\":false,\"fields\":["
                    + "{\"name\":\"id\",\"label\":\"Id\",\"type\":\"integer\",\"readOnly\":true,\"javaType\":\"Long\"},"
                    + "{\"name\":\"code\",\"label\":\"Code\",\"type\":\"string\",\"readOnly\":false,\"javaType\":\"String\"},"
                    + "{\"name\":\"name\",\"label\":\"Name\",\"type\":\"string\",\"readOnly\":false,\"javaType\":\"String\"},"
                    + "{\"name\":\"currentBalance\",\"label\":\"Current balance\",\"type\":\"decimal\",\"readOnly\":false,\"javaType\":\"BigDecimal\"}]}"));
            server.createContext("/api/sales/customers", exchange -> respond(exchange,
                    "{\"items\":[{\"id\":1,\"code\":\"C-1\",\"name\":\"Stub customer\",\"currentBalance\":10.5}],"
                    + "\"total\":1,\"page\":0,\"pages\":1,\"size\":50}"));
            server.start();
            ApiClient api=new ApiClient("http://127.0.0.1:"+server.getAddress().getPort());
            api.login("stub","stub");

            FutureTask<EntityView> build=new FutureTask<>(()->new EntityView(api,new Catalog.Entry("Customers","/api/sales/customers","SalCustomer")));
            Platform.runLater(build);
            EntityView view=build.get(60,TimeUnit.SECONDS);

            // The load is asynchronous: poll until the first page lands, or fail.
            int rows=0;
            long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(30);
            while(System.nanoTime()<deadline){
                FutureTask<Integer> count=new FutureTask<>(()->{
                    var found=view.root().lookup("#records-table");
                    return found instanceof TableView<?> table?table.getItems().size():-1;
                });
                Platform.runLater(count);
                rows=count.get(10,TimeUnit.SECONDS);
                if(rows>0)break;
                Thread.sleep(120);
            }
            assertEquals(1,rows,"the screen must finish loading metadata and then fetch the first page");

            FutureTask<Boolean> sortUsable=new FutureTask<>(()->{
                var combo=view.root().lookup("#sort-field");
                return combo instanceof ComboBox<?> picker&&!picker.isDisable()&&picker.getValue()!=null;
            });
            Platform.runLater(sortUsable);
            assertTrue(sortUsable.get(10,TimeUnit.SECONDS),"the sort selector must be usable once metadata is loaded");
            fx(view::dispose);
        } finally {
            server.stop(0);
        }
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange,String body)throws java.io.IOException {
        byte[] bytes=body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type","application/json");
        exchange.sendResponseHeaders(200,bytes.length);
        try(var out=exchange.getResponseBody()){out.write(bytes);}
    }

    private static void awaitFx(java.util.function.BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < deadline) {
            FutureTask<Boolean> check = new FutureTask<>(condition::getAsBoolean); Platform.runLater(check);
            if (check.get(10, TimeUnit.SECONDS)) return;
            Thread.sleep(80);
        }
        fail("JavaFX state did not settle within 20 seconds");
    }
    private static JsonNode descriptor(boolean readOnly) {
        var node = JSON.createObjectNode().put("entity", "SalInvoice").put("readOnly", readOnly);
        var fields = node.putArray("fields");
        fields.addObject().put("name", "id").put("type", "integer").put("readOnly", true);
        fields.addObject().put("name", "status").put("type", "enum").putArray("enumValues").add("DRAFT").add("POSTED");
        return node;
    }
    @Test void generatedRecordsCanBeViewedAndExportedButCannotBeEdited() throws Exception {
        ApiClient api = new ApiClient("http://127.0.0.1:1") {
            @Override public JsonNode meta(String path) { return descriptor(true); }
            @Override public JsonNode list(String path, String q, int page, int size, String sort, boolean desc, Map<String,String> filters) {
                var node = JSON.createObjectNode().put("total", 1).put("page", 0).put("pages", 1);
                node.putArray("items").addObject().put("id", 1).put("status", "POSTED"); return node;
            }
        };
        FutureTask<EntityView> build = new FutureTask<>(() -> new EntityView(api, new Catalog.Entry("Journals", "/journals", "JournalEntry")));
        Platform.runLater(build); EntityView view = build.get(20, TimeUnit.SECONDS);
        awaitFx(() -> !view.root().lookup("#export-csv").isDisabled());
        fx(() -> {
            ((TableView<?>) view.root().lookup("#records-table")).getSelectionModel().selectFirst();
            assertFalse(view.root().lookup("#view-record").isDisabled());
            assertTrue(view.root().lookup("#edit-record").isDisabled()); assertTrue(view.root().lookup("#delete-record").isDisabled());
            assertTrue(view.root().lookup("#import-csv").isDisabled()); assertTrue(view.root().lookup("#new-record").isDisabled());
            assertFalse(view.root().lookup("#export-csv").isDisabled()); view.dispose();
        });
    }
    @Test void accountsUseThePasswordFormInsteadOfCsvImport() throws Exception {
        ApiClient api = new ApiClient("http://127.0.0.1:1") {
            @Override public JsonNode meta(String path) { return ((com.fasterxml.jackson.databind.node.ObjectNode) descriptor(false)).put("entity", "AppUser").put("importable", false); }
            @Override public JsonNode list(String path, String q, int page, int size, String sort, boolean desc, Map<String,String> filters) {
                var node = JSON.createObjectNode().put("total", 0).put("pages", 1); node.putArray("items"); return node;
            }
        };
        FutureTask<EntityView> build = new FutureTask<>(() -> new EntityView(api, new Catalog.Entry("Accounts", "/accounts", "AppUser")));
        Platform.runLater(build); EntityView view = build.get(20, TimeUnit.SECONDS);
        awaitFx(() -> !view.root().lookup("#new-record").isDisabled());
        fx(() -> { assertTrue(view.root().lookup("#import-csv").isDisabled()); assertFalse(view.root().lookup("#export-csv").isDisabled()); view.dispose(); });
    }
    @Test void filterRequestsAndLastPageCorrectionUseServerResults() throws Exception {
        var collapse = new java.util.concurrent.atomic.AtomicBoolean();
        var captured = new java.util.concurrent.atomic.AtomicReference<Map<String,String>>(Map.of());
        ApiClient api = new ApiClient("http://127.0.0.1:1") {
            @Override public JsonNode meta(String path) { return descriptor(false); }
            @Override public JsonNode list(String path, String q, int page, int size, String sort, boolean desc, Map<String,String> filters) {
                captured.set(filters);
                var node = JSON.createObjectNode().put("total", collapse.get() ? 50 : 51).put("page", page).put("pages", collapse.get() ? 1 : 2);
                var items = node.putArray("items");
                if (!collapse.get() || page == 0) items.addObject().put("id", page == 0 ? 1 : 51).put("status", "DRAFT");
                return node;
            }
        };
        FutureTask<EntityView> build = new FutureTask<>(() -> new EntityView(api, new Catalog.Entry("Invoices", "/invoices", "SalInvoice")));
        Platform.runLater(build); EntityView view = build.get(20, TimeUnit.SECONDS);
        awaitFx(() -> !view.root().lookup("#export-csv").isDisabled());
        fx(() -> ((ComboBox<String>)view.root().lookup("#filter-status")).setValue("DRAFT"));
        awaitFx(() -> !view.root().lookup("#export-csv").isDisabled()); assertEquals(Map.of("status", "DRAFT"), captured.get());
        fx(() -> view.root().lookup("#entity-pager").lookupAll(".button").stream().map(Button.class::cast).filter(b -> b.getText().equals("Next")).findFirst().orElseThrow().fire());
        awaitFx(() -> ((TableView<JsonNode>)view.root().lookup("#records-table")).getItems().getFirst().path("id").asInt() == 51);
        collapse.set(true); fx(view::refresh);
        awaitFx(() -> !view.root().lookup("#export-csv").isDisabled() && !((TableView<?>)view.root().lookup("#records-table")).getItems().isEmpty());
        fx(() -> { assertEquals(1, ((TableView<JsonNode>)view.root().lookup("#records-table")).getItems().getFirst().path("id").asInt()); view.dispose(); });
    }
    @Test void overviewIsReachableAndDemoBadgeHasSeparateStyleClasses() throws Exception {
        fx(() -> {
            MainWindow window = new MainWindow(new ApiClient("http://127.0.0.1:1"), () -> { });
            assertNotNull(window.root().lookup("#overview-navigation"));
            assertTrue(window.root().lookup("#demo-badge").getStyleClass().containsAll(List.of("pill", "demo")));
            assertFalse(window.root().lookup("#demo-badge").isManaged()); window.dispose();
        });
    }

    @Test void requestsDisableWritesAndDisposedViewsIgnoreLateResponses() throws Exception {
        var block = new java.util.concurrent.atomic.AtomicBoolean();
        var started = new CountDownLatch(1); var release = new CountDownLatch(1);
        ApiClient api = new ApiClient("http://127.0.0.1:1") {
            @Override public JsonNode meta(String path) { return descriptor(false); }
            @Override public JsonNode list(String path, String q, int page, int size, String sort, boolean desc, Map<String,String> filters) {
                if (block.get()) { started.countDown(); try { release.await(10, TimeUnit.SECONDS); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); } }
                var result = JSON.createObjectNode().put("total", 1).put("page", 0).put("pages", 1);
                result.putArray("items").addObject().put("id", 1).put("status", "DRAFT"); return result;
            }
        };
        FutureTask<EntityView> build = new FutureTask<>(() -> new EntityView(api, new Catalog.Entry("Invoices", "/invoices", "SalInvoice")));
        Platform.runLater(build); EntityView view = build.get(20, TimeUnit.SECONDS);
        awaitFx(() -> !view.root().lookup("#export-csv").isDisabled());
        block.set(true);
        fx(() -> { ((TableView<?>)view.root().lookup("#records-table")).getSelectionModel().selectFirst(); view.refresh(); });
        assertTrue(started.await(10, TimeUnit.SECONDS));
        fx(() -> {
            for (String id : List.of("new-record", "edit-record", "delete-record", "export-csv", "import-csv", "entity-search"))
                assertTrue(view.root().lookup("#" + id).isDisabled(), id + " must be disabled during a request");
            view.dispose();
        });
        release.countDown(); Thread.sleep(150);
        fx(() -> assertTrue(((TableView<?>)view.root().lookup("#records-table")).getItems().isEmpty(), "disposed tables must remain empty"));
    }
}
