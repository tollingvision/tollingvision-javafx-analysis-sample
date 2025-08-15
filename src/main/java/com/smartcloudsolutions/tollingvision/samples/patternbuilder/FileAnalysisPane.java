package com.smartcloudsolutions.tollingvision.samples.patternbuilder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

/**
 * UI component for directory selection and sample file loading.
 * Provides file analysis capabilities with progress feedback and
 * displays analysis results including detected patterns and suggestions.
 * Uses background processing and caching for improved performance.
 */
public class FileAnalysisPane extends VBox {

    private final ObjectProperty<TokenAnalysis> analysisResults = new SimpleObjectProperty<>();
    private final TokenizationCache cache = new TokenizationCache();
    private final FilenameTokenizer tokenizer = new FilenameTokenizer(cache);
    private final BackgroundAnalysisService backgroundService = new BackgroundAnalysisService(tokenizer, cache);
    
    // Memory monitoring
    private boolean memoryMonitoringStarted = false;

    // UI Components
    private TextField directoryField;
    private Button browseButton;
    private Button analyzeButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Label cacheStatsLabel;
    private TextArea resultsArea;
    private ListView<String> sampleFilesList;
    
    // Background processing
    private Task<TokenAnalysis> currentAnalysisTask;

    /**
     * Creates a new FileAnalysisPane with directory selection and analysis
     * capabilities.
     */
    public FileAnalysisPane() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    /**
     * Initializes all UI components.
     */
    private void initializeComponents() {
        directoryField = new TextField();
        directoryField.setPromptText("Select a directory containing sample image files...");
        directoryField.setEditable(false);

        browseButton = new Button("Browse...");
        analyzeButton = new Button("Analyze Files");
        analyzeButton.setDisable(true);
        
        cancelButton = new Button("Cancel");
        cancelButton.setVisible(false);

        progressBar = new ProgressBar();
        progressBar.setVisible(false);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        statusLabel = new Label("Select a directory to begin analysis");
        statusLabel.getStyleClass().add("status-label");
        
        cacheStatsLabel = new Label("Cache: Ready");
        cacheStatsLabel.getStyleClass().add("cache-stats-label");
        cacheStatsLabel.setFont(javafx.scene.text.Font.font(9));
        
        // Start memory monitoring
        startMemoryMonitoring();

        resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefRowCount(8);
        resultsArea.setPromptText("Analysis results will appear here...");

        sampleFilesList = new ListView<>();
        sampleFilesList.setPrefHeight(150);
        sampleFilesList.setPlaceholder(new Label("No files loaded"));
    }

    /**
     * Sets up the layout structure.
     */
    private void setupLayout() {
        setSpacing(15);
        setPadding(new Insets(20));

        // Title
        Label title = new Label("Step 1: File Analysis");
        title.getStyleClass().add("step-title");

        // Description
        Label description = new Label(
                "Select a directory containing sample image files. The system will analyze " +
                        "the first 500 files to detect common patterns and suggest token types.");
        description.setWrapText(true);
        description.getStyleClass().add("step-description");

        // Directory selection
        HBox directoryBox = new HBox(10);
        directoryBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(directoryField, Priority.ALWAYS);
        directoryBox.getChildren().addAll(directoryField, browseButton);

        // Analysis controls
        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);
        controlsBox.getChildren().addAll(analyzeButton, cancelButton, progressBar);
        
        // Status and cache info
        VBox statusBox = new VBox(5);
        statusBox.getChildren().addAll(statusLabel, cacheStatsLabel);

        // Sample files preview
        Label filesLabel = new Label("Sample Files (first 20):");
        filesLabel.getStyleClass().add("section-label");

        // Analysis results
        Label resultsLabel = new Label("Analysis Results:");
        resultsLabel.getStyleClass().add("section-label");

        getChildren().addAll(
                title,
                description,
                new Label("Directory:"),
                directoryBox,
                controlsBox,
                statusBox,
                filesLabel,
                sampleFilesList,
                resultsLabel,
                resultsArea);
    }

    /**
     * Sets up event handlers for user interactions.
     */
    private void setupEventHandlers() {
        browseButton.setOnAction(e -> selectDirectory());
        analyzeButton.setOnAction(e -> analyzeFiles());
        cancelButton.setOnAction(e -> cancelAnalysis());

        // Enable analyze button when directory is selected
        directoryField.textProperty().addListener((obs, oldVal, newVal) -> {
            analyzeButton.setDisable(newVal == null || newVal.trim().isEmpty());
        });
        
        // Update cache stats periodically
        javafx.animation.Timeline cacheStatsUpdater = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), e -> updateCacheStats()));
        cacheStatsUpdater.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        cacheStatsUpdater.play();
    }

    /**
     * Opens directory chooser for user to select sample directory.
     */
    private void selectDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Sample Files Directory");

        // Set initial directory to user home
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File selectedDir = chooser.showDialog(getScene().getWindow());
        if (selectedDir != null) {
            directoryField.setText(selectedDir.getAbsolutePath());
            loadSampleFilesList(selectedDir.toPath());
        }
    }

    /**
     * Loads and displays a preview of sample files from the selected directory.
     */
    private void loadSampleFilesList(Path directory) {
        try {
            sampleFilesList.getItems().clear();

            try (Stream<Path> files = Files.list(directory)) {
                List<String> imageFiles = files
                        .filter(Files::isRegularFile)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .filter(this::isImageFile)
                        .limit(20) // Show first 20 for preview
                        .toList();

                sampleFilesList.getItems().addAll(imageFiles);

                if (imageFiles.isEmpty()) {
                    statusLabel.setText("No image files found in selected directory");
                    statusLabel.getStyleClass().removeAll("status-success", "status-error");
                    statusLabel.getStyleClass().add("status-warning");
                } else {
                    statusLabel.setText(String.format("Found %d image files", imageFiles.size()));
                    statusLabel.getStyleClass().removeAll("status-warning", "status-error");
                    statusLabel.getStyleClass().add("status-success");
                }
            }

        } catch (Exception e) {
            statusLabel.setText("Error reading directory: " + e.getMessage());
            statusLabel.getStyleClass().removeAll("status-success", "status-warning");
            statusLabel.getStyleClass().add("status-error");
        }
    }

    /**
     * Analyzes files in the selected directory using background processing.
     */
    private void analyzeFiles() {
        String directoryPath = directoryField.getText();
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return;
        }

        Path directory = Path.of(directoryPath);

        // Create background task for analysis
        currentAnalysisTask = backgroundService.createFileAnalysisTask(directory);

        // Bind UI to task progress
        progressBar.progressProperty().bind(currentAnalysisTask.progressProperty());
        statusLabel.textProperty().bind(currentAnalysisTask.messageProperty());

        // Handle task completion
        currentAnalysisTask.setOnSucceeded(e -> {
            TokenAnalysis analysis = currentAnalysisTask.getValue();
            setAnalysisResults(analysis);
            displayAnalysisResults(analysis);

            // Reset UI state
            resetAnalysisUI();
            statusLabel.setText("Analysis completed successfully (" + 
                    BackgroundAnalysisService.getMaxFilesForAnalysis() + " file limit)");
            statusLabel.getStyleClass().removeAll("status-warning", "status-error");
            statusLabel.getStyleClass().add("status-success");
            
            updateCacheStats();
        });

        currentAnalysisTask.setOnFailed(e -> {
            Throwable exception = currentAnalysisTask.getException();

            resetAnalysisUI();
            statusLabel.setText("Analysis failed: " + exception.getMessage());
            statusLabel.getStyleClass().removeAll("status-success", "status-warning");
            statusLabel.getStyleClass().add("status-error");

            // Show error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Analysis Error");
            alert.setHeaderText("Failed to analyze files");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        });
        
        currentAnalysisTask.setOnCancelled(e -> {
            resetAnalysisUI();
            statusLabel.setText("Analysis cancelled");
            statusLabel.getStyleClass().removeAll("status-success", "status-error");
            statusLabel.getStyleClass().add("status-warning");
        });

        // Start analysis
        setAnalysisUIRunning();
        Thread analysisThread = new Thread(currentAnalysisTask);
        analysisThread.setDaemon(true);
        analysisThread.start();
    }
    
    /**
     * Cancels the current analysis operation.
     */
    private void cancelAnalysis() {
        if (currentAnalysisTask != null && !currentAnalysisTask.isDone()) {
            currentAnalysisTask.cancel(true);
        }
        backgroundService.cancelAllTasks();
    }
    
    /**
     * Sets UI to running state during analysis.
     */
    private void setAnalysisUIRunning() {
        progressBar.setVisible(true);
        cancelButton.setVisible(true);
        analyzeButton.setDisable(true);
        browseButton.setDisable(true);
    }
    
    /**
     * Resets UI to ready state after analysis completion.
     */
    private void resetAnalysisUI() {
        progressBar.setVisible(false);
        progressBar.progressProperty().unbind();
        statusLabel.textProperty().unbind();
        cancelButton.setVisible(false);
        analyzeButton.setDisable(directoryField.getText() == null || directoryField.getText().trim().isEmpty());
        browseButton.setDisable(false);
    }
    
    /**
     * Updates the cache statistics display.
     */
    private void updateCacheStats() {
        String cacheStats = cache.getCacheStats();
        String memoryStats = MemoryManager.getMemoryUsageString();
        cacheStatsLabel.setText(cacheStats + " | " + memoryStats);
    }
    
    /**
     * Starts memory monitoring for the analysis operations.
     */
    private void startMemoryMonitoring() {
        if (!memoryMonitoringStarted) {
            memoryMonitoringStarted = true;
            
            MemoryManager.startMonitoring(new MemoryManager.MemoryWarningListener() {
                @Override
                public void onLowMemory(long availableMB, long usedMB, long totalMB) {
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("Warning: Low memory (" + availableMB + " MB available)");
                        statusLabel.getStyleClass().removeAll("status-success", "status-error");
                        statusLabel.getStyleClass().add("status-warning");
                    });
                }
                
                @Override
                public void onCriticalMemory(long availableMB, long usedMB, long totalMB) {
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("Critical: Very low memory (" + availableMB + " MB available)");
                        statusLabel.getStyleClass().removeAll("status-success", "status-warning");
                        statusLabel.getStyleClass().add("status-error");
                        
                        // Suggest clearing cache if memory is critical
                        if (availableMB < 10) {
                            cache.clearCache();
                            updateCacheStats();
                        }
                    });
                }
            });
        }
    }

    /**
     * Displays the analysis results in the results text area.
     */
    private void displayAnalysisResults(TokenAnalysis analysis) {
        StringBuilder results = new StringBuilder();

        results.append("ANALYSIS SUMMARY\n");
        results.append("================\n\n");

        results.append(String.format("Files analyzed: %d\n", analysis.getFilenames().size()));
        results.append(String.format("Unique patterns detected: %d\n\n", analysis.getSuggestions().size()));

        results.append("TOKEN TYPE SUGGESTIONS\n");
        results.append("======================\n\n");

        for (TokenSuggestion suggestion : analysis.getSuggestions()) {
            results.append(String.format("• %s (%.1f%% confidence)\n",
                    suggestion.getType().name(), suggestion.getConfidence() * 100));
            results.append(String.format("  Description: %s\n", suggestion.getDescription()));
            results.append(String.format("  Examples: %s\n\n",
                    String.join(", ", suggestion.getExamples().stream().limit(3).toList())));
        }

        results.append("SAMPLE TOKENIZATION\n");
        results.append("===================\n\n");

        // Show tokenization for first few files
        analysis.getFilenames().stream().limit(5).forEach(filename -> {
            results.append(String.format("File: %s\n", filename));
            List<FilenameToken> tokens = analysis.getTokensForFilename(filename);
            for (int i = 0; i < tokens.size(); i++) {
                FilenameToken token = tokens.get(i);
                results.append(String.format("  [%d] %s (%s, %.1f%%)\n",
                        i, token.getValue(), token.getSuggestedType().name(),
                        token.getConfidence() * 100));
            }
            results.append("\n");
        });

        resultsArea.setText(results.toString());
    }

    /**
     * Checks if a filename represents an image file.
     */
    private boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                lower.endsWith(".png") || lower.endsWith(".bmp") ||
                lower.endsWith(".tiff") || lower.endsWith(".gif");
    }

    /**
     * Sets the analysis results and notifies listeners.
     */
    public void setAnalysisResults(TokenAnalysis results) {
        this.analysisResults.set(results);
    }

    /**
     * @return the analysis results property for binding
     */
    public ObjectProperty<TokenAnalysis> getAnalysisResults() {
        return analysisResults;
    }
    
    /**
     * Gets the tokenization cache for sharing with other components.
     * 
     * @return the tokenization cache
     */
    public TokenizationCache getCache() {
        return cache;
    }
    
    /**
     * Gets the background analysis service for sharing with other components.
     * 
     * @return the background analysis service
     */
    public BackgroundAnalysisService getBackgroundService() {
        return backgroundService;
    }
    
    /**
     * Cleans up resources when the pane is no longer needed.
     */
    public void cleanup() {
        if (currentAnalysisTask != null && !currentAnalysisTask.isDone()) {
            currentAnalysisTask.cancel(true);
        }
        backgroundService.shutdown();
        cache.clearCache();
        
        // Stop memory monitoring
        MemoryManager.stopMonitoring();
    }
}