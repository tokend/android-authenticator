package org.tokend.authenticator.base.util.validators

interface CharSequenceValidator {
    fun isValid(sequence: CharSequence?): Boolean
}