package org.tokend.authenticator.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.security.userkey.logic.AppUserKeyProvidersHolder
import javax.inject.Singleton

@Module
class AppUserKeyProvidersHolderModule {
    @Provides
    @Singleton
    fun userKeyProvidersHolder(): AppUserKeyProvidersHolder {
        return AppUserKeyProvidersHolder()
    }
}