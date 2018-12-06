package org.tokend.authenticator.test

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.tokend.authenticator.accounts.logic.RecoverAccountUseCase
import org.tokend.authenticator.accounts.logic.storage.AccountsRepository
import org.tokend.authenticator.base.logic.api.factory.DefaultApiFactory
import org.tokend.authenticator.base.logic.db.AppDatabase
import org.tokend.authenticator.base.logic.encryption.DefaultDataCipher
import org.tokend.authenticator.base.util.ObservableTransformers
import org.tokend.authenticator.signers.storage.AccountSignersRepositoryProvider

@RunWith(AndroidJUnit4::class)
class AccountRecovery {
    private val email = "oleg@radiokot.com.ua"

    @Test
    fun recoverNonExisting() {
        val appContext = InstrumentationRegistry.getTargetContext()

        val database = Room.databaseBuilder(appContext, AppDatabase::class.java,
                "app-db-test")
                .build()
        database.clearAllTables()

        val getRepository = {
            AccountsRepository(database)
                    .also {
                        it.updateDeferred().blockingAwait()
                    }
        }

        val accountRepository = getRepository()

        performRecovery(accountRepository, database)

        Assert.assertEquals(email, accountRepository.itemsList.first().email)
    }

    @Test
    fun recoverExisting() {
        val appContext = InstrumentationRegistry.getTargetContext()

        val database = Room.databaseBuilder(appContext, AppDatabase::class.java,
                "app-db-test")
                .build()
        database.clearAllTables()

        val getRepository = {
            AccountsRepository(database)
                    .also {
                        it.updateDeferred().blockingAwait()
                    }
        }

        val accountRepository = getRepository()

        performRecovery(accountRepository, database)

        val currentKdfSalt = accountRepository.itemsList.first().kdfAttributes.encodedSalt

        performRecovery(accountRepository, database)

        val newKdfSalt = accountRepository.itemsList.first().kdfAttributes.encodedSalt

        Assert.assertNotEquals(currentKdfSalt, newKdfSalt)
        Assert.assertEquals(1, accountRepository.itemsList.size)
    }

    private fun performRecovery(accountRepository: AccountsRepository, database: AppDatabase) {
        val network = "https://api.testnet.tokend.org/"
        val recoverySeed = "SCIUKFBGL364Q2A2BVO474BBOFS6VV2K5WFAQG6WQS7WHAATGLE6CVP3".toCharArray()
        val cipher = DefaultDataCipher()
        val keyProvider = DumbEncryptionKeyProvider()

        RecoverAccountUseCase(
                networkUrl = network,
                email = email,
                recoverySeed = recoverySeed,
                cipher = cipher,
                encryptionKeyProvider = keyProvider,
                accountsRepository = accountRepository,
                accountSignersRepositoryProvider =
                AccountSignersRepositoryProvider(database, DefaultApiFactory()),
                apiFactory = DefaultApiFactory()
        )
                .perform()
                .compose(ObservableTransformers.defaultSchedulersCompletable())
                .blockingAwait()
    }
}