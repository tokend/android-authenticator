package org.tokend.authenticator.security.environment.logic

import org.tokend.authenticator.security.environment.model.EnvSecurityStatus

interface EnvSecurityStatusProvider {
    fun getEnvSecurityStatus(): EnvSecurityStatus
}