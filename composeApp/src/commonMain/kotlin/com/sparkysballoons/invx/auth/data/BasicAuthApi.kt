package com.sparkysballoons.invx.auth.data

import com.sparkysballoons.invx.auth.domain.AuthApi
import io.ktor.client.HttpClient

expect class BasicAuthApi(client: HttpClient): AuthApi
