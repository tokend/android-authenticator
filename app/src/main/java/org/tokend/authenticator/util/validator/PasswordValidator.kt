package org.tokend.authenticator.util.validator

/**
 * Validator of password strength
 */
object PasswordValidator :
        RegexValidator("^.{6,}$")