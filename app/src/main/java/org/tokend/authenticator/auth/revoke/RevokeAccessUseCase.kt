package org.tokend.authenticator.auth.revoke

import io.reactivex.Completable
import io.reactivex.Single
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.base.extensions.toSingle
import org.tokend.authenticator.base.logic.encryption.DefaultDataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.logic.transactions.TxManager
import org.tokend.authenticator.signers.model.Signer
import org.tokend.authenticator.signers.storage.AccountSignersRepository
import org.tokend.authenticator.signers.storage.AccountSignersRepositoryProvider
import org.tokend.sdk.factory.ApiFactory

class RevokeAccessUseCase(
        private val signer: Signer,
        private val account: Account,
        private val cipher: DefaultDataCipher,
        private val encryptionKeyProvider: EncryptionKeyProvider,
        private val accountSignersRepositoryProvider: AccountSignersRepositoryProvider
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
                txManager = TxManager(
                        ApiFactory(account.network.rootUrl).getApiService()
                )
        )
    }
}