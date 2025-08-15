package com.smartcloudsolutions.tollingvision.samples.patternbuilder;

import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Main container dialog that orchestrates the pattern building workflow.
 * Provides mode switching between Simple and Advanced pattern builders,
 * coordinates configuration conversion, and handles dialog lifecycle.
 */
public class PatternBuilderDialog extends Stage {

    /**
     * Enumeration of pattern builder modes.
     */
    public enum PatternBuilderMode {
        SIMPLE("Simple", "Visual pattern builder for non-regex users"),
        ADVANCED("Advanced", "Direct regex input for power users");

        private final String displayName;
        private final String description;

        PatternBuilderMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    // Current state
    private final ObjectProperty<PatternBuilderMode> currentMode = new SimpleObjectProperty<>(
            PatternBuilderMode.SIMPLE);
    private final ObjectProperty<PatternConfiguration> currentConfig = new SimpleObjectProperty<>(
            new PatternConfiguration());
    private String sharedDirectory = null; // Shared between modes

    // Preset management
    private PresetManager presetManager;
    private PresetSelector presetSelector;

    // UI Components
    private ToggleGroup modeToggleGroup;
    private RadioButton simpleModeButton;
    private RadioButton advancedModeButton;
    private Label modeDescriptionLabel;

    private SimplePatternBuilder simplePatternBuilder;
    private AdvancedPatternBuilder advancedPatternBuilder;
    private BorderPane contentPane;

    private Button okButton;
    private Button cancelButton;
    private Label validationStatusLabel;

    // Callback for configuration completion
    private Consumer<PatternConfiguration> onConfigurationComplete;
    private Consumer<PatternConfiguration> onConfigurationReady;

    // Shared input folder and resources
    private final String inputFolder;
    private final ResourceBundle messages;

    /**
     * Creates a new PatternBuilderDialog with modal behavior.
     * 
     * @param inputFolder the input folder from the main screen
     * @param messages    the resource bundle for i18n
     */
    public PatternBuilderDialog(String inputFolder, ResourceBundle messages) {
        this.inputFolder = inputFolder;
        this.messages = messages;

        // Initialize preset management
        presetManager = new PresetManager();

        initializeDialog();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupBindings();

        // Start in simple mode
        setMode(PatternBuilderMode.SIMPLE);

        // Load default preset if available
        loadDefaultPreset();
    }

    /**
     * Initializes the dialog properties.
     */
    private void initializeDialog() {
        setTitle(messages.getString("pattern.builder.title"));
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.DECORATED);
        setResizable(true);

        // Set minimum and preferred size
        setMinWidth(1000);
        setMinHeight(700);
        setWidth(1200);
        setHeight(800);

        // Center on parent
        centerOnScreen();
    }

    /**
     * Initializes all UI components.
     */
    private void initializeComponents() {
        // Mode selector
        modeToggleGroup = new ToggleGroup();
        simpleModeButton = new RadioButton(messages.getString("pattern.builder.mode.simple"));
        advancedModeButton = new RadioButton(messages.getString("pattern.builder.mode.advanced"));

        simpleModeButton.setToggleGroup(modeToggleGroup);
        advancedModeButton.setToggleGroup(modeToggleGroup);
        simpleModeButton.setSelected(true);

        modeDescriptionLabel = new Label(messages.getString("pattern.builder.mode.simple.description"));
        modeDescriptionLabel.setFont(Font.font("System", 11));
        modeDescriptionLabel.setTextFill(Color.GRAY);

        // Pattern builders with input folder
        simplePatternBuilder = new SimplePatternBuilder(inputFolder, messages);
        advancedPatternBuilder = new AdvancedPatternBuilder(inputFolder, messages);
        
        // Set up configuration ready callbacks
        simplePatternBuilder.setOnConfigurationReady(this::handleConfigurationReady);
        advancedPatternBuilder.setOnConfigurationReady(this::handleConfigurationReady);

        // Content pane
        contentPane = new BorderPane();

        // Dialog buttons
        okButton = new Button(messages.getString("button.ok"));
        cancelButton = new Button(messages.getString("button.cancel"));

        okButton.setDefaultButton(true);
        cancelButton.setCancelButton(true);

        // Validation status
        validationStatusLabel = new Label(messages.getString("status.ready"));
        validationStatusLabel.setFont(Font.font("System", FontWeight.BOLD, 11));

        // Preset selector
        presetSelector = new PresetSelector(presetManager);
        presetSelector.setCurrentConfigurationSupplier(this::getActiveConfiguration);
    }

    /**
     * Sets up the main layout structure.
     */
    private void setupLayout() {
        BorderPane root = new BorderPane();

        // Header with title and mode selector (non-scrolling)
        VBox header = createHeader();

        // Preset selector
        VBox headerContainer = new VBox(10);
        headerContainer.setPadding(new Insets(20, 20, 10, 20));
        headerContainer.getChildren().addAll(header, presetSelector, new Separator());

        // Scrollable content area
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
        scrollPane.setContent(contentPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("edge-to-edge");

        // Add padding to content pane
        contentPane.setPadding(new Insets(10, 20, 20, 20));

        // Footer with validation status and buttons (non-scrolling)
        HBox footer = createFooter();
        footer.setPadding(new Insets(10, 20, 20, 20));

        root.setTop(headerContainer);
        root.setCenter(scrollPane);
        root.setBottom(footer);

        // Create scene and apply styling
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/tollingvision-theme.css").toExternalForm());
        if (getClass().getResource("/pattern-builder-validation.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/pattern-builder-validation.css").toExternalForm());
        }
        setScene(scene);
    }

    /**
     * Creates the header section with title and mode selector.
     */
    private VBox createHeader() {
        VBox header = new VBox(10);

        // Title
        Label titleLabel = new Label(messages.getString("pattern.builder.title"));
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Subtitle
        Label subtitleLabel = new Label(messages.getString("pattern.builder.subtitle"));
        subtitleLabel.setFont(Font.font("System", 12));
        subtitleLabel.setTextFill(Color.GRAY);

        // Mode selector
        HBox modeSelector = new HBox(20);
        modeSelector.setAlignment(Pos.CENTER_LEFT);

        Label modeLabel = new Label(messages.getString("label.mode"));
        modeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        HBox modeButtons = new HBox(15);
        modeButtons.setAlignment(Pos.CENTER_LEFT);
        modeButtons.getChildren().addAll(simpleModeButton, advancedModeButton);

        modeSelector.getChildren().addAll(modeLabel, modeButtons, modeDescriptionLabel);
        HBox.setHgrow(modeDescriptionLabel, Priority.ALWAYS);

        // Separator
        Separator separator = new Separator();

        header.getChildren().addAll(titleLabel, subtitleLabel, modeSelector, separator);

        return header;
    }

    /**
     * Creates the footer section with validation status and buttons.
     */
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 0, 0, 0));

        // Separator
        Separator separator = new Separator();

        // Validation status (left side)
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.getChildren().addAll(new Label(messages.getString("label.status")), validationStatusLabel);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Buttons (right side)
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, okButton);

        footer.getChildren().addAll(statusBox, spacer, buttonBox);

        VBox footerContainer = new VBox(10);
        footerContainer.getChildren().addAll(separator, footer);

        return footer;
    }

    /**
     * Sets up event handlers for UI interactions.
     */
    private void setupEventHandlers() {
        // Mode selection
        simpleModeButton.setOnAction(e -> setMode(PatternBuilderMode.SIMPLE));
        advancedModeButton.setOnAction(e -> setMode(PatternBuilderMode.ADVANCED));

        // Dialog buttons
        okButton.setOnAction(e -> handleOkAction());
        cancelButton.setOnAction(e -> handleCancelAction());

        // Window close
        setOnCloseRequest(e -> handleCancelAction());

        // Preset selector events
        presetSelector.setOnPresetSelected(this::loadPresetConfiguration);
        presetSelector.setOnPresetSaved(preset -> {
            // Preset was saved, refresh the selector
            presetSelector.refreshPresets();
        });
    }

    /**
     * Sets up data bindings between components.
     */
    private void setupBindings() {
        // Update mode description when mode changes
        currentMode.addListener((obs, oldMode, newMode) -> {
            if (newMode != null) {
                modeDescriptionLabel.setText(newMode.getDescription());
                updateModeSelection(newMode);
            }
        });

        // Update validation status based on current configuration
        currentConfig.addListener((obs, oldConfig, newConfig) -> {
            updateValidationStatus();
        });
    }

    /**
     * Sets the current pattern builder mode.
     */
    private void setMode(PatternBuilderMode mode) {
        if (mode == currentMode.get()) {
            return;
        }

        // Preserve current configuration and directory
        PatternConfiguration config = getActiveConfiguration();
        preserveSharedState();

        PatternBuilderMode oldMode = currentMode.get();
        currentMode.set(mode);

        // Update content pane and convert configuration
        switch (mode) {
            case SIMPLE -> {
                contentPane.setCenter(simplePatternBuilder);
                convertToSimpleMode(config, oldMode);

                // Trigger validation refresh on mode switch
                simplePatternBuilder.getValidationModel().requestValidationRefresh();

                // Show custom token dialog when switching to Simple mode for the first time
                if (oldMode == PatternBuilderMode.ADVANCED) {
                    javafx.application.Platform.runLater(() -> {
                        simplePatternBuilder.showCustomTokenDialogIfNeeded();
                    });
                }
            }
            case ADVANCED -> {
                contentPane.setCenter(advancedPatternBuilder);
                convertToAdvancedMode(config, oldMode);

                // Trigger validation refresh on mode switch
                if (advancedPatternBuilder.getValidationModel() != null) {
                    advancedPatternBuilder.getValidationModel().requestValidationRefresh();
                }
            }
        }

        // Restore shared state
        restoreSharedState();

        updateValidationStatus();

        ValidationLogger.logUserAction("Mode switched",
                String.format("%s → %s", oldMode, mode));
    }

    /**
     * Updates the mode selection UI.
     */
    private void updateModeSelection(PatternBuilderMode mode) {
        switch (mode) {
            case SIMPLE -> simpleModeButton.setSelected(true);
            case ADVANCED -> advancedModeButton.setSelected(true);
        }
    }

    /**
     * Preserves shared state between modes (directory, extension settings).
     */
    private void preserveSharedState() {
        switch (currentMode.get()) {
            case SIMPLE -> {
                // Get directory from simple mode if available
                if (!simplePatternBuilder.getSampleFilenames().isEmpty()) {
                    // Directory is implicit in simple mode, we'll preserve it
                }
            }
            case ADVANCED -> {
                sharedDirectory = advancedPatternBuilder.getSelectedDirectory();
            }
        }
    }

    /**
     * Restores shared state to the new mode.
     */
    private void restoreSharedState() {
        switch (currentMode.get()) {
            case SIMPLE -> {
                if (sharedDirectory != null && !sharedDirectory.trim().isEmpty()) {
                    simplePatternBuilder.setAnalysisDirectory(sharedDirectory);
                }
            }
            case ADVANCED -> {
                if (sharedDirectory != null && !sharedDirectory.trim().isEmpty()) {
                    advancedPatternBuilder.setSelectedDirectory(sharedDirectory);
                }
            }
        }
    }

    /**
     * Converts configuration to simple mode representation.
     */
    private void convertToSimpleMode(PatternConfiguration config, PatternBuilderMode fromMode) {
        if (config == null) {
            return;
        }

        // Try to preserve as much configuration as possible
        // Simple mode will need to be enhanced to accept pre-built configurations

        // For now, we preserve the configuration object
        currentConfig.set(config);

        ValidationLogger.logConfigurationChange("Mode conversion",
                fromMode.name(), "SIMPLE");
    }

    /**
     * Converts configuration to advanced mode representation.
     */
    private void convertToAdvancedMode(PatternConfiguration config, PatternBuilderMode fromMode) {
        if (config == null) {
            config = new PatternConfiguration();
        }

        // Advanced mode can directly use any configuration
        advancedPatternBuilder.setConfiguration(config);
        currentConfig.set(config);

        ValidationLogger.logConfigurationChange("Mode conversion",
                fromMode.name(), "ADVANCED");
    }

    /**
     * Gets the current configuration from the active pattern builder.
     */
    private PatternConfiguration getActiveConfiguration() {
        return switch (currentMode.get()) {
            case SIMPLE -> simplePatternBuilder.generateConfiguration();
            case ADVANCED -> advancedPatternBuilder.getConfiguration();
        };
    }

    /**
     * Updates the validation status display.
     */
    private void updateValidationStatus() {
        PatternConfiguration config = getActiveConfiguration();

        if (config == null) {
            validationStatusLabel.setText("No configuration");
            validationStatusLabel.setTextFill(Color.GRAY);
            okButton.setDisable(true);
            return;
        }

        // Validate the configuration
        ValidationResult result = validateConfiguration(config);

        if (result.isValid()) {
            if (result.hasWarnings()) {
                validationStatusLabel.setText("Valid with warnings (" + result.getWarnings().size() + ")");
                validationStatusLabel.setTextFill(Color.ORANGE);
            } else {
                validationStatusLabel.setText("Configuration valid");
                validationStatusLabel.setTextFill(Color.GREEN);
            }
            okButton.setDisable(false);
        } else {
            validationStatusLabel.setText("Validation errors (" + result.getErrors().size() + ")");
            validationStatusLabel.setTextFill(Color.RED);
            okButton.setDisable(true);
        }
    }

    /**
     * Validates a pattern configuration.
     */
    private ValidationResult validateConfiguration(PatternConfiguration config) {
        // Use the pattern generator for validation
        PatternGenerator generator = new PatternGenerator();
        return generator.validatePatterns(config);
    }

    /**
     * Handles the OK button action.
     */
    private void handleOkAction() {
        PatternConfiguration config = getActiveConfiguration();

        if (config != null) {
            ValidationResult result = validateConfiguration(config);

            if (result.isValid()) {
                // Configuration is valid, complete the dialog
                if (onConfigurationComplete != null) {
                    onConfigurationComplete.accept(config);
                }
                close();
            } else {
                // Show validation errors
                showValidationErrors(result);
            }
        }
    }

    /**
     * Handles the Cancel button action.
     */
    private void handleCancelAction() {
        close();
    }

    /**
     * Shows validation errors in a dialog.
     */
    private void showValidationErrors(ValidationResult result) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Errors");
        alert.setHeaderText("Pattern configuration has errors");

        StringBuilder content = new StringBuilder();
        content.append("Please fix the following errors before proceeding:\n\n");

        for (ValidationError error : result.getErrors()) {
            content.append("• ").append(error.getMessage()).append("\n");
        }

        if (result.hasWarnings()) {
            content.append("\nWarnings:\n");
            for (ValidationWarning warning : result.getWarnings()) {
                content.append("• ").append(warning.getMessage()).append("\n");
            }
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    /**
     * Shows the dialog with the specified initial configuration.
     * 
     * @param initialConfig the initial pattern configuration
     */
    public void showDialog(PatternConfiguration initialConfig) {
        if (initialConfig != null) {
            currentConfig.set(initialConfig);

            // Set the configuration in the appropriate builder
            switch (currentMode.get()) {
                case SIMPLE -> {
                    // Simple mode will need to be initialized with the config
                    // For now, we'll start fresh in simple mode
                }
                case ADVANCED -> {
                    advancedPatternBuilder.setConfiguration(initialConfig);
                }
            }
        }

        updateValidationStatus();
        showAndWait();
    }

    /**
     * Sets the callback to be invoked when configuration is complete.
     * 
     * @param callback the configuration completion callback
     */
    public void setOnConfigurationComplete(Consumer<PatternConfiguration> callback) {
        this.onConfigurationComplete = callback;
    }
    
    /**
     * Sets the configuration ready callback.
     * Called immediately when a valid configuration is generated in either mode.
     * 
     * @param callback the configuration ready callback
     */
    public void setOnConfigurationReady(Consumer<PatternConfiguration> callback) {
        this.onConfigurationReady = callback;
    }
    
    /**
     * Handles when a configuration is ready from either mode.
     * Updates the current configuration and enables the OK button.
     * 
     * @param config the ready configuration
     */
    private void handleConfigurationReady(PatternConfiguration config) {
        currentConfig.set(config);
        
        // Enable OK button when configuration is valid
        okButton.setDisable(false);
        
        // Call the external callback if set
        if (onConfigurationReady != null) {
            onConfigurationReady.accept(config);
        }
        
        // Update validation status
        updateValidationStatus();
    }

    /**
     * Gets the current pattern builder mode.
     * 
     * @return the current mode
     */
    public PatternBuilderMode getCurrentMode() {
        return currentMode.get();
    }

    /**
     * Gets the current pattern configuration.
     * 
     * @return the current configuration
     */
    public PatternConfiguration getCurrentConfiguration() {
        return currentConfig.get();
    }

    /**
     * Property for the current mode (for binding).
     * 
     * @return the current mode property
     */
    public ObjectProperty<PatternBuilderMode> currentModeProperty() {
        return currentMode;
    }

    /**
     * Property for the current configuration (for binding).
     * 
     * @return the current configuration property
     */
    public ObjectProperty<PatternConfiguration> currentConfigurationProperty() {
        return currentConfig;
    }

    /**
     * Loads the default preset configuration on startup.
     */
    private void loadDefaultPreset() {
        PresetConfiguration defaultPreset = presetManager.getDefaultPreset();
        if (defaultPreset != null) {
            loadPresetConfiguration(defaultPreset);
            presetSelector.setSelectedPreset(defaultPreset);
        }
    }

    /**
     * Loads a preset configuration into the dialog.
     * 
     * @param preset the preset to load
     */
    private void loadPresetConfiguration(PresetConfiguration preset) {
        if (preset != null && preset.getPatternConfig() != null) {
            PatternConfiguration config = preset.getPatternConfig().copy();
            currentConfig.set(config);

            // Apply configuration to the appropriate builder
            switch (currentMode.get()) {
                case SIMPLE -> {
                    // For simple mode, we would need to reverse-engineer the patterns
                    // back to visual rules. For now, we'll switch to advanced mode
                    // when loading a preset with existing patterns.
                    if (hasComplexPatterns(config)) {
                        setMode(PatternBuilderMode.ADVANCED);
                    }
                    // Simple mode would need additional logic to populate from config
                }
                case ADVANCED -> {
                    advancedPatternBuilder.setConfiguration(config);
                }
            }

            updateValidationStatus();
        }
    }

    /**
     * Checks if a configuration has complex patterns that require advanced mode.
     * 
     * @param config the configuration to check
     * @return true if the configuration has complex patterns
     */
    private boolean hasComplexPatterns(PatternConfiguration config) {
        return (config.getGroupPattern() != null && !config.getGroupPattern().isEmpty()) ||
                (config.getFrontPattern() != null && !config.getFrontPattern().isEmpty()) ||
                (config.getRearPattern() != null && !config.getRearPattern().isEmpty()) ||
                (config.getOverviewPattern() != null && !config.getOverviewPattern().isEmpty());
    }

    /**
     * Gets the preset manager for external access.
     * 
     * @return the preset manager
     */
    public PresetManager getPresetManager() {
        return presetManager;
    }

    /**
     * Gets the preset selector component.
     * 
     * @return the preset selector
     */
    public PresetSelector getPresetSelector() {
        return presetSelector;
    }

    /**
     * Cleanup method to shut down background processing.
     * Task 12: Updated to use new cleanup methods for performance optimization resources.
     */
    public void shutdown() {
        if (simplePatternBuilder != null) {
            simplePatternBuilder.cleanup();
        }
        if (advancedPatternBuilder != null) {
            advancedPatternBuilder.shutdown();
        }
    }
}