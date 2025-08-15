# Requirements Document

## Introduction

The Filename Pattern Builder is a comprehensive feature designed to make the TollingVision application accessible to non-regex users while maintaining advanced capabilities for power users. Currently, users must manually write complex regular expressions to configure how vehicle images are grouped and categorized by role (front, rear, overview). This creates a significant barrier to entry for typical users who need to process vehicle images but lack regex expertise.

The Pattern Builder will provide an intuitive, visual interface that automatically generates the necessary regular expressions from user-friendly token-based rules, while offering live preview and validation to ensure correct configuration before processing begins.

## Requirements

### Requirement 1: Dual-Mode Pattern Configuration Interface

**User Story:** As a user configuring image processing patterns, I want to choose between a simple visual builder and advanced regex mode, so that I can use the approach that matches my technical expertise level.

#### Acceptance Criteria

1. WHEN the user opens pattern configuration THEN the system SHALL display a mode selector with "Simple" and "Advanced" options
2. WHEN "Simple" mode is selected THEN the system SHALL show the token-based pattern builder interface
3. WHEN "Advanced" mode is selected THEN the system SHALL show the existing regex input fields with enhanced preview capabilities
4. WHEN switching between modes THEN the system SHALL preserve any valid configuration and attempt to convert between representations where possible
5. WHEN in either mode THEN the system SHALL provide live preview of pattern matching results

### Requirement 2: Sample File Analysis and Tokenization

**User Story:** As a non-regex user, I want the system to analyze my image filenames and suggest common patterns, so that I can quickly configure grouping without understanding regex syntax.

#### Acceptance Criteria

1. WHEN the user selects a sample folder THEN the system SHALL load the first 500 image filenames for analysis
2. WHEN filenames are loaded THEN the system SHALL automatically tokenize them using common delimiters (underscore, hyphen, dot, space)
3. WHEN tokenization is complete THEN the system SHALL detect common patterns including:
   - Numeric sequences (indexes, dates)
   - Camera/side identifier words
   - Fixed prefixes/suffixes
   - File extensions
4. WHEN patterns are detected THEN the system SHALL propose token types: {prefix}, {date}, {index}, {camera}, {groupId}, {ext}
5. WHEN camera/side detection runs THEN the system SHALL recognize synonyms:
   - Overview: overview, ov, ovr, ovw, scene, full
   - Front: front, f, fr, forward
   - Rear: rear, r, rr, back, behind

### Requirement 3: Group ID Selection and Validation

**User Story:** As a user configuring image grouping, I want to clearly specify which part of the filename identifies each vehicle group, so that images of the same vehicle are processed together correctly.

#### Acceptance Criteria

1. WHEN the tokenization is complete THEN the system SHALL require the user to select exactly one token as the Group ID
2. WHEN no Group ID is selected THEN the system SHALL disable the "Next" or "Apply" button
3. WHEN a Group ID is selected THEN the system SHALL automatically generate a Group Pattern regex with capturing group 1 around the selected token
4. WHEN the Group Pattern is generated THEN the system SHALL validate that it contains exactly one capturing group
5. WHEN Group ID selection changes THEN the system SHALL immediately update the live preview to show new groupings

### Requirement 4: Role-Based Image Classification Rules

**User Story:** As a user setting up image processing, I want to define simple rules for identifying front, rear, and overview images within each vehicle group, so that the correct analysis is applied to each image type.

#### Acceptance Criteria

1. WHEN configuring role rules THEN the system SHALL provide rule types: equals, contains, startsWith, endsWith
2. WHEN creating role rules THEN the system SHALL support case-insensitive matching with a toggle option
3. WHEN defining overview rules THEN the system SHALL apply them first and exclude matched files from front/rear consideration
4. WHEN defining front/rear rules THEN the system SHALL only consider files not already matched by overview rules
5. WHEN role rules are configured THEN the system SHALL generate corresponding regex patterns for each role
6. WHEN advanced users need custom rules THEN the system SHALL provide per-role regex override options

### Requirement 5: Live Preview and Validation

**User Story:** As a user configuring patterns, I want to see immediate feedback on how my rules will categorize actual filenames, so that I can verify the configuration is correct before processing images.

#### Acceptance Criteria

1. WHEN any configuration changes THEN the system SHALL immediately update a preview table showing: Filename | Group ID | Role | Matched status
2. WHEN preview updates THEN the system SHALL display counts for each role (overview, front, rear) and list unmatched files
3. WHEN groups have missing roles THEN the system SHALL highlight them in the preview
4. WHEN configuration is invalid THEN the system SHALL show clear error messages with suggested fixes
5. WHEN patterns are generated THEN the system SHALL display the resulting regex with explanatory tooltips mapping tokens to regex components

### Requirement 6: Pattern Persistence and Presets

**User Story:** As a user who processes similar image sets regularly, I want to save and reuse my pattern configurations, so that I don't have to reconfigure the same patterns repeatedly.

#### Acceptance Criteria

1. WHEN a valid configuration is created THEN the system SHALL allow saving it as a named preset
2. WHEN presets exist THEN the system SHALL provide a dropdown to load saved configurations
3. WHEN loading a preset THEN the system SHALL restore all pattern settings and update the preview
4. WHEN managing presets THEN the system SHALL support import/export as JSON files for sharing between installations
5. WHEN the application starts THEN the system SHALL load the last used configuration as the default

### Requirement 7: Integration with Existing Processing Pipeline

**User Story:** As a user of the existing TollingVision application, I want the new pattern builder to work seamlessly with current image processing, so that I can upgrade my workflow without losing functionality.

#### Acceptance Criteria

1. WHEN patterns are generated THEN the system SHALL write them to the existing configuration fields (Group Pattern, Front Pattern, Rear Pattern, Overview Pattern)
2. WHEN processing begins THEN the system SHALL use the generated patterns with the existing bucketize() method expecting group(1) for grouping
3. WHEN switching to advanced mode THEN the system SHALL populate regex fields with current generated patterns
4. WHEN manual regex is entered THEN the system SHALL validate that Group Pattern contains exactly one capturing group
5. WHEN configuration is saved THEN the system SHALL persist both the pattern rules and generated regex for backward compatibility

### Requirement 8: Error Handling and User Guidance

**User Story:** As a non-technical user, I want clear guidance when my pattern configuration has issues, so that I can fix problems without needing regex knowledge.

#### Acceptance Criteria

1. WHEN no files match the group pattern THEN the system SHALL show "No groups found" with suggestions to adjust the Group ID selection
2. WHEN groups have no role matches THEN the system SHALL highlight affected groups and suggest rule adjustments
3. WHEN regex generation fails THEN the system SHALL show user-friendly error messages with specific fix recommendations
4. WHEN file extensions vary THEN the system SHALL offer an option to "Match any image extension" automatically
5. WHEN validation errors occur THEN the system SHALL prevent processing until issues are resolved

### Requirement 11: Live Validation and Reactive Updates

**User Story:** As a user configuring patterns, I want validation errors and warnings to update immediately when I change any setting, so that I can see the impact of my changes in real-time without manual refresh.

#### Acceptance Criteria

1. WHEN any upstream input changes (token selection, role rules, advanced regex edits, or folder change from main screen) THEN the system SHALL automatically refresh validation errors and warnings on Step 4
2. WHEN there are no validation errors or warnings THEN the system SHALL hide the validation message box entirely (no empty container displayed)
3. WHEN validation succeeds THEN the system SHALL show "pattern configuration completed successfully" banner and ensure generation succeeds consistently
4. WHEN mode is switched OR step is entered/left OR any control changes THEN the system SHALL re-run validation using a debounced ValidationModel observed by Steps 2-4
5. WHEN success banner is shown THEN the system SHALL guarantee that pattern generation will succeed without contradicting validation blocks

### Requirement 12: Enhanced Token Selection and Propagation

**User Story:** As a user working with vehicle images, I want front tokens and custom tokens to be properly recognized and available throughout the pattern building process, so that I can configure patterns for all image types including front-facing cameras.

#### Acceptance Criteria

1. WHEN filename analysis detects front tokens (with synonyms: front, f, fr, forward) THEN the system SHALL display them in the Token Selection screen as selectable options
2. WHEN preconfigured Custom Tokens exist THEN the system SHALL pre-populate them in the Token Selection screen for user selection
3. WHEN switching into Simple mode for the first time THEN the system SHALL show the Custom Token handling dialog once
4. WHEN navigating normally within Simple mode OR using Advanced mode THEN the system SHALL NOT show unexpected Custom Token dialogs
5. WHEN front tokens are selected THEN the system SHALL carry them through to the Roles step for rule configuration

### Requirement 14: Custom Token Management and Persistence

**User Story:** As a user with specialized filename patterns, I want to define and persist custom tokens that are specific to my use case, so that the system can automatically recognize and categorize my unique filename components.

#### Acceptance Criteria

1. WHEN I add, edit, or remove custom tokens THEN the system SHALL immediately refresh tokenization and preview to reflect the changes
2. WHEN I define custom tokens THEN the system SHALL persist them across application restarts in the user configuration directory
3. WHEN custom tokens are updated THEN the system SHALL handle duplicates and collisions gracefully by updating existing definitions
4. WHEN the application starts THEN the system SHALL automatically load saved custom tokens and apply them during tokenization
5. WHEN no custom tokens are saved THEN the system SHALL load preconfigured tokens for common vehicle imaging scenarios

### Requirement 15: Real-time Configuration Publishing

**User Story:** As a user completing pattern configuration, I want the system to immediately publish valid configurations without requiring mode switches, so that I can apply my settings as soon as they are ready.

#### Acceptance Criteria

1. WHEN the final step generates a valid configuration in Simple mode THEN the system SHALL immediately publish it via onConfigReady callback
2. WHEN patterns are valid in Advanced mode THEN the system SHALL immediately publish the configuration via onConfigReady callback
3. WHEN the current mode's configuration is valid THEN the system SHALL enable the OK button for immediate application
4. WHEN OK is clicked THEN the system SHALL close the dialog and return the current valid configuration to the caller
5. WHEN configuration changes make it invalid THEN the system SHALL disable the OK button until issues are resolved

### Requirement 16: Enhanced Validation and UI State Management

**User Story:** As a user configuring patterns, I want validation to refresh immediately when any input changes and show clear actionable feedback, so that I can quickly identify and fix configuration issues.

#### Acceptance Criteria

1. WHEN inputs change on the final step (tokens, rules, preview count, mode switch) THEN the system SHALL immediately re-run validation
2. WHEN there are no validation errors or warnings THEN the system SHALL hide validation message boxes entirely
3. WHEN actionable validation issues exist THEN the system SHALL show Fix Errors button and clear error descriptions
4. WHEN configuration is valid THEN the system SHALL show success banner and enable OK button consistently
5. WHEN validation state changes THEN the system SHALL keep success banners and validation boxes synchronized without stale content

### Requirement 17: Intelligent Rule Suggestions

**User Story:** As a user configuring role rules, I want the system to suggest only relevant rules that I haven't already configured, so that I can quickly complete my setup without duplicate or unnecessary suggestions.

#### Acceptance Criteria

1. WHEN generating rule suggestions THEN the system SHALL only suggest rules not already present in configured rules
2. WHEN all detected patterns already have corresponding rules THEN the system SHALL hide the suggestions panel entirely
3. WHEN rules are added or removed THEN the system SHALL refresh suggestions to maintain accurate deduplication
4. WHEN no unique suggestions exist THEN the system SHALL not display empty suggestion containers
5. WHEN suggestions are available THEN the system SHALL clearly indicate which patterns triggered each suggestion

### Requirement 13: Simplified Folder Management in Simple Mode

**User Story:** As a user in Simple mode, I want the pattern builder to automatically use the folder I've already selected in the main screen, so that I don't have to redundantly pick the same folder again.

#### Acceptance Criteria

1. WHEN opening the pattern builder THEN the system SHALL only allow opening when the main screen folder is valid and selected
2. WHEN in Simple mode THEN the system SHALL NOT display Step 1 folder selection UI or any folder chooser within the builder
3. WHEN Simple mode is active THEN the system SHALL inherit the Input Folder from the main screen and display it read-only if needed for reference
4. WHEN navigating in Simple mode THEN the system SHALL NOT expose a Step 1 that allows changing the folder
5. WHEN the builder needs folder information THEN the system SHALL use the inherited path from the main screen without user intervention

### Requirement 9: Performance and Scalability

**User Story:** As a user with large image directories, I want the pattern builder to remain responsive during analysis, so that I can efficiently configure patterns even with thousands of files.

#### Acceptance Criteria

1. WHEN analyzing large directories THEN the system SHALL limit sample analysis to the first 500 files for performance
2. WHEN tokenization runs THEN the system SHALL complete analysis within 2 seconds for 500 files
3. WHEN preview updates THEN the system SHALL refresh the display within 500ms of configuration changes
4. WHEN memory usage is high THEN the system SHALL efficiently manage filename data without impacting application performance
5. WHEN background analysis runs THEN the system SHALL not block the UI thread

### Requirement 10: Accessibility and Usability

**User Story:** As a user with varying technical backgrounds, I want the pattern builder interface to be intuitive and accessible, so that I can successfully configure image processing regardless of my regex experience.

#### Acceptance Criteria

1. WHEN using the interface THEN the system SHALL provide clear labels, tooltips, and help text for all configuration options
2. WHEN errors occur THEN the system SHALL use plain language explanations rather than technical jargon
3. WHEN configuring patterns THEN the system SHALL use visual indicators (colors, icons) to show matching status and validation state
4. WHEN the interface loads THEN the system SHALL provide a guided workflow that leads users through configuration steps
5. WHEN help is needed THEN the system SHALL include contextual help explaining how filename patterns work and providing examples