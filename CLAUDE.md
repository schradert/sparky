# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

- `npm start` - Start the React Native Metro bundler
- `npm run android` - Run the app on Android device/emulator
- `npm run ios` - Run the app on iOS device/simulator
- `npm test` - Run Jest tests
- `npm run lint` - Run ESLint for code quality checks

## Project Architecture

This is a React Native inventory management app that uses Google Sheets as a backend database.

### Navigation Structure
- **Stack Navigator**: Root navigation handling authentication flow
  - Unauthenticated: Shows `LoginScreen`
  - Authenticated: Shows main tab navigator with modal screens
- **Tab Navigator**: Main app interface with 3 tabs:
  - Inventory: View and search all inventory items
  - Scanner: QR/barcode scanning for item lookup
  - Search: Advanced search functionality
- **Modal Screens**: `ItemDetailScreen`, `AddItemScreen` for detailed operations

### Key Components

**Authentication Flow** (`src/context/AuthContext.js`):
- Email-based authentication with approved user list stored in Google Sheets
- Session management with 7-day expiration using AsyncStorage
- Demo implementation with simulated email tokens

**Data Layer** (`src/services/GoogleSheetsService.js`):
- Direct integration with Google Sheets API v4
- Inventory operations: read, update quantities, add items, search
- Approved email validation
- Requires API key and sheet IDs configuration

**Screen Architecture**:
- `InventoryScreen`: Main inventory list with search and pull-to-refresh
- `ScannerScreen`: Camera-based barcode/QR scanning using react-native-vision-camera
- `AddItemScreen`: Form for adding new inventory items
- `ItemDetailScreen`: Detailed item view with quantity updates
- `SearchScreen`: Advanced search interface
- `LoginScreen`: Email authentication interface

### Technology Stack
- React Native 0.72.6 with React Navigation v6
- UI: React Native Paper + React Native Elements
- Camera: react-native-vision-camera with code scanner
- Storage: AsyncStorage for authentication persistence
- HTTP: Axios for Google Sheets API calls
- Icons: react-native-vector-icons (MaterialIcons)

### Configuration Requirements
Before running the app, update `src/services/GoogleSheetsService.js`:
- `GOOGLE_SHEETS_API_KEY`: Your Google Sheets API key
- `INVENTORY_SHEET_ID`: Google Sheets ID containing inventory data
- `APPROVED_EMAILS_SHEET_ID`: Google Sheets ID containing approved user emails

Expected inventory sheet structure: Name, Description, Supplier, SKU, Quantity, ImageURL, Keywords