package org.tokend.authenticator.base.logic.wallet

import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.authenticator.base.extensions.toSingle
import org.tokend.sdk.api.ApiService
import org.tokend.sdk.api.models.WalletData
import org.tokend.sdk.api.requests.DataEntity
import org.tokend.sdk.api.responses.AccountResponse
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.sdk.keyserver.models.WalletInfo
import org.tokend.wallet.*
import org.tokend.wallet.xdr.Operation
import org.tokend.wallet.xdr.op_extensions.RemoveMasterKeyOp
import org.tokend.wallet.xdr.op_extensions.UpdateSignerOp
import retrofit2.HttpException
import java.net.HttpURLConnection

class WalletUpdateManager() {
//    fun recover(email: String,
//                recoverySeed: CharArray,
//                newMasterKeyPair: Account,
//                newKdfSalt: ByteArray): Completable {
//        return createKeyPairFromSeed(recoverySeed)
//                .doOnSuccess { keyPair ->
//                    val requestSigner = DefaultRequestSigner(keyPair)
//
//                    this.signKeyPair = keyPair
//                    this.api = ApiFactory(network.rootUrl)
//                            .getApiService(requestSigner)
//                    this.keyStorageApi = KeyStorage(
//                            keyServerUrl = network.rootUrl,
//                            requestSigner = requestSigner
//                    )
//                    this.walletManager = WalletManager(this.keyStorageApi)
//                }
//                // Get wallet info.
//                .flatMap {
//                    this.walletManager.getWalletInfo(email, recoverySeed, true)
//                }
//                // Update wallet with new password.
//                .flatMapCompletable { wallet ->
//                    updateWalletWithNewKeyPair(wallet, newMasterKeyPair, newKdfSalt)
//                }
//    }

    fun updateWalletWithNewKeyPair(walletInfo: WalletInfo,
                                   signedApi: ApiService,
                                   walletManager: WalletManager,
                                   network: Network,
                                   signKeyPair: Account,
                                   newMasterKeyPair: Account,
                                   newKdfAttributes: KdfAttributes): Single<WalletData> {
        val originalAccountId = walletInfo.accountId
        val walletId = walletInfo.walletIdHex

        return signedApi.getAccountSigners(originalAccountId)
                .toSingle()
                .map { it.signers ?: emptyList() }
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
                    walletManager.updateWallet(walletId, newWallet)
                            .andThen(Single.just(newWallet))
                }
    }

    // region Creation
    private fun createKeyPairFromSeed(seed: CharArray): Single<Account> {
        return {
            Account.fromSecretSeed(seed)
        }.toSingle()
    }

    private fun createWalletForUpdate(network: Network,
                                      currentWallet: WalletInfo,
                                      currentSigners: Collection<AccountResponse.Signer>,
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

                createSignersUpdateTransaction(network, currentWallet,
                        signKeyPair, currentSigners, newMasterKeyPair),

                BiFunction { t1: WalletData, t2: Transaction -> Pair(t1, t2) }
        )

                .map { (wallet, transaction) ->
                    wallet.relationships["transaction"] =
                            DataEntity(hashMapOf(
                                    "attributes" to hashMapOf(
                                            "envelope" to transaction.getEnvelope().toBase64()
                                    ))
                            )
                    wallet
                }
    }

    private fun createSignersUpdateTransaction(network: Network,
                                               currentWallet: WalletInfo,
                                               signKeyPair: Account,
                                               currentSigners: Collection<AccountResponse.Signer>,
                                               newMasterKeyPair: Account): Single<Transaction> {
        return Single.defer {
            val operationBodies = mutableListOf<Operation.OperationBody>()

            // Add new signer.
            val currentSigner =
                    currentSigners.find {
                        it.accountId == signKeyPair.accountId
                    } ?: AccountResponse.Signer(currentWallet.accountId)

            operationBodies.add(
                    Operation.OperationBody.SetOptions(
                            UpdateSignerOp(
                                    newMasterKeyPair.accountId,
                                    currentSigner.weight,
                                    currentSigner.type,
                                    currentSigner.identity
                            )
                    )
            )

            // Remove other signers.
            currentSigners
                    .sortedBy {
                        // Remove current signer lastly, otherwise tx will be failed.
                        it.accountId == signKeyPair.accountId
                    }
                    .forEach {
                        if (it.accountId != newMasterKeyPair.accountId) {
                            // Master key removal is specific.
                            if (it.accountId == currentWallet.accountId) {
                                operationBodies.add(
                                        Operation.OperationBody.SetOptions(
                                                RemoveMasterKeyOp()
                                        )
                                )
                            } else {
                                // Other keys can be removed by setting 0 weight.
                                operationBodies.add(
                                        Operation.OperationBody.SetOptions(
                                                UpdateSignerOp(it.accountId,
                                                        0, 1, it.identity)
                                        )
                                )
                            }
                        }
                    }

            val transaction =
                    TransactionBuilder(NetworkParams(network.passphrase),
                            PublicKeyFactory.fromAccountId(currentWallet.accountId))
                            .apply {
                                operationBodies.forEach { operationBody ->
                                    addOperation(operationBody)
                                }
                            }
                            .build()

            transaction.addSignature(signKeyPair)

            Single.just(transaction)
        }.subscribeOn(Schedulers.newThread())
    }
    // endregion
}