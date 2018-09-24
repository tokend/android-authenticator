package org.tokend.authenticator.base.logic.di

import dagger.Component
import javax.inject.Singleton

@Component(
        modules = [AppDatabaseModule::class, AccountsRepositoryModule::class]
)
@Singleton
interface AppComponent {
}