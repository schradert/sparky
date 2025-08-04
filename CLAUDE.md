# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

- `build-android` - Build Android app for debug
- `run-android` - Run the app on Android device/emulator
- `build-shared` - Build the shared KMP module
- `build-ios` - Build iOS framework for Xcode
- `test` - Run Kotlin tests
- `lint` - Run Kotlin linting (ktlint)
- `format` - Format Kotlin code with ktlint
- `clean` - Clean build artifacts

## Project Architecture

This is a Kotlin Multiplatform (KMP) inventory management app with shared UI using Compose Multiplatform. It uses Google Sheets as a backend database.

### KMP Structure
```
shared/                          # Shared KMP module
├── src/commonMain/kotlin/      # Shared business logic & UI
├── src/androidMain/kotlin/     # Android-specific implementations  
└── src/iosMain/kotlin/         # iOS-specific implementations
android/                        # Android application
ios/                           # iOS application
```

### Navigation Structure
- **Compose Navigation**: Bottom tab navigation with:
  - Inventory: View and search all inventory items with pull-to-refresh
  - Scanner: Camera-based barcode/QR scanning for item lookup
  - Search: Advanced search functionality
- **Authentication Flow**: Login screen for approved email validation

### Key Components

**Authentication Flow** (`shared/src/commonMain/kotlin/.../data/repository/AuthRepositoryImpl.kt`):
- Email-based authentication with approved user list stored in Google Sheets
- Session management with 7-day expiration using platform-specific storage
- Android: DataStore preferences, iOS: UserDefaults

**Data Layer** (`shared/src/commonMain/kotlin/.../data/remote/GoogleSheetsApi.kt`):
- Ktor HTTP client for Google Sheets API v4 integration
- Inventory operations: read, update quantities, add items, search
- Approved email validation
- Requires API key and sheet IDs configuration

**UI Architecture** (Compose Multiplatform):
- `InventoryScreen`: Main inventory list with search and pull-to-refresh
- `ScannerScreen`: Camera permission handling and scanning interface
- `SearchScreen`: Advanced search with real-time filtering
- `LoginScreen`: Email authentication interface
- All screens use Material Design 3 components

**Camera Integration**:
- Platform-specific camera controllers with common interface
- Android: CameraX integration (ready for ML Kit barcode scanning)
- iOS: Native camera integration points for Swift code

### Technology Stack
- **Shared**: Kotlin Multiplatform 1.9.21, Compose Multiplatform 1.5.11
- **Networking**: Ktor client with JSON serialization
- **DI**: Koin dependency injection
- **Navigation**: Compose Navigation
- **State Management**: ViewModels with StateFlow
- **Storage**: DataStore (Android), UserDefaults (iOS)
- **Build**: Gradle with version catalogs

### Configuration Requirements
Before running the app, update `shared/src/commonMain/kotlin/.../data/remote/GoogleSheetsApi.kt`:
- `apiKey`: Your Google Sheets API key
- `inventorySheetId`: Google Sheets ID containing inventory data  
- `approvedEmailsSheetId`: Google Sheets ID containing approved user emails

Expected inventory sheet structure: ID, Name, Description, Supplier, SKU, Quantity, ImageURL, Keywords