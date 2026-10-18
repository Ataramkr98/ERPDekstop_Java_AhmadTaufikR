package com.erp.desktop.view;

import java.util.Optional;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.stage.Window;

/**
 * Modal confirmation and notice dialogs.
 *
 * <p>Used for destructive actions (reset demo, leave demo) and for the short
 * confirmations that a write succeeded. Escape cancels, and initial focus is placed on
 * the safe button so a stray Return cannot trigger a destructive action blind.
 */
public final class ConfirmDialog {

    private ConfirmDialog() {
    }

    /** Returns true only when the user explicitly confirmed. */
    public static boolean confirm(String title, String detail, String acceptLabel) {
        return ask(title, detail, acceptLabel, true)
                .filter(choice -> choice.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                .isPresent();
    }

    /** Shows a dismissible notice with a single Close button. */
    public static void notify(String title, String message) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.initOwner(owner());
        dialog.setResizable(true);
        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(close);
        Label body = Ui.label(message, "dialog-body");
        body.setWrapText(true);
        body.setMaxWidth(460);
        body.setPrefWidth(460);
        body.setMinWidth(0);
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().setContent(body);
        ((Button) dialog.getDialogPane().lookupButton(close)).getStyleClass().add("primary");
        Ui.theme(dialog.getDialogPane());
        dialog.showAndWait();
    }

    private static Optional<ButtonType> ask(String title, String detail, String acceptLabel, boolean destructive) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.initOwner(owner());
        dialog.setResizable(true);

        ButtonType accept = new ButtonType(acceptLabel, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(accept, cancel);

        Label body = Ui.label(detail, "dialog-body");
        body.setWrapText(true);
        body.setMaxWidth(460);
        body.setPrefWidth(460);
        body.setMinWidth(0);
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().setContent(body);

        var acceptButton = (Button) dialog.getDialogPane().lookupButton(accept);
        acceptButton.getStyleClass().add(destructive ? "danger" : "primary");
        // Focus the safe option so Return cannot confirm a destructive action blind.
        dialog.setOnShown(event -> dialog.getDialogPane().lookupButton(cancel).requestFocus());

        Ui.theme(dialog.getDialogPane());
        return dialog.showAndWait();
    }

    private static Window owner() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }
}
