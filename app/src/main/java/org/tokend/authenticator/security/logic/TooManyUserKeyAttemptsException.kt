package org.tokend.authenticator.security.logic

class TooManyUserKeyAttemptsException(val maxAttempts: Int)
    : Exception("Maximum failed attempts count is $maxAttempts")