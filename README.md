# TollingVision JavaFX Client

A lightweight, cross-platform desktop application for batch processing vehicle images through [TollingVision](https://tollingvision.com)'s AI analysis. Built with modern Java technologies and a fully modular architecture.

## Overview

TollingVision JavaFX Client uploads grouped vehicle images (front, rear, overview) to a TollingVision server via gRPC and displays live processing results with an interactive gallery. The application features real-time progress tracking, AI overlay visualization, and comprehensive result export capabilities.

## Key Features

- **Batch Image Processing**: Automated grouping and processing of vehicle images
- **Real-time Progress Tracking**: Live status updates and comprehensive event logging
- **Interactive Gallery**: Viewer with AI overlays, zoom/pan, and thumbnail navigation
- **Cross-platform Support**: Native installers for Windows, Linux, and macOS
- **Modular Design**: Fully modular Java application with JPMS integration
- **Security**: TLS 1.2+ support with configurable certificate handling
- **Export Capabilities**: CSV export

## Technology Stack

### Build System
- **Build Tool**: Gradle with Kotlin DSL (`build.gradle.kts`)
- **Java Version**: Java 17 LTS (Temurin recommended)
- **Module System**: Fully modular Java application with JPMS

### Core Technologies
- **UI Framework**: JavaFX 17 (modular)
- **RPC Protocol**: gRPC with Netty transport
- **Security**: TLS 1.2+ support with optional insecure certificates
- **Concurrency**: Java ExecutorService with configurable thread pools

### Key Dependencies
- `io.grpc:grpc-netty` - gRPC networking
- `com.smart-cloud-solutions:tollingvision:2.6.1` - TollingVision API client
- `io.netty:netty-bom` - Network transport layer
- `com.google.protobuf:protobuf-java` - Protocol buffer serialization
- `com.google.protobuf:protobuf-java-util` - Protobuf JSON formatting utilities

### Architecture Highlights
- **No Heavy Dependencies**: Removed OpenCV and Jackson for lightweight deployment
- **Protobuf-First**: All JSON serialization uses Protobuf JsonFormat
- **Pure Java Implementation**: All overlay rendering done with JavaFX Canvas
- **Memory Efficient**: 512 MB heap default with minimal footprint  

## Quick Start

### Prerequisites
- Java 17 LTS or later
- Gradle 7.0+ (or use included wrapper)

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew run

# Clean build artifacts
./gradlew clean
```

### Packaging
```bash
# Create runtime image with jlink
./gradlew jlink

# Create platform-specific installer
./gradlew jpackage
```

## Project Structure

```
analysis-sample/
├── src/main/java/
│   ├── module-info.java                                        # Java module descriptor
│   └── com/smartcloudsolutions/tollingvision/samples/
│       ├── AnalysisSampleApp.java                             # Main JavaFX application
│       ├── model/
│       │   └── ImageGroupResult.java                         # Data model for results
│       ├── ui/
│       │   ├── MainScreen.java                               # Main application UI
│       │   └── GalleryWindow.java                            # Gallery viewer window
│       └── util/
│           └── OverlayUtils.java                             # Lightweight overlay utilities
├── src/main/resources/
│   ├── assets/logo.png                                       # Application logo
│   ├── messages.properties                                   # Internationalization
│   └── tollingvision-theme.css                              # Application theme
├── build.gradle.kts                                          # Main build configuration
└── settings.gradle.kts                                       # Project settings
```

## User Interface

### Main Application
- **Two-column Configuration Layout**: Clean, hierarchical form design
- **Real-time Status Counters**: Live tracking of processing progress
- **Expandable Event Log**: Comprehensive logging with filtering
- **Resource Bundle Support**: Internationalization ready
- **CSS Theming**: Modern design system with customizable themes

### Enhanced Gallery Window
- **Auto-rendering**: Immediate display of current image data on open
- **Thumbnail Navigation**: Clickable strip with 6 thumbnails per row
- **Zoom/Pan Functionality**: Enhanced image viewer with proper viewport clamping
- **Overlay Visualization**: Individual bounding boxes using Quadrilateral data
- **Keyboard Navigation**: Full arrow key support plus ESC/Enter shortcuts
- **Responsive Layout**: 50/50 header layout for ANPR/MMR and Analysis Data

## API Integration

### gRPC Services
| Interface                               | Streaming     | Port(s)  | Request            | Response            | Purpose                                     |
|-----------------------------------------|--------------|----------|--------------------|---------------------|---------------------------------------------|
| TollingVisionService.Search             | Server-stream | 80 / 443 | SearchRequest      | stream SearchResponse | Single-image search (front or any shot)     |
| TollingVisionService.Analyze            | Server-stream | 80 / 443 | EventRequest       | stream EventResponse | Multi-view event analysis                   |
| grpc.health.v1.Health/Check              | Unary         | 80 / 443 | HealthCheckRequest | HealthCheckResponse  | Service liveness probe                       |

All RPCs run over HTTP/2 with TLS 1.2+ support when secured mode is enabled.

## Core Workflow

1. **Configuration**: Set input folder, processing patterns, and service parameters
2. **Image Grouping**: Automatic detection and grouping of vehicle images
3. **Batch Processing**: Parallel upload with configurable thread pools
4. **Real-time Monitoring**: Live progress tracking and comprehensive logging
5. **Interactive Results**: Enhanced gallery with auto-rendering and navigation
6. **Data Export**: CSV export with intelligent log filtering

## Features in Detail

### Image Processing
- **Recursive Folder Scanning**: Automatic discovery of image files with configurable patterns
- **Smart Grouping**: Filename-based grouping with regex pattern matching
- **Role Detection**: Automatic classification of front, rear, and overview images
- **Format Support**: JPEG and PNG with automatic compression for large files
- **Batch Processing**: Parallel processing with configurable thread pools

### Real-time Monitoring
- **Live Status Counters**: Groups discovered, requests sent, responses OK/Error
- **Comprehensive Event Log**: Timestamped entries with color-coded status
- **Progress Tracking**: Real-time updates during batch processing
- **Error Handling**: Graceful handling with exponential back-off retry logic

### Gallery Features
- **Auto-rendering**: Immediate display of analysis results on gallery open
- **Enhanced Navigation**: Clickable thumbnail strip with stable sizing
- **Zoom/Pan Controls**: Enhanced image viewer with proper viewport clamping
- **Overlay Visualization**: Individual bounding boxes using Quadrilateral data
- **Keyboard Shortcuts**: Arrow keys for navigation, ESC to close, Enter to open
- **Data Binding**: Per-image results with direct path-based mapping
- **MMR Formatting**: Structured display of Make/Model/Recognition data

### Data Export
- **CSV Export**: Comprehensive results export with intelligent filtering
- **Log Filtering**: Automatic exclusion of log entries from export data
- **Configuration Persistence**: User settings saved to `~/.tollingvision-client/config.json`
- **Theme Export**: CSS stylesheet export for customization

### Security & Networking
- **TLS Support**: TLS 1.2+ with configurable certificate handling
- **gRPC Integration**: Efficient binary protocol with streaming support
- **Connection Management**: Automatic retry logic and graceful error handling
- **Resource Management**: Configurable thread pools and memory limits

## Technical Implementation

### Protocol Buffers
The application uses Protocol Buffers for all data serialization:

```proto
message SearchRequest {
  bytes image                         = 1;  // required
  bool plate_recognition              = 2;  // default: true
  bool make_and_model_recognition     = 3;  // default: false
  bool sign_recognition               = 4;  // default: false
  bool international_recognition      = 5;  // default: false
  bool resampling                     = 6;  // default: true
  bool results_without_plate_type     = 7;  // default: false
  string location                     = 8;  // optional ISO-3166-2
  repeated Region region              = 9;  // optional polygons (ROI)
  int32 max_search                    = 10; // default: 1, max: 5
  int32 max_rotation                  = 11; // default: 45°, max: 180°
  int32 max_character_size            = 12; // default: 80px (-1=unlimited)
}
```

### Modular Architecture
- **Module Name**: `analysis.sample`
- **Main Class**: `com.smartcloudsolutions.tollingvision.samples.AnalysisSampleApp`
- **Clean Separation**: UI, data models, and utilities in separate packages
- **JPMS Integration**: Fully modular with proper module descriptor

## Development Commands

### Build Operations
```bash
# Compile only
./gradlew compileJava

# Run tests
./gradlew test

# Check dependencies
./gradlew dependencies
```

### Platform-Specific Packaging
- **Windows**: Generates `.exe` installer
- **macOS**: Generates `.dmg` installer  
- **Linux**: Generates `.deb` installer (default)

## Performance & Requirements

| Category      | Specification |
|---------------|---------------|
| **Performance**   | ≤ 1s UI latency; 100 groups/min @ 8 threads |
| **Scalability**   | Tested with 50,000+ images |
| **Memory**        | 512 MB heap default |
| **Application Size** | ~55KB JAR |
| **Portability**   | Single-file installer per OS via `jpackage` |
| **Security**      | TLS 1.2+ support, no plaintext credentials |
| **Usability**     | Full keyboard navigation, tooltips, accessibility |

## Target Users

- Traffic enforcement agencies
- Tolling system operators  
- Vehicle monitoring services
- Organizations requiring automated license plate and vehicle recognition

## Configuration

User configuration is automatically saved to `~/.tollingvision-client/config.json` and includes:
- Input folder paths and processing patterns
- Service connection parameters
- Thread pool configuration
- UI preferences and themes

## Dependencies

The application maintains a minimal dependency footprint:
- **Core**: gRPC, Netty, Protocol Buffers
- **UI**: JavaFX 17 (modular)
- **No Heavy Dependencies**: Removed OpenCV and Jackson for lightweight deployment
- **BOM Management**: Uses Bill of Materials for consistent versioning

## License

This project is part of the TollingVision ecosystem. Please refer to your TollingVision license agreement for usage terms and conditions.

## Support

For technical support and documentation, please contact Smart Cloud Solutions or refer to the TollingVision API documentation.
