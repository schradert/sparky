package com.sparky.inventory.config

import platform.Foundation.NSBundle

/**
 * iOS implementation of secure API key management
 * Uses Info.plist for configuration storage
 */
actual class ApiKeyManager {
    
    actual fun getGoogleSheetsApiKey(): String {
        return getConfigValue("GOOGLE_SHEETS_API_KEY")
    }
    
    actual fun getInventorySheetId(): String {
        return getConfigValue("INVENTORY_SHEET_ID")
    }
    
    actual fun getApprovedEmailsSheetId(): String {
        return getConfigValue("APPROVED_EMAILS_SHEET_ID")
    }
    
    private fun getConfigValue(key: String): String {
        val bundle = NSBundle.mainBundle
        val value = bundle.objectForInfoDictionaryKey(key) as? String
        
        return value ?: throw ConfigurationException(
            "$key not found in Info.plist. " +
            "Please add it to your iOS configuration."
        )
    }
    
    actual fun validateConfiguration() {
        val requiredKeys = listOf(
            "GOOGLE_SHEETS_API_KEY",
            "INVENTORY_SHEET_ID", 
            "APPROVED_EMAILS_SHEET_ID"
        )
        
        val missingKeys = mutableListOf<String>()
        
        requiredKeys.forEach { key ->
            try {
                getConfigValue(key)
            } catch (e: ConfigurationException) {
                missingKeys.add(key)
            }
        }
        
        if (missingKeys.isNotEmpty()) {
            throw ConfigurationException(
                "Missing required configuration keys in Info.plist: ${missingKeys.joinToString(", ")}\n" +
                "Please add these keys to your iOS Info.plist file."
            )
        }
    }
}