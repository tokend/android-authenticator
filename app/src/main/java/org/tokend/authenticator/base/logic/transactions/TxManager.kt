package org.tokend.authenticator.base.logic.transactions

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.tokend.authenticator.base.extensions.toSingle
import org.tokend.sdk.api.TokenDApi
import org.tokend.sdk.api.transactions.model.SubmitTransactionResponse
import org.tokend.wallet.*
import org.tokend.wallet.xdr.Operation
import java.util.concurrent.TimeUnit

class TxManager(
        private val api: TokenDApi
) {
    fun submit(transaction: Transaction): Single<SubmitTransactionResponse> {
        return api
                .transactions
                .submit(transaction.getEnvelope().toBase64())
                .toSingle()
                // Magic delay is required because
                // API doesn't sync with Horizon immediately.
                .delay(1, TimeUnit.SECONDS)
    }

    companion object {
        fun createSignedTransaction(networkParams: NetworkParams,
                                    sourceAccountId: String,
                                    signer: Account,
                                    vararg operations: Operation.OperationBody
        ): Single<Transaction> {
            return Single.defer {
                val transaction =
                        TransactionBuilder(networkParams,
                                PublicKeyFactory.fromAccountId(sourceAccountId))
                                .apply {
                                    operations.forEach {
                                        addOperation(it)
                                    }
                                }
                                .build()

                transaction.addSignature(signer)

                Single.just(transaction)
            }.subscribeOn(Schedulers.newThread())
        }
    }
}