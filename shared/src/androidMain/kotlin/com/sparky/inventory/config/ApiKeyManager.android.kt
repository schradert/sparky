package com.sparky.inventory.config

/**
 * Android implementation of secure API key management
 * In production, keys should be stored in BuildConfig or encrypted preferences
 */
actual class ApiKeyManager {
    
    actual fun getGoogleSheetsApiKey(): String {
        // In production, use BuildConfig: BuildConfig.GOOGLE_SHEETS_API_KEY
        // For now, check environment or provide secure fallback
        return System.getProperty("GOOGLE_SHEETS_API_KEY") 
            ?: throw ConfigurationException(
                "Google Sheets API key not configured. " +
                "Set GOOGLE_SHEETS_API_KEY in BuildConfig or system properties."
            )
    }
    
    actual fun getInventorySheetId(): String {
        return System.getProperty("INVENTORY_SHEET_ID")
            ?: throw ConfigurationException(
                "Inventory sheet ID not configured. " +
                "Set INVENTORY_SHEET_ID in BuildConfig or system properties."
            )
    }
    
    actual fun getApprovedEmailsSheetId(): String {
        return System.getProperty("APPROVED_EMAILS_SHEET_ID")
            ?: throw ConfigurationException(
                "Approved emails sheet ID not configured. " +
                "Set APPROVED_EMAILS_SHEET_ID in BuildConfig or system properties."
            )
    }
    
    actual fun validateConfiguration() {
        val requiredKeys = mapOf(
            "GOOGLE_SHEETS_API_KEY" to ::getGoogleSheetsApiKey,
            "INVENTORY_SHEET_ID" to ::getInventorySheetId,
            "APPROVED_EMAILS_SHEET_ID" to ::getApprovedEmailsSheetId
        )
        
        val missingKeys = mutableListOf<String>()
        
        requiredKeys.forEach { (keyName, getter) ->
            try {
                getter.invoke()
            } catch (e: ConfigurationException) {
                missingKeys.add(keyName)
            }
        }
        
        if (missingKeys.isNotEmpty()) {
            throw ConfigurationException(
                "Missing required configuration keys: ${missingKeys.joinToString(", ")}\n" +
                "Please ensure these are set in your BuildConfig or system properties."
            )
        }
    }
}