package com.sparky.inventory.data.repository

import com.sparky.inventory.domain.model.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual class LocalStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val userKey = "sparky_user"
    
    actual suspend fun saveUser(user: User) {
        val userJson = Json.encodeToString(user)
        userDefaults.setObject(userJson, userKey)
    }
    
    actual suspend fun getUser(): User? {
        return try {
            val userJson = userDefaults.objectForKey(userKey) as? String
            userJson?.let { Json.decodeFromString<User>(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    actual suspend fun clearUser() {
        userDefaults.removeObjectForKey(userKey)
    }
}