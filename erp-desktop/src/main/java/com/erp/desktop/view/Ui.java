package com.erp.desktop.view;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.erp.desktop.api.ApiClient.ApiCallException;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/** Layout, typography and thread-offload helpers shared by every screen. */
public final class Ui {

    private static final String STYLESHEET = "/styles/app.css";

    /**
     * Blocking work runs here, never on the JavaFX Application Thread. JavaFX's own
     * {@code Task} performs the hop back to the FX thread for the handlers, so callers
     * only need {@link #async(Callable)} plus the usual succeeded/failed callbacks.
     */
    private static final ExecutorService WORKERS = Executors.newVirtualThreadPerTaskExecutor();

    private Ui() {
    }

    public static <T> javafx.concurrent.Task<T> async(Callable<T> work) {
        javafx.concurrent.Task<T> task = new javafx.concurrent.Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        WORKERS.execute(task);
        return task;
    }

    /**
     * Runs work off the FX thread and invokes both callbacks on the FX thread.
     *
     * <p>Handlers are installed before submission. Views may cancel reads when closed.
     */
    public static <T> javafx.concurrent.Task<T> async(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        javafx.concurrent.Task<T> task = new javafx.concurrent.Task<>() {
            @Override protected T call() throws Exception { return work.call(); }
        };
        task.setOnSucceeded(done -> onSuccess.accept(task.getValue()));
        task.setOnFailed(done -> onFailure.accept(task.getException()));
        WORKERS.execute(task);
        return task;
    }

    /** Turns any failure into something worth showing a user. */
    public static String message(Throwable error) {
        if (error instanceof ApiCallException call && call.friendlyMessage != null) {
            return call.friendlyMessage;
        }
        if (error instanceof InterruptedException) {
            return "The request was interrupted.";
        }
        String text = error == null ? "" : String.valueOf(error.getMessage());
        return text == null || text.isBlank()
                ? "Something went wrong while talking to the server."
                : text;
    }

    public static void shutdown() {
        WORKERS.shutdownNow();
    }

    /**
     * Attach the application stylesheet once per subtree. Only {@link Parent} carries a
     * stylesheet list, so dialog panes and layouts are the supported targets.
     */
    public static void theme(Parent node) {
        if (node != null && !node.getStylesheets().contains(STYLESHEET)) {
            node.getStylesheets().add(STYLESHEET);
        }
    }

    // ------------------------------------------------------------------ structure

    public static VBox column(Node... children) {
        VBox box = new VBox(8, children);
        box.setAlignment(Pos.TOP_LEFT);
        return box;
    }

    public static HBox row(Node... children) {
        HBox box = new HBox(8, children);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /** Stretches a region in whichever layout box contains it. */
    public static <T extends Region> T grow(T region) {
        HBox.setHgrow(region, Priority.ALWAYS);
        VBox.setVgrow(region, Priority.ALWAYS);
        return region;
    }

    public static Insets pad(double top, double right, double bottom, double left) {
        return new Insets(top, right, bottom, left);
    }

    public static Insets padAll(double value) {
        return new Insets(value);
    }

    // ------------------------------------------------------------------ typography

    public static Label label(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    /** Secondary text: captions, helper lines, empty-state detail. */
    public static Label muted(String text) {
        return label(text, "muted");
    }

    public static Label title(String text) {
        return label(text, "title");
    }

    /** Status-strip text; wraps so a long server error stays readable. */
    public static Label status(String text) {
        Label label = label(text, "status-label");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    public static Label pill(String text, String styleClass) {
        Label label = label(text, "pill");
        label.getStyleClass().add(styleClass);
        return label;
    }

    // ------------------------------------------------------------------ controls

    public static Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(event -> action.run());
        return button;
    }

    /** Primary call to action: at most one per screen. */
    public static Button primary(String text, Runnable action) {
        Button button = button(text, action);
        button.getStyleClass().add("primary");
        return button;
    }

    public static Button subtle(String text, Runnable action) {
        Button button = button(text, action);
        button.getStyleClass().add("subtle");
        return button;
    }

    public static Button danger(String text, Runnable action) {
        Button button = button(text, action);
        button.getStyleClass().add("danger");
        return button;
    }

    public static ToggleButton toggle(String text, boolean selected) {
        ToggleButton button = new ToggleButton(text);
        button.setSelected(selected);
        return button;
    }

    // ------------------------------------------------------------------ feedback

    /** Shown when a table has no rows: says why it is empty and what to do next. */
    public static Node emptyState(String heading, String detail) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(padAll(48));
        box.getStyleClass().add("empty-state");
        Text title = new Text(heading);
        title.getStyleClass().add("empty-state-title");
        box.getChildren().add(title);
        if (detail != null && !detail.isBlank()) {
            Label hint = muted(detail);
            hint.setWrapText(true);
            hint.setMaxWidth(460);
            hint.setAlignment(Pos.CENTER);
            box.getChildren().add(hint);
        }
        return box;
    }

    /** Shown when the first load failed: says what broke and offers a retry. */
    public static Node errorState(String message, Runnable retry) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(padAll(36));
        box.getStyleClass().add("error-state");
        box.getChildren().add(label("This screen could not load", "empty-state-title"));
        Label detail = label(message == null || message.isBlank() ? "Unknown error." : message, "error-detail");
        detail.setWrapText(true);
        detail.setMaxWidth(560);
        detail.setAlignment(Pos.CENTER);
        box.getChildren().add(detail);
        if (retry != null) {
            box.getChildren().add(primary("Try again", retry));
        }
        return box;
    }

    /** Titled panel used by the dashboard and the forms. */
    public static VBox panel(String heading, Node content) {
        VBox box = new VBox(8, label(heading, "panel-title"), content);
        box.getStyleClass().add("panel");
        return box;
    }

    // ------------------------------------------------------------------ threading

    /** Runs on the FX thread whether or not the caller is already there. */
    public static void onFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
