package org.tokend.authenticator.auth.revoke

import io.reactivex.Completable
import io.reactivex.Single
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.base.extensions.toSingle
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.logic.transactions.factory.TxManagerFactory
import org.tokend.authenticator.signers.model.Signer
import org.tokend.authenticator.signers.storage.AccountSignersRepository
import org.tokend.authenticator.signers.storage.AccountSignersRepositoryProvider

class RevokeAccessUseCase(
        private val signer: Signer,
        private val account: Account,
        private val cipher: DataCipher,
        private val encryptionKeyProvider: EncryptionKeyProvider,
        private val accountSignersRepositoryProvider: AccountSignersRepositoryProvider,
        private val txManagerFactory: TxManagerFactory
) {
    fun perform(): Completable {
        return getSignersRepository()
                .flatMapCompletable { signersRepository ->
                    deleteSigner(signersRepository)
                }
    }

    private fun getSignersRepository(): Single<AccountSignersRepository> {
        return {
            accountSignersRepositoryProvider.getForAccount(account)
        }.toSingle()
    }

    private fun deleteSigner(signersRepository: AccountSignersRepository): Completable {
        return signersRepository.delete(
                signer = signer,
                cipher = cipher,
                encryptionKeyProvider = encryptionKeyProvider,
                txManager = txManagerFactory.getTxManager(account.network.rootUrl)
        )
    }
}