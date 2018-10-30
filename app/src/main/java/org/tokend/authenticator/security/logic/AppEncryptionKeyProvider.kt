package org.tokend.authenticator.security.logic

import android.content.SharedPreferences
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toCompletable
import io.reactivex.schedulers.Schedulers
import org.tokend.authenticator.base.extensions.toSingle
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.security.logic.persistence.EncryptedMasterKeyStorage
import org.tokend.authenticator.security.logic.persistence.MasterKeyKdfAttributesStorage
import org.tokend.crypto.cipher.InvalidCipherTextException
import org.tokend.kdf.ScryptKeyDerivation
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.sdk.keyserver.models.KeychainData
import org.tokend.wallet.utils.toByteArray
import java.util.concurrent.CancellationException
import javax.crypto.KeyGenerator

/**
 * Derives encryption keys from the master seed
 * which is encrypted with user key (PIN-code, password, etc.).
 */
class AppEncryptionKeyProvider(
        preferences: SharedPreferences,
        private val cipher: DataCipher,
        private val userKeyProvidersHolder: AppUserKeyProvidersHolder
) : EncryptionKeyProvider {
    private val masterKeyKdfAttributesStorage = MasterKeyKdfAttributesStorage(preferences)
    private val encryptedMasterKeyStorage = EncryptedMasterKeyStorage(preferences)

    private var masterKey: ByteArray? = null

    override fun getKey(kdfAttributes: KdfAttributes): Single<ByteArray> {
        return getMasterKey()
                .map { masterKey ->
                    ScryptKeyDerivation(
                            kdfAttributes.n,
                            kdfAttributes.r,
                            kdfAttributes.p
                    ).derive(masterKey, kdfAttributes.salt!!, KEY_LENGTH_BYTES)
                }
    }

    private fun getMasterKey(): Single<ByteArray> {
        if (masterKey != null) {
            return Single.just(masterKey)
        }

        return decryptAndGetMasterKey()
                .doOnSuccess {
                    masterKey = it
                }
    }

    private fun decryptAndGetMasterKey(): Single<ByteArray> {
        return if (hasEncryptedMasterKey()) {
            getEncryptedMasterKey()
                    .flatMap { encryptedMasterKey ->
                        deriveMasterEncryptionKey(
                                userKeyProvidersHolder.getDefaultUserKeyProvider()
                        )
                                .map { encryptedMasterKey to it }
                    }
                    .flatMap { (encryptedMasterKey, decryptionKey) ->
                        cipher.decrypt(encryptedMasterKey, decryptionKey)
                    }
                    .retry { attempt, error ->
                        error is InvalidCipherTextException && attempt < 3
                    }
        } else {
            generateMasterKey()
                    .flatMap { newMasterKey ->
                        deriveMasterEncryptionKey(
                                userKeyProvidersHolder.getInitialUserKeyProvider()
                        )
                                .map { newMasterKey to it }
                    }
                    .flatMap { (newMasterKey, encryptionKey) ->
                        cipher.encrypt(newMasterKey, encryptionKey)
                                .map { newMasterKey to it }
                    }
                    .flatMap { (newMasterKey, encryptedMasterKey) ->
                        saveEncryptedMasterKey(encryptedMasterKey)
                                .map { newMasterKey }
                    }
        }
    }

    private fun hasEncryptedMasterKey(): Boolean {
        return encryptedMasterKeyStorage.hasData()
    }

    private fun getEncryptedMasterKey(): Single<KeychainData> {
        return {
            encryptedMasterKeyStorage.load()!!
        }.toSingle()
    }

    private fun saveEncryptedMasterKey(encryptedMasterKey: KeychainData): Single<Boolean> {
        return {
            encryptedMasterKeyStorage.save(encryptedMasterKey)
        }.toCompletable().toSingleDefault(true)
    }

    private fun generateMasterKey(): Single<ByteArray> {
        return {
            val generator = KeyGenerator.getInstance("AES")
            generator.init(KEY_LENGTH_BYTES * 8)
            generator.generateKey().encoded
        }.toSingle()
    }

    private fun deriveMasterEncryptionKey(userKeyProvider: UserKeyProvider)
            : Single<ByteArray> {
        return userKeyProvider
                .getUserKey()
                .switchIfEmpty(Single.error(CancellationException("Cancelled by user")))
                .observeOn(Schedulers.newThread())
                .map { it.toByteArray() }
                .map { userKey ->
                    val kdfAttributes = masterKeyKdfAttributesStorage.load()

                    ScryptKeyDerivation(
                            kdfAttributes.n,
                            kdfAttributes.r,
                            kdfAttributes.p
                    ).derive(userKey, kdfAttributes.salt!!, KEY_LENGTH_BYTES)
                }
    }

    fun resetUserKey(): Completable {
        return decryptAndGetMasterKey()
                .flatMap { currentMasterKey ->
                    deriveMasterEncryptionKey(
                            userKeyProvidersHolder.getInitialUserKeyProvider()
                    )
                            .map { currentMasterKey to it }
                }
                .flatMap { (currentMasterKey, newEncryptionKey) ->
                    cipher.encrypt(currentMasterKey, newEncryptionKey)
                            .map { currentMasterKey to it }
                }
                .flatMap { (currentMasterKey, encryptedMasterKey) ->
                    this.masterKey = currentMasterKey
                    saveEncryptedMasterKey(encryptedMasterKey)
                }
                .ignoreElement()
    }

    companion object {
        private const val KEY_LENGTH_BYTES = 32
    }
}