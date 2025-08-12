# Technology Stack

## Build System
- **Build Tool**: Gradle with Kotlin DSL (`build.gradle.kts`)
- **Java Version**: Java 17 LTS (Temurin recommended)
- **Module System**: Enabled (fully modular Java application)

## Core Technologies
- **UI Framework**: JavaFX 17 (modular)
- **RPC Protocol**: gRPC with Netty transport
- **Security**: TLS 1.2+ support with optional insecure certificates
- **Concurrency**: Java ExecutorService with configurable thread pools

## Key Dependencies
- `io.grpc:grpc-netty` - gRPC networking
- `com.smart-cloud-solutions:tollingvision:2.6.1` - TollingVision API client
- `io.netty:netty-bom` - Network transport layer
- `com.google.protobuf:protobuf-java` - Protocol buffer serialization
- `com.google.protobuf:protobuf-java-util` - Protobuf JSON formatting utilities

## Common Commands

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

### Development
```bash
# Compile only
./gradlew compileJava

# Run tests
./gradlew test

# Check dependencies
./gradlew dependencies
```

## Dependency Management
- **No Jackson**: Removed Jackson dependency, using only Protobuf JsonFormat
- **No OpenCV**: Removed heavy OpenCV dependency for lightweight deployment
- **Minimal Footprint**: Only essential gRPC, Netty, and Protobuf dependencies
- **Module Compatibility**: All dependencies are properly modularized
- **Version Management**: Uses BOM (Bill of Materials) for consistent versioning

## Platform-Specific Notes
- **Windows**: Generates `.exe` installer
- **macOS**: Generates `.dmg` installer  
- **Linux**: Generates `.deb` installer (default)
- Memory allocation: 512 MB heap default
- Lightweight application without heavy native dependencies
- Application JAR size: ~55KB (99.95% reduction from previous OpenCV version)