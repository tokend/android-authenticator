package org.tokend.authenticator.accounts.logic.model.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.sdk.api.models.KeychainData
import org.tokend.sdk.factory.GsonFactory
import org.tokend.sdk.keyserver.models.KdfAttributes

@Entity(tableName = "account")
data class AccountEntity(
        @PrimaryKey
        @ColumnInfo(name = "uid")
        val uid: Long,
        @ColumnInfo(name = "email")
        val email: String,
        @ColumnInfo(name = "original_account_id")
        val originalAccountId: String,
        @ColumnInfo(name = "wallet_id")
        val walletId: String,
        @ColumnInfo(name = "network_json")
        val networkJson: String,
        @ColumnInfo(name = "encrypted_seed_json")
        val encryptedSeedJson: String,
        @ColumnInfo(name = "kdf_json")
        val kdfJson: String
) {
    fun toAccount(): Account {
        val gson = GsonFactory().getBaseGson()
        return Account(
                uid = uid,
                network = gson.fromJson(networkJson, Network::class.java),
                email = email,
                originalAccountId = originalAccountId,
                walletId = walletId,
                encryptedSeed = gson.fromJson(encryptedSeedJson, KeychainData::class.java),
                kdfAttributes = gson.fromJson(kdfJson, KdfAttributes::class.java)
        )
    }

    companion object {
        fun fromAccount(account: Account): AccountEntity {
            val gson = GsonFactory().getBaseGson()
            return AccountEntity(
                    uid = account.uid,
                    email = account.email,
                    originalAccountId = account.originalAccountId,
                    walletId = account.walletId,
                    networkJson = gson.toJson(account.network),
                    encryptedSeedJson = gson.toJson(account.encryptedSeed),
                    kdfJson = gson.toJson(account.kdfAttributes)
            )
        }
    }
}