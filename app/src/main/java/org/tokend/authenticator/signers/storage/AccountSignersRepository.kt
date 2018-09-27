package org.tokend.authenticator.signers.storage

import io.reactivex.Completable
import io.reactivex.Single
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.base.extensions.toSingle
import org.tokend.authenticator.base.logic.db.AppDatabase
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.logic.repository.SimpleMultipleItemsRepository
import org.tokend.authenticator.base.logic.transactions.TxManager
import org.tokend.authenticator.signers.model.Signer
import org.tokend.sdk.factory.ApiFactory
import org.tokend.wallet.Transaction
import org.tokend.wallet.xdr.Operation
import org.tokend.wallet.xdr.op_extensions.RemoveSignerOp
import org.tokend.wallet.xdr.op_extensions.UpdateSignerOp

class AccountSignersRepository(
        val account: Account,
        database: AppDatabase
) : SimpleMultipleItemsRepository<Signer>() {
    override val itemsCache = AccountSignersCache(account.uid, database)

    override fun getItems(): Single<List<Signer>> {
        val api = ApiFactory(account.network.rootUrl).getApiService()

        return api
                .getAccountSigners(account.originalAccountId)
                .toSingle()
                .map { accountResponse ->
                    accountResponse.signers
                }
                .map { signersResponse ->
                    val existingSigners = itemsCache.items.associateBy { it.publicKey }

                    signersResponse.map { signerResponse ->
                        val existingSigner = existingSigners[signerResponse.accountId]

                        Signer(
                                uid = existingSigner?.uid ?: System.nanoTime(),
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
            txManager: TxManager): Completable {
        return account.getSeed(cipher, encryptionKeyProvider)
                .map { accountSeed ->
                    org.tokend.wallet.Account.fromSecretSeed(accountSeed)
                }
                .flatMap { signKeyPair ->
                    createSignerAddTransaction(signer, signKeyPair)
                }
                .flatMap { transaction ->
                    txManager.submit(transaction)
                }
                .ignoreElement()
                .doOnComplete {
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
                }
                .flatMap { signKeyPair ->
                    createSignerDeleteTransaction(signer, signKeyPair)
                }
                .flatMap { transaction ->
                    txManager.submit(transaction)
                }
                .ignoreElement()
                .doOnComplete {
                    itemsCache.delete(signer)
                    broadcast()
                }
    }

    private fun createSignerAddTransaction(signer: Signer,
                                           signKeyPair: org.tokend.wallet.Account): Single<Transaction> {
        return TxManager.createSignedTransaction(
                account.network.toNetParams(),
                account.originalAccountId,
                signKeyPair,
                Operation.OperationBody.SetOptions(
                        UpdateSignerOp(
                                accountID = signer.publicKey,
                                type = signer.scope,
                                name = signer.name,
                                identity = DEFAULT_SIGNER_IDENTITY,
                                weight = DEFAULT_SIGNER_WEIGHT
                        )
                )
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

    companion object {
        private const val DEFAULT_SIGNER_WEIGHT = 255
        private const val DEFAULT_SIGNER_IDENTITY = 0
    }
}