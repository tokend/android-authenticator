package org.tokend.authenticator.auth.view.accounts.selection.adapter

import org.tokend.authenticator.accounts.logic.model.Account

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