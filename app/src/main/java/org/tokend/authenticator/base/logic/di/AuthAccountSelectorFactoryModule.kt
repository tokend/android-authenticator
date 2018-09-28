package org.tokend.authenticator.base.logic.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.accounts.logic.storage.AccountsRepository
import org.tokend.authenticator.auth.view.accounts.selection.AuthAccountSelectorFactory
import javax.inject.Singleton

@Module
class AuthAccountSelectorFactoryModule {
    @Provides
    @Singleton
    fun authAccountSelectorFactory(accountsRepository: AccountsRepository): AuthAccountSelectorFactory {
        return AuthAccountSelectorFactory(accountsRepository)
    }
}