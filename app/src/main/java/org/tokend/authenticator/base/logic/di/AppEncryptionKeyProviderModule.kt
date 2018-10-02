package org.tokend.authenticator.base.logic.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.security.logic.AppEncryptionKeyProvider
import org.tokend.authenticator.security.logic.AppUserKeyProvidersHolder
import javax.inject.Singleton

@Module
class AppEncryptionKeyProviderModule(
        private val keystorePreferences: SharedPreferences
) {
    @Provides
    @Singleton
    fun encryptionKeyProvider(cipher: DataCipher,
                              appUserKeyProvidersHolder: AppUserKeyProvidersHolder
    ): AppEncryptionKeyProvider {
        return AppEncryptionKeyProvider(
                keystorePreferences,
                cipher,
                appUserKeyProvidersHolder
        )
    }
}