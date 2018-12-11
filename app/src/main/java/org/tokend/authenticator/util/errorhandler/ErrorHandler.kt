package org.tokend.authenticator.util.errorhandler

interface ErrorHandler {
    fun handle(error: Throwable): Boolean
    fun getErrorMessage(error: Throwable): String?
}