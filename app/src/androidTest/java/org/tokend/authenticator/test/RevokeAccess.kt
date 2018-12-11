package org.tokend.authenticator.test

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.accounts.data.model.Network
import org.tokend.authenticator.auth.manage.logic.RevokeAccessUseCase
import org.tokend.authenticator.logic.api.factory.DefaultApiFactory
import org.tokend.authenticator.logic.db.AppDatabase
import org.tokend.authenticator.security.encryption.cipher.DefaultDataCipher
import org.tokend.authenticator.security.encryption.logic.EncryptionKeyProvider
import org.tokend.authenticator.security.encryption.logic.KdfAttributesGenerator
import org.tokend.authenticator.logic.transactions.TxManager
import org.tokend.authenticator.logic.transactions.factory.DefaultTxManagerFactory
import org.tokend.authenticator.accounts.info.data.model.Signer
import org.tokend.authenticator.accounts.info.data.storage.AccountSignersRepositoryProvider
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.wallet.utils.toByteArray
import org.tokend.wallet.xdr.SignerType

@RunWith(AndroidJUnit4::class)
class RevokeAccess {
    private val key = ByteArray(32)
    private val seed = "SCIUKFBGL364Q2A2BVO474BBOFS6VV2K5WFAQG6WQS7WHAATGLE6CVP3".toCharArray()

    private val account = Account(
            Network("NET_NAME", "TokenD Testnet Network",
                    "NET_ACCOUNT", "https://api.testnet.tokend.org/"),
            "EMAIL",
            "GCC4IGBATFLGG5JV4DUKA2HZSS6EO6LJBCBY4AJJPYJ6U7HKZGL7VE4T",
            "WALLET_ID",
            "PUBLIC_KEY",
            DefaultDataCipher().encrypt(seed.toByteArray(), key).blockingGet(),
            KdfAttributesGenerator().withRandomSalt(),
            1L
    )

    @Test
    fun flow() {
        val appContext = InstrumentationRegistry.getTargetContext()

        val database = Room.databaseBuilder(appContext, AppDatabase::class.java,
                "app-db-test")
                .build()
//        database.signersDao.deleteAll()

        val signersRepositoryProvider = AccountSignersRepositoryProvider(database,
                DefaultApiFactory())

        val publicKey = org.tokend.wallet.Account.random().accountId
        val signer = Signer(
                name = "MyAwesomeSigner",
                scope = SignerType.READER.value,
                publicKey = publicKey,
                accountId = account.uid,
                expirationDate = null
        )

        val keyProvider = object : EncryptionKeyProvider {
            override fun getKey(kdfAttributes: KdfAttributes): Single<ByteArray> {
                return Single.just(key)
            }
        }

        val repository = signersRepositoryProvider.getForAccount(account)

        repository.add(signer, DefaultDataCipher(), keyProvider,
                TxManager(DefaultApiFactory().getApi(account.network.rootUrl))).blockingAwait()

        RevokeAccessUseCase(
                signer,
                account,
                DefaultDataCipher(),
                keyProvider,
                signersRepositoryProvider,
                DefaultTxManagerFactory(DefaultApiFactory())
        )
                .perform()
                .blockingAwait()

        Assert.assertNull(repository.itemsList.find { it.publicKey == publicKey })
    }
}