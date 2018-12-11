package org.tokend.authenticator.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.security.encryption.logic.SecureStorage
import org.tokend.authenticator.security.environment.logic.EnvSecurityStatusProvider
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