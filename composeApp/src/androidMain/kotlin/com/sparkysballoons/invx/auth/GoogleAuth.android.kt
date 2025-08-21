package com.sparkysballoons.invx.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
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
import com.sparkysballoons.invx.auth.data.BasicAuthApi
import com.sparkysballoons.invx.auth.data.BasicAuthRepository
import com.sparkysballoons.invx.auth.data.LocalAuthStorage
import com.sparkysballoons.invx.auth.domain.AuthApi
import com.sparkysballoons.invx.auth.domain.AuthRepository
import com.sparkysballoons.invx.auth.domain.AuthStorage
import com.sparkysballoons.invx.auth.domain.GoogleAccount
import com.sparkysballoons.invx.auth.domain.TokenResponse
import com.sparkysballoons.invx.core.domain.ApiError
import com.sparkysballoons.invx.core.domain.DomainResult
import com.sparkysballoons.invx.core.domain.HttpError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.core.annotation.Single

val androidClientId = "420632028099-sfhj3vemp2li7qtu2f177qprksn17aa6.apps.googleusercontent.com"

val androidAuthModule = module {
    singleOf(::BasicAuthApi) bind AuthApi::class
    singleOf(::LocalAuthStorage) bind AuthStorage::class
    singleOf(::BasicAuthRepository) bind AuthRepository::class
    factory { CredentialManager.create(androidContext()) } bind CredentialManager::class
    factory { androidContext() } bind Context::class
}

