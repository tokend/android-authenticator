package org.tokend.authenticator.base.logic.api.factory

import org.tokend.authenticator.base.extensions.addSlashIfNeed
import org.tokend.authenticator.base.logic.api.AuthenticatorApi
import org.tokend.sdk.keyserver.KeyStorage
import org.tokend.sdk.signing.AccountRequestSigner
import org.tokend.wallet.Account

class DefaultApiFactory : ApiFactory {
    override fun getApi(rootUrl: String): AuthenticatorApi {
        return AuthenticatorApi(rootUrl.addSlashIfNeed())
    }

    override fun getSignedApi(rootUrl: String,
                              signKeyPair: Account): AuthenticatorApi {
        return AuthenticatorApi(rootUrl.addSlashIfNeed(), AccountRequestSigner(signKeyPair))
    }

    override fun getKeyStorage(rootUrl: String): KeyStorage {
        return KeyStorage(getApi(rootUrl).wallets)
    }

    override fun getSignedKeyStorage(rootUrl: String,
                                     signKeyPair: Account): KeyStorage {
        return KeyStorage(getSignedApi(rootUrl, signKeyPair).wallets)
    }
}