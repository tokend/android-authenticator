package org.tokend.authenticator.base.util.error_handlers

interface ErrorHandler {
    fun handle(error: Throwable): Boolean
    fun getErrorMessage(error: Throwable): String?
}