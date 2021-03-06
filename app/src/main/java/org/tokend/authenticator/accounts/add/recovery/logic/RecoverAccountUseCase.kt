package org.tokend.authenticator.accounts.add.recovery.logic

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.HttpUrl
import org.spongycastle.util.encoders.Base64
import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.accounts.data.model.Network
import org.tokend.authenticator.accounts.data.storage.AccountsRepository
import org.tokend.authenticator.accounts.info.data.storage.AccountSignersRepositoryProvider
import org.tokend.authenticator.logic.api.factory.ApiFactory
import org.tokend.authenticator.logic.wallet.WalletUpdateManager
import org.tokend.authenticator.security.encryption.cipher.DataCipher
import org.tokend.authenticator.security.encryption.logic.EncryptionKeyProvider
import org.tokend.authenticator.security.encryption.logic.KdfAttributesGenerator
import org.tokend.authenticator.util.extensions.toSingle
import org.tokend.crypto.ecdsa.erase
import org.tokend.rx.extensions.toSingle
import org.tokend.sdk.api.TokenDApi
import org.tokend.sdk.api.general.model.SystemInfo
import org.tokend.sdk.keyserver.KeyServer
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.sdk.keyserver.models.WalletData
import org.tokend.sdk.keyserver.models.WalletInfo
import org.tokend.wallet.utils.toByteArray

class RecoverAccountUseCase(
        networkUrl: String,
        private val email: String,
        private val recoverySeed: CharArray,
        private val cipher: DataCipher,
        private val encryptionKeyProvider: EncryptionKeyProvider,
        private val accountsRepository: AccountsRepository,
        private val accountSignersRepositoryProvider: AccountSignersRepositoryProvider,
        private val apiFactory: ApiFactory
) {
    private val networkUrl = HttpUrl.parse(networkUrl).toString()

    private lateinit var signedApi: TokenDApi
    private lateinit var network: Network
    private lateinit var recoveryKeyPair: org.tokend.wallet.Account
    private lateinit var recoveryWallet: WalletInfo
    private lateinit var newMasterKeyPair: org.tokend.wallet.Account
    private lateinit var keyStorage: KeyServer
    private lateinit var walletUpdateManager: WalletUpdateManager
    private lateinit var newKdfAttributes: KdfAttributes
    private lateinit var newWalletId: String

    fun perform(): Completable {
        val scheduler = Schedulers.newThread()

        return getRecoveryKeyPair()
                .doOnSuccess { recoveryKeyPair ->
                    this.recoveryKeyPair = recoveryKeyPair
                    this.signedApi = apiFactory.getSignedApi(networkUrl, recoveryKeyPair)
                    this.keyStorage = apiFactory.getSignedKeyServer(networkUrl, recoveryKeyPair)
                }
                .flatMap {
                    getSystemInfo()
                }
                .observeOn(scheduler)
                .map { systemInfo ->
                    Network.fromSystemInfo(networkUrl, systemInfo)
                }
                .doOnSuccess { network ->
                    this.network = network
                }
                .flatMap {
                    keyStorage
                            .getWalletInfo(email, recoverySeed, true)
                            .toSingle()
                }
                .observeOn(scheduler)
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
                .observeOn(scheduler)
                .doOnSuccess { newMasterKeyPair ->
                    this.newMasterKeyPair = newMasterKeyPair
                }
                .map {
                    KdfAttributesGenerator().getRandomSalt()
                }
                .observeOn(scheduler)
                .doOnSuccess { newKdfSalt ->
                    this.newKdfAttributes = recoveryWallet.loginParams.kdfAttributes
                            .copy(encodedSalt = Base64.toBase64String(newKdfSalt))
                }
                .flatMap {
                    updateWallet()
                }
                .observeOn(scheduler)
                .doOnSuccess { updatedWallet ->
                    this.newWalletId = updatedWallet.id!!
                }
                .flatMapCompletable {
                    updateAccount()
                }
                .doOnTerminate {
                    destroyKeys()
                }
    }

    private fun getSystemInfo(): Single<SystemInfo> {
        return signedApi.general.getSystemInfo().toSingle()
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
                keyStorage = keyStorage
        )
    }

    private fun updateAccount(): Completable {
        val accountToUpdate = accountsRepository.itemsList.find {
            it.email == email && it.network == network
        }

        return encryptionKeyProvider.getKey(newKdfAttributes)
                .flatMap { encryptionKey ->
                    cipher.encrypt(newMasterKeyPair.secretSeed!!.toByteArray(), encryptionKey)
                            .doOnSuccess {
                                encryptionKey.erase()
                            }
                }
                .doOnSuccess { encryptedSeed ->
                    if (accountToUpdate != null) {
                        accountToUpdate.publicKey = newMasterKeyPair.accountId
                        accountToUpdate.encryptedSeed = encryptedSeed
                        accountToUpdate.kdfAttributes = newKdfAttributes
                        accountToUpdate.walletId = newWalletId
                        accountsRepository.update(accountToUpdate)
                        accountSignersRepositoryProvider.getForAccount(accountToUpdate)
                                .update()
                    } else {
                        val newAccount = Account(
                                network = network,
                                email = email,
                                originalAccountId = recoveryWallet.accountId,
                                walletId = newWalletId,
                                publicKey = newMasterKeyPair.accountId,
                                encryptedSeed = encryptedSeed,
                                kdfAttributes = newKdfAttributes
                        )
                        accountsRepository.add(newAccount)
                    }
                }
                .ignoreElement()
    }

    private fun destroyKeys() {
        try {
            recoveryKeyPair.destroy()
            recoveryWallet.secretSeed.erase()
            newMasterKeyPair.destroy()
        } catch (e: UninitializedPropertyAccessException) {
            // Doesn't matter
        }
    }
}