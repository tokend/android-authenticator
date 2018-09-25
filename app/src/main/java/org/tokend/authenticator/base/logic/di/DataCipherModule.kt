package org.tokend.authenticator.base.logic.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.DefaultDataCipher
import javax.inject.Singleton

@Module
class DataCipherModule {
    @Provides
    @Singleton
    fun dataCipher(): DataCipher {
        return DefaultDataCipher()
    }
}