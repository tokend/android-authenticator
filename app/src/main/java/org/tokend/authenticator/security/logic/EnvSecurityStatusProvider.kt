package org.tokend.authenticator.security.logic

interface EnvSecurityStatusProvider {
    fun getEnvSecurityStatus(): EnvSecurityStatus
}