package org.tokend.authenticator.test

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.accounts.data.model.Network
import org.tokend.authenticator.auth.request.accountselection.logic.AuthAccountSelector
import org.tokend.authenticator.auth.request.confirmation.logic.AuthRequestConfirmationProvider
import org.tokend.authenticator.auth.request.logic.AuthorizeAppUseCase
import org.tokend.authenticator.logic.api.factory.DefaultApiFactory
import org.tokend.authenticator.logic.db.AppDatabase
import org.tokend.authenticator.security.encryption.cipher.DefaultDataCipher
import org.tokend.authenticator.security.encryption.logic.KdfAttributesGenerator
import org.tokend.authenticator.logic.transactions.factory.DefaultTxManagerFactory
import org.tokend.authenticator.accounts.info.data.storage.AccountSignersRepositoryProvider
import org.tokend.sdk.api.authenticator.model.AuthRequest
import org.tokend.wallet.utils.toByteArray
import java.net.URLEncoder

@RunWith(AndroidJUnit4::class)
class AuthorizeApp {
    private val kdf = KdfAttributesGenerator().withRandomSalt()
    private val key = DumbEncryptionKeyProvider()
            .getKey(kdf)
            .blockingGet()
    private val seed = "SCIUKFBGL364Q2A2BVO474BBOFS6VV2K5WFAQG6WQS7WHAATGLE6CVP3".toCharArray()

    private val account = Account(
            Network("NET_NAME", "TokenD Testnet Network",
                    "NET_ACCOUNT", "https://api.testnet.tokend.org/"),
            "EMAIL",
            "GCC4IGBATFLGG5JV4DUKA2HZSS6EO6LJBCBY4AJJPYJ6U7HKZGL7VE4T",
            "WALLET_ID",
            "PUBLIC_KEY",
            DefaultDataCipher().encrypt(seed.toByteArray(), key).blockingGet(),
            kdf,
            1L
    )

    @Test
    fun flow() {
        val appContext = InstrumentationRegistry.getTargetContext()

        val database = Room.databaseBuilder(appContext, AppDatabase::class.java,
                "app-db-test")
                .build()
//        database.signersDao.deleteAll()

        val signersRepositoryProvider = AccountSignersRepositoryProvider(database, DefaultApiFactory())

        val pubKey = org.tokend.wallet.Account.random().accountId
        val uri = "tokend://auth?api=${account.network.rootUrl}" +
                "&app=${URLEncoder.encode("Test App", "UTF-8")}" +
                "&pubkey=$pubKey&scope=1&expires_at=0"

        val accountSelector = object : AuthAccountSelector {
            override fun selectAccountForAuth(network: Network,
                                              authRequest: AuthRequest): Maybe<Account> {
                return Maybe.just(account)
            }
        }

        val confirmationProvider = object : AuthRequestConfirmationProvider {
            override fun confirmAuthRequest(authRequest: AuthRequest): Single<Boolean> {
                return Single.just(true)
            }
        }

        AuthorizeAppUseCase(
                uri,
                accountSelector,
                signersRepositoryProvider,
                confirmationProvider,
                DefaultDataCipher(),
                DumbEncryptionKeyProvider(),
                DefaultApiFactory(),
                DefaultTxManagerFactory(DefaultApiFactory())
        )
                .perform()
                .blockingAwait()

        val repository = signersRepositoryProvider.getForAccount(account)

        Assert.assertNotNull(repository.itemsList.find { it.publicKey == pubKey })

        repository.update().blockingAwait()

        Assert.assertNotNull(repository.itemsList.find { it.publicKey == pubKey })
    }
}