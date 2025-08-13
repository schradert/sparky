package com.sparkysballoons.invx.auth

import android.content.Context
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.toResultOr
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Err
import com.sparkysballoons.invx.domain.ApiError
import com.sparkysballoons.invx.domain.DomainResult
import com.sparkysballoons.invx.domain.HttpError
import com.sparkysballoons.invx.domain.runMapCatch
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.call.body
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module

expect val authModule: Module
expect val clientId: String

data class GoogleAccount(
    val token: String,
    val displayName: String = "",
    val profileImageUrl: String? = null,
)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Int? = null,
    @SerialName("token_type") val tokenType: String? = null,
)

interface GoogleAuthApi {
    suspend fun signIn(): DomainResult<GoogleAccount>
    suspend fun signOut(): Unit
    suspend fun refreshAccessToken(refreshToken: String): DomainResult<TokenResponse> = runMapCatch(::HttpError) {
        HttpClient(CIO)
            { install(ContentNegotiation) { json(Json {}) } }
            .submitForm(
                url = "https://accounts.google.com/o/oauth2/token",
                formParameters = parameters {
                    append("grant_type", "refresh_token")
                    append("client_id", clientId)
                    append("refresh_token", refreshToken)
                }
            )
            .body<TokenResponse>()
    }
}

expect class BasicGoogleAuthApi(
    context: Context,
    credentialManager: CredentialManager,
) : GoogleAuthApi

interface GoogleAuthStorage {
    suspend fun saveToken(token: TokenResponse)
    suspend fun getToken(): TokenResponse?
    suspend fun clearToken(): Unit
}

expect class LocalGoogleAuthStorage(
    context: Context,
) : GoogleAuthStorage

interface GoogleAuthRepository {
    suspend fun signIn(): DomainResult<GoogleAccount>
    suspend fun signOut(): Unit
    suspend fun refreshToken(): DomainResult<TokenResponse>
    suspend fun getStoredToken(): TokenResponse?
}

class BasicGoogleAuthRepository(
    private val api: GoogleAuthApi,
    private val storage: GoogleAuthStorage,
) : GoogleAuthRepository {
    override suspend fun getStoredToken(): TokenResponse? = storage.getToken()
    override suspend fun signIn(): DomainResult<GoogleAccount> = api.signIn()

    override suspend fun signOut(): Unit {
        api.signOut()
        storage.clearToken()
    }

    override suspend fun refreshToken(): DomainResult<TokenResponse> = coroutineBinding {
        getStoredToken()
            .toResultOr { ApiError("No token found in local storage") }
            .bind()
            .refreshToken
            .toResultOr { ApiError("No refresh token") }
            .bind()
            .let { api.refreshAccessToken(it) }
            .bind()
            .also { storage.saveToken(it) }
    }
}

class AuthViewModel(private val repository: GoogleAuthRepository) : ViewModel() {
    fun signIn() = viewModelScope.launch { repository.signIn() }
}

interface GoogleButtonClick {
    fun onSignInClicked()
    fun onSignOutClicked()
}

@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    onGoogleSignInResult: (GoogleAccount?) -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        onClick = { /* TODO: implement sign in */ },
        content = { Text("Sign In with Google") },
    )
}