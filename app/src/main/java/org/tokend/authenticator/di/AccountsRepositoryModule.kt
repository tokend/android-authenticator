package org.tokend.authenticator.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.accounts.data.storage.AccountsRepository
import org.tokend.authenticator.logic.db.AppDatabase
import javax.inject.Singleton

@Module
class AccountsRepositoryModule {
    @Provides
    @Singleton
    fun accountsRepository(database: AppDatabase): AccountsRepository {
        return AccountsRepository(database)
    }
}