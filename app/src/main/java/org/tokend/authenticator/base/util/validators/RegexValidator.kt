package org.tokend.authenticator.base.util.validators

open class RegexValidator(pattern: String) : CharSequenceValidator {
    private val regex = Regex(pattern)

    override fun isValid(sequence: CharSequence?): Boolean {
        sequence ?: return false
        return regex.matches(sequence)
    }
}