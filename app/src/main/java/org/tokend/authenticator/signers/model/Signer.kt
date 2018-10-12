package org.tokend.authenticator.signers.model

import com.google.gson.annotations.SerializedName
import org.tokend.authenticator.base.util.LongUid
import org.tokend.wallet.xdr.SignerType
import java.util.*

class Signer(
        @SerializedName("name")
        val name: String,
        @SerializedName("accountId")
        val accountId: Long,
        @SerializedName("publicKey")
        val publicKey: String,
        /**
         * @see SignerType
         */
        @SerializedName("scope")
        val scope: Int,
        @SerializedName("expirationDate")
        val expirationDate: Date?,
        @SerializedName("uid")
        val uid: Long = LongUid.get()
) {
    val types: List<SignerType> =
            SignerType.values().fold(mutableListOf()) { result, type ->
                if (type.value and scope == type.value) {
                    result.add(type)
                }
                result
            }

    override fun equals(other: Any?): Boolean {
        return other is Signer
                && other.uid == this.uid
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }
}