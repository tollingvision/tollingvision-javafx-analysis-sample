package com.smartcloudsolutions.tollingvision.samples.patternbuilder;

import java.util.List;
import java.util.ResourceBundle;

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
 * the vehicle group
 * identifier, with validation to ensure exactly one token is selected.
 */
public class GroupIdSelector extends VBox {

    private final ResourceBundle messages;
    private final ObjectProperty<FilenameToken> selectedGroupId = new SimpleObjectProperty<>();
    private final ObservableList<FilenameToken> availableTokens = FXCollections.observableArrayList();

    // UI Components
    private ListView<FilenameToken> tokenListView;
    private TextArea previewArea;
    private Label validationLabel;
    private Button clearSelectionButton;

    /** Creates a new GroupIdSelector with token selection and validation. */
    public GroupIdSelector() {
        this(java.util.ResourceBundle.getBundle("messages"));
    }

    public GroupIdSelector(ResourceBundle messages) {
        this.messages = messages;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupValidation();
    }

    /** Initializes all UI components. */
    private void initializeComponents() {
        tokenListView = new ListView<>();
        tokenListView.setPrefHeight(200);
        tokenListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tokenListView.setCellFactory(listView -> new TokenListCell());

        previewArea = new TextArea();
        previewArea.setEditable(false);
        previewArea.setPrefRowCount(6);
        previewArea.setWrapText(true);
        previewArea.setPromptText(messages.getString("group.id.preview.prompt"));

        validationLabel = new Label();
        validationLabel.getStyleClass().add("validation-label");
        validationLabel.setWrapText(true);

        clearSelectionButton = new Button(messages.getString("button.clear.selection"));
        clearSelectionButton.setDisable(true);
    }

    /** Sets up the layout structure. */
    private void setupLayout() {
        setSpacing(20);
        setPadding(new Insets(20));

        // Title
        Label title = new Label(messages.getString("group.id.title"));
        title.getStyleClass().add("step-title");

        // Description
        Label description = new Label(messages.getString("group.id.description"));
        description.setWrapText(true);
        description.getStyleClass().add("step-description");

        // Requirements box
        VBox requirementsBox = new VBox(5);
        requirementsBox.setPadding(new Insets(15));
        requirementsBox.setStyle(
                "-fx-background-color: #fff3cd; -fx-border-color: #ffeaa7; -fx-border-radius: 5;");

        Label requirementsTitle = new Label(messages.getString("group.id.requirements.title"));
        requirementsTitle.setStyle("-fx-font-weight: bold;");

        Label requirement1 = new Label(messages.getString("group.id.requirement.1"));
        Label requirement2 = new Label(messages.getString("group.id.requirement.2"));
        Label requirement3 = new Label(messages.getString("group.id.requirement.3"));

        requirementsBox
                .getChildren()
                .addAll(requirementsTitle, requirement1, requirement2, requirement3);

        // Token selection section
        Label tokensLabel = new Label(messages.getString("group.id.tokens.label"));
        tokensLabel.getStyleClass().add("section-label");

        HBox selectionControls = new HBox(10);
        selectionControls.setAlignment(Pos.CENTER_LEFT);
        selectionControls.getChildren().add(clearSelectionButton);

        // Preview section
        Label previewLabel = new Label(messages.getString("group.id.preview.label"));
        previewLabel.getStyleClass().add("section-label");

        getChildren()
                .addAll(
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

    /** Sets up event handlers for user interactions. */
    private void setupEventHandlers() {
        // Handle token selection
        tokenListView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            selectedGroupId.set(newVal);
                            clearSelectionButton.setDisable(newVal == null);
                            updatePreview();
                            updateValidation();
                        });

        // Handle clear selection
        clearSelectionButton.setOnAction(
                e -> {
                    tokenListView.getSelectionModel().clearSelection();
                });
    }

    /** Sets up validation logic and feedback. */
    private void setupValidation() {
        selectedGroupId.addListener((obs, oldVal, newVal) -> updateValidation());
        updateValidation(); // Initial validation
    }

    /** Updates validation status and feedback. */
    private void updateValidation() {
        if (selectedGroupId.get() == null) {
            validationLabel.setText(messages.getString("validation.error.no.group.id"));
            validationLabel.setStyle(
                    "-fx-text-fill: #856404; -fx-background-color: #fff3cd; "
                            + "-fx-padding: 8; -fx-background-radius: 3;");
        } else {
            FilenameToken token = selectedGroupId.get();
            if (token.getSuggestedType() == TokenType.GROUP_ID) {
                validationLabel.setText(messages.getString("group.id.validation.good"));
                validationLabel.setStyle(
                        "-fx-text-fill: #155724; -fx-background-color: #d4edda; "
                                + "-fx-padding: 8; -fx-background-radius: 3;");
            } else if (token.getSuggestedType() == TokenType.INDEX
                    || token.getSuggestedType() == TokenType.DATE) {
                validationLabel.setText(messages.getString("group.id.validation.maybe"));
                validationLabel.setStyle(
                        "-fx-text-fill: #856404; -fx-background-color: #fff3cd; "
                                + "-fx-padding: 8; -fx-background-radius: 3;");
            } else {
                validationLabel.setText(messages.getString("group.id.validation.bad"));
                validationLabel.setStyle(
                        "-fx-text-fill: #721c24; -fx-background-color: #f8d7da; "
                                + "-fx-padding: 8; -fx-background-radius: 3;");
            }
        }
    }

    /** Updates the group pattern preview based on selected token. */
    private void updatePreview() {
        if (selectedGroupId.get() == null || availableTokens.isEmpty()) {
            previewArea.setText(messages.getString("group.id.preview.prompt"));
            return;
        }

        FilenameToken groupToken = selectedGroupId.get();

        StringBuilder preview = new StringBuilder();
        preview.append(messages.getString("group.id.preview.selected.title")).append('\n');
        preview.append("=======================\n\n");

        preview
                .append(String.format(messages.getString("group.id.preview.token"), groupToken.getValue()))
                .append('\n');
        preview
                .append(
                        String.format(
                                messages.getString("group.id.preview.type"),
                                formatTokenTypeName(groupToken.getSuggestedType())))
                .append('\n');
        preview
                .append(
                        String.format(
                                messages.getString("group.id.preview.position"), groupToken.getPosition()))
                .append('\n');
        preview
                .append(
                        String.format(
                                messages.getString("group.id.preview.confidence"),
                                groupToken.getConfidence() * 100))
                .append("%\n\n");

        preview.append(messages.getString("group.id.preview.generated.title")).append('\n');
        preview.append("=======================\n\n");

        // Generate a simple pattern preview
        String pattern = generateGroupPatternPreview(groupToken);
        preview
                .append(String.format(messages.getString("group.id.preview.regex"), pattern))
                .append('\n')
                .append('\n');

        preview.append(messages.getString("group.id.preview.explanation.title")).append('\n');
        preview.append("===================\n\n");

        preview.append(messages.getString("group.id.preview.explanation.intro")).append('\n');
        preview
                .append(messages.getString("group.id.preview.explanation.bullet.structure"))
                .append('\n');
        preview
                .append(
                        String.format(
                                messages.getString("group.id.preview.explanation.bullet.extract"),
                                groupToken.getPosition()))
                .append('\n');
        preview.append(messages.getString("group.id.preview.explanation.bullet.capture")).append('\n');
        preview
                .append(messages.getString("group.id.preview.explanation.bullet.grouping"))
                .append('\n')
                .append('\n');

        preview.append(messages.getString("group.id.preview.examples.title")).append('\n');
        preview.append("===============\n\n");

        // Show example matches
        preview.append(messages.getString("group.id.preview.examples.intro")).append('\n');
        preview.append(messages.getString("group.id.preview.examples.line1")).append('\n');
        preview.append(messages.getString("group.id.preview.examples.line2")).append('\n');
        preview.append(messages.getString("group.id.preview.examples.line3")).append('\n').append('\n');

        preview.append(messages.getString("group.id.preview.examples.summary.line1")).append('\n');
        preview.append(messages.getString("group.id.preview.examples.summary.line2"));

        previewArea.setText(preview.toString());
    }

    /** Generates a preview of the group pattern regex. */
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

    /** Formats token type name for display. */
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

    /** Sets the available tokens for selection. */
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

    /** Custom list cell for displaying tokens with type information. */
    private class TokenListCell extends ListCell<FilenameToken> {
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
                typeLabel.setStyle(
                        String.format(
                                "-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 2 8; "
                                        + "-fx-background-radius: 10; -fx-font-size: 11px;",
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

        private String formatTokenTypeName(TokenType tokenType) {
            return switch (tokenType) {
                case PREFIX -> messages.getString("token.type.prefix");
                case SUFFIX -> messages.getString("token.type.suffix");
                case GROUP_ID -> messages.getString("token.type.group.id");
                case CAMERA_SIDE -> messages.getString("token.type.camera.side");
                case DATE -> messages.getString("token.type.date");
                case INDEX -> messages.getString("token.type.index");
                case EXTENSION -> messages.getString("token.type.extension");
                case UNKNOWN -> messages.getString("token.type.unknown");
            };
        }

        private String getTokenTypeColor(TokenType tokenType) {
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
