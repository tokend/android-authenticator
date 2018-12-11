package org.tokend.authenticator.auth.request.accountselection.logic

import io.reactivex.Maybe
import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.accounts.data.model.Network
import org.tokend.sdk.api.authenticator.model.AuthRequest

interface AuthAccountSelector {
    fun selectAccountForAuth(network: Network,
                             authRequest: AuthRequest): Maybe<Account>
}