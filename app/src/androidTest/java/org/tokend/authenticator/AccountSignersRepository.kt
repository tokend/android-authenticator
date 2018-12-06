package org.tokend.authenticator

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.authenticator.base.logic.api.factory.DefaultApiFactory
import org.tokend.authenticator.base.logic.db.AppDatabase
import org.tokend.authenticator.base.logic.encryption.DefaultDataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.logic.encryption.KdfAttributesGenerator
import org.tokend.authenticator.base.logic.transactions.TxManager
import org.tokend.authenticator.signers.model.Signer
import org.tokend.authenticator.signers.storage.AccountSignersRepository
import org.tokend.authenticator.signers.storage.AccountSignersRepositoryProvider
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.wallet.utils.toByteArray
import org.tokend.wallet.xdr.SignerType

@RunWith(AndroidJUnit4::class)
class AccountSignersRepository {
    private val key = ByteArray(32)
    private val seed = "SCIUKFBGL364Q2A2BVO474BBOFS6VV2K5WFAQG6WQS7WHAATGLE6CVP3".toCharArray()

    private val account = Account(
            Network("NET_NAME", "TokenD Testnet Network",
                    "NET_ACCOUNT", "https://api.testnet.tokend.org/"),
            "EMAIL",
            "GCC4IGBATFLGG5JV4DUKA2HZSS6EO6LJBCBY4AJJPYJ6U7HKZGL7VE4T",
            "",
            "",
            DefaultDataCipher().encrypt(seed.toByteArray(), key).blockingGet(),
            KdfAttributesGenerator().withRandomSalt()
    )

    @Test
    fun persistence() {
        val appContext = InstrumentationRegistry.getTargetContext()

        val database = Room.databaseBuilder(appContext, AppDatabase::class.java,
                "app-db-test")
                .build()
        database.signersDao.deleteAll()

        val getRepository = {
            AccountSignersRepository(account, database, DefaultApiFactory())
                    .also {
                        it.updateDeferred().blockingAwait()
                    }
        }

        val repository = getRepository()

        Thread.sleep(500)

        Assert.assertArrayEquals(
                repository.itemsList.toTypedArray(),
                database.signersDao.getByAccount(account.uid).map { it.toSigner() }
                        .toTypedArray()
        )
    }

    @Test
    fun addSigner() {
        val appContext = InstrumentationRegistry.getTargetContext()

        val database = Room.databaseBuilder(appContext, AppDatabase::class.java,
                "app-db-test")
                .build()
        database.signersDao.deleteAll()

        val getRepository = {
            AccountSignersRepository(account, database, DefaultApiFactory())
                    .also {
                        it.updateDeferred().blockingAwait()
                    }
        }

        val repository = getRepository()

        val publicKey = org.tokend.wallet.Account.random().accountId
        performSignerAdd(publicKey, repository)

        Assert.assertNotNull(repository.itemsList.find { it.publicKey == publicKey })

        repository.update().blockingAwait()

        Assert.assertNotNull(repository.itemsList.find { it.publicKey == publicKey })
    }

    private fun performSignerAdd(publicKey: String, repository: AccountSignersRepository) {
        val newSigner = Signer(
                name = "MyAwesomeSigner",
                scope = SignerType.READER.value,
                publicKey = publicKey,
                accountId = account.uid,
                expirationDate = null
        )

        repository.add(newSigner, DefaultDataCipher(), object : EncryptionKeyProvider {
            override fun getKey(kdfAttributes: KdfAttributes): Single<ByteArray> {
                return Single.just(key)
            }
        }, TxManager(DefaultApiFactory().getApi(account.network.rootUrl))).blockingAwait()
    }

    @Test
    fun deleteSigner() {
        val appContext = InstrumentationRegistry.getTargetContext()

        val database = Room.databaseBuilder(appContext, AppDatabase::class.java,
                "app-db-test")
                .build()
        database.signersDao.deleteAll()

        val getRepository = {
            AccountSignersRepository(account, database, DefaultApiFactory())
                    .also {
                        it.updateDeferred().blockingAwait()
                    }
        }

        val repository = getRepository()

        val publicKey = org.tokend.wallet.Account.random().accountId

        performSignerAdd(publicKey, repository)

        val signerToDelete = repository.itemsList.find { it.publicKey == publicKey }!!
        repository.delete(signerToDelete, DefaultDataCipher(), object : EncryptionKeyProvider {
            override fun getKey(kdfAttributes: KdfAttributes): Single<ByteArray> {
                return Single.just(key)
            }
        }, TxManager(DefaultApiFactory().getApi(account.network.rootUrl))).blockingAwait()

        Assert.assertNull(repository.itemsList.find { it.publicKey == publicKey })

        repository.update().blockingAwait()

        Assert.assertNull(repository.itemsList.find { it.publicKey == publicKey })
    }

    @Test
    fun provider() {
        val appContext = InstrumentationRegistry.getTargetContext()

        val database = Room.databaseBuilder(appContext, AppDatabase::class.java,
                "app-db-test")
                .build()

        val provider = AccountSignersRepositoryProvider(database, DefaultApiFactory())

        Assert.assertEquals(provider.getForAccount(account), provider.getForAccount(account))
    }
}