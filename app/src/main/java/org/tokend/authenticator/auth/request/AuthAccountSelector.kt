package org.tokend.authenticator.auth.request

import io.reactivex.Maybe
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider

interface AuthAccountSelector {
    data class Result(
            val account: Account,
            val cipher: DataCipher,
            val encryptionKeyProvider: EncryptionKeyProvider
    )

    fun selectAccountForAuth(network: Network,
                             authRequest: AuthRequest): Maybe<Result>
}