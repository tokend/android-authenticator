package org.tokend.authenticator.base.logic.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.security.logic.AppEncryptionKeyProvider
import org.tokend.authenticator.security.logic.AppUserKeyProvidersHolder
import org.tokend.authenticator.security.logic.EnvSecurityStatusProvider
import org.tokend.authenticator.security.logic.PunishmentTimer
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