package org.tokend.authenticator.accounts.logic.storage

import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.base.logic.db.AppDatabase
import org.tokend.authenticator.base.logic.repository.LocalMultipleItemsRepository

class AccountsRepository(
        database: AppDatabase
) : LocalMultipleItemsRepository<Account>() {
    override val itemsCache = AccountsCache(database)
}