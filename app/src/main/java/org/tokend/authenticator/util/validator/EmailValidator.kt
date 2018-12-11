package org.tokend.authenticator.util.validator

object EmailValidator : RegexValidator(android.util.Patterns.EMAIL_ADDRESS.pattern())