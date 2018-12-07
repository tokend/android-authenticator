package org.tokend.authenticator.security.logic

enum class EnvSecurityStatus {
    /**
     * Android default security: application files are stored in private directory,
     * AndroidKeyStore is secure.
     */
    NORMAL,

    /**
     * Security is not granted, maybe root access is granted:
     * AndroidKeyStore can't be used, application files are accessible,
     * app memory dump can be captured, black helicopters are above you.
     */
    COMPROMISED
}