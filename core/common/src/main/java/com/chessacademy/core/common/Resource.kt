package com.chessacademy.core.common

/**
 * Universeller Wrapper zur sicheren Abbildung von Lade-, Erfolgs- und Fehlerzuständen
 * in Repositories, UseCases und ViewModels.
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
