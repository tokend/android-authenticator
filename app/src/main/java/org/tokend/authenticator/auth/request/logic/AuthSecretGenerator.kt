package org.tokend.authenticator.auth.request.logic

import io.reactivex.Single
import org.tokend.crypto.ecdsa.erase
import org.tokend.kdf.ScryptKeyDerivation
import org.tokend.sdk.keyserver.models.KdfAttributes

class AuthSecretGenerator {
    fun generate(publicKey: String,
                 derivationMasterKey: ByteArray,
                 kdfAttributes: KdfAttributes): Single<ByteArray> {
        return Single.defer {
            val salt = kdfAttributes.salt
                    ?: throw IllegalArgumentException("KDF salt is required")

            val publicKeyBytes = publicKey.toByteArray()
            val passphrase = ByteArray(publicKeyBytes.size + derivationMasterKey.size)
            System.arraycopy(publicKeyBytes, 0, passphrase, 0, publicKeyBytes.size)
            System.arraycopy(derivationMasterKey, 0, passphrase, publicKeyBytes.size, derivationMasterKey.size)

            val secret = ScryptKeyDerivation(
                    kdfAttributes.n,
                    kdfAttributes.r,
                    kdfAttributes.p
            )
                    .derive(passphrase, salt, SECRET_LENGTH_BYTES)

            passphrase.erase()

            Single.just(secret)
        }
    }

    companion object {
        const val SECRET_LENGTH_BYTES = 32
    }
}