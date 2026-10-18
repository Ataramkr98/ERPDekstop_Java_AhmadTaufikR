package com.erp.desktop.view;

import javafx.scene.Node;
import javafx.scene.shape.SVGPath;
import javafx.scene.layout.StackPane;

/** Small monochrome vectors, using one stroke and one 16px coordinate system. */
public final class Icons {
    private Icons() { }
    public static Node of(String name) {
        SVGPath path = new SVGPath();
        path.setContent(switch (name) {
            case "overview" -> "M2 2 H7 V7 H2 Z M10 2 H15 V7 H10 Z M2 10 H7 V15 H2 Z M10 10 H15 V15 H10 Z";
            case "add" -> "M8 2 V14 M2 8 H14";
            case "document" -> "M4 1 H10 L14 5 V15 H4 Z M10 1 V5 H14 M6 8 H12 M6 11 H12";
            default -> "M2 3 H6 L8 5 H15 V14 H2 Z";
        });
        path.getStyleClass().add("icon-path");
        path.setMouseTransparent(true);
        StackPane box = new StackPane(path);
        box.setMinSize(16, 16); box.setPrefSize(16, 16); box.setMaxSize(16, 16);
        box.setMouseTransparent(true);
        return box;
    }
}
