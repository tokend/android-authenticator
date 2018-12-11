package org.tokend.authenticator.auth.request.accountselection.view.adapter

import org.tokend.authenticator.accounts.data.model.Account

class ExistingAccountSelectionListItem(
        val name: String,
        val id: Long
) : AccountSelectionListItem {
    companion object {
        fun fromAccount(account: Account): ExistingAccountSelectionListItem {
            return ExistingAccountSelectionListItem(account.email, account.uid)
        }
    }
}