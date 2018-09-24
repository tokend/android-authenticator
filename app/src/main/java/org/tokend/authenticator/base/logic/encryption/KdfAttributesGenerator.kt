package org.tokend.authenticator.base.logic.encryption

import org.spongycastle.util.encoders.Base64
import org.tokend.sdk.keyserver.models.KdfAttributes
import java.security.SecureRandom

class KdfAttributesGenerator {
    fun withRandomSalt(lengthBytes: Int = DEFAULT_SALT_LENGTH_BYTES): KdfAttributes {
        val salt = SecureRandom().generateSeed(lengthBytes)
        return KdfAttributes(
                algorithm = DEFAULT_ALG,
                bits = DEFAULT_KEY_LENGTH_BYTES * 8,
                n = DEFAULT_N,
                r = DEFAULT_R,
                p = DEFAULT_P,
                encodedSalt = Base64.toBase64String(salt)
        )
    }

    companion object {
        const val DEFAULT_ALG = "scrypt"
        const val DEFAULT_N = 4096
        const val DEFAULT_R = 8
        const val DEFAULT_P = 1
        const val DEFAULT_KEY_LENGTH_BYTES = 32
        const val DEFAULT_SALT_LENGTH_BYTES = 16
    }
}