package org.tokend.authenticator.accounts.data.model

import com.google.gson.annotations.SerializedName
import org.tokend.authenticator.util.extensions.addSlashIfNeed
import org.tokend.sdk.api.general.model.SystemInfo
import org.tokend.wallet.NetworkParams
import java.io.Serializable

class Network(
        @SerializedName("name")
        val name: String,
        @SerializedName("passphrase")
        val passphrase: String,
        @SerializedName("masterAccountId")
        val masterAccountId: String,
        @SerializedName("url")
        val rootUrl: String
) : Serializable {
    override fun equals(other: Any?): Boolean {
        return other is Network
                && other.name == this.name
                && other.passphrase == this.passphrase
                && other.masterAccountId == this.masterAccountId
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + passphrase.hashCode()
        result = 31 * result + masterAccountId.hashCode()
        return result
    }

    fun toNetParams(): NetworkParams {
        return NetworkParams(passphrase)
    }

    companion object {
        fun fromSystemInfo(rootUrl: String, systemInfo: SystemInfo): Network {
            return Network(
                    name = systemInfo.masterExchangeName,
                    passphrase = systemInfo.passphrase,
                    masterAccountId = systemInfo.masterExchangeAccountId,
                    rootUrl = rootUrl.addSlashIfNeed()
            )
        }
    }
}