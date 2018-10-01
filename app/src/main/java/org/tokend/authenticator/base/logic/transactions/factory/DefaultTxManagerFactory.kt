package org.tokend.authenticator.base.logic.transactions.factory

import org.tokend.authenticator.base.logic.api.factory.ApiFactory
import org.tokend.authenticator.base.logic.transactions.TxManager
import org.tokend.sdk.api.ApiService

class DefaultTxManagerFactory(
        private val apiFactory: ApiFactory
) : TxManagerFactory {
    override fun getTxManager(apiRootUrl: String): TxManager {
        return getTxManager(apiFactory.getApi(apiRootUrl))
    }

    override fun getTxManager(api: ApiService): TxManager {
        return TxManager(api)
    }
}