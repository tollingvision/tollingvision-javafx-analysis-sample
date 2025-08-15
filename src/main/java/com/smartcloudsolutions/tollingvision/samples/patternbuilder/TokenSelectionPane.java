package com.smartcloudsolutions.tollingvision.samples.patternbuilder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * UI component for visual token display and drag-and-drop interaction. Allows
 * users to see detected
 * tokens and their suggested types, with the ability to modify token types
 * through drag-and-drop or
 * selection.
 */
public class TokenSelectionPane extends VBox {

  private TokenAnalysis tokenAnalysis;
  private FlowPane tokenDisplay;
  private VBox tokenTypesPanel;
  private TextArea explanationArea;
  private CustomTokenManager customTokenManager;
  private FilenameTokenizer tokenizer;

  /** Creates a new TokenSelectionPane with visual token display. */
  public TokenSelectionPane() {
    this.customTokenManager = new CustomTokenManager();
    this.tokenizer = new FilenameTokenizer();

    initializeComponents();
    setupLayout();
    setupDragAndDrop();
  }

  /**
   * Creates a new TokenSelectionPane with custom token manager.
   *
   * @param customTokenManager the custom token manager to use
   */
  public TokenSelectionPane(CustomTokenManager customTokenManager) {
    this.customTokenManager = customTokenManager != null ? customTokenManager : new CustomTokenManager();
    this.tokenizer = new FilenameTokenizer();

    initializeComponents();
    setupLayout();
    setupDragAndDrop();
  }

  /** Initializes all UI components. */
  private void initializeComponents() {
    tokenDisplay = new FlowPane();
    tokenDisplay.setHgap(10);
    tokenDisplay.setVgap(10);
    tokenDisplay.setPadding(new Insets(15));
    tokenDisplay.setStyle(
        "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

    tokenTypesPanel = new VBox(10);
    tokenTypesPanel.setPadding(new Insets(15));
    tokenTypesPanel.setStyle(
        "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

    explanationArea = new TextArea();
    explanationArea.setEditable(false);
    explanationArea.setPrefRowCount(4);
    explanationArea.setWrapText(true);
    explanationArea.setText(
        "Drag tokens to different type categories to adjust their classification. The system has"
            + " automatically detected token types based on common patterns. You can override these"
            + " suggestions by dragging tokens to the appropriate categories.");
  }

  /** Sets up the layout structure. */
  private void setupLayout() {
    setSpacing(20);
    setPadding(new Insets(20));

    // Title
    Label title = new Label("Step 1: Token Selection");
    title.getStyleClass().add("step-title");

    // Description
    Label description = new Label(
        "Review the detected tokens and their suggested types. You can drag tokens "
            + "between categories to correct any misclassifications.");
    description.setWrapText(true);
    description.getStyleClass().add("step-description");

    // Token display section
    Label tokensLabel = new Label("Detected Tokens:");
    tokensLabel.getStyleClass().add("section-label");

    // Token types panel
    Label typesLabel = new Label("Token Types:");
    typesLabel.getStyleClass().add("section-label");

    // Create split layout
    HBox mainContent = new HBox(20);

    VBox leftPanel = new VBox(10);
    leftPanel.getChildren().addAll(tokensLabel, tokenDisplay);

    VBox rightPanel = new VBox(10);
    rightPanel.getChildren().addAll(typesLabel, tokenTypesPanel);

    HBox.setHgrow(leftPanel, Priority.ALWAYS);
    HBox.setHgrow(rightPanel, Priority.ALWAYS);
    mainContent.getChildren().addAll(leftPanel, rightPanel);

    // Explanation
    Label explanationLabel = new Label("Instructions:");
    explanationLabel.getStyleClass().add("section-label");

    getChildren().addAll(title, description, mainContent, explanationLabel, explanationArea);
  }

  /** Sets up drag and drop functionality for token classification. */
  private void setupDragAndDrop() {
    // Token types will accept drops
    for (TokenType tokenType : TokenType.values()) {
      if (tokenType != TokenType.UNKNOWN) {
        createTokenTypeDropZone(tokenType);
      }
    }
  }

  /** Creates a drop zone for a specific token type. */
  private void createTokenTypeDropZone(TokenType tokenType) {
    VBox dropZone = new VBox(5);
    dropZone.setPadding(new Insets(10));
    dropZone.setStyle(
        "-fx-background-color: #ffffff; -fx-border-color: #ced4da; "
            + "-fx-border-radius: 3; -fx-min-height: 60;");
    dropZone.setAlignment(Pos.TOP_LEFT);

    Label typeLabel = new Label(formatTokenTypeName(tokenType));
    typeLabel.getStyleClass().add("token-type-label");
    typeLabel.setStyle(
        "-fx-font-weight: bold; -fx-text-fill: " + getTokenTypeColor(tokenType) + ";");

    Label descriptionLabel = new Label(getTokenTypeDescription(tokenType));
    descriptionLabel.getStyleClass().add("token-type-description");
    descriptionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
    descriptionLabel.setWrapText(true);

    FlowPane tokenContainer = new FlowPane(5, 5);

    dropZone.getChildren().addAll(typeLabel, descriptionLabel, tokenContainer);

    // Set up drag over and drop handlers
    dropZone.setOnDragOver(
        event -> {
          if (event.getGestureSource() != dropZone && event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.MOVE);
            dropZone.setStyle(
                "-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; "
                    + "-fx-border-radius: 3; -fx-min-height: 60;");
          }
          event.consume();
        });

    dropZone.setOnDragExited(
        event -> {
          dropZone.setStyle(
              "-fx-background-color: #ffffff; -fx-border-color: #ced4da; "
                  + "-fx-border-radius: 3; -fx-min-height: 60;");
          event.consume();
        });

    dropZone.setOnDragDropped(
        event -> {
          Dragboard db = event.getDragboard();
          boolean success = false;

          if (db.hasString()) {
            String tokenValue = db.getString();
            moveTokenToType(tokenValue, tokenType, tokenContainer);
            success = true;
          }

          event.setDropCompleted(success);
          event.consume();
        });

    tokenTypesPanel.getChildren().add(dropZone);
  }

  /** Creates a draggable token chip. */
  private Label createTokenChip(FilenameToken token) {
    Label chip = new Label(token.getValue());
    chip.getStyleClass().add("token-chip");

    // Check if this is a front token and highlight it
    boolean isFrontToken = tokenizer.isFrontToken(token.getValue());
    String backgroundColor = getTokenTypeColor(token.getSuggestedType());

    if (isFrontToken) {
      // Add special styling for front tokens
      chip.setStyle(
          String.format(
              "-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 5 10; "
                  + "-fx-background-radius: 15; -fx-cursor: hand; -fx-font-size: 12px; "
                  + "-fx-border-color: #ff6b35; -fx-border-width: 2px; -fx-border-radius: 15;",
              backgroundColor));
    } else {
      chip.setStyle(
          String.format(
              "-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 5 10; "
                  + "-fx-background-radius: 15; -fx-cursor: hand; -fx-font-size: 12px;",
              backgroundColor));
    }

    // Enhanced tooltip with additional information
    StringBuilder tooltipText = new StringBuilder();
    tooltipText.append(
        String.format(
            "Type: %s\nConfidence: %.1f%%\nPosition: %d",
            formatTokenTypeName(token.getSuggestedType()),
            token.getConfidence() * 100,
            token.getPosition()));

    if (isFrontToken) {
      tooltipText.append("\n\n✓ Front camera token detected");
      ImageRole role = tokenizer.getImageRoleForToken(token.getValue());
      if (role != null) {
        tooltipText.append("\nRole: ").append(role.name());
      }
    }

    // Check if this matches a custom token
    CustomTokenManager.CustomToken customToken = customTokenManager.findMatchingCustomToken(token.getValue());
    if (customToken != null) {
      tooltipText.append("\n\n✓ Custom token: ").append(customToken.getName());
      tooltipText.append("\nDescription: ").append(customToken.getDescription());
    }

    Tooltip tooltip = new Tooltip(tooltipText.toString());
    Tooltip.install(chip, tooltip);

    // Set up drag detection
    chip.setOnDragDetected(
        event -> {
          Dragboard db = chip.startDragAndDrop(TransferMode.MOVE);
          ClipboardContent content = new ClipboardContent();
          content.putString(token.getValue());
          db.setContent(content);
          event.consume();
        });

    return chip;
  }

  /** Moves a token to a new type category. */
  private void moveTokenToType(String tokenValue, TokenType newType, FlowPane targetContainer) {
    if (tokenAnalysis == null)
      return;

    // Find and update the token
    for (List<FilenameToken> tokens : tokenAnalysis.getTokenizedFilenames().values()) {
      for (FilenameToken token : tokens) {
        if (token.getValue().equals(tokenValue)) {
          token.setSuggestedType(newType);
          token.setConfidence(0.9); // High confidence for manual assignment
        }
      }
    }

    // Refresh display
    displayTokens();
  }

  /** Displays tokens organized by their suggested types. */
  private void displayTokens() {
    if (tokenAnalysis == null)
      return;

    // Clear existing displays
    tokenDisplay.getChildren().clear();
    tokenTypesPanel.getChildren().clear();

    // Recreate token type drop zones
    for (TokenType tokenType : TokenType.values()) {
      if (tokenType != TokenType.UNKNOWN) {
        createTokenTypeDropZone(tokenType);
      }
    }

    // Set<TokenType> seen = EnumSet.noneOf(TokenType.class);

    // Get representative tokens from first filename
    // if (!tokenAnalysis.getFilenames().isEmpty()) {
    // String firstFilename = tokenAnalysis.getFilenames().get(0);
    // List<FilenameToken> tokens =
    // tokenAnalysis.getTokensForFilename(firstFilename);

    List<FilenameToken> tokens = tokenAnalysis.getTokenizedFilenames().values().stream()
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .filter(Objects::nonNull)
        .distinct()
        /*
         * .filter(t -> t.getSuggestedType() != null)
         * .filter(t -> {
         * return t.getSuggestedType() == TokenType.CAMERA_SIDE ? true :
         * seen.add(t.getSuggestedType());
         * })
         */
        .collect(Collectors.toList());

    // Display tokens in main area
    for (FilenameToken token : tokens) {
      Label chip = createTokenChip(token);
      tokenDisplay.getChildren().add(chip);
    }

    // Organize tokens by type in drop zones
    for (int i = 0; i < tokenTypesPanel.getChildren().size(); i++) {
      VBox dropZone = (VBox) tokenTypesPanel.getChildren().get(i);
      FlowPane tokenContainer = (FlowPane) dropZone.getChildren().get(2);
      TokenType tokenType = TokenType.values()[i]; // Assuming same order

      for (FilenameToken token : tokens) {
        if (token.getSuggestedType() == tokenType) {
          Label chip = createTokenChip(token);
          tokenContainer.getChildren().add(chip);
        }
      }
    }
    // }
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

  /** Gets description for token type. */
  private String getTokenTypeDescription(TokenType tokenType) {
    return switch (tokenType) {
      case PREFIX -> "Fixed text at the beginning";
      case SUFFIX -> "Fixed text at the end";
      case GROUP_ID -> "Unique identifier for each vehicle";
      case CAMERA_SIDE -> "Camera position or image side";
      case DATE -> "Date or timestamp";
      case INDEX -> "Numeric sequence or index";
      case EXTENSION -> "File extension";
      case UNKNOWN -> "Unclassified token";
    };
  }

  /** Gets color for token type visualization. */
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

  /** Sets the token analysis results and updates the display. */
  public void setTokenAnalysis(TokenAnalysis analysis) {
    // Enhance analysis with custom tokens
    this.tokenAnalysis = analysis /* customTokenManager.enhanceWithCustomTokens(analysis) */;
    displayTokens();
  }

  /**
   * Gets the custom token manager.
   *
   * @return the custom token manager
   */
  public CustomTokenManager getCustomTokenManager() {
    return customTokenManager;
  }

  /**
   * Sets the custom token manager.
   *
   * @param customTokenManager the custom token manager to use
   */
  public void setCustomTokenManager(CustomTokenManager customTokenManager) {
    this.customTokenManager = customTokenManager != null ? customTokenManager : new CustomTokenManager();
  }

  /**
   * @return the current token analysis
   */
  public TokenAnalysis getTokenAnalysis() {
    return tokenAnalysis;
  }
}
