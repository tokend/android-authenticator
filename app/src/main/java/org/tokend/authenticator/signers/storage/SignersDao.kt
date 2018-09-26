package org.tokend.authenticator.signers.storage

import android.arch.persistence.room.*
import io.reactivex.Single
import org.tokend.authenticator.signers.model.db.SignerEntity

@Dao
interface SignersDao {
    @Query("SELECT * FROM signer WHERE account_id=:accountId ORDER BY uid DESC")
    fun getByAccount(accountId: Long): Single<List<SignerEntity>>

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