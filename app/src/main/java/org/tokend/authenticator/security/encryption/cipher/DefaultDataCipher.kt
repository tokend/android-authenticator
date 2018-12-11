package org.tokend.authenticator.security.encryption.cipher

import io.reactivex.Single
import org.tokend.crypto.cipher.Aes256GCM
import org.tokend.sdk.keyserver.models.KeychainData
import java.security.SecureRandom

class DefaultDataCipher : DataCipher {
    override fun encrypt(data: ByteArray, key: ByteArray): Single<KeychainData> {
        return Single.defer {
            val iv = SecureRandom().generateSeed(IV_LENGTH_BYTES)
            val cipher = Aes256GCM(iv)
            val cipherText = cipher.encrypt(data, key)

            Single.just(
                    KeychainData.fromRaw(iv, cipherText)
            )
        }
    }

    override fun decrypt(encryptedData: KeychainData, key: ByteArray): Single<ByteArray> {
        return Single.defer {
            val iv = encryptedData.iv
            val cipherText = encryptedData.cipherText
            val cipher = Aes256GCM(iv)

            Single.just(
                    cipher.decrypt(cipherText, key)
            )
        }
    }

    companion object {
        const val IV_LENGTH_BYTES = 16
    }
}