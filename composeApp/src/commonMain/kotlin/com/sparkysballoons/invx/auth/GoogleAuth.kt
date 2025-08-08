package com.sparkysballoons.invx.auth

import android.content.Context
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import com.sparkysballoons.invx.domain.DomainResult
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    fun signIn(): DomainResult<GoogleAccount>
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
            .body()
    }
}

expect class BasicGoogleAuthApi(
    private val context: Context,
    private val credentialManager: CredentialManager,
) : GoogleAuthApi

interface GoogleAuthStorage {
    suspend fun saveToken(token: TokenResponse)
    suspend fun getToken(): TokenResponse?
    suspend fun clearToken(): Unit
}

expect class LocalGoogleAuthStorage(
    private val context: Context,
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

    override suspend fun refreshToken(): DomainResult<TokenResponse> = binding {
        getStoredToken()
            .toResultOr { ApiError("No token found in local storage") }
            .bind()
            .refreshToken
            .toResultOr { ApiError("No refresh token") }
            .bind()
            .let { api.refreshAccessToken(it, clientId) }
            .bind()
            .let { storage.saveToken(it.data) }
    }
}

class AuthViewModel(repository: GoogleAuthRepository) : ViewModel() {
    fun signIn(): DomainResult<GoogleAccount> = viewModelScope.launch { repository.signIn() }
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
    val viewModel = koinViewModel<AuthViewModel>()
    OutlinedButton(
        modifier = modifier,
        onClick = { viewModel.signIn() },
        content = { Text("Sign In with Google") },
    )
}