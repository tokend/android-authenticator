package org.tokend.authenticator.base.logic.di

import dagger.Component
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.base.activities.SettingsFragment
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
            AppEncryptionKeyProviderModule::class,
            ApiFactoryModule::class,
            TxManagerFactoryModule::class,
            AppUserKeyProvidersHolderModule::class,
            SecureStorageModule::class,
            EnvSecurityStatusProviderModule::class,
            ActivityUserKeyProviderFactoryModule::class
        ]
)
@Singleton
interface AppComponent {
    fun inject(baseActivity: BaseActivity)
    fun inject(settingsFragment: SettingsFragment)
}