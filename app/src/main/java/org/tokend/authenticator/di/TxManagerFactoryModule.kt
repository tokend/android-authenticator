package org.tokend.authenticator.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.logic.api.factory.ApiFactory
import org.tokend.authenticator.logic.transactions.factory.DefaultTxManagerFactory
import org.tokend.authenticator.logic.transactions.factory.TxManagerFactory
import javax.inject.Singleton

@Module
class TxManagerFactoryModule {
    @Provides
    @Singleton
    fun txManagerFactory(apiFactory: ApiFactory): TxManagerFactory {
        return DefaultTxManagerFactory(apiFactory)
    }
}