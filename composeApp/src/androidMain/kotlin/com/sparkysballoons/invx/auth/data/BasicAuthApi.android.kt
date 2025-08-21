package com.sparkysballoons.invx.auth.data

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.github.michaelbull.result.toResultOr
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sparkysballoons.invx.auth.domain.AuthApi
import com.sparkysballoons.invx.auth.domain.GoogleAccount
import com.sparkysballoons.invx.auth.domain.clientId
import com.sparkysballoons.invx.core.domain.ApiError
import com.sparkysballoons.invx.core.domain.DomainResult
import com.sparkysballoons.invx.core.domain.HttpError
import kotlinx.coroutines.runBlocking

actual class BasicAuthApi actual constructor(
    private val context: Context,
    private val credentialManager: CredentialManager,
) : AuthApi {
    override suspend fun signOut() = credentialManager.clearCredentialState(ClearCredentialStateRequest())
    override suspend fun signIn(): DomainResult<GoogleAccount> = binding {
        val option = GetGoogleIdOption
            .Builder()
            .setFilterByAuthorizedAccounts(true)
            .setAutoSelectEnabled(true)
            .setServerClientId(clientId)
            .build()

        credentialManager
            .runCatching {
                runBlocking {
                    getCredential(
                        context = context,
                        request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                    )
                }
            }
            .mapError(::HttpError)
            .bind()
            .credential
            .takeIf { it is CustomCredential && it.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL }
            .toResultOr { ApiError("Credential method not supported") }
            .bind()
            .let { credential ->
                runCatching { GoogleIdTokenCredential.createFrom((credential as CustomCredential).data) }
            }
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