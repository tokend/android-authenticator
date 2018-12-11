package org.tokend.authenticator.logic.transactions.factory

import org.tokend.authenticator.logic.transactions.TxManager
import org.tokend.sdk.api.TokenDApi

interface TxManagerFactory {
    fun getTxManager(apiRootUrl: String): TxManager

    fun getTxManager(api: TokenDApi): TxManager
}