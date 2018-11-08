package org.tokend.authenticator.base.logic.wallet

import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.rx.extensions.createSignersUpdateTransactionSingle
import org.tokend.rx.extensions.toSingle
import org.tokend.rx.extensions.updateWalletCompletable
import org.tokend.sdk.api.TokenDApi
import org.tokend.sdk.keyserver.KeyStorage
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.sdk.keyserver.models.WalletData
import org.tokend.sdk.keyserver.models.WalletInfo
import org.tokend.wallet.Account
import org.tokend.wallet.NetworkParams
import org.tokend.wallet.Transaction
import retrofit2.HttpException
import java.net.HttpURLConnection

class WalletUpdateManager {
    fun updateWalletWithNewKeyPair(walletInfo: WalletInfo,
                                   signedApi: TokenDApi,
                                   keyStorage: KeyStorage,
                                   network: Network,
                                   signKeyPair: Account,
                                   newMasterKeyPair: Account,
                                   newKdfAttributes: KdfAttributes): Single<WalletData> {
        val originalAccountId = walletInfo.accountId
        val walletId = walletInfo.walletIdHex

        return signedApi.accounts.getSigners(originalAccountId)
                .toSingle()
                .onErrorResumeNext { error ->
                    // When account is not yet exists return empty signers list.
                    return@onErrorResumeNext if (error is HttpException
                            && error.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                        Single.just(emptyList())
                    } else {
                        Single.error(error)
                    }
                }
                // Create new wallet with signers update transaction inside.
                .flatMap { signers ->
                    createWalletForUpdate(
                            currentWallet = walletInfo,
                            currentSigners = signers,
                            newMasterKeyPair = newMasterKeyPair,
                            newKdfAttributes = newKdfAttributes,
                            network = network,
                            signKeyPair = signKeyPair
                    )
                }
                // Update current wallet with it.
                .flatMap { newWallet ->
                    keyStorage.updateWalletCompletable(walletId, newWallet)
                            .andThen(Single.just(newWallet))
                }
    }

    // region Creation
    private fun createWalletForUpdate(network: Network,
                                      currentWallet: WalletInfo,
                                      currentSigners: Collection<org.tokend.sdk.api.accounts.model.Account.Signer>,
                                      signKeyPair: Account,
                                      newMasterKeyPair: Account,
                                      newKdfAttributes: KdfAttributes): Single<WalletData> {
        val email = currentWallet.email

        return Single.zip(
                WalletManager.createWallet(
                        email = email,
                        kdfAttributes = newKdfAttributes,
                        masterAccount = newMasterKeyPair,
                        recoveryAccount = signKeyPair
                ),

                createSignersUpdateTransaction(
                        network.toNetParams(),
                        currentWallet,
                        signKeyPair,
                        currentSigners,
                        newMasterKeyPair
                ),

                BiFunction { t1: WalletData, t2: Transaction -> Pair(t1, t2) }
        )

                .map { (wallet, transaction) ->
                    wallet.addTransactionRelation(transaction)
                    wallet
                }
    }

    private fun createSignersUpdateTransaction(networkParams: NetworkParams,
                                               currentWallet: WalletInfo,
                                               currentAccount: Account,
                                               currentSigners: Collection<org.tokend.sdk.api.accounts.model.Account.Signer>,
                                               newAccount: Account): Single<Transaction> {
        return KeyStorage.createSignersUpdateTransactionSingle(
                networkParams,
                currentWallet.accountId,
                currentAccount,
                currentSigners,
                newAccount
        ).subscribeOn(Schedulers.newThread())
    }
    // endregion
}