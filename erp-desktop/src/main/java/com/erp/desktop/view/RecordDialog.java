package com.erp.desktop.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import com.erp.desktop.Catalog;
import com.erp.desktop.api.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Validated, scrollable form with one in-flight save and optimistic locking. */
public class RecordDialog extends Dialog<JsonNode> {
    private final ApiClient api;
    private final Catalog.Entry entry;
    private final EntityMeta meta;
    private final String id;
    private final JsonNode existing;
    private final Map<String, Supplier<String>> readers = new LinkedHashMap<>();
    private final Map<String, javafx.scene.Node> inputs = new LinkedHashMap<>();
    private GridPane form;
    private final Label error = Ui.label("", "error");
    private final Label state = Ui.label("Required fields are marked with *", "muted");
    private final ButtonType saveType = new ButtonType("Save record", ButtonBar.ButtonData.OK_DONE);
    private boolean saving;

    public RecordDialog(ApiClient api, Catalog.Entry entry, EntityMeta meta, String id, JsonNode existing) {
        this.api = api; this.entry = entry; this.meta = meta; this.id = id; this.existing = existing;
        setTitle((id == null ? "New " : "Edit ") + entry.label());
        setHeaderText(id == null ? "New record" : entry.label() + " · #" + id);
        setResizable(true);
        Ui.theme(getDialogPane());
        GridPane grid = form(); form = grid;
        ScrollPane scroll = new ScrollPane(grid); scroll.setFitToWidth(true); scroll.setPrefViewportHeight(Math.min(480, readers.size() * 60 + 24));
        error.setWrapText(true); error.setMaxWidth(520);
        VBox body = new VBox(12, state, scroll, error); body.setPadding(new Insets(8));
        getDialogPane().setContent(body); getDialogPane().setPrefWidth(660);
        getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        getDialogPane().lookupButton(saveType).getStyleClass().add("primary");
        getDialogPane().lookupButton(saveType).addEventFilter(javafx.event.ActionEvent.ACTION, e -> { e.consume(); save(); });
        setOnCloseRequest(e -> { if (saving) e.consume(); });
        setResultConverter(button -> null);
    }

    private GridPane form() {
        GridPane grid = new GridPane(); grid.setHgap(16); grid.setVgap(16); grid.setPadding(new Insets(8, 16, 8, 0));
        ColumnConstraints labels = new ColumnConstraints(170);
        ColumnConstraints values = new ColumnConstraints(); values.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labels, values);
        int row = 0;
        for (EntityMeta.Field field : meta.fields()) {
            if (field.isHiddenInForm()) continue;
            String initial = existing == null ? field.defaultValue : Values.asText(existing, field.name);
            javafx.scene.layout.Region control;
            if (!field.referencePath.isBlank()) {
                TextField input = new TextField(initial);
                input.setPromptText("Related record ID");
                input.setId("field-" + field.name);
                Label chosen = Ui.muted(initial.isBlank() ? "Select from " + field.referenceLabel : "Related record #" + initial);
                chosen.setWrapText(true);
                input.textProperty().addListener((observable, old, value) -> {
                    chosen.setText(value.isBlank() ? "Select from " + field.referenceLabel : "Related record #" + value);
                    input.setTooltip(null);
                });
                Button choose = Ui.button("Choose…", () -> {
                    ReferencePicker picker = new ReferencePicker(api, field, getDialogPane().getScene().getWindow());
                    picker.showAndWait().ifPresent(record -> {
                        input.setText(record.path("id").asText());
                        chosen.setText(ReferencePicker.describe(record));
                        input.setTooltip(new Tooltip(ReferencePicker.describe(record)));
                    });
                });
                readers.put(field.name, input::getText);
                HBox reference = new HBox(8, input, choose); HBox.setHgrow(input, Priority.ALWAYS);
                inputs.put(field.name, input); control = new VBox(4, reference, chosen);
            } else if (field.kind == EntityMeta.Kind.BOOLEAN) {
                ComboBox<String> input = new ComboBox<>(); input.getItems().addAll("", "true", "false");
                input.setValue(initial); readers.put(field.name, input::getValue); control = input;
            } else if (field.kind == EntityMeta.Kind.ENUM) {
                ComboBox<String> input = new ComboBox<>(); input.getItems().add(""); input.getItems().addAll(field.enumValues);
                input.setValue(initial); readers.put(field.name, input::getValue); control = input;
            } else if (field.kind == EntityMeta.Kind.DATE && field.javaType.equals("LocalDate")) {
                DatePicker input = new DatePicker(initial.isBlank() ? null : java.time.LocalDate.parse(initial));
                input.setEditable(false); readers.put(field.name, () -> input.getValue() == null ? "" : input.getValue().toString()); control = input;
            } else if (field.kind == EntityMeta.Kind.PASSWORD) {
                PasswordField input = new PasswordField(); input.setPromptText(id == null ? "At least 8 characters" : "Leave blank to keep current password");
                readers.put(field.name, input::getText); control = input;
            } else if (field.maxLength > 500 || field.name.equals("notes") || field.name.equals("description")) {
                TextArea input = new TextArea(initial); input.setWrapText(true); input.setPrefRowCount(3); readers.put(field.name, input::getText); control = input;
            } else {
                TextField input = new TextField(initial);
                if (field.kind == EntityMeta.Kind.DATE) input.setPromptText(field.javaType.equals("Instant") ? "YYYY-MM-DDTHH:mm:ssZ" : "YYYY-MM-DDTHH:mm:ss");
                else if (field.kind == EntityMeta.Kind.INTEGER && field.name.endsWith("Id")) input.setPromptText("ID of a related record");
                readers.put(field.name, input::getText); control = input;
            }
            if (!inputs.containsKey(field.name)) { control.setId("field-" + field.name); inputs.put(field.name, control); }
            control.setMaxWidth(Double.MAX_VALUE);
            String caption = field.referencePath.isBlank() ? field.label : field.label.replaceFirst(" [iI]d$", "");
            Label label = new Label(caption + (field.required ? " *" : "")); label.setLabelFor(inputs.get(field.name));
            grid.add(label, 0, row); grid.add(control, 1, row++);
        }
        return grid;
    }

    private void save() {
        if (saving) return;
        ObjectNode body = new ObjectMapper().createObjectNode();
        if (existing != null && existing.has("version")) body.set("version", existing.get("version"));
        for (EntityMeta.Field field : meta.fields()) {
            Supplier<String> reader = readers.get(field.name);
            if (reader == null) continue;
            try { Values.put(body, field, reader.get()); }
            catch (IllegalArgumentException ex) { error.setText(ex.getMessage()); inputs.get(field.name).requestFocus(); return; }
        }
        error.setText(""); setSaving(true);
        Ui.async(() -> id == null ? api.create(entry.path(), body) : api.update(entry.path(), id, body), saved -> {
            setSaving(false); setResult(saved); close();
        }, failure -> { setSaving(false); error.setText(Ui.message(failure)); });
    }
    private void setSaving(boolean busy) {
        saving = busy;
        form.setDisable(busy);
        getDialogPane().lookupButton(saveType).setDisable(busy);
        getDialogPane().lookupButton(ButtonType.CANCEL).setDisable(busy);
        state.setText(busy ? "Saving record…" : "Required fields are marked with *");
    }
}
