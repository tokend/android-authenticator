package org.tokend.authenticator.base.logic.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import org.tokend.authenticator.accounts.logic.model.db.AccountEntity
import org.tokend.authenticator.accounts.logic.storage.AccountsDao
import org.tokend.authenticator.signers.model.db.SignerEntity
import org.tokend.authenticator.signers.storage.SignersDao

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