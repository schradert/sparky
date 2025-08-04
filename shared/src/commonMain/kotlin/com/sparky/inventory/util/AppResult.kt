package com.sparky.inventory.util

/**
 * Sealed class for consistent error handling across the application
 * Replaces the generic Result class with more specific app-focused states
 */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val exception: AppException) : AppResult<Nothing>()
    data class Loading(val message: String = "Loading...") : AppResult<Nothing>()
    
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading
    
    fun getDataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    fun getErrorOrNull(): AppException? = when (this) {
        is Error -> exception
        else -> null
    }
    
    fun getDataOrElse(defaultValue: () -> @UnsafeVariance T): @UnsafeVariance T = when (this) {
        is Success -> data
        else -> defaultValue()
    }
    
    inline fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }
    
    inline fun <R> flatMap(transform: (T) -> AppResult<R>): AppResult<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> this
    }
    
    inline fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (AppException) -> Unit): AppResult<T> {
        if (this is Error) action(exception)
        return this
    }
    
    companion object {
        fun <T> success(data: T): AppResult<T> = Success(data)
        fun error(exception: AppException): AppResult<Nothing> = Error(exception)
        fun error(message: String, type: ErrorType = ErrorType.UNKNOWN): AppResult<Nothing> = 
            Error(AppException(message, type))
        fun loading(message: String = "Loading..."): AppResult<Nothing> = Loading(message)
    }
}

/**
 * Application-specific exception with categorized error types
 */
data class AppException(
    override val message: String,
    val type: ErrorType,
    val originalException: Throwable? = null,
    val timestamp: Long = PlatformUtils.currentTimeMillis()
) : Exception(message, originalException)

/**
 * Categorized error types for better error handling
 */
enum class ErrorType {
    NETWORK,
    AUTHENTICATION, 
    AUTHORIZATION,
    VALIDATION,
    CONFIGURATION,
    API_ERROR,
    UNKNOWN
}

/**
 * Extension functions for converting standard Result to AppResult
 */
fun <T> Result<T>.toAppResult(): AppResult<T> = when {
    isSuccess -> AppResult.success(getOrThrow())
    else -> AppResult.error(
        AppException(
            message = exceptionOrNull()?.message ?: "Unknown error",
            type = ErrorType.UNKNOWN,
            originalException = exceptionOrNull()
        )
    )
}