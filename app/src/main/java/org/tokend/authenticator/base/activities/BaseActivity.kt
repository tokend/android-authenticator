package org.tokend.authenticator.base.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import io.reactivex.disposables.CompositeDisposable
import org.tokend.authenticator.App
import org.tokend.authenticator.accounts.logic.storage.AccountsRepository
import org.tokend.authenticator.auth.view.accounts.selection.AuthAccountSelectorFactory
import org.tokend.authenticator.auth.view.confirmation.AuthRequestConfirmationDialogFactory
import org.tokend.authenticator.base.logic.di.DateOnlyDateFormat
import org.tokend.authenticator.base.logic.di.DateTimeDateFormat
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.util.ToastManager
import org.tokend.authenticator.base.util.error_handlers.ErrorHandlerFactory
import org.tokend.authenticator.signers.storage.AccountSignersRepositoryProvider
import java.text.DateFormat
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

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
    lateinit var encryptionKeyProvider: EncryptionKeyProvider
    @Inject
    @field:DateTimeDateFormat
    lateinit var dateTimeDateFormat: DateFormat
    @Inject
    @field:DateOnlyDateFormat
    lateinit var dateOnlyDateFormat: DateFormat

    protected val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as? App)?.appComponent?.inject(this)

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

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}