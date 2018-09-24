package org.tokend.authenticator.accounts.logic.model

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.sdk.api.models.KeychainData
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.wallet.utils.toCharArray

class Account(
        @SerializedName("network")
        val network: Network,
        @SerializedName("email")
        val email: String,
        @SerializedName("originalAccountId")
        val originalAccountId: String,
        @SerializedName("encryptedSeed")
        val encryptedSeed: KeychainData,
        @SerializedName("kdfAttributes")
        val kdfAttributes: KdfAttributes
) {
    fun getSeed(cipher: DataCipher,
                keyProvider: EncryptionKeyProvider): Single<CharArray> {
        return keyProvider.getKey(kdfAttributes)
                .flatMap { key ->
                    cipher.decrypt(encryptedSeed, key)
                }
                .map { seedBytes ->
                    seedBytes.toCharArray()
                }
    }

    override fun equals(other: Any?): Boolean {
        return other is Account
                && other.network == this.network
                && other.email == this.email
                && other.originalAccountId == this.originalAccountId
                && other.encryptedSeed.cipherText.contentEquals(this.encryptedSeed.cipherText)
                && other.kdfAttributes.encodedSalt == this.kdfAttributes.encodedSalt
    }

    override fun hashCode(): Int {
        var result = network.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + originalAccountId.hashCode()
        return result
    }
}