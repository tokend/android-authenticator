package org.tokend.authenticator.base.util.validators

/**
 * Validator of password strength
 */
object PasswordValidator :
        RegexValidator("^.{6,}$")