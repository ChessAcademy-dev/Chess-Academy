package com.chessacademy.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Interface zur Bereitstellung von Coroutine-Dispatchern.
 * Ermöglicht sauberes Unit-Testing von ViewModels und UseCases durch Injektion von Test-Dispatchern.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher

    class Standard : DispatcherProvider {
        override val main: CoroutineDispatcher get() = Dispatchers.Main
        override val io: CoroutineDispatcher get() = Dispatchers.IO
        override val default: CoroutineDispatcher get() = Dispatchers.Default
    }
}
