package org.tokend.authenticator.base.activities.account_list.adapter

import org.tokend.authenticator.accounts.logic.model.Account

class AccountListItem(account: Account) {
    val uid = account.uid
    val isBroken = account.isBroken
    val network = account.network
    val email = account.email
    val source = account
}