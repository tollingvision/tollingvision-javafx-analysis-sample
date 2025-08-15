package com.smartcloudsolutions.tollingvision.samples.patternbuilder;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for advanced preset management operations.
 * Provides rename, duplicate, organize, and batch import/export functionality.
 */
public class PresetManagementDialog extends Stage {
    private static final Logger LOGGER = Logger.getLogger(PresetManagementDialog.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PresetManager presetManager;

    // UI Components
    private TableView<PresetConfiguration> presetsTable;
    private ObservableList<PresetConfiguration> presets;

    private Button renameButton;
    private Button duplicateButton;
    private Button deleteButton;
    private Button importButton;
    private Button exportButton;
    private Button closeButton;

    /**
     * Creates a new PresetManagementDialog.
     * 
     * @param presetManager the preset manager to use
     */
    public PresetManagementDialog(PresetManager presetManager) {
        this.presetManager = presetManager;

        initializeDialog();
        initializeComponents();
        setupLayout();
        setupEventHandlers();

        // Load presets
        refreshPresets();
    }

    /**
     * Initializes the dialog properties.
     */
    private void initializeDialog() {
        setTitle("Manage Presets");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);

        setMinWidth(600);
        setMinHeight(400);
        setWidth(800);
        setHeight(500);

        centerOnScreen();
    }

    /**
     * Initializes all UI components.
     */
    private void initializeComponents() {
        // Presets table
        presetsTable = new TableView<>();
        presets = FXCollections.observableArrayList();
        presetsTable.setItems(presets);

        // Table columns
        TableColumn<PresetConfiguration, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(150);

        TableColumn<PresetConfiguration, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(250);

        TableColumn<PresetConfiguration, String> createdColumn = new TableColumn<>("Created");
        createdColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getCreated().format(DATE_FORMATTER)));
        createdColumn.setPrefWidth(120);

        TableColumn<PresetConfiguration, String> lastUsedColumn = new TableColumn<>("Last Used");
        lastUsedColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getLastUsed().format(DATE_FORMATTER)));
        lastUsedColumn.setPrefWidth(120);

        presetsTable.getColumns().addAll(nameColumn, descriptionColumn, createdColumn, lastUsedColumn);

        // Selection mode
        presetsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Action buttons
        renameButton = new Button("Rename");
        renameButton.setTooltip(new Tooltip("Rename the selected preset"));

        duplicateButton = new Button("Duplicate");
        duplicateButton.setTooltip(new Tooltip("Create a copy of the selected preset"));

        deleteButton = new Button("Delete");
        deleteButton.setTooltip(new Tooltip("Delete the selected preset"));

        importButton = new Button("Import...");
        importButton.setTooltip(new Tooltip("Import presets from JSON files"));

        exportButton = new Button("Export...");
        exportButton.setTooltip(new Tooltip("Export selected preset to JSON file"));

        closeButton = new Button("Close");
        closeButton.setDefaultButton(true);
    }

    /**
     * Sets up the layout structure.
     */
    private void setupLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Header
        VBox header = new VBox(5);
        Label titleLabel = new Label("Preset Management");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Manage your saved pattern presets");
        subtitleLabel.setStyle("-fx-text-fill: gray;");

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Center - table
        VBox.setVgrow(presetsTable, Priority.ALWAYS);

        // Right side - action buttons
        VBox buttonPanel = new VBox(10);
        buttonPanel.setPadding(new Insets(0, 0, 0, 15));
        buttonPanel.setAlignment(Pos.TOP_CENTER);
        buttonPanel.setMinWidth(120);

        // Preset actions
        Label presetActionsLabel = new Label("Preset Actions");
        presetActionsLabel.setStyle("-fx-font-weight: bold;");

        VBox presetActions = new VBox(5);
        presetActions.getChildren().addAll(renameButton, duplicateButton, deleteButton);

        // File operations
        Label fileOpsLabel = new Label("File Operations");
        fileOpsLabel.setStyle("-fx-font-weight: bold;");

        VBox fileOps = new VBox(5);
        fileOps.getChildren().addAll(importButton, exportButton);

        buttonPanel.getChildren().addAll(
                presetActionsLabel, presetActions,
                new Separator(),
                fileOpsLabel, fileOps);

        // Bottom - close button
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(15, 0, 0, 0));
        footer.getChildren().add(closeButton);

        // Layout
        root.setTop(header);
        root.setCenter(presetsTable);
        root.setRight(buttonPanel);
        root.setBottom(footer);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/tollingvision-theme.css").toExternalForm());
        setScene(scene);
    }

    /**
     * Sets up event handlers for UI interactions.
     */
    private void setupEventHandlers() {
        // Table selection
        presetsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            boolean canModify = hasSelection && !"Default".equals(newSelection.getName());

            renameButton.setDisable(!canModify);
            duplicateButton.setDisable(!hasSelection);
            deleteButton.setDisable(!canModify);
            exportButton.setDisable(!hasSelection);
        });

        // Button actions
        renameButton.setOnAction(e -> renameSelectedPreset());
        duplicateButton.setOnAction(e -> duplicateSelectedPreset());
        deleteButton.setOnAction(e -> deleteSelectedPreset());
        importButton.setOnAction(e -> importPresets());
        exportButton.setOnAction(e -> exportSelectedPreset());
        closeButton.setOnAction(e -> close());

        // Double-click to rename
        presetsTable.setRowFactory(tv -> {
            TableRow<PresetConfiguration> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    PresetConfiguration preset = row.getItem();
                    if (!"Default".equals(preset.getName())) {
                        renameSelectedPreset();
                    }
                }
            });
            return row;
        });

        // Window close
        setOnCloseRequest(e -> close());
    }

    /**
     * Refreshes the presets list from the preset manager.
     */
    private void refreshPresets() {
        presets.clear();
        presets.addAll(presetManager.listPresets());

        // Clear selection
        presetsTable.getSelectionModel().clearSelection();
    }

    /**
     * Renames the selected preset.
     */
    private void renameSelectedPreset() {
        PresetConfiguration selected = presetsTable.getSelectionModel().getSelectedItem();
        if (selected == null || "Default".equals(selected.getName())) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Rename Preset");
        dialog.setHeaderText("Rename preset '" + selected.getName() + "'");
        dialog.setContentText("New name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newName = result.get().trim();
            if (!newName.isEmpty() && !newName.equals(selected.getName())) {
                boolean renamed = presetManager.renamePreset(selected.getName(), newName);
                if (renamed) {
                    refreshPresets();
                    showInfoAlert("Preset Renamed", "Preset renamed to '" + newName + "'");
                } else {
                    showErrorAlert("Rename Failed", "Failed to rename preset. The name may already exist.");
                }
            }
        }
    }

    /**
     * Duplicates the selected preset.
     */
    private void duplicateSelectedPreset() {
        PresetConfiguration selected = presetsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        String defaultName = selected.getName() + " Copy";

        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle("Duplicate Preset");
        dialog.setHeaderText("Duplicate preset '" + selected.getName() + "'");
        dialog.setContentText("Name for copy:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newName = result.get().trim();
            if (!newName.isEmpty()) {
                boolean duplicated = presetManager.duplicatePreset(selected.getName(), newName);
                if (duplicated) {
                    refreshPresets();
                    showInfoAlert("Preset Duplicated", "Preset duplicated as '" + newName + "'");
                } else {
                    showErrorAlert("Duplicate Failed", "Failed to duplicate preset. The name may already exist.");
                }
            }
        }
    }

    /**
     * Deletes the selected preset.
     */
    private void deleteSelectedPreset() {
        PresetConfiguration selected = presetsTable.getSelectionModel().getSelectedItem();
        if (selected == null || "Default".equals(selected.getName())) {
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Preset");
        confirmAlert.setHeaderText("Delete preset '" + selected.getName() + "'?");
        confirmAlert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = presetManager.deletePreset(selected.getName());
            if (deleted) {
                refreshPresets();
                showInfoAlert("Preset Deleted", "Preset '" + selected.getName() + "' has been deleted.");
            } else {
                showErrorAlert("Delete Failed", "Failed to delete preset.");
            }
        }
    }

    /**
     * Imports presets from JSON files.
     */
    private void importPresets() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Presets");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showOpenDialog(this);
        if (file != null) {
            try {
                presetManager.importAndSavePreset(file.toPath(), true);
                refreshPresets();
                showInfoAlert("Import Successful", "Preset imported successfully from " + file.getName());

            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to import preset", e);
                showErrorAlert("Import Failed", "Failed to import preset: " + e.getMessage());
            }
        }
    }

    /**
     * Exports the selected preset to a JSON file.
     */
    private void exportSelectedPreset() {
        PresetConfiguration selected = presetsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Preset");
        fileChooser.setInitialFileName(selected.getName() + ".json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showSaveDialog(this);
        if (file != null) {
            try {
                presetManager.exportPreset(selected, file.toPath());
                showInfoAlert("Export Successful", "Preset exported to " + file.getName());

            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to export preset", e);
                showErrorAlert("Export Failed", "Failed to export preset: " + e.getMessage());
            }
        }
    }

    /**
     * Shows an information alert.
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an error alert.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}