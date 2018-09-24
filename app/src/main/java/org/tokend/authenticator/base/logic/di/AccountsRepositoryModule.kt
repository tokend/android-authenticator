package org.tokend.authenticator.base.logic.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.accounts.logic.storage.AccountsRepository
import org.tokend.authenticator.base.logic.db.AppDatabase
import javax.inject.Singleton

@Module
class AccountsRepositoryModule {
    @Provides
    @Singleton
    fun accountsRepository(database: AppDatabase): AccountsRepository {
        return AccountsRepository(database)
    }
}