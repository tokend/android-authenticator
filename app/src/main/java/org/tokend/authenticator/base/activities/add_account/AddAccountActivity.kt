package org.tokend.authenticator.base.activities.add_account

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_add_account.*
import kotlinx.android.synthetic.main.layout_network_field.*
import kotlinx.android.synthetic.main.layout_progress.*
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.logic.CreateAccountUseCase
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.base.extensions.addSlashIfNeed
import org.tokend.authenticator.base.extensions.hasError
import org.tokend.authenticator.base.extensions.setErrorAndFocus
import org.tokend.authenticator.base.logic.encryption.TmpEncryptionKeyProvider
import org.tokend.authenticator.base.util.ObservableTransformers
import org.tokend.authenticator.base.util.Permission
import org.tokend.authenticator.base.util.QrScannerUtil
import org.tokend.authenticator.base.util.SoftInputUtil
import org.tokend.authenticator.base.util.validators.EmailValidator
import org.tokend.authenticator.base.view.util.LoadingIndicatorManager
import org.tokend.authenticator.base.view.util.SimpleTextWatcher

class AddAccountActivity : BaseActivity() {

    private lateinit var networkUrl: String
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

        CreateAccountUseCase(
            networkUrl,
            email,
            dataCipher,
            TmpEncryptionKeyProvider(),
            accountsRepository)
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
                        finish()
                    },
                    onError = {
                        it.printStackTrace()
                        handleAddError(it)
                    })
    }

    private fun handleAddError(error: Throwable) {

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

        QrScannerUtil.getStringFromResult(requestCode, resultCode, data)?.also {

            val api = JSONObject(it).getString("api").addSlashIfNeed()
            HttpUrl.parse(api).also { httpUrl ->
                networkUrl = httpUrl.toString()
                network_edit_text.setText(httpUrl.toString())
            }
        }
    }
}
