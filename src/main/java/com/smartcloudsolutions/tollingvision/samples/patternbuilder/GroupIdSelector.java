package com.smartcloudsolutions.tollingvision.samples.patternbuilder;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * UI component for Group ID selection with required selection validation and
 * blocking logic.
 * Provides a clear interface for users to select which token should be used as
 * the
 * vehicle group identifier, with validation to ensure exactly one token is
 * selected.
 */
public class GroupIdSelector extends VBox {

    private final ObjectProperty<FilenameToken> selectedGroupId = new SimpleObjectProperty<>();
    private final ObservableList<FilenameToken> availableTokens = FXCollections.observableArrayList();

    // UI Components
    private ListView<FilenameToken> tokenListView;
    private TextArea previewArea;
    private Label validationLabel;
    private Button clearSelectionButton;

    /**
     * Creates a new GroupIdSelector with token selection and validation.
     */
    public GroupIdSelector() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupValidation();
    }

    /**
     * Initializes all UI components.
     */
    private void initializeComponents() {
        tokenListView = new ListView<>();
        tokenListView.setPrefHeight(200);
        tokenListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tokenListView.setCellFactory(listView -> new TokenListCell());

        previewArea = new TextArea();
        previewArea.setEditable(false);
        previewArea.setPrefRowCount(6);
        previewArea.setWrapText(true);
        previewArea.setPromptText("Select a token to see the generated group pattern preview...");

        validationLabel = new Label();
        validationLabel.getStyleClass().add("validation-label");
        validationLabel.setWrapText(true);

        clearSelectionButton = new Button("Clear Selection");
        clearSelectionButton.setDisable(true);
    }

    /**
     * Sets up the layout structure.
     */
    private void setupLayout() {
        setSpacing(20);
        setPadding(new Insets(20));

        // Title
        Label title = new Label("Step 2: Group ID Selection");
        title.getStyleClass().add("step-title");

        // Description
        Label description = new Label(
                "Select the token that uniquely identifies each vehicle group. This token will be " +
                        "used to group images of the same vehicle together. Choose a token that has different " +
                        "values for different vehicles but the same value for all images of the same vehicle.");
        description.setWrapText(true);
        description.getStyleClass().add("step-description");

        // Requirements box
        VBox requirementsBox = new VBox(5);
        requirementsBox.setPadding(new Insets(15));
        requirementsBox.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffeaa7; -fx-border-radius: 5;");

        Label requirementsTitle = new Label("Requirements:");
        requirementsTitle.setStyle("-fx-font-weight: bold;");

        Label requirement1 = new Label("• Must select exactly one token as Group ID");
        Label requirement2 = new Label("• Token should have unique values for different vehicles");
        Label requirement3 = new Label("• Token should be consistent across all images of the same vehicle");

        requirementsBox.getChildren().addAll(requirementsTitle, requirement1, requirement2, requirement3);

        // Token selection section
        Label tokensLabel = new Label("Available Tokens:");
        tokensLabel.getStyleClass().add("section-label");

        HBox selectionControls = new HBox(10);
        selectionControls.setAlignment(Pos.CENTER_LEFT);
        selectionControls.getChildren().add(clearSelectionButton);

        // Preview section
        Label previewLabel = new Label("Group Pattern Preview:");
        previewLabel.getStyleClass().add("section-label");

        getChildren().addAll(
                title,
                description,
                requirementsBox,
                tokensLabel,
                tokenListView,
                selectionControls,
                validationLabel,
                previewLabel,
                previewArea);
    }

    /**
     * Sets up event handlers for user interactions.
     */
    private void setupEventHandlers() {
        // Handle token selection
        tokenListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedGroupId.set(newVal);
            clearSelectionButton.setDisable(newVal == null);
            updatePreview();
            updateValidation();
        });

        // Handle clear selection
        clearSelectionButton.setOnAction(e -> {
            tokenListView.getSelectionModel().clearSelection();
        });
    }

    /**
     * Sets up validation logic and feedback.
     */
    private void setupValidation() {
        selectedGroupId.addListener((obs, oldVal, newVal) -> updateValidation());
        updateValidation(); // Initial validation
    }

    /**
     * Updates validation status and feedback.
     */
    private void updateValidation() {
        if (selectedGroupId.get() == null) {
            validationLabel.setText("⚠ Please select a token to use as Group ID");
            validationLabel.setStyle("-fx-text-fill: #856404; -fx-background-color: #fff3cd; " +
                    "-fx-padding: 8; -fx-background-radius: 3;");
        } else {
            FilenameToken token = selectedGroupId.get();
            if (token.getSuggestedType() == TokenType.GROUP_ID) {
                validationLabel.setText("✓ Good choice! This token appears to be a unique identifier");
                validationLabel.setStyle("-fx-text-fill: #155724; -fx-background-color: #d4edda; " +
                        "-fx-padding: 8; -fx-background-radius: 3;");
            } else if (token.getSuggestedType() == TokenType.INDEX ||
                    token.getSuggestedType() == TokenType.DATE) {
                validationLabel.setText("⚠ This token might work, but verify it uniquely identifies vehicles");
                validationLabel.setStyle("-fx-text-fill: #856404; -fx-background-color: #fff3cd; " +
                        "-fx-padding: 8; -fx-background-radius: 3;");
            } else {
                validationLabel.setText("⚠ This token type is not typically used for grouping. " +
                        "Consider selecting a GROUP_ID, INDEX, or DATE token instead");
                validationLabel.setStyle("-fx-text-fill: #721c24; -fx-background-color: #f8d7da; " +
                        "-fx-padding: 8; -fx-background-radius: 3;");
            }
        }
    }

    /**
     * Updates the group pattern preview based on selected token.
     */
    private void updatePreview() {
        if (selectedGroupId.get() == null || availableTokens.isEmpty()) {
            previewArea.setText("Select a token to see the generated group pattern preview...");
            return;
        }

        FilenameToken groupToken = selectedGroupId.get();

        StringBuilder preview = new StringBuilder();
        preview.append("SELECTED GROUP ID TOKEN\n");
        preview.append("=======================\n\n");

        preview.append(String.format("Token: %s\n", groupToken.getValue()));
        preview.append(String.format("Type: %s\n", formatTokenTypeName(groupToken.getSuggestedType())));
        preview.append(String.format("Position: %d\n", groupToken.getPosition()));
        preview.append(String.format("Confidence: %.1f%%\n\n", groupToken.getConfidence() * 100));

        preview.append("GENERATED GROUP PATTERN\n");
        preview.append("=======================\n\n");

        // Generate a simple pattern preview
        String pattern = generateGroupPatternPreview(groupToken);
        preview.append(String.format("Regex Pattern: %s\n\n", pattern));

        preview.append("PATTERN EXPLANATION\n");
        preview.append("===================\n\n");

        preview.append("This pattern will:\n");
        preview.append("• Match filenames with the same structure\n");
        preview.append(String.format("• Extract the value at position %d as the group ID\n", groupToken.getPosition()));
        preview.append("• Use capturing group (parentheses) to identify the group ID value\n");
        preview.append("• Group all images with the same group ID value together\n\n");

        preview.append("EXAMPLE MATCHES\n");
        preview.append("===============\n\n");

        // Show example matches
        preview.append("If your filenames look like:\n");
        preview.append("• vehicle_001_front.jpg → Group ID: 001\n");
        preview.append("• vehicle_001_rear.jpg → Group ID: 001\n");
        preview.append("• vehicle_002_front.jpg → Group ID: 002\n\n");

        preview.append("Then images with Group ID '001' will be processed together,\n");
        preview.append("and images with Group ID '002' will be processed together.");

        previewArea.setText(preview.toString());
    }

    /**
     * Generates a preview of the group pattern regex.
     */
    private String generateGroupPatternPreview(FilenameToken groupToken) {
        StringBuilder pattern = new StringBuilder();

        // Build pattern based on token positions
        for (int i = 0; i < availableTokens.size(); i++) {
            if (i > 0) {
                pattern.append("[_\\-\\.\\s]+"); // Delimiter pattern
            }

            if (i == groupToken.getPosition()) {
                // This is the group ID token - add capturing group
                pattern.append("([^_\\-\\.\\s]+)");
            } else {
                // Other tokens - match but don't capture
                pattern.append("[^_\\-\\.\\s]+");
            }
        }

        return pattern.toString();
    }

    /**
     * Formats token type name for display.
     */
    private String formatTokenTypeName(TokenType tokenType) {
        return switch (tokenType) {
            case PREFIX -> "Prefix";
            case SUFFIX -> "Suffix";
            case GROUP_ID -> "Group ID";
            case CAMERA_SIDE -> "Camera/Side";
            case DATE -> "Date";
            case INDEX -> "Index";
            case EXTENSION -> "Extension";
            case UNKNOWN -> "Unknown";
        };
    }

    /**
     * Sets the available tokens for selection.
     */
    public void setAvailableTokens(List<FilenameToken> tokens) {
        availableTokens.clear();
        if (tokens != null) {
            availableTokens.addAll(tokens);
        }
        tokenListView.setItems(availableTokens);

        // Auto-select GROUP_ID token if available
        tokens.stream()
                .filter(token -> token.getSuggestedType() == TokenType.GROUP_ID)
                .findFirst()
                .ifPresent(token -> tokenListView.getSelectionModel().select(token));
    }

    /**
     * @return the selected group ID token property
     */
    public ObjectProperty<FilenameToken> selectedGroupIdProperty() {
        return selectedGroupId;
    }

    /**
     * @return the currently selected group ID token
     */
    public FilenameToken getSelectedGroupId() {
        return selectedGroupId.get();
    }

    /**
     * Custom list cell for displaying tokens with type information.
     */
    private static class TokenListCell extends ListCell<FilenameToken> {
        @Override
        protected void updateItem(FilenameToken token, boolean empty) {
            super.updateItem(token, empty);

            if (empty || token == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                HBox content = new HBox(10);
                content.setAlignment(Pos.CENTER_LEFT);

                // Token value
                Label valueLabel = new Label(token.getValue());
                valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                // Token type badge
                Label typeLabel = new Label(formatTokenTypeName(token.getSuggestedType()));
                typeLabel.setStyle(String.format(
                        "-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 2 8; " +
                                "-fx-background-radius: 10; -fx-font-size: 11px;",
                        getTokenTypeColor(token.getSuggestedType())));

                // Confidence indicator
                Label confidenceLabel = new Label(String.format("%.1f%%", token.getConfidence() * 100));
                confidenceLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

                content.getChildren().addAll(valueLabel, typeLabel, confidenceLabel);
                setGraphic(content);
                setText(null);

                // Highlight recommended tokens
                if (token.getSuggestedType() == TokenType.GROUP_ID) {
                    setStyle("-fx-background-color: #e8f5e8;");
                } else {
                    setStyle("");
                }
            }
        }

        private static String formatTokenTypeName(TokenType tokenType) {
            return switch (tokenType) {
                case PREFIX -> "Prefix";
                case SUFFIX -> "Suffix";
                case GROUP_ID -> "Group ID";
                case CAMERA_SIDE -> "Camera/Side";
                case DATE -> "Date";
                case INDEX -> "Index";
                case EXTENSION -> "Extension";
                case UNKNOWN -> "Unknown";
            };
        }

        private static String getTokenTypeColor(TokenType tokenType) {
            return switch (tokenType) {
                case PREFIX -> "#6c757d";
                case SUFFIX -> "#6c757d";
                case GROUP_ID -> "#dc3545";
                case CAMERA_SIDE -> "#fd7e14";
                case DATE -> "#20c997";
                case INDEX -> "#0d6efd";
                case EXTENSION -> "#6f42c1";
                case UNKNOWN -> "#adb5bd";
            };
        }
    }
}