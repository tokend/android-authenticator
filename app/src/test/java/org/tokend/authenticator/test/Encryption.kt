package org.tokend.authenticator.test

import org.junit.Assert
import org.junit.Test
import org.tokend.authenticator.security.encryption.cipher.DefaultDataCipher
import org.tokend.authenticator.security.encryption.logic.KdfAttributesGenerator
import java.security.SecureRandom

class Encryption {
    @Test
    fun kdfGeneratorRandomSalt() {
        val generator = KdfAttributesGenerator()
        val x = generator.withRandomSalt()
        val y = generator.withRandomSalt()

        Assert.assertNotEquals(x.encodedSalt, y.encodedSalt)
    }

    @Test
    fun defaultDataCipher() {
        val data = "TokenD is awesome".toByteArray()
        val key = SecureRandom().generateSeed(32)
        val cipher = DefaultDataCipher()
        val encrypted = cipher.encrypt(data, key).blockingGet()
        val decrypted = cipher.decrypt(encrypted, key).blockingGet()

        Assert.assertArrayEquals(data, decrypted)
    }
}