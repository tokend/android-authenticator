package org.tokend.authenticator.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.security.encryption.cipher.DataCipher
import org.tokend.authenticator.security.encryption.cipher.DefaultDataCipher
import javax.inject.Singleton

@Module
class DataCipherModule {
    @Provides
    @Singleton
    fun dataCipher(): DataCipher {
        return DefaultDataCipher()
    }
}