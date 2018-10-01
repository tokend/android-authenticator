package org.tokend.authenticator.base.logic.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.base.logic.api.factory.ApiFactory
import org.tokend.authenticator.base.logic.api.factory.DefaultApiFactory
import javax.inject.Singleton

@Module
class ApiFactoryModule {
    @Provides
    @Singleton
    fun apiFactory(): ApiFactory {
        return DefaultApiFactory()
    }
}