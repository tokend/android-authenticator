package org.tokend.authenticator.base.logic.di

import dagger.Component
import org.tokend.authenticator.base.activities.BaseActivity
import javax.inject.Singleton

@Component(
        modules = [
            AppModule::class,
            AppDatabaseModule::class,
            AccountsRepositoryModule::class,
            UtilModule::class,
            DataCipherModule::class,
            AccountSignersRepositoryProviderModule::class,
            DateFormatModule::class,
            AuthRequestConfirmationProviderFactoryModule::class,
            AuthAccountSelectorFactoryModule::class,
            EncryptionKeyProviderModule::class,
            ApiFactoryModule::class,
            TxManagerFactoryModule::class
        ]
)
@Singleton
interface AppComponent {
    fun inject(baseActivity: BaseActivity)
}