package com.smartcloudsolutions.tollingvision.samples.patternbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Advanced pattern builder for regex power users with enhanced regex editors,
 * syntax highlighting, validation, and live preview integration.
 * Provides direct regex input with comprehensive validation and explanation
 * features.
 */
public class AdvancedPatternBuilder extends VBox {

    private static final ExecutorService VALIDATION_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "AdvancedPatternBuilder-Validation");
        t.setDaemon(true);
        return t;
    });

    // Services
    private final PatternGenerator patternGenerator = new PatternGenerator();
    private final ValidationBlocker validationBlocker = new ValidationBlocker();
    
    // Task 15: ValidationModel for live validation refresh and empty-state handling
    private ValidationModel validationModel;
    private ValidationMessageBox validationMessageBox;

    // Configuration property
    private final ObjectProperty<PatternConfiguration> configurationProperty = new SimpleObjectProperty<>();

    // UI Components
    private TextArea groupPatternField;
    private TextArea frontPatternField;
    private TextArea rearPatternField;
    private TextArea overviewPatternField;

    private Label groupPatternValidation;
    private Label frontPatternValidation;
    private Label rearPatternValidation;
    private Label overviewPatternValidation;

    private Button explainGroupPatternButton;
    private Button explainFrontPatternButton;
    private Button explainRearPatternButton;
    private Button explainOverviewPatternButton;

    private Button copyGroupPatternButton;
    private Button copyFrontPatternButton;
    private Button copyRearPatternButton;
    private Button copyOverviewPatternButton;

    private PatternPreviewPane previewPane;
    private Label overallValidationStatus;
    private CheckBox extensionMatchingCheckBox;
    private TitledPane errorDetailsPane;
    private TitledPane warningDetailsPane;
    private TextField directoryField;
    private Button browseButton;

    // Sample filenames for preview
    private List<String> sampleFilenames = new ArrayList<>();

    // Background validation task
    private Task<ValidationResult> currentValidationTask;

    // Input folder and i18n resources
    private final String inputFolder;
    private final ResourceBundle messages;
    
    // Configuration ready callback
    private java.util.function.Consumer<PatternConfiguration> onConfigurationReady;

    /**
     * Creates a new AdvancedPatternBuilder with regex input fields and live
     * preview.
     * 
     * @param inputFolder the input folder from the main screen
     * @param messages    the resource bundle for i18n
     */
    public AdvancedPatternBuilder(String inputFolder, ResourceBundle messages) {
        this.inputFolder = inputFolder;
        this.messages = messages;

        initializeComponents();
        setupLayout();
        setupValidation();
        setupEventHandlers();

        // Initialize with empty configuration
        setConfiguration(new PatternConfiguration());

        // Automatically load files from input folder
        if (inputFolder != null && !inputFolder.trim().isEmpty()) {
            setSelectedDirectory(inputFolder);
        }
    }

    /**
     * Initializes all UI components.
     * Task 15: Creates ValidationModel and ValidationMessageBox for live validation.
     */
    private void initializeComponents() {
        // Task 15: Initialize validation model with messages for i18n support
        validationModel = new ValidationModel(messages);
        validationMessageBox = new ValidationMessageBox(validationModel, messages);
        // Pattern input fields
        groupPatternField = createPatternField(
                messages.getString("pattern.builder.advanced.group.pattern.placeholder"));
        frontPatternField = createPatternField(
                messages.getString("pattern.builder.advanced.front.pattern.placeholder"));
        rearPatternField = createPatternField(messages.getString("pattern.builder.advanced.rear.pattern.placeholder"));
        overviewPatternField = createPatternField(
                messages.getString("pattern.builder.advanced.overview.pattern.placeholder"));

        // Validation labels
        groupPatternValidation = createValidationLabel();
        frontPatternValidation = createValidationLabel();
        rearPatternValidation = createValidationLabel();
        overviewPatternValidation = createValidationLabel();

        // Explain buttons
        explainGroupPatternButton = createExplainButton(messages.getString("button.explain"));
        explainFrontPatternButton = createExplainButton(messages.getString("button.explain"));
        explainRearPatternButton = createExplainButton(messages.getString("button.explain"));
        explainOverviewPatternButton = createExplainButton(messages.getString("button.explain"));

        // Copy buttons
        copyGroupPatternButton = createCopyButton(messages.getString("button.copy"));
        copyFrontPatternButton = createCopyButton(messages.getString("button.copy"));
        copyRearPatternButton = createCopyButton(messages.getString("button.copy"));
        copyOverviewPatternButton = createCopyButton(messages.getString("button.copy"));

        // Preview pane
        previewPane = new PatternPreviewPane(messages);

        // Overall validation status
        overallValidationStatus = new Label("Ready");
        overallValidationStatus.setFont(Font.font("System", FontWeight.BOLD, 12));

        // Extension matching checkbox
        extensionMatchingCheckBox = new CheckBox(messages.getString("extension.matching.label"));
        ContextualHelpProvider.addTooltip(extensionMatchingCheckBox, "extension-flexible");

        // Error and warning details panes
        errorDetailsPane = new TitledPane("Validation Errors", new Label("No errors"));
        errorDetailsPane.setExpanded(false);
        errorDetailsPane.getStyleClass().add("validation-error-pane");
        errorDetailsPane.setVisible(false);
        errorDetailsPane.setManaged(false);

        warningDetailsPane = new TitledPane("Validation Warnings", new Label("No warnings"));
        warningDetailsPane.setExpanded(false);
        warningDetailsPane.getStyleClass().add("validation-warning-pane");
        warningDetailsPane.setVisible(false);
        warningDetailsPane.setManaged(false);

        // Input folder display (read-only)
        directoryField = new TextField();
        directoryField.setPromptText(messages.getString("placeholder.input.folder"));
        directoryField.setEditable(false);
        directoryField.setPrefWidth(400);
        directoryField.setText(inputFolder != null ? inputFolder : "");

        // No browse button - using inherited folder

        // Add contextual help to pattern fields
        ContextualHelpProvider.addTooltip(groupPatternField, "capturing-group-required");
        ContextualHelpProvider.addTooltip(frontPatternField, "regex-syntax");
        ContextualHelpProvider.addTooltip(rearPatternField, "regex-syntax");
        ContextualHelpProvider.addTooltip(overviewPatternField, "regex-syntax");
        ContextualHelpProvider.addTooltip(directoryField, "sample-files");
        ContextualHelpProvider.addTooltip(browseButton, "sample-files");
    }

    /**
     * Creates a pattern input field with syntax highlighting support.
     */
    private TextArea createPatternField(String promptText) {
        TextArea textArea = new TextArea();
        textArea.setPromptText(promptText);
        textArea.setPrefRowCount(3);
        textArea.setWrapText(true);
        textArea.getStyleClass().add("regex-editor");

        // Add basic syntax highlighting through CSS classes
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            updateSyntaxHighlighting(textArea, newText);
        });

        return textArea;
    }

    /**
     * Creates a validation label for displaying pattern validation results.
     */
    private Label createValidationLabel() {
        Label label = new Label();
        label.setWrapText(true);
        label.setFont(Font.font("System", 10));
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    /**
     * Creates an explain button for pattern explanation.
     */
    private Button createExplainButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("explain-button");
        return button;
    }

    /**
     * Creates a copy button for copying patterns to clipboard.
     */
    private Button createCopyButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("copy-button");
        return button;
    }

    /**
     * Sets up the main layout structure.
     */
    private void setupLayout() {
        setSpacing(15);
        setPadding(new Insets(20));

        // Header
        Label title = new Label(messages.getString("pattern.builder.advanced.title"));
        title.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label description = new Label(messages.getString("pattern.builder.advanced.description"));
        description.setWrapText(true);
        description.setFont(Font.font("System", 11));
        description.setTextFill(Color.GRAY);

        // Input folder display
        Label directoryLabel = new Label(messages.getString("label.input.folder"));
        directoryLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        HBox directoryBox = new HBox(10);
        directoryBox.setAlignment(Pos.CENTER_LEFT);
        directoryBox.getChildren().add(directoryField);
        HBox.setHgrow(directoryField, Priority.ALWAYS);

        // Pattern input grid
        GridPane patternGrid = createPatternGrid();

        // Overall validation status
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.getChildren().addAll(new Label("Status:"), overallValidationStatus);

        // Extension matching option
        HBox extensionBox = new HBox(10);
        extensionBox.setAlignment(Pos.CENTER_LEFT);
        extensionBox.getChildren().add(extensionMatchingCheckBox);

        // Task 15: Add ValidationMessageBox for live validation display
        // Validation details section
        VBox validationDetails = new VBox(5);
        validationDetails.getChildren().addAll(validationMessageBox, errorDetailsPane, warningDetailsPane);

        // Preview section
        Label previewTitle = new Label(messages.getString("preview.title"));
        previewTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        VBox.setVgrow(previewPane, Priority.ALWAYS);

        getChildren().addAll(title, description, directoryLabel, directoryBox, patternGrid,
                statusBox, extensionBox, validationDetails, previewTitle, previewPane);
    }

    /**
     * Creates the pattern input grid with fields, validation, and buttons.
     */
    private GridPane createPatternGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Column constraints
        grid.getColumnConstraints().addAll(
                new javafx.scene.layout.ColumnConstraints(120), // Label column
                new javafx.scene.layout.ColumnConstraints(), // Field column (grows)
                new javafx.scene.layout.ColumnConstraints(100), // Button column
                new javafx.scene.layout.ColumnConstraints(100) // Button column
        );
        grid.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int row = 0;

        // Group Pattern
        addPatternRow(grid, row++, messages.getString("pattern.builder.advanced.group.pattern"), groupPatternField,
                groupPatternValidation,
                explainGroupPatternButton, copyGroupPatternButton);

        // Front Pattern
        addPatternRow(grid, row++, messages.getString("pattern.builder.advanced.front.pattern"), frontPatternField,
                frontPatternValidation,
                explainFrontPatternButton, copyFrontPatternButton);

        // Rear Pattern
        addPatternRow(grid, row++, messages.getString("pattern.builder.advanced.rear.pattern"), rearPatternField,
                rearPatternValidation,
                explainRearPatternButton, copyRearPatternButton);

        // Overview Pattern
        addPatternRow(grid, row++, messages.getString("pattern.builder.advanced.overview.pattern"),
                overviewPatternField, overviewPatternValidation,
                explainOverviewPatternButton, copyOverviewPatternButton);

        return grid;
    }

    /**
     * Adds a pattern input row to the grid.
     */
    private void addPatternRow(GridPane grid, int row, String labelText, TextArea field, Label validation,
            Button explainButton, Button copyButton) {
        // Label
        Label label = new Label(labelText);
        label.setFont(Font.font("System", FontWeight.BOLD, 11));
        grid.add(label, 0, row * 2);

        // Field
        grid.add(field, 1, row * 2);

        // Buttons
        grid.add(explainButton, 2, row * 2);
        grid.add(copyButton, 3, row * 2);

        // Validation label (spans all columns)
        grid.add(validation, 0, row * 2 + 1, 4, 1);
    }

    /**
     * Sets up validation for all pattern fields.
     * Task 15: Enhanced with ValidationModel for debounced validation refresh.
     */
    private void setupValidation() {
        // Task 15: Add validation listeners to all fields that trigger ValidationModel refresh
        groupPatternField.textProperty().addListener((obs, oldText, newText) -> {
            validatePatterns();
            updateValidationModel();
        });
        frontPatternField.textProperty().addListener((obs, oldText, newText) -> {
            validatePatterns();
            updateValidationModel();
        });
        rearPatternField.textProperty().addListener((obs, oldText, newText) -> {
            validatePatterns();
            updateValidationModel();
        });
        overviewPatternField.textProperty().addListener((obs, oldText, newText) -> {
            validatePatterns();
            updateValidationModel();
        });
        
        // Task 15: Bind sample filenames to validation model for automatic validation refresh
        // This will be updated when directory changes
        if (inputFolder != null && !inputFolder.trim().isEmpty()) {
            loadSampleFilenames(inputFolder);
        }
    }

    /**
     * Sets up event handlers for buttons and other interactions.
     */
    private void setupEventHandlers() {
        // Explain button handlers
        explainGroupPatternButton.setOnAction(e -> explainPattern(groupPatternField.getText(), "Group Pattern"));
        explainFrontPatternButton.setOnAction(e -> explainPattern(frontPatternField.getText(), "Front Pattern"));
        explainRearPatternButton.setOnAction(e -> explainPattern(rearPatternField.getText(), "Rear Pattern"));
        explainOverviewPatternButton
                .setOnAction(e -> explainPattern(overviewPatternField.getText(), "Overview Pattern"));

        // Copy button handlers
        copyGroupPatternButton.setOnAction(e -> copyToClipboard(groupPatternField.getText(), "Group Pattern"));
        copyFrontPatternButton.setOnAction(e -> copyToClipboard(frontPatternField.getText(), "Front Pattern"));
        copyRearPatternButton.setOnAction(e -> copyToClipboard(rearPatternField.getText(), "Rear Pattern"));
        copyOverviewPatternButton.setOnAction(e -> copyToClipboard(overviewPatternField.getText(), "Overview Pattern"));

        // Extension matching handler
        extensionMatchingCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            ValidationLogger.logConfigurationChange("Extension Matching",
                    oldVal.toString(), newVal.toString());
            validatePatterns();
            // Task 15: Trigger validation model refresh on extension matching change
            updateValidationModel();
        });

        // Configuration property listener
        configurationProperty.addListener((obs, oldConfig, newConfig) -> {
            if (newConfig != null) {
                updateFieldsFromConfiguration(newConfig);
                updatePreview();
                // Task 15: Trigger validation model refresh on configuration change
                updateValidationModel();
            }
        });

        // Validation blocker listeners
        validationBlocker.blockedProperty().addListener((obs, oldVal, newVal) -> {
            updateValidationStatusDisplay();
        });

        validationBlocker.blockingReasonProperty().addListener((obs, oldVal, newVal) -> {
            updateValidationStatusDisplay();
        });
    }

    /**
     * Updates syntax highlighting for a text area (basic implementation).
     */
    private void updateSyntaxHighlighting(TextArea textArea, String text) {
        // Remove existing style classes
        textArea.getStyleClass().removeAll("regex-valid", "regex-invalid");

        // Add style class based on regex validity
        if (text != null && !text.trim().isEmpty()) {
            try {
                Pattern.compile(text);
                textArea.getStyleClass().add("regex-valid");
            } catch (PatternSyntaxException e) {
                textArea.getStyleClass().add("regex-invalid");
            }
        }
    }

    /**
     * Validates all patterns and updates validation displays.
     */
    private void validatePatterns() {
        // Cancel any running validation task
        if (currentValidationTask != null && !currentValidationTask.isDone()) {
            currentValidationTask.cancel(true);
        }

        // Create configuration from current field values
        PatternConfiguration config = createConfigurationFromFields();

        // Start background validation
        currentValidationTask = new Task<ValidationResult>() {
            @Override
            protected ValidationResult call() throws Exception {
                // Apply extension matching if enabled
                if (extensionMatchingCheckBox.isSelected()) {
                    config.setGroupPattern(ExtensionMatcher.applyExtensionMatching(config.getGroupPattern(), true));
                    config.setFrontPattern(ExtensionMatcher.applyExtensionMatching(config.getFrontPattern(), true));
                    config.setRearPattern(ExtensionMatcher.applyExtensionMatching(config.getRearPattern(), true));
                    config.setOverviewPattern(
                            ExtensionMatcher.applyExtensionMatching(config.getOverviewPattern(), true));
                    List<RoleRule> roleRules = new ArrayList<>();
                    roleRules.add(new RoleRule(ImageRole.FRONT, RuleType.REGEX_OVERRIDE, frontPatternField.getText(),
                            false, 1));
                    roleRules.add(new RoleRule(ImageRole.REAR, RuleType.REGEX_OVERRIDE, rearPatternField.getText(),
                            false, 1));
                    roleRules.add(
                            new RoleRule(ImageRole.OVERVIEW, RuleType.REGEX_OVERRIDE, overviewPatternField.getText(),
                                    false, 1));
                    config.setRoleRules(roleRules);
                }

                return patternGenerator.validatePatterns(config);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    ValidationResult result = getValue();
                    updateValidationDisplay(result);
                    validationBlocker.updateValidationState(result);
                    updatePreview();

                    // Log validation results
                    for (ValidationError error : result.getErrors()) {
                        ValidationLogger.logValidationError(error, "Advanced Pattern Builder");
                    }

                    for (ValidationWarning warning : result.getWarnings()) {
                        ValidationLogger.logValidationWarning(warning, "Advanced Pattern Builder");
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    Throwable throwable = getException();
                    Exception exception = throwable instanceof Exception ? (Exception) throwable
                            : new Exception(throwable);
                    ValidationLogger.logException(exception, "Pattern validation failed");

                    overallValidationStatus.setText("Validation failed: " + exception.getMessage());
                    overallValidationStatus.setTextFill(Color.RED);

                    validationBlocker.clearValidationState();
                });
            }
        };

        VALIDATION_EXECUTOR.execute(currentValidationTask);
    }

    /**
     * Creates a PatternConfiguration from current field values.
     */
    private PatternConfiguration createConfigurationFromFields() {
        PatternConfiguration config = new PatternConfiguration();
        config.setGroupPattern(groupPatternField.getText());
        config.setFrontPattern(frontPatternField.getText());
        config.setRearPattern(rearPatternField.getText());
        config.setOverviewPattern(overviewPatternField.getText());
        List<RoleRule> roleRules = new ArrayList<>();
        roleRules.add(new RoleRule(ImageRole.FRONT, RuleType.REGEX_OVERRIDE, frontPatternField.getText(), false, 1));
        roleRules.add(new RoleRule(ImageRole.REAR, RuleType.REGEX_OVERRIDE, rearPatternField.getText(), false, 1));
        roleRules.add(
                new RoleRule(ImageRole.OVERVIEW, RuleType.REGEX_OVERRIDE, overviewPatternField.getText(), false, 1));
        config.setRoleRules(roleRules);
        return config;
    }

    /**
     * Updates the validation display with results.
     */
    private void updateValidationDisplay(ValidationResult result) {
        // Clear all validation labels
        clearValidationLabel(groupPatternValidation);
        clearValidationLabel(frontPatternValidation);
        clearValidationLabel(rearPatternValidation);
        clearValidationLabel(overviewPatternValidation);

        // Show errors for specific patterns
        for (ValidationError error : result.getErrors()) {
            switch (error.getType()) {
                case NO_GROUP_ID_SELECTED, INVALID_GROUP_PATTERN, EMPTY_GROUP_PATTERN,
                        MULTIPLE_CAPTURING_GROUPS, NO_CAPTURING_GROUPS, INCOMPLETE_GROUPS ->
                    showValidationMessage(groupPatternValidation, error.getMessage(), true);
                case NO_ROLE_PATTERNS, NO_ROLE_RULES_DEFINED -> {
                    // Show on all role pattern fields
                    showValidationMessage(frontPatternValidation, error.getMessage(), true);
                    showValidationMessage(rearPatternValidation, error.getMessage(), true);
                    showValidationMessage(overviewPatternValidation, error.getMessage(), true);
                }
                case REGEX_SYNTAX_ERROR, NO_FILES_MATCHED, INVALID_REGEX_PATTERN, INVALID_RULE_CONFIGURATION,
                        INVALID_RULE_VALUE -> {
                    // Try to determine which field has the syntax error
                    showRegexSyntaxError(error.getMessage());
                }
            }
        }

        // Update overall status
        if (result.isValid()) {
            if (result.hasWarnings()) {
                overallValidationStatus.setText("Valid with warnings (" + result.getWarnings().size() + ")");
                overallValidationStatus.setTextFill(Color.ORANGE);
            } else {
                overallValidationStatus.setText("All patterns valid");
                overallValidationStatus.setTextFill(Color.GREEN);
            }
            
            // Notify parent dialog that configuration is ready when valid
            if (onConfigurationReady != null) {
                PatternConfiguration config = createConfigurationFromFields();
                onConfigurationReady.accept(config);
            }
        } else {
            overallValidationStatus.setText("Validation errors (" + result.getErrors().size() + ")");
            overallValidationStatus.setTextFill(Color.RED);
        }

        // Update detailed validation panes
        updateValidationDetailPanes(result);
    }

    /**
     * Updates the validation status display based on validation blocker state.
     */
    private void updateValidationStatusDisplay() {
        String status = validationBlocker.getValidationSummary();
        overallValidationStatus.setText(status);

        // Update style based on validation state
        if (validationBlocker.isBlocked()) {
            overallValidationStatus.setTextFill(Color.RED);
        } else if (!validationBlocker.getActiveWarnings().isEmpty()) {
            overallValidationStatus.setTextFill(Color.ORANGE);
        } else {
            overallValidationStatus.setTextFill(Color.GREEN);
        }
    }

    /**
     * Updates the detailed validation panes with error and warning information.
     */
    private void updateValidationDetailPanes(ValidationResult result) {
        // Update error details pane
        if (result.hasErrors()) {
            String errorDetails = validationBlocker.getErrorDetails();
            Label errorLabel = new Label(errorDetails);
            errorLabel.setWrapText(true);
            errorDetailsPane.setContent(errorLabel);
            errorDetailsPane.setVisible(true);
            errorDetailsPane.setManaged(true);
            errorDetailsPane.setExpanded(true);
        } else {
            errorDetailsPane.setVisible(false);
            errorDetailsPane.setManaged(false);
        }

        // Update warning details pane
        if (result.hasWarnings()) {
            String warningDetails = validationBlocker.getWarningDetails();
            Label warningLabel = new Label(warningDetails);
            warningLabel.setWrapText(true);
            warningDetailsPane.setContent(warningLabel);
            warningDetailsPane.setVisible(true);
            warningDetailsPane.setManaged(true);
            warningDetailsPane.setExpanded(false);
        } else {
            warningDetailsPane.setVisible(false);
            warningDetailsPane.setManaged(false);
        }
    }

    /**
     * Shows a regex syntax error on the appropriate field.
     */
    private void showRegexSyntaxError(String message) {
        // Check each field for syntax errors
        checkFieldSyntax(groupPatternField, groupPatternValidation, "Group Pattern", message);
        checkFieldSyntax(frontPatternField, frontPatternValidation, "Front Pattern", message);
        checkFieldSyntax(rearPatternField, rearPatternValidation, "Rear Pattern", message);
        checkFieldSyntax(overviewPatternField, overviewPatternValidation, "Overview Pattern", message);
    }

    /**
     * Checks a field for regex syntax errors and shows validation message if found.
     */
    private void checkFieldSyntax(TextArea field, Label validationLabel, String fieldName, String errorMessage) {
        String text = field.getText();
        if (text != null && !text.trim().isEmpty()) {
            try {
                Pattern.compile(text);
            } catch (PatternSyntaxException e) {
                showValidationMessage(validationLabel, fieldName + " syntax error: " + e.getMessage(), true);
            }
        }
    }

    /**
     * Shows a validation message on a label.
     */
    private void showValidationMessage(Label label, String message, boolean isError) {
        label.setText(message);
        label.setTextFill(isError ? Color.RED : Color.ORANGE);
        label.setVisible(true);
        label.setManaged(true);
    }

    /**
     * Clears a validation label.
     */
    private void clearValidationLabel(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    /**
     * Updates the fields from a configuration object.
     */
    private void updateFieldsFromConfiguration(PatternConfiguration config) {
        groupPatternField.setText(config.getGroupPattern() != null ? config.getGroupPattern() : "");
        frontPatternField.setText(config.getFrontPattern() != null ? config.getFrontPattern() : "");
        rearPatternField.setText(config.getRearPattern() != null ? config.getRearPattern() : "");
        overviewPatternField.setText(config.getOverviewPattern() != null ? config.getOverviewPattern() : "");
    }

    /**
     * Updates the preview pane with current configuration.
     */
    private void updatePreview() {
        if (!sampleFilenames.isEmpty()) {
            PatternConfiguration config = createConfigurationFromFields();
            previewPane.updatePreview(config, sampleFilenames);
        }
    }

    /**
     * Explains a regex pattern in a user-friendly dialog.
     */
    private void explainPattern(String pattern, String patternName) {
        if (pattern == null || pattern.trim().isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Pattern Explanation",
                    patternName + " is empty", "Enter a regex pattern to see its explanation.");
            return;
        }

        String explanation = generatePatternExplanation(pattern);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pattern Explanation");
        alert.setHeaderText(patternName + " Explanation");

        TextArea explanationArea = new TextArea(explanation);
        explanationArea.setEditable(false);
        explanationArea.setWrapText(true);
        explanationArea.setPrefRowCount(10);
        explanationArea.setPrefColumnCount(60);

        alert.getDialogPane().setContent(explanationArea);
        alert.showAndWait();
    }

    /**
     * Generates a user-friendly explanation of a regex pattern.
     */
    private String generatePatternExplanation(String pattern) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("Pattern: ").append(pattern).append("\n\n");

        // Basic pattern analysis
        if (pattern.startsWith("^")) {
            explanation.append("• Starts with '^' - matches beginning of string\n");
        }
        if (pattern.endsWith("$")) {
            explanation.append("• Ends with '$' - matches end of string\n");
        }

        // Capturing groups
        int capturingGroups = countCapturingGroups(pattern);
        if (capturingGroups > 0) {
            explanation.append("• Contains ").append(capturingGroups)
                    .append(" capturing group").append(capturingGroups > 1 ? "s" : "")
                    .append(" - extracts matched text\n");
        }

        // Common patterns
        if (pattern.contains("\\d+")) {
            explanation.append("• '\\d+' - matches one or more digits\n");
        }
        if (pattern.contains("\\w+")) {
            explanation.append("• '\\w+' - matches one or more word characters (letters, digits, underscore)\n");
        }
        if (pattern.contains("[_\\-\\.\\s]+")) {
            explanation.append("• '[_\\-\\.\\s]+' - matches delimiters (underscore, hyphen, dot, space)\n");
        }
        if (pattern.contains("(?i:")) {
            explanation.append("• '(?i:...)' - case-insensitive matching\n");
        }
        if (pattern.contains(".*")) {
            explanation.append("• '.*' - matches any characters (zero or more)\n");
        }

        // Character classes
        if (pattern.contains("[\\w\\-]+")) {
            explanation.append("• '[\\w\\-]+' - matches word characters and hyphens\n");
        }

        explanation.append(
                "\nFor Group Pattern: The capturing group (parentheses) extracts the vehicle ID for grouping images.");

        return explanation.toString();
    }

    /**
     * Counts capturing groups in a regex pattern.
     */
    private int countCapturingGroups(String pattern) {
        if (pattern == null)
            return 0;

        int count = 0;
        boolean inCharClass = false;
        boolean escaped = false;

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '[') {
                inCharClass = true;
            } else if (c == ']') {
                inCharClass = false;
            } else if (!inCharClass && c == '(' && i + 1 < pattern.length()) {
                // Check if it's a non-capturing group
                if (pattern.charAt(i + 1) != '?') {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Copies text to the system clipboard.
     */
    private void copyToClipboard(String text, String patternName) {
        if (text == null || text.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Copy Pattern",
                    patternName + " is empty", "Enter a pattern to copy it to the clipboard.");
            return;
        }

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);

        // Show brief confirmation
        overallValidationStatus.setText(patternName + " copied to clipboard");
        overallValidationStatus.setTextFill(Color.BLUE);

        // Reset status after 2 seconds
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> validatePatterns()));
        timeline.play();
    }

    /**
     * Shows an alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Sets the pattern configuration.
     * 
     * @param configuration the pattern configuration
     */
    public void setConfiguration(PatternConfiguration configuration) {
        configurationProperty.set(configuration);
    }

    /**
     * Gets the current pattern configuration from the input fields.
     * 
     * @return the current pattern configuration
     */
    public PatternConfiguration getConfiguration() {
        return createConfigurationFromFields();
    }
    
    /**
     * Sets the configuration ready callback.
     * 
     * @param callback the callback to call when configuration is ready
     */
    public void setOnConfigurationReady(java.util.function.Consumer<PatternConfiguration> callback) {
        this.onConfigurationReady = callback;
    }

    /**
     * @return the configuration property for binding
     */
    public ObjectProperty<PatternConfiguration> configurationProperty() {
        return configurationProperty;
    }

    /**
     * Loads sample files from the selected directory.
     */
    private void loadSampleFiles(java.nio.file.Path directory) {
        try {
            sampleFilenames.clear();

            try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.list(directory)) {
                List<String> imageFiles = files
                        .filter(java.nio.file.Files::isRegularFile)
                        .map(java.nio.file.Path::getFileName)
                        .map(java.nio.file.Path::toString)
                        .filter(this::isImageFile)
                        .limit(20) // Limit for performance
                        .toList();

                sampleFilenames.addAll(imageFiles);
                updatePreview();

                ValidationLogger.logFileAnalysis(imageFiles.size(), 0, 0);
            }

        } catch (Exception e) {
            ValidationLogger.logException(e, "Failed to load sample files");
            overallValidationStatus.setText("Error loading files: " + e.getMessage());
            overallValidationStatus.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    /**
     * Checks if a filename represents an image file.
     */
    private boolean isImageFile(String filename) {
        return ExtensionMatcher.hasImageExtension(filename);
    }

    /**
     * Sets the sample filenames for preview.
     * 
     * @param filenames the sample filenames
     */
    public void setSampleFilenames(List<String> filenames) {
        this.sampleFilenames = filenames != null ? new ArrayList<>(filenames) : new ArrayList<>();
        updatePreview();
    }

    /**
     * Gets the preview pane for external access.
     * 
     * @return the pattern preview pane
     */
    public PatternPreviewPane getPreviewPane() {
        return previewPane;
    }

    /**
     * Clears all pattern fields.
     */
    public void clearPatterns() {
        groupPatternField.clear();
        frontPatternField.clear();
        rearPatternField.clear();
        overviewPatternField.clear();
    }

    /**
     * Validates the current configuration and returns the result.
     * 
     * @return the validation result
     */
    public ValidationResult validateConfiguration() {
        PatternConfiguration config = createConfigurationFromFields();
        return patternGenerator.validatePatterns(config);
    }

    /**
     * Gets the validation blocker for external access.
     * 
     * @return the validation blocker
     */
    public ValidationBlocker getValidationBlocker() {
        return validationBlocker;
    }

    /**
     * Gets the extension matching checkbox for external binding.
     * 
     * @return the extension matching checkbox
     */
    public CheckBox getExtensionMatchingCheckBox() {
        return extensionMatchingCheckBox;
    }

    /**
     * Gets whether flexible extension matching is enabled.
     * 
     * @return true if flexible extension matching is enabled
     */
    public boolean isFlexibleExtensionMatchingEnabled() {
        return extensionMatchingCheckBox.isSelected();
    }

    /**
     * Sets whether flexible extension matching is enabled.
     * 
     * @param enabled true to enable flexible extension matching
     */
    public void setFlexibleExtensionMatchingEnabled(boolean enabled) {
        extensionMatchingCheckBox.setSelected(enabled);
    }

    /**
     * Gets the selected directory path.
     * 
     * @return the selected directory path, or null if none selected
     */
    public String getSelectedDirectory() {
        return directoryField.getText();
    }

    /**
     * Sets the selected directory path and loads sample files.
     * Task 15: Enhanced to trigger validation refresh when folder changes.
     * 
     * @param directoryPath the directory path to set
     */
    public void setSelectedDirectory(String directoryPath) {
        if (directoryPath != null && !directoryPath.trim().isEmpty()) {
            directoryField.setText(directoryPath);
            loadSampleFiles(java.nio.file.Paths.get(directoryPath));
            // Task 15: Load sample filenames for validation model
            loadSampleFilenames(directoryPath);
        }
    }

    /**
     * Cleanup method to shut down background processing.
     * Task 15: Enhanced to include ValidationModel cleanup.
     */
    public void shutdown() {
        if (currentValidationTask != null && !currentValidationTask.isDone()) {
            currentValidationTask.cancel(true);
        }
        if (previewPane != null) {
            previewPane.shutdown();
        }
        // Task 15: Shutdown ValidationModel background processing
        if (validationModel != null) {
            validationModel.shutdown();
        }
        VALIDATION_EXECUTOR.shutdown();
    }

    /**
     * Task 15: Gets the ValidationModel for external access and mode coordination.
     * 
     * @return the validation model instance
     */
    public ValidationModel getValidationModel() {
        return validationModel;
    }
    
    /**
     * Task 15: Updates the ValidationModel with current configuration.
     * This triggers debounced validation refresh automatically.
     */
    private void updateValidationModel() {
        try {
            PatternConfiguration config = createConfigurationFromFields();
            // Task 15: Update configuration triggers automatic debounced validation
            validationModel.updateConfiguration(config);
        } catch (Exception e) {
            ValidationLogger.logException(e, "Failed to update validation model in AdvancedPatternBuilder");
            validationModel.updateConfiguration(null);
        }
    }
    
    /**
     * Task 15: Loads sample filenames from the specified directory for validation.
     * 
     * @param directoryPath the directory path to load filenames from
     */
    private void loadSampleFilenames(String directoryPath) {
        try {
            java.nio.file.Path directory = java.nio.file.Path.of(directoryPath);
            if (java.nio.file.Files.exists(directory) && java.nio.file.Files.isDirectory(directory)) {
                List<String> filenames = new ArrayList<>();
                
                try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.list(directory)) {
                    files.filter(java.nio.file.Files::isRegularFile)
                         .map(java.nio.file.Path::getFileName)
                         .map(java.nio.file.Path::toString)
                         .filter(this::isImageFile)
                         .limit(500) // Limit for performance
                         .forEach(filenames::add);
                }
                
                sampleFilenames = filenames;
                // Task 15: Update validation model with sample filenames
                validationModel.updateSampleFilenames(filenames);
            }
        } catch (Exception e) {
            ValidationLogger.logException(e, "Failed to load sample filenames in AdvancedPatternBuilder");
            sampleFilenames = new ArrayList<>();
            validationModel.updateSampleFilenames(new ArrayList<>());
        }
    }
    

}