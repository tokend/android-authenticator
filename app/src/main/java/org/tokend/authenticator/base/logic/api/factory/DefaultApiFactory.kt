package org.tokend.authenticator.base.logic.api.factory

import org.tokend.authenticator.base.extensions.addSlashIfNeed
import org.tokend.authenticator.base.logic.api.DefaultRequestSigner
import org.tokend.sdk.api.ApiService
import org.tokend.sdk.keyserver.KeyStorage
import org.tokend.wallet.Account

class DefaultApiFactory : ApiFactory {
    override fun getApi(rootUrl: String): ApiService {
        return org.tokend.sdk.factory.ApiFactory(rootUrl.addSlashIfNeed())
                .getApiService()
    }

    override fun getSignedApi(rootUrl: String,
                              signKeyPair: Account): ApiService {
        return org.tokend.sdk.factory.ApiFactory(rootUrl.addSlashIfNeed())
                .getApiService(DefaultRequestSigner(signKeyPair))
    }

    override fun getKeyStorage(rootUrl: String): KeyStorage {
        return KeyStorage(rootUrl.addSlashIfNeed())
    }

    override fun getSignedKeyStorage(rootUrl: String,
                                     signKeyPair: Account): KeyStorage {
        return KeyStorage(rootUrl.addSlashIfNeed(),
                requestSigner = DefaultRequestSigner(signKeyPair))
    }

    override fun <T> getCustomApi(rootUrl: String,
                                  apiClass: Class<T>): T {
        return org.tokend.sdk.factory.ApiFactory(rootUrl.addSlashIfNeed())
                .getCustomService(apiClass)
    }

    override fun <T> getSignedCustomApi(rootUrl: String,
                                        apiClass: Class<T>,
                                        signKeyPair: Account): T {
        return org.tokend.sdk.factory.ApiFactory(rootUrl.addSlashIfNeed())
                .getCustomService(apiClass, DefaultRequestSigner(signKeyPair))
    }
}