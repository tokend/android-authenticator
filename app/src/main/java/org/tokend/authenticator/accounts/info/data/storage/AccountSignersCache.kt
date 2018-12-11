package org.tokend.authenticator.accounts.info.data.storage

import org.tokend.authenticator.logic.db.AppDatabase
import org.tokend.authenticator.base.repository.RepositoryCache
import org.tokend.authenticator.accounts.info.data.model.Signer
import org.tokend.authenticator.accounts.info.data.model.db.SignerEntity

class AccountSignersCache(
        val accountId: Long,
        val database: AppDatabase
) : RepositoryCache<Signer>() {
    private val dao = database.signersDao

    override fun isContentSame(first: Signer, second: Signer): Boolean {
        return first.scope == second.scope
                && first.name == second.name
                && first.publicKey == second.publicKey
                && first.expirationDate == second.expirationDate
    }

    override fun getAllFromDb(): List<Signer> {
        return dao.getByAccount(accountId)
                .map {
                    it.toSigner()
                }
    }

    override fun addToDb(items: List<Signer>) {
        dao.insert(*items.map { SignerEntity.fromSigner(it) }.toTypedArray())
    }

    override fun updateInDb(items: List<Signer>) {
        dao.update(*items.map { SignerEntity.fromSigner(it) }.toTypedArray())
    }

    override fun deleteFromDb(items: List<Signer>) {
        dao.delete(*items.map { SignerEntity.fromSigner(it) }.toTypedArray())
    }

    override fun clearDb() {
        dao.deleteAllByAccount(accountId)
    }

    override fun sortItems() {
        mItems.sortByDescending { it.uid }
    }
}