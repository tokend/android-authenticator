package org.tokend.authenticator.util.errorhandler

import android.content.Context
import org.tokend.authenticator.view.util.ToastManager

class ErrorHandlerFactory(
        private val context: Context,
        private val toastManager: ToastManager
) {
    private val defaultErrorHandler: ErrorHandler by lazy {
        DefaultErrorHandler(context, toastManager)
    }

    fun getDefault(): ErrorHandler {
        return defaultErrorHandler
    }
}