package com.sparkysballoons.invx.auth.data

import android.content.Context
import com.sparkysballoons.invx.auth.domain.AuthStorage

// FIXME remove coupling to android
expect class LocalAuthStorage(
    context: Context,
) : AuthStorage

