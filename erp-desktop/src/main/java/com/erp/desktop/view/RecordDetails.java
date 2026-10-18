package com.erp.desktop.view;

import java.util.ArrayList;
import java.util.List;
import com.erp.desktop.Catalog;
import com.erp.desktop.api.EntityMeta;
import com.erp.desktop.api.Values;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Window;

/** Readable details and a paginated printable summary, including generated records. */
public final class RecordDetails {
    private RecordDetails() { }
    public static void show(Catalog.Entry entry, EntityMeta meta, JsonNode record, Window owner) {
        Dialog<Void> dialog = new Dialog<>();
        if (owner != null) dialog.initOwner(owner);
        dialog.setTitle(entry.label() + " · #" + record.path("id").asText());
        dialog.setHeaderText(ReferencePicker.describe(record)); dialog.setResizable(true);
        Ui.theme(dialog.getDialogPane());
        GridPane grid = new GridPane(); grid.setHgap(24); grid.setVgap(16);
        ColumnConstraints key = new ColumnConstraints(170);
        ColumnConstraints value = new ColumnConstraints(); value.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(key, value);
        int index = 0;
        for (EntityMeta.Field field : meta.fields()) {
            if (field.kind == EntityMeta.Kind.PASSWORD) continue;
            Label text = new Label(Values.display(record, field.name));
            text.setWrapText(true); text.setMaxWidth(Double.MAX_VALUE); text.setMinWidth(0);
            grid.add(Ui.muted(field.label), 0, index); grid.add(text, 1, index++);
        }
        ScrollPane scroll = new ScrollPane(grid); scroll.setFitToWidth(true); scroll.setPrefViewportHeight(440);
        Button print = Ui.button("Print summary…", () -> print(entry, meta, record, owner));
        dialog.getDialogPane().setContent(new VBox(16, print, scroll));
        dialog.getDialogPane().setPrefWidth(700); dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }

    public static void print(Catalog.Entry entry, EntityMeta meta, JsonNode record, Window owner) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) { ConfirmDialog.notify("Printer unavailable", "Install or select a system printer, including a PDF printer, then try again."); return; }
        if (!job.showPrintDialog(owner)) return;
        List<String> lines = new ArrayList<>();
        lines.add(entry.label() + " · " + ReferencePicker.describe(record)); lines.add("");
        for (EntityMeta.Field field : meta.fields()) {
            if (field.kind == EntityMeta.Kind.PASSWORD) continue;
            String value = field.label + ": " + Values.display(record, field.name);
            for (String paragraph : value.split("\\R", -1)) {
                if (paragraph.isEmpty()) lines.add("");
                for (int offset = 0; offset < paragraph.length(); offset += 80)
                    lines.add(paragraph.substring(offset, Math.min(offset + 80, paragraph.length())));
            }
        }
        double height = job.getJobSettings().getPageLayout().getPrintableHeight();
        int pageLines = Math.max(10, (int)((height - 40) / 15));
        boolean printed = true;
        for (int start = 0, page = 1; start < lines.size(); start += pageLines, page++) {
            VBox sheet = new VBox(0); sheet.setPrefWidth(job.getJobSettings().getPageLayout().getPrintableWidth());
            for (String line : lines.subList(start, Math.min(start + pageLines, lines.size()))) {
                Text text = new Text(line.isBlank() ? " " : line); text.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 10px;");
                TextFlow flow = new TextFlow(text); flow.setMinHeight(15); sheet.getChildren().add(flow);
            }
            sheet.getChildren().add(new Label("Page " + page));
            if (!job.printPage(sheet)) { printed = false; break; }
        }
        if (printed && job.endJob()) ConfirmDialog.notify("Summary sent", "The record summary was sent to your selected printer.");
        else { job.cancelJob(); ConfirmDialog.notify("Print incomplete", "The printer could not finish the summary. Check the print queue and try again."); }
    }
}
