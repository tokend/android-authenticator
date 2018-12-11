package org.tokend.authenticator.accounts.view.adapter

import org.tokend.authenticator.accounts.data.model.Account

class AccountListItem(account: Account) {
    val uid = account.uid
    val isBroken = account.isBroken
    val network = account.network
    val email = account.email
    val source = account
}