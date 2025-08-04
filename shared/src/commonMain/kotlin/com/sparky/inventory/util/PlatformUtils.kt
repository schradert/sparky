package com.sparky.inventory.util

import kotlinx.datetime.Clock

/**
 * Multiplatform utility functions
 */
object PlatformUtils {
    /**
     * Get current timestamp in milliseconds
     */
    fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
}