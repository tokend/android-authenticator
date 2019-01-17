package org.tokend.authenticator.logic.api.factory

import org.tokend.authenticator.logic.api.AuthenticatorApi
import org.tokend.authenticator.util.extensions.addSlashIfNeed
import org.tokend.sdk.keyserver.KeyServer
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

    override fun getKeyServer(rootUrl: String): KeyServer {
        return KeyServer(getApi(rootUrl).wallets)
    }

    override fun getSignedKeyServer(rootUrl: String,
                                    signKeyPair: Account): KeyServer {
        return KeyServer(getSignedApi(rootUrl, signKeyPair).wallets)
    }
}