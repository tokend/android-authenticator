package org.tokend.authenticator.auth.view.accounts.selection

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import io.reactivex.Maybe
import io.reactivex.subjects.MaybeSubject
import org.jetbrains.anko.intentFor
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.authenticator.accounts.logic.storage.AccountsRepository
import org.tokend.authenticator.auth.request.AuthAccountSelector
import org.tokend.sdk.api.authenticator.model.AuthRequest

class ActivityAuthAccountSelector
private constructor(
        private val accountsRepository: AccountsRepository,
        private val parentFragment: Fragment? = null,
        private val parentActivity: Activity? = null
) : AuthAccountSelector {
    private val resultSubject = MaybeSubject.create<Account>()

    override fun selectAccountForAuth(network: Network,
                                      authRequest: AuthRequest): Maybe<Account> {
        openSelectionActivity(authRequest.appName, network)
        return resultSubject
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != ACCOUNT_SELECTION_REQUEST_CODE) {
            return false
        }

        val accountId = data
                ?.getLongExtra(AuthAccountSelectionActivity.ACCOUNT_ID_RESULT_EXTRA, -1L)
                ?.takeIf { it > 0 }
        val account = accountsRepository.itemsList.find { it.uid == accountId }

        return if (resultCode == Activity.RESULT_CANCELED || account == null) {
            cancelSelection()
            true
        } else {
            postSelectionResult(account)
            true
        }
    }

    private fun openSelectionActivity(appName: String, network: Network) {
        val extras = arrayOf(
                AuthAccountSelectionActivity.APP_NAME_EXTRA to appName,
                AuthAccountSelectionActivity.NETWORK_EXTRA to network
        )

        if (parentActivity != null) {
            parentActivity.startActivityForResult(
                    parentActivity.intentFor<AuthAccountSelectionActivity>(*extras),
                    ACCOUNT_SELECTION_REQUEST_CODE
            )
        } else parentFragment?.startActivityForResult(
                parentFragment.intentFor<AuthAccountSelectionActivity>(*extras),
                ACCOUNT_SELECTION_REQUEST_CODE
        )
    }

    private fun cancelSelection() {
        resultSubject.onComplete()
    }

    private fun postSelectionResult(account: Account) {
        resultSubject.onSuccess(account)
    }

    companion object {
        private val ACCOUNT_SELECTION_REQUEST_CODE = "select_account".hashCode() and 0xffff

        fun forFragment(accountsRepository: AccountsRepository,
                        fragment: Fragment): ActivityAuthAccountSelector {
            return ActivityAuthAccountSelector(
                    accountsRepository,
                    parentFragment = fragment
            )
        }

        fun forActivity(accountsRepository: AccountsRepository,
                        activity: Activity?): ActivityAuthAccountSelector {
            return ActivityAuthAccountSelector(
                    accountsRepository,
                    parentActivity = activity
            )
        }
    }
}