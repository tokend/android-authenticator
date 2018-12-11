package org.tokend.authenticator.base.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import io.reactivex.disposables.CompositeDisposable
import org.tokend.authenticator.App
import org.tokend.authenticator.accounts.data.storage.AccountsRepository
import org.tokend.authenticator.auth.request.accountselection.view.AuthAccountSelectorFactory
import org.tokend.authenticator.auth.request.confirmation.view.AuthRequestConfirmationDialogFactory
import org.tokend.authenticator.logic.api.factory.ApiFactory
import org.tokend.authenticator.di.DateOnlyDateFormat
import org.tokend.authenticator.di.DateTimeDateFormat
import org.tokend.authenticator.security.encryption.cipher.DataCipher
import org.tokend.authenticator.security.encryption.logic.SecureStorage
import org.tokend.authenticator.util.FingerprintUtil
import org.tokend.authenticator.logic.transactions.factory.TxManagerFactory
import org.tokend.authenticator.view.util.ToastManager
import org.tokend.authenticator.util.errorhandler.ErrorHandlerFactory
import org.tokend.authenticator.security.encryption.logic.AppEncryptionKeyProvider
import org.tokend.authenticator.security.userkey.logic.AppUserKeyProvidersHolder
import org.tokend.authenticator.security.environment.logic.EnvSecurityStatusProvider
import org.tokend.authenticator.security.userkey.punishment.PunishmentTimer
import org.tokend.authenticator.security.userkey.view.ActivityUserKeyProvider
import org.tokend.authenticator.security.userkey.view.ActivityUserKeyProviderFactory
import org.tokend.authenticator.accounts.info.data.storage.AccountSignersRepositoryProvider
import java.text.DateFormat
import javax.inject.Inject

abstract class BaseActivity(
        private val canShowUserKeyRequest: Boolean = true
) : AppCompatActivity() {

    @Inject
    lateinit var accountsRepository: AccountsRepository
    @Inject
    lateinit var errorHandlerFactory: ErrorHandlerFactory
    @Inject
    lateinit var toastManager: ToastManager
    @Inject
    lateinit var dataCipher: DataCipher
    @Inject
    lateinit var authRequestConfirmationDialogFactory: AuthRequestConfirmationDialogFactory
    @Inject
    lateinit var authAccountSelectorFactory: AuthAccountSelectorFactory
    @Inject
    lateinit var signersRepositoryProvider: AccountSignersRepositoryProvider
    @Inject
    lateinit var encryptionKeyProvider: AppEncryptionKeyProvider
    @Inject
    @field:DateTimeDateFormat
    lateinit var dateTimeDateFormat: DateFormat
    @Inject
    @field:DateOnlyDateFormat
    lateinit var dateOnlyDateFormat: DateFormat
    @Inject
    lateinit var apiFactory: ApiFactory
    @Inject
    lateinit var txManagerFactory: TxManagerFactory
    @Inject
    lateinit var userKeyProvidersHolder: AppUserKeyProvidersHolder
    @Inject
    lateinit var secureStorage: SecureStorage
    @Inject
    lateinit var envSecurityStatusProvider: EnvSecurityStatusProvider
    @Inject
    lateinit var fingerprintUtil: FingerprintUtil
    @Inject
    lateinit var punishmentTimer: PunishmentTimer
    @Inject
    lateinit var activityUserKeyProviderFactory: ActivityUserKeyProviderFactory

    protected val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private lateinit var initialUserKeyProvider: ActivityUserKeyProvider
    private lateinit var defaultUserKeyProvider: ActivityUserKeyProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as? App)?.appComponent?.inject(this)

        if (canShowUserKeyRequest) {
            initialUserKeyProvider = activityUserKeyProviderFactory.getForSetUp(this)
            defaultUserKeyProvider = activityUserKeyProviderFactory.getForRequest(this)
        }

        //replace to logic of allow creation
        if (true) {
            onCreateAllowed(savedInstanceState)
        } else {

        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    abstract fun onCreateAllowed(savedInstanceState: Bundle?)

    override fun onStart() {
        super.onStart()
        if (canShowUserKeyRequest) {
            registerUserKeyProviders()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
        if (canShowUserKeyRequest) {
            unregisterUserKeyProviders()
        }
    }

    protected open fun registerUserKeyProviders() {
        userKeyProvidersHolder.registerInitialUserKeyProvider(initialUserKeyProvider)
        userKeyProvidersHolder.registerDefaultUserKeyProvider(defaultUserKeyProvider)
    }

    protected open fun unregisterUserKeyProviders() {
        userKeyProvidersHolder.unregisterInitialUserKeyProvider(initialUserKeyProvider)
        userKeyProvidersHolder.unregisterDefaultUserKeyProvider(defaultUserKeyProvider)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (canShowUserKeyRequest) {
            initialUserKeyProvider.handleActivityResult(requestCode, resultCode, data)
                    || defaultUserKeyProvider.handleActivityResult(requestCode, resultCode, data)
        }
    }
}