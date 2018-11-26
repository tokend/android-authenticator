package org.tokend.authenticator.base.logic.api

import android.os.Build
import org.tokend.authenticator.BuildConfig
import org.tokend.sdk.api.TokenDApi
import org.tokend.sdk.signing.RequestSigner
import org.tokend.sdk.tfa.TfaCallback
import org.tokend.sdk.utils.CookieJarProvider

class AuthenticatorApi(
        rootUrl: String,
        requestSigner: RequestSigner? = null,
        tfaCallback: TfaCallback? = null,
        cookieJarProvider: CookieJarProvider? = null
) : TokenDApi(rootUrl, requestSigner, tfaCallback, cookieJarProvider, USER_AGENT) {
    companion object {
        private val USER_AGENT = "TokenD Authenticator/${BuildConfig.VERSION_NAME} " +
                "(Android; ${Build.VERSION.RELEASE})"
    }
}