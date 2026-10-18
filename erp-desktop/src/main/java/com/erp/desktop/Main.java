package com.erp.desktop;

import javafx.application.Application;

/**
 * Process entry point.
 *
 * <p>This class deliberately does NOT extend {@link Application}. When the JavaFX
 * runtime is on the classpath rather than the module path, launching from a class
 * that extends Application makes the launcher bail out with "JavaFX runtime
 * components are missing". Keeping the launcher separate avoids that entirely,
 * which also matters once jpackage bundles the app.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        DesktopEnvironment.configure();
        Application.launch(DesktopApp.class, args);
    }
}
