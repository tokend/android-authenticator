package org.tokend.authenticator.base.logic.di

import android.content.Context
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.security.logic.DefaultEnvSecurityStatusProvider
import org.tokend.authenticator.security.logic.EnvSecurityStatusProvider
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