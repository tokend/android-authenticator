package org.tokend.authenticator.auth.request.confirmation.logic

import io.reactivex.Single
import org.tokend.sdk.api.authenticator.model.AuthRequest

interface AuthRequestConfirmationProvider {
    fun confirmAuthRequest(authRequest: AuthRequest): Single<Boolean>
}