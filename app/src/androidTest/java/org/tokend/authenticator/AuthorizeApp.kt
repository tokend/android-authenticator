package org.tokend.authenticator

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.accounts.logic.model.Network
import org.tokend.authenticator.auth.request.AuthAccountSelector
import org.tokend.authenticator.auth.request.AuthRequest
import org.tokend.authenticator.auth.request.AuthRequestConfirmationProvider
import org.tokend.authenticator.auth.request.AuthorizeAppUseCase
import org.tokend.authenticator.base.logic.db.AppDatabase
import org.tokend.authenticator.base.logic.encryption.DefaultDataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.logic.encryption.KdfAttributesGenerator
import org.tokend.authenticator.signers.storage.AccountSignersRepositoryProvider
import org.tokend.sdk.keyserver.models.KdfAttributes
import org.tokend.wallet.utils.toByteArray
import java.net.URLEncoder

@RunWith(AndroidJUnit4::class)
class AuthorizeApp {
    private val key = ByteArray(32)
    private val seed = "SCIUKFBGL364Q2A2BVO474BBOFS6VV2K5WFAQG6WQS7WHAATGLE6CVP3".toCharArray()

    private val account = Account(
            Network("NET_NAME", "TokenD Testnet Network",
                    "NET_ACCOUNT", "https://api.testnet.tokend.org/"),
            "EMAIL",
            "GCC4IGBATFLGG5JV4DUKA2HZSS6EO6LJBCBY4AJJPYJ6U7HKZGL7VE4T",
            "WALLET_ID",
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

        val signersRepositoryProvider = AccountSignersRepositoryProvider(database)

        val pubKey = org.tokend.wallet.Account.random().accountId
        val uri = "tdauth://?action=auth&api=${account.network.rootUrl}" +
                "&app=${URLEncoder.encode("Test App", "UTF-8")}" +
                "&pubkey=$pubKey&scope=1&expires_at=0"

        val accountSelector = object : AuthAccountSelector {
            override fun selectAccountForAuth(network: Network,
                                              authRequest: AuthRequest): Maybe<AuthAccountSelector.Result> {
                return Maybe.just(
                        AuthAccountSelector.Result(
                                account, DefaultDataCipher(), object : EncryptionKeyProvider {
                            override fun getKey(kdfAttributes: KdfAttributes): Single<ByteArray> {
                                return Single.just(key)
                            }
                        }
                        )
                )
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
                confirmationProvider
        )
                .perform()
                .blockingAwait()

        val repository = signersRepositoryProvider.getForAccount(account)

        Assert.assertNotNull(repository.itemsList.find { it.publicKey == pubKey })

        repository.update().blockingAwait()

        Assert.assertNotNull(repository.itemsList.find { it.publicKey == pubKey })
    }
}