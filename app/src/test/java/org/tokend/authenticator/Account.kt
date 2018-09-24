package org.tokend.authenticator

import io.reactivex.Single
import org.junit.Assert
import org.junit.Test
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.authenticator.base.logic.encryption.DefaultDataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.logic.encryption.KdfAttributesGenerator
import org.tokend.kdf.ScryptKeyDerivation
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.wallet.utils.toByteArray

class Account {
    @Test
    fun encryptDecryptSeed() {
        val seed = org.tokend.wallet.Account.random().secretSeed?.toByteArray() ?: return

        val kdf = KdfAttributesGenerator().withRandomSalt()
        val password = "12345".toByteArray()
        val keyProvider = object : EncryptionKeyProvider {
            override fun getKey(kdfAttributes: KdfAttributes): Single<ByteArray> {
                return Single.defer {
                    Single.just(ScryptKeyDerivation(kdf.n, kdf.r, kdf.p)
                            .derive(password, kdf.salt, 32)
                    )
                }
            }
        }

        val cipher = DefaultDataCipher()
        val encryptedSeed = cipher.encrypt(seed, keyProvider.getKey(kdf).blockingGet()).blockingGet()

        val account = Account(
                network = Network("", "", "", ""),
                email = "",
                originalAccountId = "",
                kdfAttributes = kdf,
                encryptedSeed = encryptedSeed
        )

        Assert.assertArrayEquals(seed,
                account.getSeed(cipher, keyProvider).blockingGet().toByteArray())
    }
}