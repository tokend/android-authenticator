package org.tokend.authenticator.security.userkey.model

class TooManyUserKeyAttemptsException(val maxAttempts: Int)
    : Exception("Maximum failed attempts count is $maxAttempts")