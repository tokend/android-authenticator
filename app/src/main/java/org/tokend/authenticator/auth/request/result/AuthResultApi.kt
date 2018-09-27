package org.tokend.authenticator.auth.request.result

import org.tokend.sdk.api.requests.DataEntity
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthResultApi {
    @POST("authresult/{publicKey}")
    fun postAuthResult(@Path("publicKey") publicKey: String,
                       @Body resultData: DataEntity<AuthResultData>): Call<Void>

}