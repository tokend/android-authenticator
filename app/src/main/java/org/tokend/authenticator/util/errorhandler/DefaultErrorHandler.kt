package org.tokend.authenticator.util.errorhandler

import android.content.Context
import org.tokend.authenticator.R
import org.tokend.authenticator.view.util.ToastManager
import org.tokend.crypto.cipher.InvalidCipherTextException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.util.concurrent.CancellationException

open class DefaultErrorHandler(
        private val context: Context,
        private val toastManager: ToastManager
) : ErrorHandler {
    /**
     * Handles given [Throwable]
     * @return [true] if [error] was handled, [false] otherwise
     */
    override fun handle(error: Throwable): Boolean {
        when (error) {
            is CancellationException ->
                return true
            else -> {
                return getErrorMessage(error)?.let {
                    toastManager.short(it)
                    true
                } ?: false
            }
        }
    }

    /**
     * @return Localized error message for given [Throwable]
     */
    override fun getErrorMessage(error: Throwable): String? {
        return when (error) {
            is SocketTimeoutException ->
                context.getString(R.string.error_connection_try_again)
            is CancellationException, is InterruptedIOException ->
                null
            is IOException ->
                context.getString(R.string.error_connection_try_again)
            is InvalidCipherTextException ->
                context.getString(R.string.error_cipher_failed)
            else -> {
                context.getString(R.string.error_try_again)
            }
        }
    }
}