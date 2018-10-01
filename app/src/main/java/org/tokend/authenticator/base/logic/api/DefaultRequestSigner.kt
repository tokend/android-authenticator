package org.tokend.authenticator.base.logic.api

import org.tokend.sdk.api.requests.RequestSigner
import org.tokend.wallet.Account

class DefaultRequestSigner(
        private val signer: Account
) : RequestSigner {
    override val accountId: String = signer.accountId

    override fun signToBase64(data: ByteArray): String {
        return signer.signDecorated(data).toBase64()
    }
}