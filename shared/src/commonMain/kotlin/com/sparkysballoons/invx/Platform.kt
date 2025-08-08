package com.sparkysballoons.invx

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform