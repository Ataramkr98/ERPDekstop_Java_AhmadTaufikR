package com.erp.desktop;

import java.util.concurrent.atomic.AtomicBoolean;

import com.erp.desktop.api.ApiClient;
import com.erp.desktop.api.ApiClient.ApiCallException;
import com.erp.desktop.view.Ui;

import io.quarkus.runtime.Quarkus;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Single-JVM shell: the window is JavaFX, the API is Quarkus in the same JVM.
 *
 * <p>The shell owns three screens - connecting, sign-in (normal or demo) and the main
 * window - and is the only place that swaps the scene. All data loading happens on a
 * worker thread through {@link Ui#async}; nothing here touches the network on the FX
 * thread.
 */
public class DesktopApp extends Application {

    static final int DEFAULT_PORT = 8080;

    private static final AtomicBoolean QUARKUS_STARTED = new AtomicBoolean();

    private final ApiClient api = new ApiClient(port());
    private Stage stage;
    private final AtomicBoolean busy = new AtomicBoolean();
    /**
     * Set once the shell is shutting down.
     *
     * <p>The connection probe is asynchronous, so its result can arrive after {@link #stop()}
     * has already shut the worker pool down. Acting on it then tried to start Quarkus on a
     * dead executor and printed a {@code RejectedExecutionException} on the way out; every
     * late callback checks this flag first instead.
     */
    private final AtomicBoolean shuttingDown = new AtomicBoolean();

    private Parent signInRoot;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label signInError;
    private Label signInStatus;
    private Button signInButton;
    private Button demoButton;
    private Button reloadButton;
    private ProgressBar progress;

    private MainWindow mainWindow;
    private boolean bootAttempted;
    private long bootDeadline;
    private final javafx.animation.PauseTransition startupRetry = new javafx.animation.PauseTransition(Duration.seconds(2));

    @Override
    public void start(Stage stage) {
        // The shell controls shutdown itself, so closing the window must not tear the
        // toolkit down while Quarkus is still serving requests.
        Platform.setImplicitExit(false);
        this.stage = stage;
        this.stage.setTitle("ERP Desktop");
        this.stage.setMinWidth(620);
        this.stage.setMinHeight(520);

        api.setSessionExpiredHandler(this::onSessionExpired);
        buildSignIn();
        stage.setScene(scene(signInRoot, 940, 660));
        stage.show();
        probe();
    }

    // ------------------------------------------------------------------ connecting

    /** Checks the service, then asks it whether this build offers the demo. */
    private void probe() {
        status("Contacting the ERP service...");
        progress.setVisible(true);
        Ui.async(() -> api.isUp(), up -> {
            if (Boolean.TRUE.equals(up)) {
                offerDemo();
            } else {
                // Nothing is serving the API yet: start the embedded instance rather
                // than showing a dead-end error.
                bootQuarkus();
            }
        }, error -> Ui.onFxThread(this::bootQuarkus));
    }

    private void serviceUnreachable() {
        progress.setVisible(false);
        error("The ERP service is not reachable at " + api.baseUrl() + ".");
        status("Start it with run-desktop.cmd (single window) or run-backend.cmd, then choose Reload.");
        reloadButton.setVisible(true);
        signInButton.setDisable(true);
        demoButton.setVisible(false);
    }

    /** Reveals the demo button only when the backend really offers demo mode. */
    private void offerDemo() {
        if (shuttingDown.get()) {
            return;
        }
        Ui.async(() -> api.demoAvailable(), offered -> {
            progress.setVisible(false);
            boolean demo = Boolean.TRUE.equals(offered);
            demoButton.setVisible(demo);
            signInButton.setDisable(false);
            status(demo
                    ? "Sign in, or explore a synthetic dataset in an isolated demo tenant."
                    : "Sign in to continue.");
        }, error -> {
            progress.setVisible(false);
            signInButton.setDisable(false);
            status("Sign in to continue.");
        });
    }

    /**
     * Boots the embedded Quarkus instance once, off the FX thread. When something is
     * already serving the API (server mode, screenshot harness) Quarkus is never
     * started and this app is a pure client.
     */
    private void bootQuarkus() {
        if (QUARKUS_STARTED.get() || bootAttempted || shuttingDown.get()) {
            return;
        }
        bootAttempted = true;
        bootDeadline = System.nanoTime() + java.util.concurrent.TimeUnit.MINUTES.toNanos(3);
        status("Starting the ERP service...");
        // Quarkus reports started before the REST layer accepts connections, so the
        // flag is optimistic and waitForApi() confirms the port really answers.
        QUARKUS_STARTED.set(true);
        // The service owns a lifecycle thread. Cancelling UI request workers on
        // exit must not interrupt Quarkus while it drains active transactions.
        Thread.ofPlatform().name("erp-embedded-runtime").start(() -> {
            try {
                if (shuttingDown.get()) return;
                Quarkus.run(EmbeddedRuntime.class, (exitCode, failure) -> {
                    if (failure != null) throw new IllegalStateException("The ERP service could not start.", failure);
                    if (exitCode != 0) throw new IllegalStateException("The ERP service exited with code " + exitCode + ".");
                });
            } catch (Throwable failure) {
                System.getLogger(DesktopApp.class.getName()).log(System.Logger.Level.ERROR, "Embedded ERP startup failed.", failure);
                if (!shuttingDown.get()) Ui.onFxThread(() -> {
                    if (!shuttingDown.get()) { startupRetry.stop(); serviceUnreachable(); }
                });
            } finally {
                QUARKUS_STARTED.set(false);
            }
        });
        // Quarkus.run blocks for the service's entire lifetime. Readiness must be
        // probed concurrently, not from its completion callback.
        waitForApi();
    }

    /** Quarkus reports started before the REST layer accepts connections, so retry. */
    private void waitForApi() {
        if (shuttingDown.get()) {
            return;
        }
        status("Starting the ERP service...");
        Ui.async(() -> api.isUp(), up -> {
            if (shuttingDown.get()) return;
            if (Boolean.TRUE.equals(up)) {
                offerDemo();
            } else if (QUARKUS_STARTED.get() && System.nanoTime() < bootDeadline) {
                startupRetry.setOnFinished(event -> waitForApi());
                startupRetry.playFromStart();
            } else {
                serviceUnreachable();
            }
        }, error -> Ui.onFxThread(this::serviceUnreachable));
    }

    // ------------------------------------------------------------------ sign-in screen

    private void buildSignIn() {
        usernameField = new TextField();
        usernameField.setId("username");
        usernameField.setPromptText("admin");
        passwordField = new PasswordField();
        passwordField.setId("password");
        usernameField.setOnAction(event -> passwordField.requestFocus());
        passwordField.setOnAction(event -> signIn());

        signInError = Ui.label("", "form-error");
        signInError.setWrapText(true);
        signInError.setMaxWidth(Double.MAX_VALUE);
        signInError.setVisible(false);

        signInStatus = Ui.muted("Contacting the ERP service...");
        signInStatus.setWrapText(true);
        signInStatus.setMaxWidth(380);

        signInButton = Ui.primary("Sign in", this::signIn);
        signInButton.setMaxWidth(Double.MAX_VALUE);
        signInButton.setDisable(true);

        demoButton = Ui.button("Coba Demo", this::enterDemo);
        demoButton.getStyleClass().add("demo");
        demoButton.setMaxWidth(Double.MAX_VALUE);
        demoButton.setVisible(false);
        demoButton.setTooltip(new javafx.scene.control.Tooltip(
                "Opens the demo workspace without a password. Synthetic data in an isolated tenant."));

        reloadButton = Ui.subtle("Reload", this::reload);
        reloadButton.setVisible(false);

        progress = new ProgressBar();
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.setVisible(false);

        VBox form = Ui.column(
                Ui.label("Username", "field-label"), usernameField,
                Ui.label("Password", "field-label"), passwordField,
                signInButton,
                divider(),
                demoButton,
                progress,
                reloadButton,
                signInError,
                signInStatus);
        form.setMaxWidth(Double.MAX_VALUE);
        form.getStyleClass().add("form");

        VBox content = Ui.column(
                Ui.label("ERP Desktop", "auth-title"),
                Ui.label("Operations, finance and inventory in one workspace", "auth-subtitle"),
                form);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(440);

        StackPane root = new StackPane(content);
        root.setPadding(Ui.padAll(28));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("auth-root");
        signInRoot = root;
    }

    private static Region divider() {
        Region line = new Region();
        line.setMinHeight(1);
        line.setPrefHeight(1);
        line.setMaxHeight(1);
        line.getStyleClass().add("divider");
        return line;
    }

    private void reload() {
        reloadButton.setVisible(false);
        signInError.setVisible(false);
        signInButton.setDisable(true);
        demoButton.setVisible(false);
        bootAttempted = false;
        status("Contacting the ERP service...");
        progress.setVisible(true);
        Ui.async(() -> api.isUp(), up -> {
            if (Boolean.TRUE.equals(up)) {
                offerDemo();
            } else {
                bootQuarkus();
            }
        }, error -> Ui.onFxThread(this::bootQuarkus));
    }

    // ------------------------------------------------------------------ actions

    private void signIn() {
        if (!busy.compareAndSet(false, true)) {
            return;
        }
        String user = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        if (user.isEmpty() || password.isEmpty()) {
            busy.set(false);
            error("Enter both a username and a password.");
            return;
        }
        setBusy(true, "Signing in...");
        Ui.async(() -> {
            api.login(user, password);
            return null;
        }, ignored -> {
            busy.set(false);
            openMainWindow();
        }, error -> {
            busy.set(false);
            setBusy(false, "Sign-in failed. Check the credentials and try again.");
            error(error instanceof ApiCallException call && call.friendlyMessage != null
                    ? call.friendlyMessage
                    : "Sign-in failed unexpectedly. Check the backend log.");
            passwordField.clear();
            passwordField.requestFocus();
        });
    }

    private void enterDemo() {
        if (!busy.compareAndSet(false, true)) {
            return;
        }
        setBusy(true, "Preparing the demo workspace...");
        Ui.async(() -> {
            api.startDemo();
            return null;
        }, ignored -> {
            busy.set(false);
            openMainWindow();
        }, error -> {
            busy.set(false);
            setBusy(false, "The demo workspace could not be prepared.");
            error(error instanceof ApiCallException call && call.friendlyMessage != null
                    ? call.friendlyMessage
                    : "The demo workspace could not be prepared.");
        });
    }

    private void setBusy(boolean value, String message) {
        progress.setVisible(value);
        signInButton.setDisable(value);
        demoButton.setDisable(value);
        reloadButton.setDisable(value);
        if (message != null && !message.isBlank()) {
            status(message);
        }
    }

    private void status(String message) {
        signInStatus.setText(message);
    }

    private void error(String message) {
        signInError.setText(message);
        signInError.setVisible(true);
    }

    // ------------------------------------------------------------------ main window

    private void openMainWindow() {
        if (mainWindow != null) {
            mainWindow.dispose();
        }
        mainWindow = new MainWindow(api, () -> signOut(null));
        Region root = mainWindow.root();
        stage.setMinWidth(1020);
        stage.setMinHeight(640);
        stage.setTitle("ERP Desktop - " + (api.isDemo() ? "Demo workspace" : api.username()));
        stage.setScene(scene(root, 1280, 800));

        FadeTransition fade = new FadeTransition(Duration.millis(160), root);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    /** Leaves the current session and returns to the sign-in screen. */
    void signOut(String message) {
        Ui.onFxThread(() -> {
            if (mainWindow != null) {
                mainWindow.dispose();
                mainWindow = null;
            }
            api.clearSession();
            buildSignIn();
            stage.setMinWidth(620);
            stage.setMinHeight(520);
            stage.setTitle("ERP Desktop");
            stage.setScene(scene(signInRoot, 940, 660));
            if (message != null && !message.isBlank()) {
                error(message);
            }
            stage.show();
            probe();
        });
    }

    private void onSessionExpired(String message) {
        signOut(message);
    }

    private Scene scene(Parent root, double width, double height) {
        Ui.theme(root);
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add("/styles/app.css");
        return scene;
    }

    @Override
    public void stop() {
        // Set before anything is torn down: the connection probe is in flight and its
        // callback must not try to start Quarkus on a pool that is about to disappear.
        shuttingDown.set(true);
        startupRetry.stop();
        if (mainWindow != null) {
            mainWindow.dispose();
        }
        if (QUARKUS_STARTED.get()) {
            Quarkus.asyncExit();
        }
        Ui.shutdown();
    }

    static int port() {
        String override = System.getProperty("erp.port", System.getenv("ERP_PORT"));
        if (override != null && !override.isBlank()) {
            try {
                return Integer.parseInt(override.trim());
            } catch (NumberFormatException ignored) {
                // Fall back to the default rather than failing on a bad override.
            }
        }
        return DEFAULT_PORT;
    }
}
