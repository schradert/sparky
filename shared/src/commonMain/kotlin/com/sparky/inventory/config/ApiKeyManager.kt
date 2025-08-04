package com.sparky.inventory.config

/**
 * Secure API key management for Kotlin Multiplatform
 * Uses expect/actual pattern to implement platform-specific secure storage
 */
expect class ApiKeyManager {
    fun getGoogleSheetsApiKey(): String
    fun getInventorySheetId(): String 
    fun getApprovedEmailsSheetId(): String
    fun validateConfiguration()
}

/**
 * Configuration validation errors
 */
class ConfigurationException(message: String) : Exception(message)