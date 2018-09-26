package org.tokend.authenticator.base.activities.add_account

import android.os.Bundle
import android.text.Editable
import kotlinx.android.synthetic.main.activity_add_account.*
import kotlinx.android.synthetic.main.layout_network_field.*
import kotlinx.android.synthetic.main.layout_progress.*
import org.tokend.authenticator.R
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.base.extensions.hasError
import org.tokend.authenticator.base.extensions.setErrorAndFocus
import org.tokend.authenticator.base.util.SoftInputUtil
import org.tokend.authenticator.base.util.validators.EmailValidator
import org.tokend.authenticator.base.view.util.LoadingIndicatorManager
import org.tokend.authenticator.base.view.util.SimpleTextWatcher

class AddAccountActivity : BaseActivity() {

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
        title = getString(R.string.add_account)
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
            //Add qr_scan_request
        }

        add_account_button.setOnClickListener {
            tryToAddAccount()
        }

        recovery_button.setOnClickListener {
            //Start recovery flow
        }
    }

    private fun tryToAddAccount() {
        when {
            !EmailValidator.isValid(email_edit_text.text) ->
                email_edit_text.setErrorAndFocus("Invalid Email")
            else -> {
                SoftInputUtil.hideSoftInput(this)

            }
        }
        updateAddAccountAvailability()
    }
}
