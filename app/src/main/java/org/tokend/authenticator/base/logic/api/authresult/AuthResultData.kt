package org.tokend.authenticator.base.logic.api.authresult

import com.google.gson.annotations.SerializedName

class AuthResultData(
        @SerializedName("success")
        val success: Boolean,
        @SerializedName("wallet_id")
        val walletId: String?
)