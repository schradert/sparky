# 🔒 Security Configuration Guide

## **CRITICAL: API Key Security Setup**

Your app now uses secure API key management. **Never commit API keys to version control!**

### **Android Setup (BuildConfig)**

1. **Add to your `android/local.properties`** (this file is already in .gitignore):
```properties
GOOGLE_SHEETS_API_KEY=your_actual_google_sheets_api_key_here
INVENTORY_SHEET_ID=your_actual_inventory_sheet_id_here  
APPROVED_EMAILS_SHEET_ID=your_actual_approved_emails_sheet_id_here
```

2. **Update `android/build.gradle.kts`** to use BuildConfig:
```kotlin
android {
    defaultConfig {
        // Add BuildConfig fields for secure API key access
        buildConfigField("String", "GOOGLE_SHEETS_API_KEY", "\"${project.findProperty("GOOGLE_SHEETS_API_KEY") ?: ""}\"")
        buildConfigField("String", "INVENTORY_SHEET_ID", "\"${project.findProperty("INVENTORY_SHEET_ID") ?: ""}\"")
        buildConfigField("String", "APPROVED_EMAILS_SHEET_ID", "\"${project.findProperty("APPROVED_EMAILS_SHEET_ID") ?: ""}\"")
    }
}
```

3. **Update Android ApiKeyManager implementation** to use BuildConfig:
```kotlin
// In ApiKeyManager.android.kt
actual fun getGoogleSheetsApiKey(): String {
    return BuildConfig.GOOGLE_SHEETS_API_KEY.takeIf { it.isNotBlank() }
        ?: throw ConfigurationException("Google Sheets API key not configured in BuildConfig")
}
```

### **iOS Setup (Info.plist)**

1. **Add to your `ios/iosApp/Info.plist`**:
```xml
<key>GOOGLE_SHEETS_API_KEY</key>
<string>your_actual_google_sheets_api_key_here</string>
<key>INVENTORY_SHEET_ID</key>
<string>your_actual_inventory_sheet_id_here</string>
<key>APPROVED_EMAILS_SHEET_ID</key>
<string>your_actual_approved_emails_sheet_id_here</string>
```

2. **Add Info.plist to .gitignore** (or use a template approach):
```gitignore
ios/iosApp/Info.plist
```

### **Getting Your API Keys**

1. **Google Sheets API Key**:
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Enable Google Sheets API
   - Create credentials → API Key
   - Restrict the key to Google Sheets API only

2. **Sheet IDs**:
   - From your Google Sheets URL: `https://docs.google.com/spreadsheets/d/SHEET_ID_HERE/edit`
   - Copy the `SHEET_ID_HERE` portion

### **Required Sheet Structure**

**Inventory Sheet (Sheet1)**:
| ID | Name | Description | Supplier | SKU | Quantity | ImageURL | Keywords |
|----|------|-------------|----------|-----|----------|----------|----------|

**Approved Emails Sheet (Sheet1)**:
| Email |
|-------|
| user1@company.com |
| user2@company.com |

## **Security Features Implemented** ✅

- ✅ **No hardcoded API keys** - All keys moved to secure configuration
- ✅ **Environment-based configuration** - Different setups for dev/prod
- ✅ **Input validation** - All user inputs validated before API calls
- ✅ **Network timeouts** - Protection against hanging requests
- ✅ **Structured error handling** - Categorized errors with proper messages
- ✅ **API key validation** - Configuration checked on app startup
- ✅ **HTTP error handling** - Proper handling of 401, 403, 404, 429 errors
- ✅ **Secure logging** - No sensitive data in logs

## **Security Checklist** 

### **Before Deployment:**
- [ ] API keys are in secure configuration (not committed to git)
- [ ] Test with real Google Sheets setup
- [ ] Verify approved emails list works
- [ ] Test error handling scenarios
- [ ] Check that sensitive data is not logged
- [ ] Validate input sanitization
- [ ] Test network timeout behavior

### **Production Security:**
- [ ] Use restricted API keys (limit to Google Sheets API only)
- [ ] Regularly rotate API keys
- [ ] Monitor API usage in Google Cloud Console
- [ ] Keep approved emails list updated
- [ ] Enable 2FA on Google account managing the sheets
- [ ] Consider using service accounts for production

## **Troubleshooting**

### **"Configuration Exception: Missing required configuration keys"**
- Check that API keys are properly set in local.properties (Android) or Info.plist (iOS)
- Verify the key names match exactly (case-sensitive)

### **"Invalid API key" (401 Error)**
- Verify the API key is correct in Google Cloud Console
- Check that Google Sheets API is enabled
- Ensure API key restrictions allow your app

### **"Access forbidden" (403 Error)**  
- Check Google Sheets sharing permissions
- Verify the sheets are accessible with "Anyone with link can view"
- Confirm API key has proper permissions

### **"Sheet not found" (404 Error)**
- Double-check the sheet IDs are correct
- Ensure sheets exist and are accessible
- Verify sheet names match (case-sensitive)

---

**⚠️ IMPORTANT**: Never commit actual API keys to version control. Always use the secure configuration methods described above.