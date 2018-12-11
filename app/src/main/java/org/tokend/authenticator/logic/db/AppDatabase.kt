package org.tokend.authenticator.logic.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import org.tokend.authenticator.accounts.data.model.db.AccountEntity
import org.tokend.authenticator.accounts.data.storage.AccountsDao
import org.tokend.authenticator.accounts.info.data.model.db.SignerEntity
import org.tokend.authenticator.accounts.info.data.storage.SignersDao

@Database(
        entities = [
            AccountEntity::class,
            SignerEntity::class
        ],
        version = 4,
        exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val accountsDao: AccountsDao
    abstract val signersDao: SignersDao
}