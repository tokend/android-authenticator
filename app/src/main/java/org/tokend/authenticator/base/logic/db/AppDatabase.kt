package org.tokend.authenticator.base.logic.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import org.tokend.authenticator.accounts.logic.model.db.AccountEntity
import org.tokend.authenticator.accounts.logic.storage.AccountsDao

@Database(
        entities = [AccountEntity::class],
        version = 1,
        exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val accountsDao: AccountsDao
}