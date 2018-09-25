package org.tokend.authenticator.accounts.logic

import io.reactivex.Single
import io.reactivex.functions.BiFunction
import okhttp3.HttpUrl
import org.spongycastle.util.encoders.Base64
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.authenticator.accounts.logic.storage.AccountsRepository
import org.tokend.authenticator.base.extensions.toSingle
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.logic.encryption.KdfAttributesGenerator
import org.tokend.authenticator.base.logic.wallet.WalletManager
import org.tokend.sdk.api.models.SystemInfo
import org.tokend.sdk.api.models.WalletData
import org.tokend.sdk.factory.ApiFactory
import org.tokend.sdk.keyserver.KeyStorage
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.wallet.utils.toByteArray

/**
 * Create account, submit it to the system,
 * encrypt it and save in the repo.
 */
class CreateAccountUseCase(
        networkUrl: String,
        private val email: String,
        private val cipher: DataCipher,
        private val encryptionKeyProvider: EncryptionKeyProvider,
        private val accountsRepository: AccountsRepository
) {
    class Result(
            val account: Account,
            val recoverySeed: CharArray
    )

    private val networkUrl = HttpUrl.parse(networkUrl).toString()

    private val api = ApiFactory(networkUrl).getApiService()
    private val keyStorageApi = KeyStorage(networkUrl)
    private val walletManager = WalletManager(keyStorageApi)

    private lateinit var kdfAttributes: KdfAttributes
    private lateinit var network: Network
    private lateinit var masterKeyPair: org.tokend.wallet.Account
    private lateinit var recoveryKeyPair: org.tokend.wallet.Account

    fun perform(): Single<Result> {
        return getKdfAttributes()
                .doOnSuccess { kdfAttributes ->
                    this.kdfAttributes = kdfAttributes
                }
                // Get network information.
                .flatMap {
                    getSystemInfo()
                }
                .map { systemInfo ->
                    Network.fromSystemInfo(networkUrl, systemInfo)
                }
                .doOnSuccess { network ->
                    this.network = network
                }
                // Generate master and recovery keys.
                .flatMap {
                    Single.zip(
                            getRandomKeyPair(),
                            getRandomKeyPair(),
                            BiFunction { x: org.tokend.wallet.Account,
                                         y: org.tokend.wallet.Account ->
                                x to y
                            }
                    )
                }
                .doOnSuccess { (masterKeyPair, recoveryKeyPair) ->
                    this.masterKeyPair = masterKeyPair
                    this.recoveryKeyPair = recoveryKeyPair
                }
                // Generate TokenD wallet.
                .flatMap {
                    getWallet()
                }
                // Post wallet to the system.
                .flatMap { wallet ->
                    postWallet(wallet)
                }
                // Create account.
                .flatMap {
                    createAccount()
                }
                // Save it finally.
                .doOnSuccess { account ->
                    accountsRepository.add(account)
                }
                .map { account ->
                    Result(
                            account = account,
                            recoverySeed = recoveryKeyPair.secretSeed!!
                    )
                }
    }

    private fun getKdfAttributes(): Single<KdfAttributes> {
        return {
            keyStorageApi.getApiLoginParams()
        }
                .toSingle()
                .map {
                    it.kdfAttributes
                }
                .map {
                    it.copy(encodedSalt = Base64.toBase64String(
                            KdfAttributesGenerator().getRandomSalt()
                    ))
                }
    }

    private fun getSystemInfo(): Single<SystemInfo> {
        return api.getSystemInfo().toSingle()
    }

    private fun getRandomKeyPair(): Single<org.tokend.wallet.Account> {
        return {
            org.tokend.wallet.Account.random()
        }.toSingle()
    }

    private fun getWallet(): Single<WalletData> {
        return {
            KeyStorage.getWalletIdHex(
                    email,
                    masterKeyPair.secretSeed!!,
                    kdfAttributes
            )
        }
                .toSingle()
                .flatMap { walletId ->
                    WalletManager.createWallet(
                            email,
                            walletId,
                            masterKeyPair.accountId,
                            recoveryKeyPair,
                            kdfAttributes
                    )
                }
    }

    private fun postWallet(wallet: WalletData): Single<Boolean> {
        return walletManager.saveWallet(wallet).toSingleDefault(true)
    }

    private fun createAccount(): Single<Account> {
        return encryptionKeyProvider.getKey(kdfAttributes)
                .flatMap { encryptionKey ->
                    cipher.encrypt(masterKeyPair.secretSeed!!.toByteArray(), encryptionKey)
                }
                .map { encryptedSeed ->
                    Account(
                            network = network,
                            email = email,
                            originalAccountId = masterKeyPair.accountId,
                            encryptedSeed = encryptedSeed,
                            kdfAttributes = kdfAttributes
                    )
                }
    }
}