package com.smartcloudsolutions.tollingvision.samples.patternbuilder;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * UI component for chip-based rule creation and dropdown selectors.
 * Provides an intuitive interface for creating role-based classification rules
 * with visual chips for each rule and easy editing capabilities.
 */
public class RoleRulesPane extends VBox {
    
    private final ObservableList<RoleRule> roleRules = FXCollections.observableArrayList();
    
    // UI Components
    private ComboBox<ImageRole> roleComboBox;
    private ComboBox<RuleType> ruleTypeComboBox;
    private TextField ruleValueField;
    private CheckBox caseSensitiveCheckBox;
    private Button addRuleButton;
    private FlowPane rulesContainer;
    private TextArea explanationArea;
    private VBox suggestionsBox;
    
    // Services
    private FilenameTokenizer tokenizer = new FilenameTokenizer();
    
    /**
     * Creates a new RoleRulesPane with rule creation and management capabilities.
     */
    public RoleRulesPane() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupValidation();
    }
    
    /**
     * Initializes all UI components.
     */
    private void initializeComponents() {
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(ImageRole.values());
        roleComboBox.setValue(ImageRole.FRONT);
        roleComboBox.setPromptText("Select role...");
        
        ruleTypeComboBox = new ComboBox<>();
        ruleTypeComboBox.getItems().addAll(RuleType.CONTAINS, RuleType.EQUALS, 
                                          RuleType.STARTS_WITH, RuleType.ENDS_WITH);
        ruleTypeComboBox.setValue(RuleType.CONTAINS);
        ruleTypeComboBox.setPromptText("Select rule type...");
        
        ruleValueField = new TextField();
        ruleValueField.setPromptText("Enter text to match...");
        
        caseSensitiveCheckBox = new CheckBox("Case sensitive");
        caseSensitiveCheckBox.setSelected(false);
        
        addRuleButton = new Button("Add Rule");
        addRuleButton.setDefaultButton(true);
        
        rulesContainer = new FlowPane();
        rulesContainer.setHgap(10);
        rulesContainer.setVgap(10);
        rulesContainer.setPadding(new Insets(15));
        rulesContainer.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        
        explanationArea = new TextArea();
        explanationArea.setEditable(false);
        explanationArea.setPrefRowCount(4);
        explanationArea.setWrapText(true);
        explanationArea.setText(
            "Create rules to identify different image types within each vehicle group:\n\n" +
            "• OVERVIEW rules are applied first and exclude matched files from front/rear consideration\n" +
            "• FRONT and REAR rules only consider files not already matched by overview rules\n" +
            "• Use CONTAINS for partial matches, EQUALS for exact matches\n" +
            "• STARTS_WITH and ENDS_WITH match the beginning or end of filenames"
        );
        
        // Suggestions box for detected tokens
        suggestionsBox = new VBox(10);
        suggestionsBox.setPadding(new Insets(15));
        suggestionsBox.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-radius: 5;");
        suggestionsBox.setVisible(false);
        suggestionsBox.setManaged(false);
    }
    
    /**
     * Sets up the layout structure.
     */
    private void setupLayout() {
        setSpacing(20);
        setPadding(new Insets(20));
        
        // Title
        Label title = new Label("Step 3: Role Rules");
        title.getStyleClass().add("step-title");
        
        // Description
        Label description = new Label(
            "Define rules to classify images by their role (front, rear, overview). " +
            "Rules are applied in precedence order: overview first, then front and rear."
        );
        description.setWrapText(true);
        description.getStyleClass().add("step-description");
        
        // Rule creation form
        Label formLabel = new Label("Create New Rule:");
        formLabel.getStyleClass().add("section-label");
        
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(15));
        formGrid.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        
        // Form labels and controls
        formGrid.add(new Label("Image Role:"), 0, 0);
        formGrid.add(roleComboBox, 1, 0);
        
        formGrid.add(new Label("Rule Type:"), 0, 1);
        formGrid.add(ruleTypeComboBox, 1, 1);
        
        formGrid.add(new Label("Match Text:"), 0, 2);
        formGrid.add(ruleValueField, 1, 2);
        
        formGrid.add(caseSensitiveCheckBox, 1, 3);
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(addRuleButton);
        formGrid.add(buttonBox, 1, 4);
        
        // Configure column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        formGrid.getColumnConstraints().addAll(col1, col2);
        
        // Current rules section
        Label rulesLabel = new Label("Current Rules:");
        rulesLabel.getStyleClass().add("section-label");
        
        ScrollPane rulesScrollPane = new ScrollPane(rulesContainer);
        rulesScrollPane.setFitToWidth(true);
        rulesScrollPane.setPrefHeight(150);
        rulesScrollPane.setStyle("-fx-background: transparent;");
        
        // Explanation section
        Label explanationLabel = new Label("Rule Precedence:");
        explanationLabel.getStyleClass().add("section-label");
        
        getChildren().addAll(
            title,
            description,
            suggestionsBox, // Add suggestions box
            formLabel,
            formGrid,
            rulesLabel,
            rulesScrollPane,
            explanationLabel,
            explanationArea
        );
    }
    
    /**
     * Sets up event handlers for user interactions.
     */
    private void setupEventHandlers() {
        addRuleButton.setOnAction(e -> addRule());
        
        // Allow Enter key to add rule
        ruleValueField.setOnAction(e -> addRule());
        
        // Update button state when form changes
        ruleValueField.textProperty().addListener((obs, oldVal, newVal) -> updateAddButtonState());
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateAddButtonState());
        ruleTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateAddButtonState());
    }
    
    /**
     * Sets up form validation.
     */
    private void setupValidation() {
        updateAddButtonState();
    }
    
    /**
     * Updates the add button state based on form validation.
     */
    private void updateAddButtonState() {
        boolean isValid = roleComboBox.getValue() != null &&
                         ruleTypeComboBox.getValue() != null &&
                         ruleValueField.getText() != null &&
                         !ruleValueField.getText().trim().isEmpty();
        
        addRuleButton.setDisable(!isValid);
    }
    
    /**
     * Adds a new rule based on current form values.
     */
    private void addRule() {
        if (!validateForm()) {
            return;
        }
        
        RoleRule rule = new RoleRule(
            roleComboBox.getValue(),
            ruleTypeComboBox.getValue(),
            ruleValueField.getText().trim(),
            caseSensitiveCheckBox.isSelected(),
            calculatePriority(roleComboBox.getValue())
        );
        
        roleRules.add(rule);
        addRuleChip(rule);
        clearForm();
    }
    
    /**
     * Validates the current form state.
     */
    private boolean validateForm() {
        if (roleComboBox.getValue() == null) {
            showValidationError("Please select an image role");
            return false;
        }
        
        if (ruleTypeComboBox.getValue() == null) {
            showValidationError("Please select a rule type");
            return false;
        }
        
        String ruleValue = ruleValueField.getText();
        if (ruleValue == null || ruleValue.trim().isEmpty()) {
            showValidationError("Please enter text to match");
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculates priority for a rule based on image role precedence.
     */
    private int calculatePriority(ImageRole role) {
        return switch (role) {
            case OVERVIEW -> 1; // Highest priority
            case FRONT -> 2;
            case REAR -> 3;
        };
    }
    
    /**
     * Adds a visual chip for the rule.
     */
    private void addRuleChip(RoleRule rule) {
        HBox chip = createRuleChip(rule);
        rulesContainer.getChildren().add(chip);
    }
    
    /**
     * Creates a visual chip representation of a rule.
     */
    private HBox createRuleChip(RoleRule rule) {
        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(8, 12, 8, 12));
        chip.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 20; -fx-border-color: %s; -fx-border-radius: 20;",
            getRoleColor(rule.getTargetRole(), 0.1),
            getRoleColor(rule.getTargetRole(), 1.0)
        ));
        
        // Role label
        Label roleLabel = new Label(formatRoleName(rule.getTargetRole()));
        roleLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 12px;",
                                        getRoleColor(rule.getTargetRole(), 1.0)));
        
        // Rule description
        Label ruleLabel = new Label(formatRuleDescription(rule));
        ruleLabel.setStyle("-fx-text-fill: #495057; -fx-font-size: 11px;");
        
        // Remove button
        Button removeButton = new Button("×");
        removeButton.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #dc3545; " +
            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 4; " +
            "-fx-cursor: hand; -fx-background-radius: 10;"
        );
        removeButton.setOnAction(e -> removeRule(rule, chip));
        
        chip.getChildren().addAll(roleLabel, new Label("•"), ruleLabel, removeButton);
        
        // Add tooltip with full rule details
        Tooltip tooltip = new Tooltip(String.format(
            "Role: %s\nType: %s\nValue: %s\nCase Sensitive: %s\nPriority: %d",
            formatRoleName(rule.getTargetRole()),
            formatRuleTypeName(rule.getRuleType()),
            rule.getRuleValue(),
            rule.isCaseSensitive() ? "Yes" : "No",
            rule.getPriority()
        ));
        Tooltip.install(chip, tooltip);
        
        return chip;
    }
    
    /**
     * Removes a rule and its visual representation.
     */
    private void removeRule(RoleRule rule, HBox chip) {
        roleRules.remove(rule);
        rulesContainer.getChildren().remove(chip);
    }
    
    /**
     * Clears the form after adding a rule.
     */
    private void clearForm() {
        ruleValueField.clear();
        caseSensitiveCheckBox.setSelected(false);
        ruleValueField.requestFocus();
    }
    
    /**
     * Shows a validation error message.
     */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Invalid Rule");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Formats role name for display.
     */
    private String formatRoleName(ImageRole role) {
        return switch (role) {
            case FRONT -> "Front";
            case REAR -> "Rear";
            case OVERVIEW -> "Overview";
        };
    }
    
    /**
     * Formats rule type name for display.
     */
    private String formatRuleTypeName(RuleType ruleType) {
        return switch (ruleType) {
            case EQUALS -> "Equals";
            case CONTAINS -> "Contains";
            case STARTS_WITH -> "Starts With";
            case ENDS_WITH -> "Ends With";
            case REGEX_OVERRIDE -> "Regex";
        };
    }
    
    /**
     * Formats rule description for chip display.
     */
    private String formatRuleDescription(RoleRule rule) {
        String typeText = switch (rule.getRuleType()) {
            case EQUALS -> "equals";
            case CONTAINS -> "contains";
            case STARTS_WITH -> "starts with";
            case ENDS_WITH -> "ends with";
            case REGEX_OVERRIDE -> "matches";
        };
        
        String caseText = rule.isCaseSensitive() ? "" : " (ignore case)";
        return String.format("%s \"%s\"%s", typeText, rule.getRuleValue(), caseText);
    }
    
    /**
     * Gets color for image role visualization.
     */
    private String getRoleColor(ImageRole role, double opacity) {
        String baseColor = switch (role) {
            case FRONT -> "0, 123, 255"; // Blue
            case REAR -> "220, 53, 69";  // Red
            case OVERVIEW -> "40, 167, 69"; // Green
        };
        
        return String.format("rgba(%s, %.1f)", baseColor, opacity);
    }
    
    /**
     * Sets the role rules and updates the display.
     */
    public void setRoleRules(ObservableList<RoleRule> rules) {
        roleRules.clear();
        rulesContainer.getChildren().clear();
        
        if (rules != null) {
            roleRules.addAll(rules);
            for (RoleRule rule : rules) {
                addRuleChip(rule);
            }
        }
    }
    
    /**
     * @return the observable list of role rules
     */
    public ObservableList<RoleRule> getRoleRules() {
        return roleRules;
    }
    
    /**
     * Analyzes detected tokens and suggests rules for front tokens and camera/side identifiers.
     * 
     * @param detectedTokens the list of detected tokens from filename analysis
     */
    public void suggestRulesFromTokens(java.util.List<FilenameToken> detectedTokens) {
        if (detectedTokens == null || detectedTokens.isEmpty()) {
            hideSuggestions();
            return;
        }

        java.util.List<RuleSuggestion> suggestions = new java.util.ArrayList<>();
        
        // Look for camera/side tokens and suggest appropriate rules
        for (FilenameToken token : detectedTokens) {
            if (token.getSuggestedType() == TokenType.CAMERA_SIDE) {
                ImageRole role = tokenizer.getImageRoleForToken(token.getValue());
                if (role != null) {
                    // Check if this rule already exists (dedupe)
                    boolean alreadyExists = roleRules.stream().anyMatch(existingRule ->
                        existingRule.getTargetRole() == role &&
                        existingRule.getRuleType() == RuleType.CONTAINS &&
                        existingRule.getRuleValue().equalsIgnoreCase(token.getValue())
                    );
                    
                    if (!alreadyExists) {
                        suggestions.add(new RuleSuggestion(
                            role,
                            RuleType.CONTAINS,
                            token.getValue(),
                            false,
                            String.format("Detected %s token: '%s'", role.name().toLowerCase(), token.getValue())
                        ));
                    }
                }
            }
        }
        
        // Hide suggestions panel entirely if no unique suggestions exist
        if (suggestions.isEmpty()) {
            hideSuggestions();
        } else {
            showSuggestions(suggestions);
        }
    }
    
    /**
     * Shows rule suggestions to the user.
     */
    private void showSuggestions(java.util.List<RuleSuggestion> suggestions) {
        suggestionsBox.getChildren().clear();
        
        Label suggestionsTitle = new Label("💡 Suggested Rules");
        suggestionsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1976d2;");
        
        Label suggestionsDesc = new Label("Based on detected tokens, we suggest these rules:");
        suggestionsDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #424242;");
        
        suggestionsBox.getChildren().addAll(suggestionsTitle, suggestionsDesc);
        
        for (RuleSuggestion suggestion : suggestions) {
            HBox suggestionBox = createSuggestionBox(suggestion);
            suggestionsBox.getChildren().add(suggestionBox);
        }
        
        suggestionsBox.setVisible(true);
        suggestionsBox.setManaged(true);
    }
    
    /**
     * Hides rule suggestions.
     */
    private void hideSuggestions() {
        suggestionsBox.setVisible(false);
        suggestionsBox.setManaged(false);
    }
    
    /**
     * Refreshes suggestions based on current rules and detected tokens.
     * Should be called when rules are added or removed.
     */
    public void refreshSuggestions(java.util.List<FilenameToken> detectedTokens) {
        suggestRulesFromTokens(detectedTokens);
    }
    
    /**
     * Creates a suggestion box for a rule suggestion.
     */
    private HBox createSuggestionBox(RuleSuggestion suggestion) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 3;");
        
        Label suggestionLabel = new Label(String.format(
            "%s %s \"%s\"",
            formatRoleName(suggestion.role),
            suggestion.ruleType.name().toLowerCase().replace("_", " "),
            suggestion.value
        ));
        suggestionLabel.setStyle("-fx-font-size: 12px;");
        
        Label reasonLabel = new Label(suggestion.reason);
        reasonLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
        
        Button addButton = new Button("Add");
        addButton.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
        addButton.setOnAction(e -> {
            applySuggestion(suggestion);
            box.setVisible(false);
            box.setManaged(false);
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        VBox textBox = new VBox(2);
        textBox.getChildren().addAll(suggestionLabel, reasonLabel);
        
        box.getChildren().addAll(textBox, spacer, addButton);
        return box;
    }
    
    /**
     * Applies a rule suggestion.
     */
    private void applySuggestion(RuleSuggestion suggestion) {
        RoleRule rule = new RoleRule(
            suggestion.role,
            suggestion.ruleType,
            suggestion.value,
            suggestion.caseSensitive,
            calculatePriority(suggestion.role)
        );
        
        // Check if rule already exists
        boolean exists = roleRules.stream()
            .anyMatch(existing -> existing.getTargetRole() == rule.getTargetRole() &&
                                existing.getRuleType() == rule.getRuleType() &&
                                existing.getRuleValue().equals(rule.getRuleValue()));
        
        if (!exists) {
            roleRules.add(rule);
            addRuleChip(rule);
        }
    }
    
    /**
     * Represents a rule suggestion.
     */
    private static class RuleSuggestion {
        final ImageRole role;
        final RuleType ruleType;
        final String value;
        final boolean caseSensitive;
        final String reason;
        
        RuleSuggestion(ImageRole role, RuleType ruleType, String value, boolean caseSensitive, String reason) {
            this.role = role;
            this.ruleType = ruleType;
            this.value = value;
            this.caseSensitive = caseSensitive;
            this.reason = reason;
        }
    }
}