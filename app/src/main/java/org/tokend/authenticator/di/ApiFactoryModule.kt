package org.tokend.authenticator.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.logic.api.factory.ApiFactory
import org.tokend.authenticator.logic.api.factory.DefaultApiFactory
import javax.inject.Singleton

@Module
class ApiFactoryModule {
    @Provides
    @Singleton
    fun apiFactory(): ApiFactory {
        return DefaultApiFactory()
    }
}