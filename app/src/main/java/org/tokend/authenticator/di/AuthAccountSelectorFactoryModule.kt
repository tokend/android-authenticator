package org.tokend.authenticator.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.accounts.data.storage.AccountsRepository
import org.tokend.authenticator.auth.request.accountselection.view.AuthAccountSelectorFactory
import javax.inject.Singleton

@Module
class AuthAccountSelectorFactoryModule {
    @Provides
    @Singleton
    fun authAccountSelectorFactory(accountsRepository: AccountsRepository): AuthAccountSelectorFactory {
        return AuthAccountSelectorFactory(accountsRepository)
    }
}