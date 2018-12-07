package org.tokend.authenticator.base.logic.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.base.logic.encryption.SecureStorage
import org.tokend.authenticator.security.logic.EnvSecurityStatusProvider
import javax.inject.Singleton

@Module
class SecureStorageModule(
        private val storagePreferences: SharedPreferences
) {
    @Provides
    @Singleton
    fun secureStorage(envSecurityStatusProvider: EnvSecurityStatusProvider): SecureStorage {
        return SecureStorage(
                storagePreferences,
                envSecurityStatusProvider.getEnvSecurityStatus()
        )
    }
}