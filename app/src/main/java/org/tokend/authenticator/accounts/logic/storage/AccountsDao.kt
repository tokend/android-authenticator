package org.tokend.authenticator.accounts.logic.storage

import android.arch.persistence.room.*
import io.reactivex.Single
import org.tokend.authenticator.accounts.logic.model.db.AccountEntity

@Dao
interface AccountsDao {
    @Query("SELECT * FROM account ORDER BY uid DESC")
    fun getAll(): Single<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg items: AccountEntity)

    @Update()
    fun update(vararg items: AccountEntity)

    @Delete()
    fun delete(vararg items: AccountEntity)

    @Query("DELETE FROM account")
    fun deleteAll()
}