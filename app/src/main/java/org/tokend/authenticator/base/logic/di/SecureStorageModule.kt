package org.tokend.authenticator.base.logic.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.base.logic.encryption.SecureStorage
import javax.inject.Singleton

@Module
class SecureStorageModule(
        private val storagePreferences: SharedPreferences
) {
    @Provides
    @Singleton
    fun secureStorage(): SecureStorage {
        return SecureStorage(storagePreferences)
    }
}