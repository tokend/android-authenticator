package org.tokend.authenticator.logic.api.factory

import org.tokend.authenticator.logic.api.AuthenticatorApi
import org.tokend.sdk.keyserver.KeyServer
import org.tokend.wallet.Account

interface ApiFactory {
    fun getApi(rootUrl: String): AuthenticatorApi

    fun getSignedApi(rootUrl: String, signKeyPair: Account): AuthenticatorApi

    fun getKeyServer(rootUrl: String): KeyServer

    fun getSignedKeyServer(rootUrl: String, signKeyPair: Account): KeyServer
}