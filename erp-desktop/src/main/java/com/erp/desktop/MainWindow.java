package com.erp.desktop;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.erp.desktop.api.ApiClient;
import com.erp.desktop.view.ConfirmDialog;
import com.erp.desktop.view.EntityView;
import com.erp.desktop.view.OverviewView;
import com.erp.desktop.view.PasswordDialog;
import com.erp.desktop.view.Ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Main window: module navigation on the left, the active screen on the right.
 *
 * <p>The tree is built from the shared {@link Catalog}, which mirrors the server
 * registry, so a module appears as soon as the backend exposes it. Only one screen is
 * alive at a time: opening a new one disposes the previous so nothing can touch a
 * detached scene graph.
 */
public class MainWindow {

    /** Screens a non-administrator must not see. The server enforces the same rule. */
    private static final Set<String> ADMIN_ONLY = Set.of(
            "/api/platform/companies",
            "/api/platform/app_users",
            "/api/platform/roles",
            "/api/platform/user_roles",
            "/api/platform/permissions",
            "/api/platform/audit_logs",
            "/api/platform/number_ranges",
            "/api/platform/system_configs");

    private final ApiClient api;
    private final Runnable onSignOut;

    private final BorderPane root = new BorderPane();
    private final TreeView<Object> nav = new TreeView<>();
    private final ProgressBar busyBar = new ProgressBar();
    private final VBox pageHolder = new VBox();
    private final Label breadcrumb = Ui.muted("");
    private final HBox demoActions = new HBox(8);
    private final Button overviewButton = Ui.button("Overview", this::openOverview);

    private Runnable disposeView = () -> { };
    private com.erp.desktop.view.ActiveScreen activeScreen;
    private String navFilter = "";
    private String activePath = "";
    private boolean disposed;

    public MainWindow(ApiClient api, Runnable onSignOut) {
        this.api = api;
        this.onSignOut = onSignOut;
        build();
        rebuildTree();
        openOverview();
    }

    public Region root() {
        return root;
    }

    /** Releases listeners and cancels in-flight work. Safe to call more than once. */
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        releaseView();
        disposeView = () -> { };
    }

    private void releaseView() {
        try {
            disposeView.run();
        } catch (RuntimeException e) {
            // A screen that fails to close must never block navigation, so the failure
            // is absorbed here rather than wrapped and rethrown.
            disposeView = () -> { };
        }
    }

    // ------------------------------------------------------------------ layout

    private void build() {
        root.getStyleClass().add("app-root");

        TextField search = new TextField();
        search.setId("module-search");
        search.setPromptText("Find a page");
        search.setPrefColumnCount(18);
        // Filtering rebuilds an in-memory tree, so typing never hits the API.
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            String next = newValue == null ? "" : newValue.trim();
            if (!next.equals(navFilter)) {
                navFilter = next;
                rebuildTree();
            }
        });

        // The root is only a container for the divisions, so it is hidden rather than
        // shown collapsed: a collapsed root hides every module behind a disclosure arrow
        // and leaves the sidebar looking empty until the user finds it.
        nav.setShowRoot(false);
        nav.getStyleClass().add("module-tree");
        nav.setId("module-tree");
        // One click (or arrow-key movement) opens the selected page. A selection
        // listener covers mouse, keyboard and programmatic selection alike, unlike
        // per-cell click handlers, which never fire when a harness selects a node.
        nav.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) -> {
            if (selected != null && selected.getValue() instanceof EntryItem entry) {
                open(entry);
            }
        });
        nav.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER
                    && nav.getSelectionModel().getSelectedItem() instanceof TreeItem<?> selected
                    && selected.getValue() instanceof EntryItem entry) {
                open(entry);
                event.consume();
            }
        });

        overviewButton.setId("overview-navigation");
        overviewButton.setGraphic(com.erp.desktop.view.Icons.of("overview"));
        overviewButton.setMaxWidth(Double.MAX_VALUE);
        overviewButton.getStyleClass().add("overview-navigation");
        VBox sidebar = new VBox(8, overviewButton, Ui.label("Modules", "sidebar-title"), search, nav, sidebarFooter());
        sidebar.setPadding(new Insets(16));
        sidebar.setPrefWidth(264);
        sidebar.setMinWidth(228);
        sidebar.getStyleClass().add("sidebar");
        VBox.setVgrow(nav, Priority.ALWAYS);
        root.setLeft(sidebar);

        VBox.setVgrow(pageHolder, Priority.ALWAYS);
        pageHolder.setId("page-holder");
        HBox.setHgrow(pageHolder, Priority.ALWAYS);

        root.setTop(buildTopBar());
        root.setCenter(pageHolder);
        installShortcuts(search);
    }

    /**
     * Application-level shortcuts. Bound on the root pane as a filter so they work
     * wherever focus happens to be in the workspace. Dialogs retain their own keys.
     *
     * <p>Ctrl+K, Ctrl+F, Ctrl+N and Ctrl+E mirror what a desktop user already expects;
     * F5 is the universal refresh key. Every one of them is also reachable by Tab,
     * because the underlying controls are ordinary focus-traversable nodes.
     */
    private void installShortcuts(TextField search) {
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            boolean ctrl = event.isControlDown();
            switch (event.getCode()) {
                case K -> {
                    if (ctrl) {
                        search.requestFocus();
                        search.selectAll();
                        event.consume();
                    }
                }
                case F -> {
                    if (ctrl && activeScreen != null) {
                        activeScreen.focusSearch();
                        event.consume();
                    }
                }
                case N -> {
                    if (ctrl && activeScreen != null) {
                        activeScreen.createRecord();
                        event.consume();
                    }
                }
                case E -> {
                    if (ctrl && activeScreen != null) {
                        activeScreen.editRecord();
                        event.consume();
                    }
                }
                case F5 -> {
                    if (activeScreen == null) {
                        openOverview();
                    } else {
                        activeScreen.refresh();
                    }
                    event.consume();
                }
                default -> { }
            }
        });
    }

    private Node sidebarFooter() {
        Label name = Ui.label("", "sidebar-session");
        name.setId("sidebar-session");
        name.setWrapText(true);
        if (api.isDemo()) {
            name.setText(api.companyName() + " (demo)");
        } else {
            String person = api.fullName().isBlank() ? api.username() : api.fullName();
            name.setText(person + (api.companyName().isBlank() ? "" : "\n" + api.companyName()));
        }
        VBox box = new VBox(2, Ui.label("Signed in", "muted"), name);
        box.getStyleClass().add("sidebar-footer");
        if (api.mustChangePassword()) {
            box.getChildren().add(passwordWarning());
        }
        return box;
    }

    /**
     * Shown when the account still uses a seeded or shared password. The seeded
     * administrator is flagged by migration V15, so this is the prompt that stops the
     * documented default credential from surviving.
     */
    private Node passwordWarning() {
        Label warning = Ui.label("This account still uses its initial password.", "password-warning");
        warning.setWrapText(true);
        Button change = Ui.subtle("Change password", this::changePassword);
        change.setMaxWidth(Double.MAX_VALUE);
        VBox box = new VBox(6, warning, change);
        box.getStyleClass().add("password-warning-box");
        return box;
    }

    private void changePassword() {
        Ui.onFxThread(() -> {
            if (disposed || busyBar.isVisible()) return;
            PasswordDialog dialog = new PasswordDialog();
            if (!dialog.show()) {
                return;
            }
            busyBar.setVisible(true);
            root.setDisable(true);
            Ui.async(() -> {
                api.changePassword(dialog.currentPassword(), dialog.newPassword());
                return null;
            }, ignored -> {
                if (disposed) return;
                root.setDisable(false);
                busyBar.setVisible(false);
                ConfirmDialog.notify("Password changed",
                        "Use the new password the next time you sign in.");
                onSignOut.run();
            }, error -> {
                if (disposed) return;
                root.setDisable(false);
                busyBar.setVisible(false);
                ConfirmDialog.notify("Password not changed", Ui.message(error));
            });
        });
    }

    private Node buildTopBar() {
        breadcrumb.setId("breadcrumb");
        HBox.setHgrow(breadcrumb, Priority.ALWAYS);

        var demoBadge = Ui.pill("Mode Demo", "demo");
        demoBadge.setId("demo-badge");
        demoBadge.setVisible(api.isDemo());

        demoActions.setVisible(api.isDemo());
        demoActions.setAlignment(Pos.CENTER_RIGHT);
        demoActions.getChildren().addAll(
                Ui.subtle("Reset Data Demo", this::confirmResetDemo),
                Ui.button("Keluar Demo", this::exitDemo));

        var signOut = Ui.subtle("Sign out", onSignOut);
        signOut.getStyleClass().add("sign-out");

        busyBar.setMaxWidth(90);
        busyBar.setVisible(false);
        busyBar.managedProperty().bind(busyBar.visibleProperty());
        busyBar.setId("top-progress");

        demoBadge.managedProperty().bind(demoBadge.visibleProperty());
        demoActions.managedProperty().bind(demoActions.visibleProperty());
        Button password = Ui.subtle("Change password", this::changePassword);
        password.setVisible(!api.isDemo()); password.managedProperty().bind(password.visibleProperty());
        FlowPane bar = new FlowPane(8, 8, demoBadge, breadcrumb, demoActions, busyBar, password, signOut);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setId("top-bar");
        bar.setPadding(new Insets(8, 16, 8, 24));
        return bar;
    }

    // ------------------------------------------------------------------ navigation

    /**
     * Rebuilds the tree from the catalog, keeping only entries matching the search box.
     *
     * <p>Rebuilding rather than hiding rows is deliberate: this JavaFX build has no
     * {@code TreeItem.setHidden}, and the catalog is small enough that a rebuild is
     * imperceptible while typing.
     */
    private void rebuildTree() {
        TreeItem<Object> treeRoot = new TreeItem<>("ERP");
        String needle = navFilter.toLowerCase();

        for (String module : Catalog.modules()) {
            String divisionLabel = Catalog.moduleLabel(module);
            boolean divisionMatches = needle.isEmpty() || containsIgnoreCase(divisionLabel, needle);
            List<EntryItem> pages = new ArrayList<>();
            for (Catalog.Entry entry : Catalog.entries(module)) {
                if (canSee(entry.path())
                        && (divisionMatches || containsIgnoreCase(entry.label(), needle))) {
                    pages.add(new EntryItem(entry));
                }
            }
            if (pages.isEmpty()) {
                continue;
            }
            TreeItem<Object> division = new TreeItem<>(divisionLabel);
            pages.forEach(page -> division.getChildren().add(new TreeItem<>(page)));
            division.setExpanded(true);
            treeRoot.getChildren().add(division);
        }
        nav.setRoot(treeRoot);

        nav.setCellFactory(view -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("module-leaf", "module-group");
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (item instanceof EntryItem entry) {
                    setText(entry.entry.label());
                    setGraphic(null);
                    getStyleClass().add("module-leaf");
                } else {
                    setText(String.valueOf(item));
                    setGraphic(com.erp.desktop.view.Icons.of("folder"));
                    getStyleClass().add("module-group");
                }
            }
        });
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        return haystack.toLowerCase().contains(needle);
    }

    private boolean canSee(String path) {
        return api.roles().contains("ADMIN") || !ADMIN_ONLY.contains(path);
    }

    // ------------------------------------------------------------------ screens

    private void openOverview() {
        if (disposed) return;
        releaseView();
        activePath = "";
        nav.getSelectionModel().clearSelection();
        if (!overviewButton.getStyleClass().contains("active")) overviewButton.getStyleClass().add("active");
        breadcrumb.setText("Overview");
        OverviewView view = new OverviewView(api, this::openPath);
        disposeView = view::dispose;
        activeScreen = new com.erp.desktop.view.ActiveScreen() {
            @Override public void refresh() { view.refresh(); }

            @Override public void focusSearch() { }

            @Override public void createRecord() { }

            @Override public void editRecord() { }
        };
        pageHolder.getChildren().setAll(view.root());
        VBox.setVgrow(view.root(), Priority.ALWAYS);
        fade();
    }

    private void open(EntryItem item) {
        if (disposed || item.entry.path().equals(activePath)) {
            return;
        }
        activePath = item.entry.path();
        overviewButton.getStyleClass().remove("active");
        breadcrumb.setText(item.entry.label());
        releaseView();
        EntityView view = new EntityView(api, item.entry);
        disposeView = view::dispose;
        activeScreen = view;
        pageHolder.getChildren().setAll(view.root());
        VBox.setVgrow(view.root(), Priority.ALWAYS);
        fade();
    }

    private void openPath(String path) {
        if (!canSee(path)) return;
        for (var division : nav.getRoot().getChildren()) for (var leaf : division.getChildren()) {
            if (leaf.getValue() instanceof EntryItem item && item.entry.path().equals(path)) {
                nav.getSelectionModel().select(leaf); nav.scrollTo(nav.getRow(leaf)); return;
            }
        }
        for (String module : Catalog.modules()) for (var entry : Catalog.entries(module))
            if (entry.path().equals(path)) { open(new EntryItem(entry)); return; }
    }

    private void fade() {
        if (!pageHolder.getChildren().isEmpty()) {
            Node content = pageHolder.getChildren().get(0);
            FadeTransition fade = new FadeTransition(Duration.millis(140), content);
            fade.setFromValue(0.4);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    private record EntryItem(Catalog.Entry entry) {
        @Override
        public String toString() {
            return entry.label();
        }
    }

    // ------------------------------------------------------------------ demo actions

    private void confirmResetDemo() {
        Ui.onFxThread(() -> {
            if (disposed || busyBar.isVisible()) return;
            if (!ConfirmDialog.confirm(
                    "Reset Data Demo?",
                    "Everything created or changed in the demo tenant is deleted and the original "
                            + "sample dataset is restored. This cannot be undone.",
                    "Reset")) {
                return;
            }
            busyBar.setVisible(true);
            root.setDisable(true);
            releaseView();
            Ui.async(api::resetDemo,
                    summary -> {
                        if (disposed) return;
                        root.setDisable(false);
                        busyBar.setVisible(false);
                        activePath = "";
                        openOverview();
                        ConfirmDialog.notify("Data demo direset",
                                "The demo workspace is back at its starting point: "
                                        + summary.path("customers").asInt(0) + " customers, "
                                        + summary.path("orders").asInt(0) + " orders and "
                                        + summary.path("invoices").asInt(0) + " invoices.");
                    },
                    error -> {
                        if (disposed) return;
                        root.setDisable(false);
                        busyBar.setVisible(false);
                        openOverview();
                        ConfirmDialog.notify("Reset failed", Ui.message(error));
                    });
        });
    }

    private void exitDemo() {
        Ui.onFxThread(() -> {
            if (disposed || busyBar.isVisible()) return;
            if (!ConfirmDialog.confirm(
                    "Keluar dari workspace demo?",
                    "The demo session ends and you return to the sign-in screen. Your changes stay "
                            + "in the demo tenant and can be reset at any time.",
                    "Keluar")) {
                return;
            }
            busyBar.setVisible(true);
            root.setDisable(true);
            Ui.async(() -> {
                api.exitDemo();
                return null;
            }, ignored -> { if (!disposed) onSignOut.run(); }, error -> { if (!disposed) onSignOut.run(); });
        });
    }
}
