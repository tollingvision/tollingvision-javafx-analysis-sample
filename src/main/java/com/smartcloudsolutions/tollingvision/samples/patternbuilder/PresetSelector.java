package com.smartcloudsolutions.tollingvision.samples.patternbuilder;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UI component for selecting and managing pattern presets.
 * Provides dropdown selection, save/load functionality, and preset management operations.
 */
public class PresetSelector extends VBox {
    private static final Logger LOGGER = Logger.getLogger(PresetSelector.class.getName());
    
    private final PresetManager presetManager;
    private final ObjectProperty<PresetConfiguration> selectedPreset = new SimpleObjectProperty<>();
    
    // UI Components
    private ComboBox<PresetConfiguration> presetComboBox;
    private Button saveButton;
    private Button deleteButton;
    private Button manageButton;
    private Button importButton;
    private Button exportButton;
    
    // Callbacks
    private Consumer<PresetConfiguration> onPresetSelected;
    private Consumer<PresetConfiguration> onPresetSaved;
    
    /**
     * Creates a new PresetSelector with the specified preset manager.
     * 
     * @param presetManager the preset manager to use
     */
    public PresetSelector(PresetManager presetManager) {
        this.presetManager = presetManager;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupBindings();
        
        // Load presets
        refreshPresets();
        
        // Select default preset if available
        PresetConfiguration defaultPreset = presetManager.getDefaultPreset();
        if (defaultPreset != null) {
            presetComboBox.setValue(defaultPreset);
        }
    }
    
    /**
     * Initializes all UI components.
     */
    private void initializeComponents() {
        // Preset dropdown
        presetComboBox = new ComboBox<>();
        presetComboBox.setPromptText("Select a preset...");
        presetComboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Custom cell factory to display preset names
        presetComboBox.setCellFactory(listView -> new ListCell<PresetConfiguration>() {
            @Override
            protected void updateItem(PresetConfiguration preset, boolean empty) {
                super.updateItem(preset, empty);
                if (empty || preset == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(preset.getName());
                    if (preset.getDescription() != null && !preset.getDescription().trim().isEmpty()) {
                        setTooltip(new Tooltip(preset.getDescription()));
                    }
                }
            }
        });
        
        // Button text for selected item
        presetComboBox.setButtonCell(new ListCell<PresetConfiguration>() {
            @Override
            protected void updateItem(PresetConfiguration preset, boolean empty) {
                super.updateItem(preset, empty);
                if (empty || preset == null) {
                    setText("Select a preset...");
                } else {
                    setText(preset.getName());
                }
            }
        });
        
        // Action buttons
        saveButton = new Button("Save");
        saveButton.setTooltip(new Tooltip("Save current configuration as a preset"));
        
        deleteButton = new Button("Delete");
        deleteButton.setTooltip(new Tooltip("Delete the selected preset"));
        
        manageButton = new Button("Manage...");
        manageButton.setTooltip(new Tooltip("Open preset management dialog"));
        
        importButton = new Button("Import");
        importButton.setTooltip(new Tooltip("Import preset from JSON file"));
        
        exportButton = new Button("Export");
        exportButton.setTooltip(new Tooltip("Export selected preset to JSON file"));
    }
    
    /**
     * Sets up the layout structure.
     */
    private void setupLayout() {
        setSpacing(10);
        setPadding(new Insets(10));
        
        // Preset selection row
        HBox selectionRow = new HBox(10);
        selectionRow.setAlignment(Pos.CENTER_LEFT);
        
        Label presetLabel = new Label("Preset:");
        presetLabel.setMinWidth(60);
        
        HBox.setHgrow(presetComboBox, Priority.ALWAYS);
        
        selectionRow.getChildren().addAll(presetLabel, presetComboBox);
        
        // Action buttons row
        HBox buttonRow = new HBox(5);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        
        buttonRow.getChildren().addAll(
            saveButton, deleteButton, new Separator(),
            manageButton, new Separator(),
            importButton, exportButton
        );
        
        getChildren().addAll(selectionRow, buttonRow);
    }
    
    /**
     * Sets up event handlers for UI interactions.
     */
    private void setupEventHandlers() {
        // Preset selection
        presetComboBox.setOnAction(e -> {
            PresetConfiguration selected = presetComboBox.getValue();
            if (selected != null) {
                selectedPreset.set(selected);
                if (onPresetSelected != null) {
                    onPresetSelected.accept(selected);
                }
            }
        });
        
        // Save button
        saveButton.setOnAction(e -> showSavePresetDialog());
        
        // Delete button
        deleteButton.setOnAction(e -> deleteSelectedPreset());
        
        // Manage button
        manageButton.setOnAction(e -> showPresetManagementDialog());
        
        // Import button
        importButton.setOnAction(e -> importPreset());
        
        // Export button
        exportButton.setOnAction(e -> exportSelectedPreset());
    }
    
    /**
     * Sets up data bindings between components.
     */
    private void setupBindings() {
        // Enable/disable buttons based on selection
        deleteButton.disableProperty().bind(
            selectedPreset.isNull().or(
                selectedPreset.isEqualTo(presetManager.getDefaultPreset())
            )
        );
        
        exportButton.disableProperty().bind(selectedPreset.isNull());
    }
    
    /**
     * Refreshes the preset list from the preset manager.
     */
    public void refreshPresets() {
        ObservableList<PresetConfiguration> presets = presetManager.listPresets();
        presetComboBox.setItems(FXCollections.observableArrayList(presets));
    }
    
    /**
     * Gets the currently selected preset.
     * 
     * @return the selected preset, or null if none selected
     */
    public PresetConfiguration getSelectedPreset() {
        return selectedPreset.get();
    }
    
    /**
     * Sets the selected preset.
     * 
     * @param preset the preset to select
     */
    public void setSelectedPreset(PresetConfiguration preset) {
        presetComboBox.setValue(preset);
        selectedPreset.set(preset);
    }
    
    /**
     * Sets the callback to be invoked when a preset is selected.
     * 
     * @param callback the selection callback
     */
    public void setOnPresetSelected(Consumer<PresetConfiguration> callback) {
        this.onPresetSelected = callback;
    }
    
    /**
     * Sets the callback to be invoked when a preset is saved.
     * 
     * @param callback the save callback
     */
    public void setOnPresetSaved(Consumer<PresetConfiguration> callback) {
        this.onPresetSaved = callback;
    }
    
    /**
     * Property for the selected preset (for binding).
     * 
     * @return the selected preset property
     */
    public ObjectProperty<PresetConfiguration> selectedPresetProperty() {
        return selectedPreset;
    }
    
    /**
     * Shows the save preset dialog.
     */
    private void showSavePresetDialog() {
        SavePresetDialog dialog = new SavePresetDialog();
        Optional<SavePresetDialog.PresetInfo> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            SavePresetDialog.PresetInfo info = result.get();
            
            // Create preset configuration
            // Note: This would need to get the current configuration from the parent dialog
            PatternConfiguration currentConfig = getCurrentConfiguration();
            if (currentConfig != null) {
                PresetConfiguration preset = new PresetConfiguration(
                    info.getName(),
                    info.getDescription(),
                    currentConfig
                );
                
                try {
                    presetManager.savePreset(preset);
                    refreshPresets();
                    setSelectedPreset(preset);
                    
                    if (onPresetSaved != null) {
                        onPresetSaved.accept(preset);
                    }
                    
                    showInfoAlert("Preset Saved", "Preset '" + info.getName() + "' has been saved successfully.");
                    
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to save preset", e);
                    showErrorAlert("Save Failed", "Failed to save preset: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Deletes the currently selected preset.
     */
    private void deleteSelectedPreset() {
        PresetConfiguration selected = getSelectedPreset();
        if (selected == null) {
            return;
        }
        
        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Preset");
        confirmAlert.setHeaderText("Delete preset '" + selected.getName() + "'?");
        confirmAlert.setContentText("This action cannot be undone.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = presetManager.deletePreset(selected.getName());
            if (deleted) {
                refreshPresets();
                presetComboBox.setValue(null);
                selectedPreset.set(null);
                
                showInfoAlert("Preset Deleted", "Preset '" + selected.getName() + "' has been deleted.");
            } else {
                showErrorAlert("Delete Failed", "Failed to delete preset '" + selected.getName() + "'.");
            }
        }
    }
    
    /**
     * Shows the preset management dialog.
     */
    private void showPresetManagementDialog() {
        PresetManagementDialog dialog = new PresetManagementDialog(presetManager);
        dialog.showAndWait();
        
        // Refresh presets after management
        refreshPresets();
    }
    
    /**
     * Imports a preset from a JSON file.
     */
    private void importPreset() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Preset");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        Stage stage = (Stage) getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                PresetConfiguration imported = presetManager.importPreset(file.toPath());
                
                // Check if preset already exists
                boolean exists = presetManager.listPresets().stream()
                        .anyMatch(p -> p.getName().equals(imported.getName()));
                
                if (exists) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Preset Exists");
                    confirmAlert.setHeaderText("Preset '" + imported.getName() + "' already exists");
                    confirmAlert.setContentText("Do you want to overwrite the existing preset?");
                    
                    Optional<ButtonType> result = confirmAlert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        return;
                    }
                }
                
                presetManager.savePreset(imported);
                refreshPresets();
                setSelectedPreset(imported);
                
                showInfoAlert("Import Successful", "Preset '" + imported.getName() + "' has been imported successfully.");
                
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
        PresetConfiguration selected = getSelectedPreset();
        if (selected == null) {
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Preset");
        fileChooser.setInitialFileName(selected.getName() + ".json");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        Stage stage = (Stage) getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                presetManager.exportPreset(selected, file.toPath());
                showInfoAlert("Export Successful", "Preset '" + selected.getName() + "' has been exported to " + file.getName());
                
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to export preset", e);
                showErrorAlert("Export Failed", "Failed to export preset: " + e.getMessage());
            }
        }
    }
    
    // Callback to get current configuration
    private java.util.function.Supplier<PatternConfiguration> currentConfigurationSupplier;
    
    /**
     * Sets the supplier for getting the current configuration.
     * 
     * @param supplier the configuration supplier
     */
    public void setCurrentConfigurationSupplier(java.util.function.Supplier<PatternConfiguration> supplier) {
        this.currentConfigurationSupplier = supplier;
    }
    
    /**
     * Gets the current configuration from the parent dialog.
     * 
     * @return the current pattern configuration
     */
    private PatternConfiguration getCurrentConfiguration() {
        if (currentConfigurationSupplier != null) {
            return currentConfigurationSupplier.get();
        }
        return new PatternConfiguration();
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