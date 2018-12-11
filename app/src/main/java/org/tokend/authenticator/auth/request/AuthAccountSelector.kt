package org.tokend.authenticator.auth.request

import io.reactivex.Maybe
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.sdk.api.authenticator.model.AuthRequest

interface AuthAccountSelector {
    fun selectAccountForAuth(network: Network,
                             authRequest: AuthRequest): Maybe<Account>
}