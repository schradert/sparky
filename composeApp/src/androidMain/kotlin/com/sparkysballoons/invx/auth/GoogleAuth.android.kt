package com.sparkysballoons.invx.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManager.Companion.create
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.github.michaelbull.result.toResultOr
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.sparkysballoons.invx.domain.ApiError
import com.sparkysballoons.invx.domain.DomainResult
import com.sparkysballoons.invx.domain.HttpError
import com.sparkysballoons.invx.domain.runMapCatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val clientId = "420632028099-sfhj3vemp2li7qtu2f177qprksn17aa6.apps.googleusercontent.com"
actual val authModule = module {
    singleOf(::BasicGoogleAuthApi)
    singleOf(::BasicGoogleAuthRepository)
    singleOf(::LocalGoogleAuthStorage)
    factoryOf(::AuthViewModel)
    factory { create(androidContext()) } bind CredentialManager::class
}

actual class BasicGoogleAuthApi(
    private val context: Context,
    private val credentialManager: CredentialManager,
) : GoogleAuthApi {
    actual suspend fun signOut() = credentialManager.clearCredentialState(ClearCredentialStateRequest())
    actual fun signIn(): DomainResult<GoogleAccount> = binding {
        credentialManager
            .runCatching {
                let option = GetGoogleIdOption
                    .Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setAutoSelectEnabled(true)
                    .setServerClientId(clientId)
                    // TODO set nonce
                    // .setNonce("")
                    .build()

                it.getCredential(
                    context = context,
                    request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                )
            }
            .mapError(::HttpError)
            .bind()
            .takeIf { it is CustomCredential && it.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL }
            .toResultOr { ApiError("Credential method not supported") }
            .bind()
            .runCatching { GoogleIdTokenCredential.createFrom(it.data) }
            .mapError(::HttpError)
            .bind()
            .let {
                GoogleAccount(
                    token = it.idToken,
                    displayName = it.displayName ?: "",
                    profileImageUrl = it.profilePictureUri?.toString(),
                )
            }
    }
}

actual class LocalGoogleAuthStorage(
    private val context: Context,
) : GoogleAuthStorage {
    private val json = Json {}
    private val prefs = context.getSharedPreferences("auth")

    override suspend fun saveToken(token: TokenResponse) = withContext(Dispatchers.IO) {
        val jsonString = json.encodeToString(token)
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