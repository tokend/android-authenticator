package org.tokend.authenticator.security.userkey.logic

/**
 * Holds active user key providers.
 * Foreground activity, if it can show user key input, must register its providers here.
 * Initial user key provider will be called if there is no set up user key
 * (first key request after app install or on user key change).
 * Default user key provider will be called it there is set up user key.
 */
class AppUserKeyProvidersHolder {
    private val initialUserKeyProviders = linkedSetOf<UserKeyProvider>()
    private val defaultUserKeyProviders = linkedSetOf<UserKeyProvider>()

    /**
     * @return Last registered initial user key provider.
     */
    fun getInitialUserKeyProvider(): UserKeyProvider {
        return initialUserKeyProviders.last()
    }

    /**
     * @return Last registered default user key provider.
     */
    fun getDefaultUserKeyProvider(): UserKeyProvider {
        return defaultUserKeyProviders.last()
    }

    // region Register/Unregister
    fun registerInitialUserKeyProvider(userKeyProvider: UserKeyProvider) {
        initialUserKeyProviders.add(userKeyProvider)
    }

    fun unregisterInitialUserKeyProvider(userKeyProvider: UserKeyProvider) {
        initialUserKeyProviders.remove(userKeyProvider)
    }

    fun registerDefaultUserKeyProvider(userKeyProvider: UserKeyProvider) {
        defaultUserKeyProviders.add(userKeyProvider)
    }

    fun unregisterDefaultUserKeyProvider(userKeyProvider: UserKeyProvider) {
        defaultUserKeyProviders.remove(userKeyProvider)
    }
    // endregion
}