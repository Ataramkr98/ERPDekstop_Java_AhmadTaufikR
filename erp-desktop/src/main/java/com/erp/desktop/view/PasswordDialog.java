package com.erp.desktop.view;

import java.util.Optional;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

/**
 * Asks for the current and replacement password.
 *
 * <p>Used when an account is still on its initial password. The two fields are
 * validated locally for the obvious mistakes (empty, too short, mismatched) so the
 * round trip is not wasted, and the server remains the authority.
 */
public final class PasswordDialog {

    private String currentPassword = "";
    private String newPassword = "";

    /** Returns true when the user submitted a complete, self-consistent pair. */
    public boolean show() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change password");
        dialog.setHeaderText(null);
        dialog.initOwner(owner());
        dialog.setResizable(false);

        ButtonType apply = new ButtonType("Change password", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(apply, cancel);

        PasswordField current = new PasswordField();
        current.setPromptText("Current password");
        current.setId("current-password");
        PasswordField replacement = new PasswordField();
        replacement.setPromptText("At least 8 characters");
        replacement.setId("new-password");
        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Repeat the new password");
        confirm.setId("confirm-password");

        Label error = Ui.label("", "form-error");
        error.setWrapText(true);
        error.setMaxWidth(420);
        error.setVisible(false);

        current.setOnAction(event -> replacement.requestFocus());
        replacement.setOnAction(event -> confirm.requestFocus());

        VBox body = new VBox(10,
                Ui.label("Current password", "field-label"), current,
                Ui.label("New password", "field-label"), replacement,
                Ui.label("Confirm new password", "field-label"), confirm,
                error);
        body.setPadding(Ui.padAll(8));
        dialog.getDialogPane().setContent(body);
        dialog.getDialogPane().getButtonTypes().setAll(apply, cancel);

        ((Button) dialog.getDialogPane().lookupButton(apply)).getStyleClass().add("primary");

        // Cancel keeps focus so Return cannot submit a half-filled form.
        dialog.getDialogPane().lookupButton(cancel).requestFocus();
        Ui.theme(dialog.getDialogPane());

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != apply) {
            return false;
        }
        return validate(error, current.getText(), replacement.getText(), confirm.getText(), dialog);
    }

    private boolean validate(Label error, String current, String next, String confirm, Dialog<ButtonType> dialog) {
        if (current == null || current.isBlank()) {
            error.setText("Enter your current password.");
            error.setVisible(true);
            return false;
        }
        if (next == null || next.length() < 8) {
            error.setText("Use a new password with at least 8 characters.");
            error.setVisible(true);
            return false;
        }
        if (!next.equals(confirm)) {
            error.setText("The two new passwords do not match.");
            error.setVisible(true);
            return false;
        }
        currentPassword = current;
        newPassword = next;
        return true;
    }

    public String currentPassword() {
        return currentPassword;
    }

    public String newPassword() {
        return newPassword;
    }

    private static javafx.stage.Window owner() {
        return javafx.stage.Window.getWindows().stream()
                .filter(javafx.stage.Window::isShowing)
                .findFirst()
                .orElse(null);
    }
}