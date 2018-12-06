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
import org.tokend.crypto.ecdsa.erase
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
        return decryptAndGetMasterKey()
    }

    private fun decryptAndGetMasterKey(): Single<ByteArray> {
        return if (hasEncryptedMasterKey()) {
            var decryptionKey: ByteArray = byteArrayOf()

            getEncryptedMasterKey()
                    .flatMap { encryptedMasterKey ->
                        deriveMasterEncryptionKey(
                                userKeyProvidersHolder.getDefaultUserKeyProvider()
                        )
                                .doOnSuccess {
                                    decryptionKey = it
                                }
                                .map { encryptedMasterKey to it }
                    }
                    .flatMap { (encryptedMasterKey, decryptionKey) ->
                        cipher.decrypt(encryptedMasterKey, decryptionKey)
                    }
                    .retry { attempt, error ->
                        error is InvalidCipherTextException
                                && attempt < MAX_FAILED_USER_KEY_ATTEMPTS
                    }
                    .onErrorResumeNext { error ->
                        if (error is InvalidCipherTextException)
                            Single.error(
                                    TooManyUserKeyAttemptsException(MAX_FAILED_USER_KEY_ATTEMPTS)
                            )
                        else
                            Single.error(error)
                    }
                    .doOnEvent { _, _ ->
                        decryptionKey.erase()
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
                                .map {
                                    encryptionKey.erase()
                                    newMasterKey to it
                                }
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
                    currentMasterKey.erase()
                    cipher.encrypt(currentMasterKey, newEncryptionKey)
                            .doOnSuccess {
                                newEncryptionKey.erase()
                            }
                }
                .flatMap { encryptedMasterKey ->
                    saveEncryptedMasterKey(encryptedMasterKey)
                }
                .ignoreElement()
    }

    companion object {
        private const val KEY_LENGTH_BYTES = 32
        private const val MAX_FAILED_USER_KEY_ATTEMPTS = 3
    }
}