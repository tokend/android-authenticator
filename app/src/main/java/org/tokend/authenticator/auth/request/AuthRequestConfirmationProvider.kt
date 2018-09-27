package org.tokend.authenticator.auth.request

import io.reactivex.Single

interface AuthRequestConfirmationProvider {
    fun confirmAuthRequest(authRequest: AuthRequest): Single<Boolean>
}