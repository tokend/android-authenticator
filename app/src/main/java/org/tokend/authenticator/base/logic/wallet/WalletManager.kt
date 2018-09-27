package org.tokend.authenticator.base.logic.wallet

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.tokend.authenticator.base.extensions.toSingle
import org.tokend.sdk.api.models.WalletData
import org.tokend.sdk.api.models.WalletRelation
import org.tokend.sdk.keyserver.KeyStorage
import org.tokend.sdk.keyserver.models.KdfAttributes
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
        }.toSingle().toCompletable()
    }

    fun updateWallet(walletId: String, walletData: WalletData): Completable {
        return {
            keyStorage.updateWallet(walletId, walletData)
        }.toSingle().toCompletable()
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
                val walletId =
                        KeyStorage.getWalletIdHex(email, masterAccount.secretSeed!!, kdfAttributes)
                val originalAccountId = masterAccount.accountId

                // Base wallet has only KDF relation, we need more.
                val wallet = KeyStorage.createBaseWallet(
                        email, kdfAttributes.salt, walletId, ByteArray(32),
                        CharArray(0), originalAccountId, STUB_KDF_VERSION
                )

                // Add recovery relation.
                // Recovery seed must be encrypted with itself.
                val recoverySeed = recoveryAccount.secretSeed
                        ?: throw IllegalStateException("Provided recovery account has no private key")
                val (recoveryWalletId, recoveryKey) =
                        deriveKeys(email, recoverySeed, kdfAttributes).blockingGet()
                val encryptedRecovery = KeyStorage.encryptWalletKey(email, recoverySeed,
                        recoveryAccount.accountId, recoveryKey, kdfAttributes.salt)
                wallet.addRelation(WalletRelation(WalletRelation.RELATION_RECOVERY,
                        WalletRelation.RELATION_RECOVERY, recoveryWalletId,
                        recoveryAccount.accountId, encryptedRecovery))

                // TODO: Remove me, legacy.
                // Add password factor relation.
                val passwordFactorAccount = Account.random()
                val passwordFactorSeed = passwordFactorAccount.secretSeed!!
                val encryptedPasswordFactor = KeyStorage.encryptWalletKey(email, passwordFactorSeed,
                        passwordFactorAccount.accountId, ByteArray(32), kdfAttributes.salt)
                wallet.addRelation(WalletRelation(WalletRelation.RELATION_PASSWORD_FACTOR,
                        WalletRelation.RELATION_PASSWORD, walletId,
                        passwordFactorAccount.accountId, encryptedPasswordFactor))

                wallet
            }.toSingle().subscribeOn(Schedulers.newThread())
        }

        /**
         * @return [Pair] with the HEX-encoded wallet ID as a first element
         * and the wallet key as a second.
         */
        fun deriveKeys(email: String, password: CharArray, kdfAttributes: KdfAttributes)
                : Single<Pair<String, ByteArray>> {
            return {
                Pair(
                        KeyStorage.getWalletIdHex(email, password, kdfAttributes),
                        KeyStorage.getWalletKey(email, password, kdfAttributes)
                )
            }.toSingle().subscribeOn(Schedulers.newThread())
        }
        // endregion
    }
}