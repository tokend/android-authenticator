package org.tokend.authenticator.util.validator

interface CharSequenceValidator {
    fun isValid(sequence: CharSequence?): Boolean
}