package org.tokend.authenticator.auth.request

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.authenticator.base.extensions.toSingle
import org.tokend.authenticator.base.logic.api.AuthenticatorApi
import org.tokend.authenticator.base.logic.api.factory.ApiFactory
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.logic.transactions.factory.TxManagerFactory
import org.tokend.authenticator.signers.model.Signer
import org.tokend.authenticator.signers.storage.AccountSignersRepository
import org.tokend.authenticator.signers.storage.AccountSignersRepositoryProvider
import org.tokend.rx.extensions.toCompletable
import org.tokend.rx.extensions.toSingle
import org.tokend.sdk.api.authenticator.model.AuthRequest
import org.tokend.sdk.api.authenticator.model.AuthResult
import java.util.concurrent.CancellationException

class AuthorizeAppUseCase(
        private val authUri: String,
        private val accountSelector: AuthAccountSelector,
        private val accountSignersRepositoryProvider: AccountSignersRepositoryProvider,
        private val confirmationProvider: AuthRequestConfirmationProvider,
        private val cipher: DataCipher,
        private val encryptionKeyProvider: EncryptionKeyProvider,
        private val apiFactory: ApiFactory,
        private val txManagerFactory: TxManagerFactory
) {
    private lateinit var authRequest: AuthRequest
    private lateinit var api: AuthenticatorApi
    private lateinit var network: Network
    private lateinit var account: Account
    private lateinit var signersRepository: AccountSignersRepository

    fun perform(): Completable {
        val scheduler = Schedulers.newThread()

        return getAuthRequest()
                .doOnSuccess { authRequest ->
                    this.authRequest = authRequest
                }
                .map {
                    getApi()
                }
                .doOnSuccess { api ->
                    this.api = api
                }
                .flatMap {
                    getNetwork()
                }
                .doOnSuccess { network ->
                    this.network = network
                }
                .flatMap {
                    getAccount()
                }
                .observeOn(scheduler)
                .doOnSuccess { account ->
                    this.account = account
                }
                .flatMap {
                    getConfirmation()
                }
                .observeOn(scheduler)
                .map {
                    getSignersRepository()
                }
                .doOnSuccess { signersRepository ->
                    this.signersRepository = signersRepository
                }
                .map {
                    getNewSigner()
                }
                .flatMapCompletable { newSigner ->
                    addNewSigner(newSigner)
                }
                .andThen(postAuthResult(true))
                .onErrorResumeNext {
                    if (it is CancellationException) {
                        postAuthResultAsync(false)
                    }
                    Completable.error(it)
                }
    }

    private fun getAuthRequest(): Single<AuthRequest> {
        return {
            AuthRequest.parse(authUri)
        }.toSingle()
    }

    private fun getApi(): AuthenticatorApi {
        return apiFactory.getApi(authRequest.networkUrl)
    }

    private fun getNetwork(): Single<Network> {
        return api
                .general
                .getSystemInfo()
                .toSingle()
                .map {
                    Network.fromSystemInfo(authRequest.networkUrl, it)
                }
    }

    private fun getAccount(): Single<Account> {
        return accountSelector.selectAccountForAuth(network, authRequest)
                .switchIfEmpty(
                        Single.error(CancellationException("Auth canceled on account request"))
                )
    }

    private fun getSignersRepository(): AccountSignersRepository {
        return accountSignersRepositoryProvider.getForAccount(account)
    }

    private fun getConfirmation(): Single<Boolean> {
        return confirmationProvider.confirmAuthRequest(authRequest)
                .map { isConfirmed ->
                    if (!isConfirmed) {
                        throw CancellationException("Auth canceled on confirmation")
                    }

                    isConfirmed
                }
    }

    private fun getNewSigner(): Signer {
        return Signer(
                name = authRequest.appName,
                publicKey = authRequest.publicKey,
                scope = authRequest.scope,
                expirationDate = authRequest.expirationDate,
                accountId = account.uid
        )
    }

    private fun addNewSigner(signer: Signer): Completable {
        return signersRepository.add(
                signer = signer,
                cipher = cipher,
                encryptionKeyProvider = encryptionKeyProvider,
                txManager = txManagerFactory.getTxManager(api)
        )
    }

    private fun postAuthResult(success: Boolean): Completable {
        return Completable.defer {
            val result = AuthResult(
                    isSuccessful = success,
                    walletId =
                    if (success)
                        account.walletId
                    else
                        ""
            )

            api.authResults.postAuthResult(authRequest.publicKey, result)
                    .toCompletable()
        }
    }

    private fun postAuthResultAsync(success: Boolean) {
        doAsync {
            postAuthResult(success).blockingAwait()
        }
    }
}