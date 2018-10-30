package org.tokend.authenticator.base.logic.api.authresult

import org.tokend.sdk.api.base.model.DataEntity
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthResultService {
    @POST("authresult/{publicKey}")
    fun postAuthResult(@Path("publicKey") publicKey: String,
                       @Body resultData: DataEntity<AuthResultData>): Call<Void>
}