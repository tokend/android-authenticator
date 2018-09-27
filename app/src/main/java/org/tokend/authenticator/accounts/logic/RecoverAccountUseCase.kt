package org.tokend.authenticator.accounts.logic

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.HttpUrl
import org.spongycastle.util.encoders.Base64
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.authenticator.accounts.logic.storage.AccountsRepository
import org.tokend.authenticator.base.extensions.toSingle
import org.tokend.authenticator.base.logic.DefaultRequestSigner
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.logic.encryption.KdfAttributesGenerator
import org.tokend.authenticator.base.logic.wallet.WalletManager
import org.tokend.authenticator.base.logic.wallet.WalletUpdateManager
import org.tokend.sdk.api.ApiService
import org.tokend.sdk.api.models.SystemInfo
import org.tokend.sdk.api.models.WalletData
import org.tokend.sdk.api.requests.RequestSigner
import org.tokend.sdk.factory.ApiFactory
import org.tokend.sdk.keyserver.KeyStorage
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.sdk.keyserver.models.WalletInfo
import org.tokend.wallet.utils.toByteArray

class RecoverAccountUseCase(
        networkUrl: String,
        private val email: String,
        private val recoverySeed: CharArray,
        private val cipher: DataCipher,
        private val encryptionKeyProvider: EncryptionKeyProvider,
        private val accountsRepository: AccountsRepository
) {
    private val networkUrl = HttpUrl.parse(networkUrl).toString()

    private lateinit var signedApi: ApiService
    private lateinit var network: Network
    private lateinit var recoveryKeyPair: org.tokend.wallet.Account
    private lateinit var requestSigner: RequestSigner
    private lateinit var recoveryWallet: WalletInfo
    private lateinit var newMasterKeyPair: org.tokend.wallet.Account
    private lateinit var walletManager: WalletManager
    private lateinit var walletUpdateManager: WalletUpdateManager
    private lateinit var newKdfAttributes: KdfAttributes
    private lateinit var newWalletId: String

    fun perform(): Completable {
        return getRecoveryKeyPair()
                .doOnSuccess { recoveryKeyPair ->
                    this.recoveryKeyPair = recoveryKeyPair
                    this.requestSigner = DefaultRequestSigner(recoveryKeyPair)
                    this.signedApi = ApiFactory(networkUrl).getApiService(this.requestSigner)
                }
                .flatMap {
                    getSystemInfo()
                }
                .map { systemInfo ->
                    Network.fromSystemInfo(networkUrl, systemInfo)
                }
                .doOnSuccess { network ->
                    this.network = network
                }

                .map {
                    WalletManager(
                            KeyStorage(
                                    keyServerUrl = networkUrl,
                                    requestSigner = requestSigner
                            )
                    )
                }
                .doOnSuccess { walletManager ->
                    this.walletManager = walletManager
                }
                .flatMap { walletManager ->
                    walletManager.getWalletInfo(email, recoverySeed, true)
                }
                .doOnSuccess { recoveryWallet ->
                    this.recoveryWallet = recoveryWallet
                }
                .map {
                    WalletUpdateManager()
                }
                .doOnSuccess { walletUpdateManager ->
                    this.walletUpdateManager = walletUpdateManager
                }
                .flatMap {
                    getRandomKeyPair()
                }
                .doOnSuccess { newMasterKeyPair ->
                    this.newMasterKeyPair = newMasterKeyPair
                }
                .map {
                    KdfAttributesGenerator().getRandomSalt()
                }
                .doOnSuccess { newKdfSalt ->
                    this.newKdfAttributes = recoveryWallet.loginParams.kdfAttributes
                            .copy(encodedSalt = Base64.toBase64String(newKdfSalt))
                }
                .flatMap {
                    updateWallet()
                }
                .doOnSuccess { updatedWallet ->
                    this.newWalletId = updatedWallet.id!!
                }
                .flatMapCompletable {
                    updateAccount()
                }
    }

    private fun getSystemInfo(): Single<SystemInfo> {
        return signedApi.getSystemInfo().toSingle()
    }

    private fun getRandomKeyPair(): Single<org.tokend.wallet.Account> {
        return {
            org.tokend.wallet.Account.random()
        }.toSingle()
    }

    private fun getRecoveryKeyPair(): Single<org.tokend.wallet.Account> {
        return {
            org.tokend.wallet.Account.fromSecretSeed(recoverySeed)
        }.toSingle()
    }

    private fun updateWallet(): Single<WalletData> {
        return walletUpdateManager.updateWalletWithNewKeyPair(
                walletInfo = recoveryWallet,
                newMasterKeyPair = newMasterKeyPair,
                newKdfAttributes = newKdfAttributes,
                network = network,
                signKeyPair = recoveryKeyPair,
                signedApi = signedApi,
                walletManager = walletManager
        )
    }

    private fun updateAccount(): Completable {
        val accountToUpdate = accountsRepository.itemsList.find {
            it.email == email && it.network == network
        }

        return encryptionKeyProvider.getKey(newKdfAttributes)
                .flatMap { encryptionKey ->
                    cipher.encrypt(newMasterKeyPair.secretSeed!!.toByteArray(), encryptionKey)
                }
                .doOnSuccess { encryptedSeed ->
                    if (accountToUpdate != null) {
                        accountToUpdate.encryptedSeed = encryptedSeed
                        accountToUpdate.kdfAttributes = newKdfAttributes
                        accountToUpdate.walletId = newWalletId
                        accountsRepository.update(accountToUpdate)
                    } else {
                        val newAccount = Account(
                                network = network,
                                email = email,
                                originalAccountId = recoveryWallet.accountId,
                                walletId = newWalletId,
                                encryptedSeed = encryptedSeed,
                                kdfAttributes = newKdfAttributes
                        )
                        accountsRepository.add(newAccount)
                    }
                }
                .ignoreElement()
    }
}