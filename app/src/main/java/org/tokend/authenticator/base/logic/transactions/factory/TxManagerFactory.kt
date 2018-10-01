package org.tokend.authenticator.base.logic.transactions.factory

import org.tokend.authenticator.base.logic.transactions.TxManager
import org.tokend.sdk.api.ApiService

interface TxManagerFactory {
    fun getTxManager(apiRootUrl: String): TxManager

    fun getTxManager(api: ApiService): TxManager
}