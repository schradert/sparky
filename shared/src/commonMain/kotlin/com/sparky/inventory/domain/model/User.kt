package com.sparky.inventory.domain.model

import kotlinx.serialization.Serializable
import com.sparky.inventory.util.PlatformUtils

@Serializable
data class User(
    val email: String,
    val isApproved: Boolean = false,
    val lastLogin: Long = PlatformUtils.currentTimeMillis(),
    val sessionToken: String = generateSessionToken(),
    val tokenExpiry: Long = PlatformUtils.currentTimeMillis() + SESSION_DURATION_MS
) {
    companion object {
        const val SESSION_DURATION_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
        
        private fun generateSessionToken(): String {
            // Generate a secure session token using timestamp and random data
            val timestamp = PlatformUtils.currentTimeMillis()
            val random = (0..1000000).random()
            return "session_${timestamp}_${random}"
        }
    }
    
    /**
     * Check if the current session is still valid
     */
    fun isSessionValid(): Boolean {
        return PlatformUtils.currentTimeMillis() < tokenExpiry
    }
    
    /**
     * Get remaining session time in milliseconds
     */
    fun getRemainingSessionTime(): Long {
        return maxOf(0, tokenExpiry - PlatformUtils.currentTimeMillis())
    }
}