# Design Document

## Overview

The Filename Pattern Builder is a sophisticated UI component that transforms the complex task of regex pattern creation into an intuitive, visual workflow. The design centers around a dual-mode interface that serves both novice and expert users, with intelligent file analysis, live preview capabilities, and seamless integration with the existing TollingVision processing pipeline.

The architecture emphasizes separation of concerns with dedicated services for pattern building, file analysis, and preview generation, ensuring maintainability and testability while providing a responsive user experience.

## Architecture

### High-Level Component Structure

```
PatternBuilderDialog
├── ModeSelector (Simple/Advanced)
├── SimplePatternBuilder
│   ├── FileAnalysisPane
│   ├── TokenSelectionPane
│   ├── GroupIdSelector
│   ├── RoleRulesPane
│   └── PatternPreviewPane
├── AdvancedPatternBuilder
│   ├── RegexInputFields
│   └── PatternPreviewPane (shared)
├── PresetManager
└── ValidationEngine
```

### Service Layer Architecture

```
PatternBuilderService
├── FilenameTokenizer
├── PatternGenerator
├── RuleEngine
└── PreviewEngine

ConfigurationService
├── PresetStorage
├── PatternPersistence
└── ValidationService
```

## Components and Interfaces

### 1. PatternBuilderDialog

**Purpose:** Main container dialog that orchestrates the pattern building workflow.

**Key Responsibilities:**
- Mode switching between Simple and Advanced
- Coordination between sub-components
- Integration with MainScreen configuration
- Preset management UI

**Interface:**
```java
public class PatternBuilderDialog extends Stage {
    private PatternBuilderMode currentMode;
    private PatternConfiguration currentConfig;
    private Consumer<PatternConfiguration> onConfigurationComplete;

    public void showDialog(PatternConfiguration initialConfig);
    public void setOnConfigurationComplete(Consumer<PatternConfiguration> callback);
}
```

### 2. SimplePatternBuilder

**Purpose:** Visual pattern builder for non-regex users with guided workflow.

**Key Features:**
- File analysis with automatic tokenization
- Visual token selection with drag-and-drop
- Group ID selection with validation
- Role rule configuration with chips/dropdowns
- Live preview integration

**Interface:**
```java
public class SimplePatternBuilder extends VBox {
    private ObservableList<String> sampleFilenames;
    private ObservableList<FilenameToken> detectedTokens;
    private ObjectProperty<FilenameToken> selectedGroupId;
    private ObservableList<RoleRule> roleRules;

    public void analyzeSampleFiles(Path sampleDirectory);
    public PatternConfiguration generateConfiguration();
}
```

### 3. FilenameTokenizer

**Purpose:** Intelligent analysis of filename patterns with automatic token detection.

**Key Capabilities:**
- Multi-delimiter tokenization (_, -, ., space)
- Pattern recognition (dates, indexes, camera identifiers)
- Synonym detection for camera/side terms
- Statistical analysis for pattern confidence

**Interface:**
```java
public class FilenameTokenizer {
    public List<FilenameToken> tokenizeFilename(String filename);
    public TokenAnalysis analyzeFilenames(List<String> filenames);
    public List<TokenSuggestion> suggestTokenTypes(List<FilenameToken> tokens);
}

public class FilenameToken {
    private String value;
    private int position;
    private TokenType suggestedType;
    private double confidence;
}

public enum TokenType {
    PREFIX, SUFFIX, GROUP_ID, CAMERA_SIDE, DATE, INDEX, EXTENSION, UNKNOWN
}
```

### 4. RoleRule System

**Purpose:** Flexible rule engine for categorizing images by role with precedence handling.

**Rule Types:**
- EQUALS: Exact match (case-sensitive/insensitive)
- CONTAINS: Substring match
- STARTS_WITH: Prefix match
- ENDS_WITH: Suffix match
- REGEX_OVERRIDE: Custom regex for advanced users

**Interface:**
```java
public class RoleRule {
    private ImageRole targetRole;
    private RuleType ruleType;
    private String ruleValue;
    private boolean caseSensitive;
    private int priority; // For precedence handling
}

public enum ImageRole {
    OVERVIEW(1), FRONT(2), REAR(3); // Numbers indicate precedence
}

public class RuleEngine {
    public ImageRole classifyFilename(String filename, List<RoleRule> rules);
    public String generateRegexPattern(List<RoleRule> rules, ImageRole role);
}
```

### 5. PatternPreviewPane

**Purpose:** Real-time preview of pattern matching results with comprehensive feedback.

**Features:**
- Tabular display: Filename | Group ID | Role | Status
- Role count summaries
- Unmatched file highlighting
- Group completeness validation
- Export preview results

**Interface:**
```java
public class PatternPreviewPane extends VBox {
    private TableView<FilenamePreview> previewTable;
    private Label summaryLabel;
    private ListView<String> unmatchedFiles;

    public void updatePreview(PatternConfiguration config, List<String> filenames);
    public PreviewSummary getPreviewSummary();
}

public class FilenamePreview {
    private String filename;
    private String groupId;
    private ImageRole role;
    private boolean matched;
    private String errorMessage;
}
```

### 6. PatternGenerator

**Purpose:** Converts visual configuration to regex patterns with validation.

**Key Functions:**
- Token-to-regex conversion
- Capturing group insertion for Group ID
- Role pattern generation with precedence
- Validation and error reporting

**Interface:**
```java
public class PatternGenerator {
    public String generateGroupPattern(List<FilenameToken> tokens, FilenameToken groupIdToken);
    public String generateRolePattern(List<RoleRule> rules, ImageRole role);
    public ValidationResult validatePatterns(PatternConfiguration config);
}

public class PatternConfiguration {
    private String groupPattern;
    private String frontPattern;
    private String rearPattern;
    private String overviewPattern;
    private List<RoleRule> roleRules;
    private List<FilenameToken> tokens;
    private FilenameToken groupIdToken;
}
```

## Data Models

### Core Data Structures

```java
public class TokenAnalysis {
    private List<String> filenames;
    private Map<String, List<FilenameToken>> tokenizedFilenames;
    private List<TokenSuggestion> suggestions;
    private Map<TokenType, Double> confidenceScores;
}

public class TokenSuggestion {
    private TokenType type;
    private String description;
    private List<String> examples;
    private double confidence;
}

public class PresetConfiguration {
    private String name;
    private String description;
    private PatternConfiguration patternConfig;
    private LocalDateTime created;
    private LocalDateTime lastUsed;
}

public class ValidationResult {
    private boolean valid;
    private List<ValidationError> errors;
    private List<ValidationWarning> warnings;
}
```

## Error Handling

### Validation Strategy

1. **Real-time Validation:** Immediate feedback as users modify configuration
2. **Comprehensive Checks:** Group pattern capturing group validation, role rule completeness
3. **User-Friendly Messages:** Plain language error descriptions with fix suggestions
4. **Progressive Disclosure:** Show warnings that don't block usage, errors that do

### Error Categories

```java
public enum ValidationErrorType {
    NO_GROUP_ID_SELECTED("Please select a token to use as Group ID"),
    INVALID_GROUP_PATTERN("Group pattern must contain exactly one capturing group"),
    NO_ROLE_RULES_DEFINED("Please define rules for identifying image roles"),
    NO_FILES_MATCHED("No files match the current pattern - try adjusting Group ID"),
    INCOMPLETE_GROUPS("Some groups are missing required image types"),
    REGEX_SYNTAX_ERROR("Invalid regular expression syntax");
}
```

## Testing Strategy

### Unit Testing Focus

1. **FilenameTokenizer:** Test tokenization accuracy with various filename formats
2. **PatternGenerator:** Verify regex generation correctness and capturing group placement
3. **RuleEngine:** Test role classification with precedence rules and edge cases
4. **ValidationEngine:** Comprehensive validation logic testing

### Integration Testing

1. **End-to-End Workflow:** Complete pattern building from file analysis to regex generation
2. **Preview Accuracy:** Verify preview results match actual processing behavior
3. **Preset Persistence:** Save/load functionality with various configuration types
4. **Mode Switching:** Seamless conversion between Simple and Advanced modes

### Test Data Sets

```java
public class TestDataSets {
    // Standard vehicle image naming patterns
    public static final List<String> STANDARD_VEHICLE_NAMES = Arrays.asList(
        "vehicle_001_front.jpg", "vehicle_001_rear.jpg", "vehicle_001_overview.jpg",
        "vehicle_002_front.jpg", "vehicle_002_rear.jpg", "vehicle_002_overview.jpg"
    );

    // Complex naming with dates and cameras
    public static final List<String> COMPLEX_VEHICLE_NAMES = Arrays.asList(
        "2024-01-15_cam1_vehicle_ABC123_front.jpg",
        "2024-01-15_cam1_vehicle_ABC123_rear.jpg",
        "2024-01-15_cam2_vehicle_ABC123_scene.jpg"
    );

    // Edge cases and problematic patterns
    public static final List<String> EDGE_CASE_NAMES = Arrays.asList(
        "IMG_001.jpg", "DSC_002.JPG", "photo.png",
        "vehicle__double_underscore.jpg", "vehicle-with-dashes.jpg"
    );
}
```

## Performance Considerations

### Optimization Strategies

1. **Lazy Loading:** Load and analyze files only when needed
2. **Caching:** Cache tokenization results for repeated analysis
3. **Background Processing:** Perform heavy analysis off the UI thread
4. **Memory Management:** Limit sample size to prevent memory issues
5. **Incremental Updates:** Update preview incrementally rather than full refresh

### Performance Targets

- **File Analysis:** Complete tokenization of 500 files within 2 seconds
- **Preview Updates:** Refresh preview within 500ms of configuration changes
- **Memory Usage:** Maintain reasonable memory footprint (< 100MB for analysis data)
- **UI Responsiveness:** Never block UI thread for more than 100ms

## Integration Points

### MainScreen Integration

The Pattern Builder integrates with the existing MainScreen through a new "Pattern Builder" button that opens the dialog. The system now supports real-time configuration publishing and immediate pattern application.

```java
// In MainScreen.java
private void openPatternBuilder() {
    PatternBuilderDialog dialog = new PatternBuilderDialog(inputFolder, messages);

    // Set up real-time configuration callback
    dialog.setOnConfigurationReady(config -> {
        // Update existing pattern fields immediately when valid
        groupPatternField.setText(config.getGroupPattern());
        frontPatternField.setText(config.getFrontPattern());
        rearPatternField.setText(config.getRearPattern());
        overviewPatternField.setText(config.getOverviewPattern());
    });

    // Set up completion callback for dialog close
    dialog.setOnConfigurationComplete(config -> {
        // Final save when user clicks OK
        saveConfiguration();
    });

    // Initialize with current patterns
    PatternConfiguration currentConfig = getCurrentPatternConfiguration();
    dialog.showDialog(currentConfig);
}
```

### Custom Token Management

Custom tokens are now fully integrated with persistence and real-time updates:

```java
public class CustomTokenManager {
    // Persistence using simple text format
    public void saveCustomTokens(); // Saves to ~/.tollingvision-client/custom-tokens.txt
    public void loadCustomTokens(); // Loads on startup

    // Real-time updates with callback
    public void updateCustomToken(String oldName, CustomToken token, Runnable refreshCallback);

    // Graceful collision handling
    public CustomToken findMatchingCustomToken(String tokenValue);
}
```

### Configuration Persistence

The pattern builder now includes comprehensive persistence for both configurations and custom tokens:

```java
public class UserConfiguration {
    // Existing fields...
    private PatternBuilderConfig patternBuilderConfig;
    private List<PresetConfiguration> savedPresets;
}

// Custom tokens stored separately for cross-session persistence
// Location: ~/.tollingvision-client/custom-tokens.txt
// Format: name|description|mappedType|example1,example2,example3
```

### Real-time Validation and State Management

The system now provides immediate feedback and state synchronization:

```java
public class ValidationModel {
    // Debounced validation with 300ms delay
    public void requestValidationRefresh();

    // Empty state handling
    public boolean shouldHideValidationDisplay();
    public boolean shouldShowSuccessBanner();

    // Configuration readiness
    public boolean canGeneratePatterns();
}

public class PatternBuilderDialog {
    // Real-time configuration publishing
    private Consumer<PatternConfiguration> onConfigurationReady;

    // Immediate OK button state management
    private void handleConfigurationReady(PatternConfiguration config);
}
```

### Enhanced Rule Suggestion System

Rule suggestions now include intelligent deduplication and empty state handling:

```java
public class RoleRulesPane {
    // Deduplication logic
    public void suggestRulesFromTokens(List<FilenameToken> detectedTokens);

    // Empty state management
    private void hideSuggestions(); // Hides panel entirely when no suggestions

    // Real-time refresh
    public void refreshSuggestions(List<FilenameToken> detectedTokens);
}
```

This enhanced design ensures that the Filename Pattern Builder provides a comprehensive, user-friendly solution with real-time feedback, persistent custom tokens, and immediate configuration application while maintaining full compatibility with the existing TollingVision processing pipeline.
