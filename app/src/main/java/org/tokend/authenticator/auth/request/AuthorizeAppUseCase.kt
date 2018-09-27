package org.tokend.authenticator.auth.request

import android.net.Uri
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.authenticator.base.extensions.toSingle
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.logic.transactions.TxManager
import org.tokend.authenticator.signers.model.Signer
import org.tokend.authenticator.signers.storage.AccountSignersRepository
import org.tokend.authenticator.signers.storage.AccountSignersRepositoryProvider
import org.tokend.sdk.api.ApiService
import org.tokend.sdk.factory.ApiFactory
import java.util.concurrent.CancellationException

class AuthorizeAppUseCase(
        private val authUri: String,
        private val accountSelector: AuthAccountSelector,
        private val accountSignersRepositoryProvider: AccountSignersRepositoryProvider,
        private val confirmationProvider: AuthRequestConfirmationProvider
) {
    private lateinit var authRequest: AuthRequest
    private lateinit var api: ApiService
    private lateinit var network: Network
    private lateinit var account: Account
    private lateinit var cipher: DataCipher
    private lateinit var encryptionKeyProvider: EncryptionKeyProvider
    private lateinit var signersRepository: AccountSignersRepository

    fun perform(): Completable {
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
                .doOnSuccess { (account, cipher, encryptionKeyProvider) ->
                    this.account = account
                    this.cipher = cipher
                    this.encryptionKeyProvider = encryptionKeyProvider
                }
                .flatMap {
                    getConfirmation()
                }
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
                        postAuthResult(false)
                    } else {
                        Completable.error(it)
                    }
                }
    }

    private fun getAuthRequest(): Single<AuthRequest> {
        return {
            AuthRequest.fromUri(Uri.parse(authUri))
        }.toSingle()
    }

    private fun getApi(): ApiService {
        return ApiFactory(authRequest.networkUrl).getApiService()
    }

    private fun getNetwork(): Single<Network> {
        return api
                .getSystemInfo()
                .toSingle()
                .map {
                    Network.fromSystemInfo(authRequest.networkUrl, it)
                }
    }

    private fun getAccount(): Single<AuthAccountSelector.Result> {
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
                txManager = TxManager(api)
        )
    }

    private fun postAuthResult(success: Boolean): Completable {
        return Completable.defer {
            Log.i("Oleg", "Post success:$success")

            Completable.complete()
        }
    }
}