package com.sparkysballoons.invx.auth.data

import android.content.Context
import com.sparkysballoons.invx.auth.domain.AuthStorage
import com.sparkysballoons.invx.auth.domain.TokenResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class LocalAuthStorage actual constructor(
    private val context: Context,
) : AuthStorage {
    private val json = Json {}
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    override suspend fun saveToken(token: TokenResponse) = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY_TOKEN, json.encodeToString(token)).apply()
    }

    override suspend fun getToken(): TokenResponse? = withContext(Dispatchers.IO) {
        val jsonString = prefs.getString(KEY_TOKEN, null) ?: return@withContext null
        return@withContext try {
            json.decodeFromString<TokenResponse>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun clearToken() = withContext(Dispatchers.IO) {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    companion object {
        private const val KEY_TOKEN = "token_response"
    }
}