package com.erp.desktop.view;

/**
 * The operations the window-level keyboard shortcuts need from whichever screen is
 * active. Keeping it a tiny interface lets {@code MainWindow} stay ignorant of the
 * concrete screen types and keeps the shortcut bindings in one place.
 */
public interface ActiveScreen {

    /** F5 - reload the current screen from the server. */
    void refresh();

    /** Ctrl+F - move focus to the screen's search box. */
    void focusSearch();

    /** Ctrl+N - open the create form. */
    void createRecord();

    /** Ctrl+E - open the selected record for editing. */
    void editRecord();
}