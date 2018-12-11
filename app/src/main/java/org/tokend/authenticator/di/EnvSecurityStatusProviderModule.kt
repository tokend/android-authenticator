package org.tokend.authenticator.di

import android.content.Context
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.security.environment.logic.DefaultEnvSecurityStatusProvider
import org.tokend.authenticator.security.environment.logic.EnvSecurityStatusProvider
import javax.inject.Singleton

@Module
class EnvSecurityStatusProviderModule() {
    @Provides
    @Singleton
    fun envSecurityStatusProvider(context: Context): EnvSecurityStatusProvider {
        return DefaultEnvSecurityStatusProvider(
                context
        )
    }
}