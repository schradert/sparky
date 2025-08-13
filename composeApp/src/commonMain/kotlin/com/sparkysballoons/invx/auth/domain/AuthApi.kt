package com.sparkysballoons.invx.auth.domain

import com.sparkysballoons.invx.core.domain.DomainResult
import com.sparkysballoons.invx.core.domain.HttpError
import com.sparkysballoons.invx.core.domain.runMapCatch
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect val clientId: String

// FIXME avoid dependency on Google OAuth2.0 workflow
interface AuthApi {
    suspend fun signIn(): DomainResult<GoogleAccount>
    suspend fun signOut(): Unit
    suspend fun refreshAccessToken(refreshToken: String): DomainResult<TokenResponse> = runMapCatch(::HttpError) {
        HttpClient
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
