package org.tokend.authenticator.accounts.add.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_add_account.*
import kotlinx.android.synthetic.main.layout_network_field.*
import kotlinx.android.synthetic.main.layout_progress.*
import okhttp3.HttpUrl
import org.json.JSONObject
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.add.logic.CreateAccountUseCase
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.util.Navigator
import org.tokend.authenticator.util.ObservableTransformers
import org.tokend.authenticator.util.Permission
import org.tokend.authenticator.util.QrScannerUtil
import org.tokend.authenticator.util.extensions.addSlashIfNeed
import org.tokend.authenticator.util.extensions.hasError
import org.tokend.authenticator.util.extensions.setErrorAndFocus
import org.tokend.authenticator.util.validator.EmailValidator
import org.tokend.authenticator.view.util.LoadingIndicatorManager
import org.tokend.authenticator.view.util.input.SimpleTextWatcher
import org.tokend.authenticator.view.util.input.SoftInputUtil
import org.tokend.crypto.ecdsa.erase
import org.tokend.sdk.api.wallets.model.EmailAlreadyTakenException

class AddAccountActivity : BaseActivity() {

    companion object {
        private val SAVE_SEED_REQUEST = "save_recovery_seed".hashCode() and 0xffff
        private val RECOVER_REQUEST = "recover".hashCode() and 0xffff
        const val NETWORK_URL_EXTRA = "network_url"
    }

    private var networkUrl: String = ""
    private val cameraPermission = Permission(Manifest.permission.CAMERA, 404)

    private val loadingIndicator = LoadingIndicatorManager(
            showLoading = { progress.show() },
            hideLoading = { progress.hide() }
    )

    private var isLoading: Boolean = false
        set(value) {
            field = value
            loadingIndicator.setLoading(value, "add_account")
            updateAddAccountAvailability()
        }

    private var canAddAccount: Boolean = false
        set(value) {
            field = value
            add_account_button.isEnabled = value
        }

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_add_account)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initFields()
        initButtons()

        intent.getStringExtra(NETWORK_URL_EXTRA)?.also {
            onNetworkUrlObtained(it)
        }
    }

    private fun updateAddAccountAvailability() {
        canAddAccount = !isLoading
                && network_edit_text.text.isNotEmpty()
                && email_edit_text.text.isNotEmpty()
                && !email_edit_text.hasError()
    }

    private fun initFields() {
        object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                email_edit_text.error = null
                updateAddAccountAvailability()
            }
        }.also {
            network_edit_text.addTextChangedListener(it)
            email_edit_text.addTextChangedListener(it)
        }
    }

    private fun initButtons() {
        scan_qr_button.setOnClickListener {
            tryOpenQrScanner()
        }

        add_account_button.setOnClickListener {
            tryToAddAccount()
        }

        recovery_button.setOnClickListener {
            val email = email_edit_text.text.toString()
            Navigator.openRecoveryActivity(this, networkUrl, email, RECOVER_REQUEST)
        }
    }

    private fun tryToAddAccount() {
        when {
            !EmailValidator.isValid(email_edit_text.text) ->
                email_edit_text.setErrorAndFocus(getString(R.string.error_invalid_email))
            else -> {
                SoftInputUtil.hideSoftInput(this)
                addAccount()
            }
        }
        updateAddAccountAvailability()
    }

    private fun addAccount() {

        val email = email_edit_text.text.toString()
        val keyProvider = encryptionKeyProvider

        CreateAccountUseCase(
                networkUrl,
                email,
                dataCipher,
                keyProvider,
                accountsRepository,
                apiFactory
        )
                .perform()
                .compose(ObservableTransformers.defaultSchedulersSingle())
                .doOnSubscribe {
                    isLoading = true
                }
                .doOnError {
                    isLoading = false
                }
                .subscribeBy(
                        onSuccess = {
                            canAddAccount = false
                            isLoading = false

                            val recoverySeed = it.recoverySeed
                            Navigator.openRecoverySeedSaving(this,
                                    SAVE_SEED_REQUEST,
                                    recoverySeed
                            )
                            recoverySeed.erase()
                        },
                        onError = {
                            it.printStackTrace()
                            handleAddError(it)
                        })
                .addTo(compositeDisposable)
    }

    private fun handleAddError(error: Throwable) {
        when (error) {
            is EmailAlreadyTakenException ->
                email_edit_text.setErrorAndFocus(R.string.error_email_already_taken)
            else ->
                errorHandlerFactory.getDefault().handle(error)
        }
        updateAddAccountAvailability()
    }

    private fun tryOpenQrScanner() {
        cameraPermission.check(this) {
            QrScannerUtil.openScanner(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraPermission.handlePermissionResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (setOf(requestCode, resultCode)) {
            setOf(SAVE_SEED_REQUEST, Activity.RESULT_OK) -> onSuccessfulCreated()
            setOf(RECOVER_REQUEST, Activity.RESULT_OK) -> onRecoverSuccess()
            else -> {
                QrScannerUtil.getStringFromResult(requestCode, resultCode, data).also {
                    try {
                        val api = JSONObject(it).getString("api").addSlashIfNeed()
                        onNetworkUrlObtained(api)
                        return@also
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (it != null) {
                        toastManager.short(R.string.error_unknown_qr)
                    }
                }
            }
        }
    }

    private fun onNetworkUrlObtained(networkUrl: String) {
        HttpUrl.parse(networkUrl)?.also { httpUrl ->
            this.networkUrl = httpUrl.toString()
            network_edit_text.setText(httpUrl.host())
        }
    }

    private fun onRecoverSuccess() {
        finish()
    }

    private fun onSuccessfulCreated() {
        toastManager.long(getString(R.string.account_created))
        finish()
    }
}
