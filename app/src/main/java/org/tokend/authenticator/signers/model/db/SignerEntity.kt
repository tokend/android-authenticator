package org.tokend.authenticator.signers.model.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.tokend.authenticator.signers.model.Signer
import org.tokend.sdk.utils.ApiDateUtil

@Entity(tableName = "signer")
data class SignerEntity(
        @PrimaryKey
        @ColumnInfo(name = "uid")
        val uid: Long,
        @ColumnInfo(name = "account_id")
        val accountId: Long,
        @ColumnInfo(name = "name")
        val name: String,
        @ColumnInfo(name = "public_key")
        val publicKey: String,
        @ColumnInfo(name = "scope")
        val scope: Int,
        @ColumnInfo(name = "expiration_date")
        val expirationDateString: String?
) {
    fun toSigner(): Signer {
        return Signer(
                uid = uid,
                name = name,
                accountId = accountId,
                publicKey = publicKey,
                scope = scope,
                expirationDate = expirationDateString?.let { ApiDateUtil.tryParseDate(it) }
        )
    }

    companion object {
        fun fromSigner(signer: Signer): SignerEntity {
            return SignerEntity(
                    uid = signer.uid,
                    accountId = signer.accountId,
                    publicKey = signer.publicKey,
                    name = signer.name,
                    scope = signer.scope,
                    expirationDateString = signer.expirationDate.toString()
            )
        }
    }
}