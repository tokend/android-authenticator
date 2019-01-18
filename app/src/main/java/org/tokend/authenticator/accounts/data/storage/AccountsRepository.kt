package org.tokend.authenticator.accounts.data.storage

import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.base.repository.LocalMultipleItemsRepository
import org.tokend.authenticator.base.repository.RepositoryCache

class AccountsRepository(
        override val itemsCache: RepositoryCache<Account>
) : LocalMultipleItemsRepository<Account>()