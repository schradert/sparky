package com.sparkysballoons.invx.auth.domain

data class GoogleAccount(
    val token: String,
    val displayName: String = "",
    val profileImageUrl: String? = null,
)
