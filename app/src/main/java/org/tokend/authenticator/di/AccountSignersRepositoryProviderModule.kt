package org.tokend.authenticator.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.logic.api.factory.ApiFactory
import org.tokend.authenticator.logic.db.AppDatabase
import org.tokend.authenticator.accounts.info.data.storage.AccountSignersRepositoryProvider
import javax.inject.Singleton

@Module
class AccountSignersRepositoryProviderModule {
    @Provides
    @Singleton
    fun accountSignersRepositoryProvider(database: AppDatabase,
                                         apiFactory: ApiFactory): AccountSignersRepositoryProvider {
        return AccountSignersRepositoryProvider(database, apiFactory)
    }
}