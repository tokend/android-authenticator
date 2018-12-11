package org.tokend.authenticator.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.security.encryption.cipher.DataCipher
import org.tokend.authenticator.security.encryption.logic.AppEncryptionKeyProvider
import org.tokend.authenticator.security.userkey.logic.AppUserKeyProvidersHolder
import org.tokend.authenticator.security.environment.logic.EnvSecurityStatusProvider
import org.tokend.authenticator.security.userkey.punishment.PunishmentTimer
import javax.inject.Singleton

@Module
class AppEncryptionKeyProviderModule(
        private val keystorePreferences: SharedPreferences
) {
    @Provides
    @Singleton
    fun encryptionKeyProvider(cipher: DataCipher,
                              appUserKeyProvidersHolder: AppUserKeyProvidersHolder,
                              envSecurityStatusProvider: EnvSecurityStatusProvider,
                              punishmentTimer: PunishmentTimer
    ): AppEncryptionKeyProvider {
        return AppEncryptionKeyProvider(
                keystorePreferences,
                cipher,
                appUserKeyProvidersHolder,
                envSecurityStatusProvider.getEnvSecurityStatus(),
                punishmentTimer
        )
    }
}