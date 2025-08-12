# Product Overview

TollingVision JavaFX Client is a cross-platform desktop application for batch processing vehicle images through AI analysis. The application uploads grouped vehicle images (front, rear, overview) to a TollingVision server via gRPC and displays live processing results with an interactive gallery.

## Key Features
- Image grouping and batch processing
- gRPC integration with TollingVision service
- Real-time progress tracking and logging
- Interactive gallery with AI overlay results and zoom/pan functionality
- Plate recognition and Make/Model recognition (MMR)
- Cross-platform support (Windows, Linux, macOS)
- SSL/TLS security support
- Parallel processing with configurable thread pools
- Lightweight application without heavy dependencies
- Auto-rendering gallery with thumbnail navigation
- CSV export with intelligent filtering
- Fully modular Java application with jlink/jpackage support

## Target Users
- Traffic enforcement agencies
- Tolling system operators
- Vehicle monitoring services
- Any organization requiring automated license plate and vehicle recognition

## Core Workflow
1. Configure input folder and processing patterns
2. Set service connection parameters
3. Start batch processing with real-time monitoring
4. Review results in interactive gallery with:
   - Auto-rendered current image data
   - Clickable thumbnail navigation
   - Zoom/pan functionality with proper clamping
   - Overlay visualization of detection results
5. Export results to CSV format (excludes log entries)