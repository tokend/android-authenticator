package org.tokend.authenticator.base.logic.api.authresult

import org.tokend.sdk.api.base.ApiRequest
import org.tokend.sdk.api.base.SimpleRetrofitApiRequest
import org.tokend.sdk.api.base.model.DataEntity

class AuthResultApi(
        private val authResultService: AuthResultService
) {
    fun post(publicKey: String,
             result: AuthResultData): ApiRequest<Void> {
        return SimpleRetrofitApiRequest(
                authResultService.postAuthResult(
                        publicKey,
                        DataEntity(result)
                )
        )
    }
}