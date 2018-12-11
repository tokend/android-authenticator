package org.tokend.authenticator.logic.transactions.factory

import org.tokend.authenticator.logic.api.factory.ApiFactory
import org.tokend.authenticator.logic.transactions.TxManager
import org.tokend.sdk.api.TokenDApi

class DefaultTxManagerFactory(
        private val apiFactory: ApiFactory
) : TxManagerFactory {
    override fun getTxManager(apiRootUrl: String): TxManager {
        return getTxManager(apiFactory.getApi(apiRootUrl))
    }

    override fun getTxManager(api: TokenDApi): TxManager {
        return TxManager(api)
    }
}