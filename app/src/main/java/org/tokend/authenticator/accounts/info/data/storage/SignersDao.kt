package org.tokend.authenticator.accounts.info.data.storage

import android.arch.persistence.room.*
import org.tokend.authenticator.accounts.info.data.model.db.SignerEntity

@Dao
interface SignersDao {
    @Query("SELECT * FROM signer WHERE account_id=:accountId ORDER BY uid DESC")
    fun getByAccount(accountId: Long): List<SignerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg items: SignerEntity)

    @Update()
    fun update(vararg items: SignerEntity)

    @Delete()
    fun delete(vararg items: SignerEntity)

    @Query("DELETE FROM signer WHERE account_id=:accountId")
    fun deleteAllByAccount(accountId: Long)

    @Query("DELETE FROM signer")
    fun deleteAll()
}