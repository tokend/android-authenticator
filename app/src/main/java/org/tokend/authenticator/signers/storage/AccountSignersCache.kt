package org.tokend.authenticator.signers.storage

import io.reactivex.Single
import org.jetbrains.anko.doAsync
import org.tokend.authenticator.base.logic.db.AppDatabase
import org.tokend.authenticator.base.logic.repository.RepositoryCache
import org.tokend.authenticator.signers.model.Signer
import org.tokend.authenticator.signers.model.db.SignerEntity

class AccountSignersCache(
        val accountId: Long,
        val database: AppDatabase
) : RepositoryCache<Signer>() {
    private val dao = database.signersDao

    override fun isContentSame(first: Signer, second: Signer): Boolean {
        return first.scope == second.scope
                && first.name == second.name
                && first.publicKey == second.publicKey
    }

    override fun getAllFromDb(): Single<List<Signer>> {
        return dao.getByAccount(accountId)
                .map {
                    it.map {
                        it.toSigner()
                    }
                }
    }

    override fun addToDb(items: List<Signer>) {
        doAsync {
            dao.insert(*items.map { SignerEntity.fromSigner(it) }.toTypedArray())
        }
    }

    override fun updateInDb(items: List<Signer>) {
        doAsync {
            dao.update(*items.map { SignerEntity.fromSigner(it) }.toTypedArray())
        }
    }

    override fun deleteFromDb(items: List<Signer>) {
        doAsync {
            dao.delete(*items.map { SignerEntity.fromSigner(it) }.toTypedArray())
        }
    }

    override fun clearDb() {
        doAsync {
            dao.deleteAllByAccount(accountId)
        }
    }

    override fun sortItems() {
        mItems.sortByDescending { it.uid }
    }
}