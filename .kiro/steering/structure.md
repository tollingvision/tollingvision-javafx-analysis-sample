# Project Structure

## Root Directory Layout
```
analysis-sample/
├── .github/           # GitHub Actions workflows
├── .gradle/           # Gradle cache and build files
├── .kiro/             # Kiro IDE configuration and steering
├── .vscode/           # VS Code configuration
├── bin/               # Compiled class files
├── build/             # Build output directory
├── gradle/wrapper/    # Gradle wrapper files
├── src/               # Source code
├── build.gradle.kts   # Main build configuration
├── settings.gradle.kts # Project settings
├── gradlew           # Gradle wrapper script (Unix)
└── gradlew.bat       # Gradle wrapper script (Windows)
```

## Source Code Organization
```
src/main/java/
├── module-info.java                                        # Java module descriptor
└── com/smartcloudsolutions/tollingvision/samples/
    ├── AnalysisSampleApp.java                             # Main JavaFX application class
    ├── model/
    │   └── ImageGroupResult.java                         # Data model for analysis results
    ├── ui/
    │   ├── MainScreen.java                               # Main application UI
    │   └── GalleryWindow.java                            # Gallery viewer window
    └── util/
        └── OverlayUtils.java                             # Lightweight overlay utilities (no OpenCV)
```

## Module System
- **Module System**: Enabled (fully modular Java application)
- **Module Name**: `analysis.sample`
- **Main Class**: `com.smartcloudsolutions.tollingvision.samples.AnalysisSampleApp`
- **Package Structure**: 
  - `com.smartcloudsolutions.tollingvision.samples` (main application)
  - `com.smartcloudsolutions.tollingvision.samples.model` (data models)
  - `com.smartcloudsolutions.tollingvision.samples.ui` (UI components)
  - `com.smartcloudsolutions.tollingvision.samples.util` (lightweight overlay utilities)

## Key Files

### Build Configuration
- `build.gradle.kts` - Main build script with dependencies, JavaFX plugin, jlink, and jpackage configuration
- `settings.gradle.kts` - Project name and plugin management
- `module-info.java` - Java module descriptor for fully modular application

### Application Structure
- **Modular Architecture**: Separated into distinct classes for maintainability
  - `AnalysisSampleApp`: Main JavaFX application class (startup and processing coordination)
  - `MainScreen`: Main UI with configuration form, status counters, and event log
  - `GalleryWindow`: Dedicated gallery viewer with enhanced features
  - `ImageGroupResult`: Clean data model for analysis results
  - `OverlayUtils`: Lightweight overlay rendering without external dependencies

- **Modern UI Features**:
  - Two-column configuration layout with visual hierarchy
  - Resource bundle internationalization support
  - CSS theming with modern design system
  - Real-time status counters and progress tracking
  - Logo integration with high-quality scaling
  - Expandable event log that fills available space

- **Enhanced Gallery Window**:
  - Individual image overlay rendering using SearchResponse.result.frame and plate.position
  - 50/50 responsive two-column header layout (ANPR/MMR + Analysis Data)
  - Clickable thumbnail strip with stable sizing and proper spacing (6 thumbnails per row)
  - Per-image results binding using direct path-based mapping
  - Lightweight overlay rendering without external dependencies
  - Current image SearchResponse data panel with proper per-image binding
  - Enhanced image viewer with overlays and zoom/pan functionality
  - Full keyboard navigation (arrow keys + ESC, Enter to open gallery)
  - Individual bounding boxes per image using Quadrilateral data
  - Proper pan/zoom clamping to keep image within viewport bounds
  - Minimum zoom factor of 1.0 with pan-only-when-zoomed behavior
  - Auto-rendering of data on gallery open (no click required)
  - Symmetrical thumbnail strip layout with proper margins
  - MMR formatting: `<make> <model> <variation> <generation> | <category> (<body type>)`

- **Technical Implementation**:
  - Protobuf JsonFormat for all JSON serialization (no Jackson dependency)
  - Proto-backed SearchResponse parsing for accurate overlay positioning
  - Proper keyboard event handling and focus management
  - Accessibility and keyboard navigation throughout
  - Comprehensive Javadoc documentation for all public APIs
  - Fully modular architecture with proper JPMS integration
  - CSV export with [LOG] line filtering
  - camelCase JSON formatting for consistency

## Naming Conventions
- **Package**: `com.smartcloudsolutions.tollingvision.samples` (follows reverse domain convention)
- **Main Class**: `AnalysisSampleApp` (descriptive application name)
- **Module**: `analysis.sample` (kebab-case module naming)
- **Project**: `analysis-sample` (kebab-case for Gradle project)

## Configuration Files
- User configuration stored in `~/.tollingvision-client/config.json`
- CSS themes exported to user-specified locations
- CSV results exported to configurable output files

## Development Notes
- **Modular Design**: Separated concerns into distinct classes for maintainability
- **Clean Architecture**: UI, data models, and utilities in separate packages
- **Proto Integration**: Uses actual TollingVision proto definitions for accurate data handling
- **Lightweight Architecture**: No heavy external dependencies (removed OpenCV)
- **Pure Java Implementation**: All overlay rendering done with JavaFX Canvas
- **Protobuf-First**: All JSON serialization uses Protobuf JsonFormat for consistency
- **Direct Path Mapping**: SearchResponse lookup uses direct path-based mapping for simplicity
- **Comprehensive Documentation**: All public APIs have proper Javadoc documentation

## Current Architecture Highlights
- **Application Size**: ~55KB JAR (99.95% reduction from OpenCV version)
- **Module System**: Fully modular with proper JPMS integration
- **Dependencies**: Only essential gRPC, Netty, and Protobuf dependencies
- **JSON Handling**: Unified Protobuf JsonFormat for all serialization
- **Gallery Features**: Auto-rendering, thumbnail navigation, zoom/pan with clamping
- **CSV Export**: Intelligent filtering to exclude log entries
- **Cross-Platform**: Native installers for Windows (.exe), macOS (.dmg), Linux (.deb)