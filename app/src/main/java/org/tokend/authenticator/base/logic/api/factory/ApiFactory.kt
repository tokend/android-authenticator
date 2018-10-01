package org.tokend.authenticator.base.logic.api.factory

import org.tokend.sdk.api.ApiService
import org.tokend.sdk.keyserver.KeyStorage
import org.tokend.wallet.Account

interface ApiFactory {
    fun getApi(rootUrl: String): ApiService

    fun getSignedApi(rootUrl: String, signKeyPair: Account): ApiService

    fun getKeyStorage(rootUrl: String): KeyStorage

    fun getSignedKeyStorage(rootUrl: String, signKeyPair: Account): KeyStorage

    fun <T> getCustomApi(rootUrl: String, apiClass: Class<T>): T

    fun <T> getSignedCustomApi(rootUrl: String, apiClass: Class<T>, signKeyPair: Account): T
}