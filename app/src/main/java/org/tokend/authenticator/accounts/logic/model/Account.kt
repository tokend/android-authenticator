package org.tokend.authenticator.accounts.logic.model

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.util.LongUid
import org.tokend.crypto.ecdsa.erase
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.sdk.keyserver.models.KeychainData
import org.tokend.wallet.utils.toCharArray

class Account(
        @SerializedName("network")
        val network: Network,
        @SerializedName("email")
        val email: String,
        @SerializedName("original_account_id")
        val originalAccountId: String,
        @SerializedName("wallet_id")
        var walletId: String,
        @SerializedName("public_key")
        var publicKey: String,
        @SerializedName("encrypted_seed")
        var encryptedSeed: KeychainData,
        @SerializedName("kdf_attributes")
        var kdfAttributes: KdfAttributes,
        @SerializedName("uid")
        val uid: Long = LongUid.get(),
        @SerializedName("is_broken")
        var isBroken: Boolean = false
) {
    fun getSeed(cipher: DataCipher,
                keyProvider: EncryptionKeyProvider): Single<CharArray> {
        return keyProvider.getKey(kdfAttributes)
                .flatMap { key ->
                    cipher.decrypt(encryptedSeed, key)
                            .doOnEvent { _, _ ->
                                key.erase()
                            }
                }
                .map { seedBytes ->
                    seedBytes.toCharArray()
                }
    }

    override fun equals(other: Any?): Boolean {
        return other is Account
                && other.uid == this.uid
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }
}