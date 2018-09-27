package org.tokend.authenticator.auth.request

import android.net.Uri
import org.tokend.wallet.Base32Check
import org.tokend.wallet.xdr.SignerType
import java.lang.IllegalArgumentException
import java.net.URLDecoder
import java.util.*

class AuthRequest(
        val networkUrl: String,
        val appName: String,
        val scope: Int,
        val publicKey: String,
        val expirationDate: Date?
) {
    val accessTypes: List<SignerType> =
            SignerType.values().fold(mutableListOf()) { result, type ->
                if (type.value and scope == type.value) {
                    result.add(type)
                }
                result
            }

    companion object {
        private const val URI_SCHEME = "tdauth"
        private const val URI_ACTION_KEY = "action"
        private const val URI_AUTH_ACTION = "auth"
        private const val URI_NETWORK_URL_KEY = "api"
        private const val URI_APP_NAME_KEY = "app"
        private const val URI_PUBKEY_KEY = "pubkey"
        private const val URI_SCOPE_KEY = "scope"
        private const val URI_EXPIRATION_TIMESTAMP_KEY = "expires_at"
        private const val URI_ENCODING = "UTF-8"

        fun fromUri(uri: Uri): AuthRequest {
            if (uri.scheme != URI_SCHEME) {
                throw IllegalArgumentException("Auth URI must have '$URI_SCHEME' scheme")
            }

            if (uri.getQueryParameter(URI_ACTION_KEY) != URI_AUTH_ACTION) {
                throw IllegalArgumentException("Invalid action")
            }

            return AuthRequest(
                    networkUrl =
                    uri.getQueryParameter(URI_NETWORK_URL_KEY)
                            ?: throw IllegalArgumentException("No API URL specified"),
                    appName =
                    try {
                        URLDecoder.decode(uri.getQueryParameter(URI_APP_NAME_KEY), URI_ENCODING)
                    } catch (e: Exception) {
                        throw IllegalArgumentException("Invalid app name")
                    },
                    scope =
                    uri.getQueryParameter(URI_SCOPE_KEY)?.toInt()
                            ?: throw IllegalArgumentException("Invalid scope"),
                    publicKey =
                    uri.getQueryParameter(URI_PUBKEY_KEY)
                            ?.also { Base32Check.decodeAccountId(it) }
                            ?: throw IllegalArgumentException("No public key specified"),
                    expirationDate = uri.getQueryParameter(URI_EXPIRATION_TIMESTAMP_KEY)
                            ?.toLongOrNull()
                            ?.takeIf { it > 0 }
                            ?.let { Date(it) }
            )
        }
    }
}