package org.tokend.authenticator

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import org.junit.Assert
import org.junit.Test
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.authenticator.accounts.logic.storage.AccountsRepository
import org.tokend.authenticator.base.logic.db.AppDatabase
import org.tokend.authenticator.base.logic.encryption.KdfAttributesGenerator
import org.tokend.sdk.api.models.KeychainData

class AccountsRepository {
    private val account = Account(
            Network("NET_NAME", "NET_PASSPHRASE",
                    "NET_ACCOUNT", "NET_URL"),
            "EMAIL",
            "ORIGINAL_ACCOUNT_ID",
            KeychainData.fromDecoded(ByteArray(16), ByteArray(16)),
            KdfAttributesGenerator().withRandomSalt()
    )

    @Test
    fun persistence() {
        val appContext = InstrumentationRegistry.getTargetContext()

        val database = Room.databaseBuilder(appContext, AppDatabase::class.java,
                "app-db-test")
                .build()
        database.accountsDao.deleteAll()

        val getRepository = {
            AccountsRepository(database)
                    .also {
                        it.updateDeferred().blockingAwait()
                    }
        }

        getRepository().add(account)
        val loadedAccount = getRepository().itemsList.first()

        Assert.assertEquals(account, loadedAccount)
    }
}