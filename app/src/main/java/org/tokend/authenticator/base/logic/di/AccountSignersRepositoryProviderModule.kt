package org.tokend.authenticator.base.logic.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.base.logic.db.AppDatabase
import org.tokend.authenticator.signers.storage.AccountSignersRepositoryProvider
import javax.inject.Singleton

@Module
class AccountSignersRepositoryProviderModule {
    @Provides
    @Singleton
    fun accountSignersRepositoryProvider(database: AppDatabase): AccountSignersRepositoryProvider {
        return AccountSignersRepositoryProvider(database)
    }
}