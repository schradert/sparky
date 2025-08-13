package com.sparkysballoons.invx.auth.data

import android.content.Context
import androidx.credentials.CredentialManager
import com.sparkysballoons.invx.auth.domain.AuthApi

// FIXME remove coupling to android
expect class BasicAuthApi(
    context: Context,
    credentialManager: CredentialManager,
) : AuthApi

