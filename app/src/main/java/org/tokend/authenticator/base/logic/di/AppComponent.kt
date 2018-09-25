package org.tokend.authenticator.base.logic.di

import dagger.Component
import javax.inject.Singleton

@Component(
        modules = [
            AppDatabaseModule::class,
            AccountsRepositoryModule::class,
            DataCipherModule::class
        ]
)
@Singleton
interface AppComponent {
}