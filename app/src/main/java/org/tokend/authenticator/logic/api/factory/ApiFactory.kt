package org.tokend.authenticator.logic.api.factory

import org.tokend.authenticator.logic.api.AuthenticatorApi
import org.tokend.sdk.keyserver.KeyStorage
import org.tokend.wallet.Account

interface ApiFactory {
    fun getApi(rootUrl: String): AuthenticatorApi

    fun getSignedApi(rootUrl: String, signKeyPair: Account): AuthenticatorApi

    fun getKeyStorage(rootUrl: String): KeyStorage

    fun getSignedKeyStorage(rootUrl: String, signKeyPair: Account): KeyStorage
}