# Implementation Plan

- [x] 1. Create core data models and enums
  - Implement TokenType enum with PREFIX, SUFFIX, GROUP_ID, CAMERA_SIDE, DATE, INDEX, EXTENSION, UNKNOWN values
  - Create FilenameToken class with value, position, suggestedType, and confidence fields
  - Implement ImageRole enum with OVERVIEW(1), FRONT(2), REAR(3) precedence values
  - Create RuleType enum with EQUALS, CONTAINS, STARTS_WITH, ENDS_WITH, REGEX_OVERRIDE values
  - Implement RoleRule class with targetRole, ruleType, ruleValue, caseSensitive, and priority fields
  - Create PatternConfiguration class to hold all pattern settings and generated regex
  - _Requirements: 1.1, 2.4, 4.1, 4.2_

- [x] 2. Implement FilenameTokenizer service for intelligent pattern analysis
  - Create FilenameTokenizer class with tokenizeFilename method using common delimiters
  - Implement analyzeFilenames method to process multiple files and detect patterns
  - Add suggestTokenTypes method with confidence scoring for token type detection
  - Create camera/side synonym detection for overview, front, rear keywords
  - Implement statistical analysis for pattern recognition (dates, indexes, sequences)
  - Add TokenAnalysis class to hold analysis results and suggestions
  - Write unit tests for various filename formats and edge cases
  - _Requirements: 2.2, 2.3, 2.4, 2.5_

- [x] 3. Create PatternGenerator service for regex generation
  - Implement PatternGenerator class with generateGroupPattern method
  - Add automatic capturing group insertion around selected Group ID token
  - Create generateRolePattern method for converting role rules to regex
  - Implement pattern validation with capturing group verification
  - Add ValidationResult class with errors and warnings lists
  - Create comprehensive validation for Group Pattern capturing group requirements
  - Write unit tests for regex generation accuracy and validation logic
  - _Requirements: 3.2, 3.4, 7.2, 7.4_

- [x] 4. Build RuleEngine for role-based image classification
  - Create RuleEngine class with classifyFilename method using precedence rules
  - Implement rule type handlers for EQUALS, CONTAINS, STARTS_WITH, ENDS_WITH
  - Add case-sensitive/insensitive matching support with toggle option
  - Create precedence logic where overview rules exclude files from front/rear consideration
  - Implement generateRegexPattern method for converting rules to regex patterns
  - Add comprehensive error handling for invalid rule configurations
  - Write unit tests for role classification accuracy and precedence handling
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 5. Create PatternPreviewPane for live validation feedback
  - Implement PatternPreviewPane as VBox with TableView for filename preview
  - Create FilenamePreview class with filename, groupId, role, matched, errorMessage fields
  - Add updatePreview method that processes filenames with current configuration
  - Implement real-time preview updates with filename categorization results
  - Create summary display showing counts for each role and unmatched files
  - Add highlighting for groups with missing roles or validation errors
  - Implement PreviewSummary class for aggregated statistics and validation status
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 6. Build SimplePatternBuilder visual interface
  - Create SimplePatternBuilder as VBox with guided workflow steps
  - Implement FileAnalysisPane with directory selection and sample file loading
  - Add TokenSelectionPane with visual token display and drag-and-drop interaction
  - Create GroupIdSelector with required selection validation and blocking logic
  - Implement RoleRulesPane with chip-based rule creation and dropdown selectors
  - Add live preview integration that updates automatically on configuration changes
  - Create step-by-step wizard navigation with validation at each step
  - _Requirements: 1.2, 2.1, 3.1, 3.3, 4.1, 5.1_

- [x] 7. Implement AdvancedPatternBuilder for regex power users
  - Create AdvancedPatternBuilder as VBox with existing regex input fields
  - Add enhanced regex editors with syntax highlighting and validation
  - Implement live preview integration using shared PatternPreviewPane
  - Create regex explanation tooltips mapping patterns to functionality
  - Add "Explain" feature showing how regex components work
  - Implement Copy button for generated regex patterns
  - Add validation for Group Pattern capturing group requirements
  - _Requirements: 1.3, 5.5, 7.4_

- [x] 8. Create PatternBuilderDialog main container
  - Implement PatternBuilderDialog as Stage with modal dialog behavior
  - Add ModeSelector with Simple/Advanced toggle and mode switching logic
  - Create coordination between SimplePatternBuilder and AdvancedPatternBuilder
  - Implement configuration conversion between visual rules and regex patterns
  - Add OK/Cancel buttons with validation before accepting configuration
  - Create integration callback for returning configuration to MainScreen
  - Implement dialog sizing and responsive layout for different screen sizes
  - _Requirements: 1.1, 1.4, 7.1, 7.3_

- [x] 9. Build PresetManager for configuration persistence
  - Create PresetConfiguration class with name, description, config, timestamps
  - Implement PresetManager with save, load, delete, and list operations
  - Add preset dropdown selector in PatternBuilderDialog
  - Create import/export functionality for JSON preset files
  - Implement preset validation and migration for configuration changes
  - Add preset management UI with rename, duplicate, and organize features
  - Create default preset loading on application startup
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 10. Integrate with MainScreen and existing configuration
  - Add "Pattern Builder" button to MainScreen configuration section
  - Create openPatternBuilder method that launches dialog with current settings
  - Implement configuration callback that updates existing regex fields
  - Add PatternBuilderConfig to UserConfiguration for persistence
  - Create backward compatibility handling for existing regex configurations
  - Implement automatic configuration saving when patterns are applied
  - Add validation integration with existing pattern validation logic
  - _Requirements: 7.1, 7.2, 7.3, 7.5_

- [x] 11. Implement comprehensive error handling and user guidance
  - Create ValidationErrorType enum with user-friendly error messages
  - Implement real-time validation with immediate feedback on configuration changes
  - Add contextual help tooltips and explanatory text throughout the interface
  - Create guided error resolution with specific fix recommendations
  - Implement "Match any image extension" option for file extension handling
  - Add validation blocking that prevents processing until issues are resolved
  - Create comprehensive error logging for debugging and support
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 12. Optimize performance for large file sets
  - Implement 500-file limit for sample analysis to maintain performance
  - Add background processing for file analysis using JavaFX Task
  - Create caching system for tokenization results to avoid repeated analysis
  - Implement incremental preview updates to minimize UI refresh overhead
  - Add memory management for large filename datasets
  - Create progress indicators for long-running analysis operations
  - Implement cancellation support for background analysis tasks
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [-] 13. Add accessibility and usability enhancements
  - Implement clear labeling and tooltips for all UI components
  - Create visual indicators (colors, icons) for validation status and matching results
  - Implement contextual help system with examples and explanations
  - Add guided workflow with step-by-step instructions for new users
  - Create responsive layout that works on different screen sizes
  - Implement high contrast and accessibility-friendly color schemes
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 14. Create comprehensive test suite
  - Write unit tests for FilenameTokenizer with various filename formats
  - Create tests for PatternGenerator regex generation and validation
  - Implement RuleEngine tests with precedence rules and edge cases
  - Add integration tests for complete pattern building workflow
  - Create test data sets with standard, complex, and edge case filenames
  - Implement preview accuracy tests comparing results with actual processing
  - Add preset persistence tests for save/load functionality
  - Create performance tests for large file set handling
  - _Requirements: All requirements validation_

- [x] 15. Implement live validation refresh and empty-state handling
  - Create ValidationModel as observable shared state between Steps 2-4
  - Implement debounced validation refresh on mode switch, step enter/leave, and control changes
  - Add automatic validation refresh when upstream inputs change (token selection, role rules, regex edits, folder changes)
  - Hide validation message box entirely when no errors/warnings exist (no empty container)
  - Ensure "pattern configuration completed successfully" banner and validation blocks are consistent
  - Guarantee pattern generation succeeds when success banner is shown
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [x] 16. Enhance token selection with front and custom token propagation
  - Ensure front token detection with synonyms (front, f, fr, forward) appears in Token Selection screen
  - Pre-populate preconfigured Custom Tokens in Token Selection screen
  - Show Custom Token handling dialog only when switching into Simple mode (once)
  - Prevent unexpected Custom Token dialogs during normal navigation or Advanced mode
  - Carry front tokens through to Roles step for proper rule configuration
  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_

- [x] 17. Remove redundant folder step in Simple mode
  - Remove Step 1 folder selection UI from Simple mode entirely
  - Inherit Input Folder from main screen and validate before opening builder
  - Display inherited folder path read-only if needed for reference
  - Disable navigation to Step 1 in Simple mode (no folder changing capability)
  - Ensure builder only opens when main screen folder is valid
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

- [x] 18. Implement custom token management and persistence
  - Wire CustomTokenManager and CustomTokenDialog into SimplePatternBuilder
  - Add persistence for custom tokens using simple text format in user config directory
  - Implement graceful handling of duplicates and collisions with immediate refresh
  - Load custom tokens automatically at application start and apply during tokenization
  - Provide callback system for immediate tokenization refresh when tokens are updated
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_

- [x] 19. Implement real-time configuration publishing
  - Add onConfigurationReady callback to both Simple and Advanced modes
  - Publish valid configurations immediately without requiring mode switches
  - Enable OK button whenever current mode's configuration is valid
  - Implement immediate dialog close and configuration return on OK click
  - Synchronize success banners and validation boxes with same validation model
  - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5_

- [x] 20. Enhanced validation refresh and UI state management
  - Implement immediate validation refresh on final step when any inputs change
  - Hide validation message boxes entirely when no errors or warnings exist
  - Show Fix Errors button only when actionable issues exist
  - Ensure consistent success banner display when configuration is valid
  - Synchronize validation state across all UI components without stale content
  - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_

- [x] 21. Intelligent rule suggestions with deduplication
  - Generate suggested rules only for recommendations not already present in configured rules
  - Hide suggestions panel entirely when no unique suggestions exist
  - Refresh suggestions when rules are added or removed to maintain deduplication
  - Prevent display of empty suggestion containers
  - Clearly indicate which detected patterns triggered each suggestion
  - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5_

- [x] 22. Final integration and polish
  - Integrate all components into cohesive PatternBuilderDialog with real-time callbacks
  - Implement comprehensive validation refresh and empty state handling
  - Add custom token persistence and immediate refresh capabilities
  - Create intelligent rule suggestion system with deduplication
  - Implement real-time configuration publishing without mode switch requirements
  - Add final validation state synchronization and UI consistency
  - _Requirements: All requirements integration_