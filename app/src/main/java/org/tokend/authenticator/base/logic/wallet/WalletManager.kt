package org.tokend.authenticator.base.logic.wallet

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.tokend.authenticator.base.extensions.toSingle
import org.tokend.sdk.keyserver.KeyStorage
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.sdk.keyserver.models.WalletData
import org.tokend.sdk.keyserver.models.WalletInfo
import org.tokend.wallet.Account

class WalletManager(
        private val keyStorage: KeyStorage
) {
    // region API interaction
    fun getWalletInfo(email: String, password: CharArray,
                      isRecovery: Boolean = false): Single<WalletInfo> {
        return {
            keyStorage.getWalletInfo(email, password, isRecovery)
        }.toSingle().subscribeOn(Schedulers.newThread())
    }

    fun saveWallet(walletData: WalletData): Completable {
        return {
            keyStorage.saveWallet(walletData)
        }.toSingle().ignoreElement()
    }

    fun updateWallet(walletId: String, walletData: WalletData): Completable {
        return {
            keyStorage.updateWallet(walletId, walletData)
        }.toSingle().ignoreElement()
    }
    // endregion

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
                val (wallet, _, _) = KeyStorage.createWallet(
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