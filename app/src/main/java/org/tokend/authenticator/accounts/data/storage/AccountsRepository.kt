package org.tokend.authenticator.accounts.data.storage

import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.logic.db.AppDatabase
import org.tokend.authenticator.base.repository.LocalMultipleItemsRepository

class AccountsRepository(
        database: AppDatabase
) : LocalMultipleItemsRepository<Account>() {
    override val itemsCache = AccountsCache(database)
}