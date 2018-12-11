package org.tokend.authenticator.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.security.environment.logic.EnvSecurityStatusProvider
import org.tokend.authenticator.security.userkey.logic.UserKeyTypeStorage
import org.tokend.authenticator.security.userkey.view.ActivityUserKeyProviderFactory
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