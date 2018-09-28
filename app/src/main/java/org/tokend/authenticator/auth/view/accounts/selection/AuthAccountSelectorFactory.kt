package org.tokend.authenticator.auth.view.accounts.selection

import android.app.Activity
import android.app.Fragment
import org.tokend.authenticator.accounts.logic.storage.AccountsRepository

class AuthAccountSelectorFactory(
        private val accountsRepository: AccountsRepository
) {
    fun getForActivity(activity: Activity): ActivityAuthAccountSelector {
        return ActivityAuthAccountSelector.forActivity(
                accountsRepository,
                activity = activity
        )
    }

    fun getForFragment(fragment: Fragment): ActivityAuthAccountSelector {
        return ActivityAuthAccountSelector.forFragment(
                accountsRepository,
                fragment = fragment
        )
    }
}