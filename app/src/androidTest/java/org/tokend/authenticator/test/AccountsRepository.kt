package org.tokend.authenticator.test

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import org.junit.Assert
import org.junit.Test
import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.accounts.data.model.Network
import org.tokend.authenticator.accounts.data.storage.AccountsCache
import org.tokend.authenticator.accounts.data.storage.AccountsRepository
import org.tokend.authenticator.logic.db.AppDatabase
import org.tokend.authenticator.security.encryption.logic.KdfAttributesGenerator
import org.tokend.sdk.keyserver.models.KeychainData

class AccountsRepository {
    private val account = Account(
            Network("NET_NAME", "NET_PASSPHRASE",
                    "NET_ACCOUNT", "NET_URL"),
            "EMAIL",
            "ORIGINAL_ACCOUNT_ID",
            "",
            "",
            KeychainData.fromRaw(ByteArray(16), ByteArray(16)),
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
            AccountsRepository(AccountsCache(database))
                    .also {
                        it.updateDeferred().blockingAwait()
                    }
        }

        getRepository().add(account)

        Thread.sleep(500)

        val loadedAccount = getRepository().itemsList.first()

        Assert.assertEquals(account, loadedAccount)
    }

    @Test
    fun update() {
        val appContext = InstrumentationRegistry.getTargetContext()

        val database = Room.databaseBuilder(appContext, AppDatabase::class.java,
                "app-db-test")
                .build()
        database.accountsDao.deleteAll()

        val getRepository = {
            AccountsRepository(AccountsCache(database))
                    .also {
                        it.updateDeferred().blockingAwait()
                    }
        }

        val repository = getRepository()

        repository.add(account)
        val newKdf = KdfAttributesGenerator().withRandomSalt()
        val newEncryptedSeed = KeychainData.fromRaw(byteArrayOf(1, 2, 3, 4), byteArrayOf(5, 6, 7, 8))
        account.apply {
            kdfAttributes = newKdf
            encryptedSeed = newEncryptedSeed
        }
        repository.update(account)

        Thread.sleep(250)

        val loadedAccount = getRepository().itemsList.first()

        Assert.assertEquals(newKdf.encodedSalt, loadedAccount.kdfAttributes.encodedSalt)
        Assert.assertArrayEquals(newEncryptedSeed.cipherText, loadedAccount.encryptedSeed.cipherText)
        Assert.assertArrayEquals(newEncryptedSeed.iv, loadedAccount.encryptedSeed.iv)
    }
}