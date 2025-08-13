package com.sparkysballoons.invx.domain

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching

sealed interface DomainMessage
data class ApiError(val m: String): DomainMessage
data class HttpError(val t: Throwable): DomainMessage

typealias DomainResult<T> = Result<T, DomainMessage>
inline fun <V> runMapCatch(
    mapErr: (Throwable) -> DomainMessage,
    block: () -> V,
): DomainResult<V> =
    runCatching { block() }.mapError(mapErr)
