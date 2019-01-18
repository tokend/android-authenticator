package org.tokend.authenticator.accounts.info.data.storage

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.accounts.info.data.model.Signer
import org.tokend.authenticator.base.repository.RepositoryCache
import org.tokend.authenticator.base.repository.SimpleMultipleItemsRepository
import org.tokend.authenticator.logic.api.factory.ApiFactory
import org.tokend.authenticator.logic.transactions.TxManager
import org.tokend.authenticator.security.encryption.cipher.DataCipher
import org.tokend.authenticator.security.encryption.logic.EncryptionKeyProvider
import org.tokend.authenticator.util.LongUid
import org.tokend.crypto.ecdsa.erase
import org.tokend.rx.extensions.toCompletable
import org.tokend.rx.extensions.toSingle
import org.tokend.wallet.Transaction
import org.tokend.wallet.xdr.Operation
import org.tokend.wallet.xdr.op_extensions.RemoveSignerOp
import org.tokend.wallet.xdr.op_extensions.UpdateSignerOp
import retrofit2.HttpException
import java.net.HttpURLConnection

class AccountSignersRepository(
        val account: Account,
        private val apiFactory: ApiFactory,
        override val itemsCache: RepositoryCache<Signer>
) : SimpleMultipleItemsRepository<Signer>() {
    private val api = apiFactory.getApi(account.network.rootUrl)

    override fun getItems(): Single<List<Signer>> {
        return api
                .accounts
                .getSigners(account.originalAccountId)
                .toSingle()
                .onErrorReturn { error ->
                    // No account on server means there are no signers ¯\_(ツ)_/¯
                    if (error is HttpException &&
                            error.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                        emptyList()
                    } else {
                        throw error
                    }
                }
                .map { signersResponse ->
                    val existingSigners = itemsCache.items.associateBy { it.publicKey }

                    signersResponse.map { signerResponse ->
                        val existingSigner = existingSigners[signerResponse.accountId]

                        Signer(
                                uid = existingSigner?.uid ?: LongUid.get(),
                                accountId = account.uid,
                                name = signerResponse.name,
                                publicKey = signerResponse.accountId,
                                scope = signerResponse.type,
                                expirationDate = null
                        )
                    }
                }
    }

    fun add(signer: Signer,
            cipher: DataCipher,
            encryptionKeyProvider: EncryptionKeyProvider,
            txManager: TxManager,
            isUnique: Boolean = false): Completable {

        var signersToRemove = emptyList<Signer>()
        return account.getSeed(cipher, encryptionKeyProvider)
                .map { accountSeed ->
                    org.tokend.wallet.Account.fromSecretSeed(accountSeed)
                            .also {
                                accountSeed.erase()
                            }
                }
                .flatMap { signKeyPair ->
                    if (isUnique) {
                        updateIfNotFreshDeferred()
                                .doOnComplete {
                                    signersToRemove = itemsCache.items.filter {
                                        it.name == signer.name
                                    }
                                }
                    } else {
                        Completable.complete()
                    }.toSingleDefault(signKeyPair)
                }
                .flatMap { signKeyPair ->
                    Single.zip(
                            createSignerAddTransaction(signer, signersToRemove, signKeyPair),
                            createAccountIfNeeded(signKeyPair),
                            BiFunction { transaction: Transaction, _: Boolean ->
                                transaction to signKeyPair
                            }
                    )
                }
                .flatMap { (transaction, signKeyPair) ->
                    txManager.submit(transaction)
                            .doOnEvent { _, _ ->
                                signKeyPair.destroy()
                            }
                }
                .ignoreElement()
                .doOnComplete {
                    itemsCache.delete(*signersToRemove.toTypedArray())
                    itemsCache.add(signer)
                    broadcast()
                }
    }

    fun delete(signer: Signer,
               cipher: DataCipher,
               encryptionKeyProvider: EncryptionKeyProvider,
               txManager: TxManager): Completable {
        return account.getSeed(cipher, encryptionKeyProvider)
                .map { accountSeed ->
                    org.tokend.wallet.Account.fromSecretSeed(accountSeed)
                            .also {
                                accountSeed.erase()
                            }
                }
                .flatMap { signKeyPair ->
                    createSignerDeleteTransaction(signer, signKeyPair)
                            .map {
                                it to signKeyPair
                            }
                }
                .flatMap { (transaction, signKeyPair) ->
                    txManager.submit(transaction)
                            .doOnEvent { _, _ ->
                                signKeyPair.destroy()
                            }
                }
                .ignoreElement()
                .doOnComplete {
                    itemsCache.delete(signer)
                    broadcast()
                }
    }

    private fun createSignerAddTransaction(signer: Signer,
                                           sameSigners: List<Signer>,
                                           signKeyPair: org.tokend.wallet.Account): Single<Transaction> {

        val operations = mutableListOf<Operation.OperationBody>()
        sameSigners.forEach {
            operations.add(Operation.OperationBody.SetOptions(
                    RemoveSignerOp(
                            accountID = it.publicKey,
                            identity = DEFAULT_SIGNER_IDENTITY
                    )
            ))
        }
        operations.add(Operation.OperationBody.SetOptions(
                UpdateSignerOp(
                        accountID = signer.publicKey,
                        type = signer.scope,
                        name = signer.name,
                        identity = DEFAULT_SIGNER_IDENTITY,
                        weight = DEFAULT_SIGNER_WEIGHT
                )
        ))

        return TxManager.createSignedTransaction(
                account.network.toNetParams(),
                account.originalAccountId,
                signKeyPair,
                *operations.toTypedArray()
        )
    }

    private fun createSignerDeleteTransaction(signer: Signer,
                                              signKeyPair: org.tokend.wallet.Account): Single<Transaction> {
        return TxManager.createSignedTransaction(
                account.network.toNetParams(),
                account.originalAccountId,
                signKeyPair,
                Operation.OperationBody.SetOptions(
                        RemoveSignerOp(
                                accountID = signer.publicKey,
                                identity = DEFAULT_SIGNER_IDENTITY
                        )
                )
        )
    }

    private fun createAccountIfNeeded(keyPair: org.tokend.wallet.Account): Single<Boolean> {
        return apiFactory
                .getSignedApi(account.network.rootUrl, keyPair)
                .users
                .create(account.originalAccountId)
                .toCompletable()
                .toSingleDefault(true)
                .onErrorReturnItem(false)
    }

    companion object {
        private const val DEFAULT_SIGNER_WEIGHT = 255
        private const val DEFAULT_SIGNER_IDENTITY = 0
    }
}