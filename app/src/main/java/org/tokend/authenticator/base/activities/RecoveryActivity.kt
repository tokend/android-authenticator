package org.tokend.authenticator.base.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import com.rengwuxian.materialedittext.MaterialEditText
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_recovery.*
import kotlinx.android.synthetic.main.layout_network_field.*
import kotlinx.android.synthetic.main.layout_progress.*
import okhttp3.HttpUrl
import org.json.JSONObject
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.logic.RecoverAccountUseCase
import org.tokend.authenticator.base.extensions.addSlashIfNeed
import org.tokend.authenticator.base.extensions.getChars
import org.tokend.authenticator.base.extensions.getStringExtra
import org.tokend.authenticator.base.extensions.setErrorAndFocus
import org.tokend.authenticator.base.util.*
import org.tokend.authenticator.base.view.util.LoadingIndicatorManager
import org.tokend.authenticator.base.view.util.SimpleTextWatcher
import org.tokend.sdk.federation.EmailNotVerifiedException
import org.tokend.sdk.federation.InvalidCredentialsException
import org.tokend.wallet.Base32Check

class RecoveryActivity : BaseActivity() {

    companion object {
        const val EXTRA_EMAIL = "email"
        const val EXTRA_API = "api"
    }

    private val loadingIndicator = LoadingIndicatorManager(
            showLoading = { progress.show() },
            hideLoading = { progress.hide() }
    )
    private var isLoading: Boolean = false
        set(value) {
            field = value
            loadingIndicator.setLoading(value, "main")
            updateRecoveryAvailability()
        }

    private var canRecover: Boolean = false
        set(value) {
            field = value
            recovery_button.isEnabled = value
        }

    private val cameraPermission = Permission(Manifest.permission.CAMERA, 404)

    private val api: String
        get() = intent.getStringExtra(EXTRA_API, "")

    private val email: String
        get() = intent.getStringExtra(EXTRA_EMAIL, "")

    private lateinit var networkHttpUrl: HttpUrl

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_recovery)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initFields()
        initButtons()

        canRecover = false
    }

    private fun initFields() {

        if (api.isNotEmpty()) {
            networkHttpUrl = HttpUrl.parse(api)
            network_edit_text.setText(networkHttpUrl.host())
        }

        if (email.isNotEmpty()) {
            email_edit_text.setText(email)
            seed_edit_text.requestFocus()
        }

        addSimpleTextWatcher(network_edit_text)
        addSimpleTextWatcher(email_edit_text)
        addSimpleTextWatcher(seed_edit_text)
    }

    private fun initButtons() {
        scan_qr_button.setOnClickListener {
            tryOpenQrScanner()
        }

        recovery_button.setOnClickListener {
            tryToRecover()
        }
    }

    private fun updateRecoveryAvailability() {
        canRecover = !isLoading
                && network_edit_text.text.isNotBlank()
                && email_edit_text.text.isNotBlank()
                && seed_edit_text.text.isNotBlank()
                && email_edit_text.error == null
                && seed_edit_text.error == null
    }

    private fun addSimpleTextWatcher(editText: MaterialEditText) {
        editText.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                editText.error = null
                updateRecoveryAvailability()
            }
        })
    }

    private fun tryToRecover() {
        checkSeed()
        updateRecoveryAvailability()
        if (canRecover) {
            SoftInputUtil.hideSoftInput(this)
            recover()
        }
    }

    private fun checkSeed() {
        val seedChars = seed_edit_text.text.getChars()
        if (!Base32Check.isValid(Base32Check.VersionByte.SEED, seedChars)) {
            seed_edit_text.setErrorAndFocus(R.string.error_invalid_seed)
        } else {
            seed_edit_text.error = null
        }
        seedChars.fill('0')
    }

    private fun recover() {
        val keyProvider = encryptionKeyProvider
        val email = email_edit_text.text.toString()
        val recoverySeed = seed_edit_text.text.getChars()

        RecoverAccountUseCase(
                networkHttpUrl.toString(),
                email,
                recoverySeed,
                dataCipher,
                keyProvider,
                accountsRepository,
                signersRepositoryProvider,
                apiFactory
        )
                .perform()
                .compose(ObservableTransformers.defaultSchedulersCompletable())
                .doOnSubscribe {
                    isLoading = true
                }
                .doOnTerminate {
                    isLoading = false
                    recoverySeed.fill('0')
                }
                .subscribeBy(
                        onError = {
                            it.printStackTrace()
                            handleRecoveryError(it)
                        },
                        onComplete = {
                            setResult(Activity.RESULT_OK)
                            finish()
                        })
                .addTo(compositeDisposable)
    }

    private fun handleRecoveryError(error: Throwable) {
        error.printStackTrace()
        when (error) {
            is InvalidCredentialsException ->
                when (error.credential) {
                    InvalidCredentialsException.Credential.EMAIL ->
                        email_edit_text.setErrorAndFocus(R.string.error_invalid_email)
                    InvalidCredentialsException.Credential.PASSWORD ->
                        seed_edit_text.setErrorAndFocus(R.string.error_invalid_seed)
                    is EmailNotVerifiedException ->
                        email_edit_text.setErrorAndFocus(R.string.error_email_not_verified)
                    else -> errorHandlerFactory.getDefault().handle(error)
                }
            else -> errorHandlerFactory.getDefault().handle(error)
        }
        updateRecoveryAvailability()
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

        QrScannerUtil.getStringFromResult(requestCode, resultCode, data).also {
            try {
                val api = JSONObject(it).getString("api").addSlashIfNeed()
                networkHttpUrl = HttpUrl.parse(api).also { httpUrl ->
                    network_edit_text.setText(httpUrl.host())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (it != null) {
                ToastManager(this).short(R.string.error_unknown_qr)
            }
        }
    }
}
