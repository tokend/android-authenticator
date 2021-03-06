package org.tokend.authenticator.logic.wallet

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.tokend.authenticator.util.extensions.toSingle
import org.tokend.sdk.keyserver.KeyServer
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.sdk.keyserver.models.WalletData
import org.tokend.wallet.Account

class WalletManager {
    companion object {
        private const val STUB_KDF_VERSION = 2L

        // region Creation
        /**
         * @return Fully armed and ready to go [WalletData].
         */
        fun createWallet(email: String,
                         masterAccount: Account,
                         recoveryAccount: Account,
                         kdfAttributes: KdfAttributes): Single<WalletData> {
            return {
                val (wallet, _, _) = KeyServer.createWallet(
                        email = email,
                        password = masterAccount.secretSeed!!,
                        kdfVersion = STUB_KDF_VERSION,
                        kdfAttributes = kdfAttributes,
                        rootAccount = masterAccount,
                        recoveryAccount = recoveryAccount
                )

                wallet
            }.toSingle().subscribeOn(Schedulers.newThread())
        }
    }
}