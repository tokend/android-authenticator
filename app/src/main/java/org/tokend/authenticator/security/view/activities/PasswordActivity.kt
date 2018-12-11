package org.tokend.authenticator.security.view.activities

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.View
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.activity_password.*
import kotlinx.android.synthetic.main.include_fingerprint_field_hint.*
import org.tokend.authenticator.R
import org.tokend.authenticator.base.extensions.getChars
import org.tokend.authenticator.base.extensions.onEditorAction
import org.tokend.authenticator.base.view.util.AnimationUtil
import org.tokend.authenticator.base.view.util.SimpleTextWatcher
import org.tokend.authenticator.security.view.UserKeyActivity
import java.nio.CharBuffer

open class PasswordActivity : UserKeyActivity() {

    override val errorMessage: String
        get() = getString(R.string.error_invalid_password)
    override val entryEditText: MaterialEditText
        get() = password_edit_text

    protected var canContinue: Boolean = false
        set(value) {
            field = value
            continue_button.isEnabled = value
        }

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_password)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initFields()
        initButtons()

        updateContinueAvailability()
    }

    private fun initFields() {
        password_edit_text.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                updateContinueAvailability()
            }
        })

        password_edit_text.onEditorAction {
            if (canContinue) {
                onPasswordEntered()
            }
        }
    }

    private fun initButtons() {
        continue_button.setOnClickListener {
            onPasswordEntered()
        }
    }

    protected open fun updateContinueAvailability() {
        canContinue = password_edit_text.text.isNotEmpty()
    }

    protected open fun onPasswordEntered() {
        val password = password_edit_text.text.getChars()
        onUserKeyEntered(password)
    }

    // region Fingerprint
    override fun requestFingerprintAuth() {
        super.requestFingerprintAuth()
        showFingerprintHint()
    }

    override fun onFingerprintAuthSuccess() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        val savedPinCode = loadUserKey() ?: return

        password_edit_text.text.apply {
            replace(0, length, CharBuffer.wrap(savedPinCode))
        }

        onPasswordEntered()
    }

    protected open fun showFingerprintHint() {
        AnimationUtil.fadeInView(fingerprint_hint_layout)
    }

    protected open fun hideFingerprintHint() {
        fingerprint_hint_layout.visibility = View.GONE
    }
    // endregion
}