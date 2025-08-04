package com.sparky.inventory.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sparky.inventory.domain.model.User
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sparky_prefs")

actual class LocalStorage(private val context: Context) {
    private val userKey = stringPreferencesKey("user")
    
    actual suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[userKey] = Json.encodeToString(user)
        }
    }
    
    actual suspend fun getUser(): User? {
        return try {
            val preferences = context.dataStore.data.first()
            val userJson = preferences[userKey]
            userJson?.let { Json.decodeFromString<User>(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    actual suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(userKey)
        }
    }
}