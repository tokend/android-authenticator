package org.tokend.authenticator.base.logic.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.security.logic.AppUserKeyProvidersHolder
import javax.inject.Singleton

@Module
class AppUserKeyProvidersHolderModule {
    @Provides
    @Singleton
    fun userKeyProvidersHolder(): AppUserKeyProvidersHolder {
        return AppUserKeyProvidersHolder()
    }
}