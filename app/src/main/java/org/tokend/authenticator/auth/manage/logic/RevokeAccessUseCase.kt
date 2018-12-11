package org.tokend.authenticator.auth.manage.logic

import io.reactivex.Completable
import io.reactivex.Single
import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.util.extensions.toSingle
import org.tokend.authenticator.security.encryption.cipher.DataCipher
import org.tokend.authenticator.security.encryption.logic.EncryptionKeyProvider
import org.tokend.authenticator.logic.transactions.factory.TxManagerFactory
import org.tokend.authenticator.accounts.info.data.model.Signer
import org.tokend.authenticator.accounts.info.data.storage.AccountSignersRepository
import org.tokend.authenticator.accounts.info.data.storage.AccountSignersRepositoryProvider

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