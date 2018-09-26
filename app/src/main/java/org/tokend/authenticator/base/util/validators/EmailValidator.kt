package org.tokend.authenticator.base.util.validators

object EmailValidator : RegexValidator(android.util.Patterns.EMAIL_ADDRESS.pattern())