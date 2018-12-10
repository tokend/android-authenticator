package org.tokend.authenticator.base.logic.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.security.logic.EnvSecurityStatusProvider
import org.tokend.authenticator.security.logic.persistence.UserKeyTypeStorage
import org.tokend.authenticator.security.view.ActivityUserKeyProviderFactory
import javax.inject.Singleton

@Module
class ActivityUserKeyProviderFactoryModule(
        private val userKeyTypePreferences: SharedPreferences
) {
    @Provides
    @Singleton
    fun activityUserKeyProviderFactory(envSecurityStatusProvider: EnvSecurityStatusProvider)
            : ActivityUserKeyProviderFactory {
        return ActivityUserKeyProviderFactory(
                envSecurityStatusProvider.getEnvSecurityStatus(),
                UserKeyTypeStorage(userKeyTypePreferences)
        )
    }
}