package org.tokend.authenticator.auth.request.logic

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.accounts.data.model.Network
import org.tokend.authenticator.accounts.info.data.model.Signer
import org.tokend.authenticator.accounts.info.data.storage.AccountSignersRepository
import org.tokend.authenticator.accounts.info.data.storage.AccountSignersRepositoryProvider
import org.tokend.authenticator.auth.request.accountselection.logic.AuthAccountSelector
import org.tokend.authenticator.auth.request.confirmation.logic.AuthRequestConfirmationProvider
import org.tokend.authenticator.logic.api.AuthenticatorApi
import org.tokend.authenticator.logic.api.factory.ApiFactory
import org.tokend.authenticator.logic.transactions.factory.TxManagerFactory
import org.tokend.authenticator.security.encryption.cipher.DataCipher
import org.tokend.authenticator.security.encryption.logic.EncryptionKeyProvider
import org.tokend.authenticator.util.extensions.toSingle
import org.tokend.crypto.ecdsa.erase
import org.tokend.rx.extensions.toSingle
import org.tokend.sdk.api.authenticator.model.AuthRequest
import org.tokend.sdk.api.authenticator.model.AuthResult
import org.tokend.sdk.keyserver.models.KdfAttributes
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
    private lateinit var accountEncryptionKey: ByteArray
    private lateinit var authSecret: ByteArray

    class Result(
            val authSecret: ByteArray
    )

    fun perform(): Single<Result> {
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
                .flatMap { newSigner ->
                    addNewSigner(newSigner)
                }
                .flatMap {
                    deriveAuthSecret()
                }
                .doOnSuccess { authSecret ->
                    this.authSecret = authSecret
                }
                .flatMap {
                    postAuthResult(true)
                }
                .onErrorResumeNext {
                    if (it is CancellationException) {
                        postAuthResultAsync(false)
                    }
                    Single.error(it)
                }
                .map {
                    Result(authSecret)
                }
                .doOnEvent { _, _ ->
                    eraseKeys()
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

    private fun addNewSigner(signer: Signer): Single<Boolean> {
        // Back up account encryption key to avoid requesting it twice.
        val localEncryptionKeyProvider = object : EncryptionKeyProvider {
            override fun getKey(kdfAttributes: KdfAttributes): Single<ByteArray> {
                return encryptionKeyProvider.getKey(kdfAttributes)
                        .doOnSuccess {
                            accountEncryptionKey = it.copyOf()
                        }
            }
        }

        return signersRepository.add(
                signer = signer,
                cipher = cipher,
                encryptionKeyProvider = localEncryptionKeyProvider,
                txManager = txManagerFactory.getTxManager(api)
        )
                .toSingleDefault(true)
    }

    private fun deriveAuthSecret(): Single<ByteArray> {
        return AuthSecretGenerator().generate(
                authRequest.publicKey,
                accountEncryptionKey,
                account.kdfAttributes
        )
    }

    private fun postAuthResult(success: Boolean): Single<Boolean> {
        return Single.defer {
            val result = AuthResult(
                    isSuccessful = success,
                    walletId =
                    if (success)
                        account.walletId
                    else
                        ""
            )

            api.authResults.postAuthResult(authRequest.publicKey, result)
                    .toSingle()
                    .map { true }
        }
    }

    private fun postAuthResultAsync(success: Boolean) {
        doAsync {
            postAuthResult(success).blockingGet()
        }
    }

    private fun eraseKeys() {
        try {
            accountEncryptionKey.erase()
        } catch (e: UninitializedPropertyAccessException) {
            // Doesn't matter
        }
    }
}