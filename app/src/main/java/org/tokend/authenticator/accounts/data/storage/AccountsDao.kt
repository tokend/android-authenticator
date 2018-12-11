package org.tokend.authenticator.accounts.data.storage

import android.arch.persistence.room.*
import org.tokend.authenticator.accounts.data.model.db.AccountEntity

@Dao
interface AccountsDao {
    @Query("SELECT * FROM account ORDER BY uid DESC")
    fun getAll(): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg items: AccountEntity)

    @Update()
    fun update(vararg items: AccountEntity)

    @Delete()
    fun delete(vararg items: AccountEntity)

    @Query("DELETE FROM account")
    fun deleteAll()
}