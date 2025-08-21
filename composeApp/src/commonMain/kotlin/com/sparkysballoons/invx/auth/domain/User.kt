package com.sparkysballoons.invx.auth.domain

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val profileImageUrl: String? = null,
)