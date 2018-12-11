package org.tokend.authenticator.security.userkey.pin

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.view.View
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.activity_pin_code.*
import kotlinx.android.synthetic.main.include_fingerprint_field_hint.*
import org.tokend.authenticator.R
import org.tokend.authenticator.security.userkey.view.UserKeyActivity
import org.tokend.authenticator.util.extensions.getChars
import org.tokend.authenticator.view.util.AnimationUtil
import org.tokend.authenticator.view.util.input.SimpleTextWatcher
import org.tokend.authenticator.view.util.input.SoftInputUtil
import java.nio.CharBuffer

open class PinCodeActivity : UserKeyActivity() {

    override val errorMessage: String
        get() = getString(R.string.invalid_pin)
    override val entryEditText: MaterialEditText
        get() = pin_code_edit_text

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_pin_code)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initFields()
    }

    private fun initFields() {
        pin_code_edit_text.filters = arrayOf(
                InputFilter.LengthFilter(PIN_CODE_LENGTH)
        )

        pin_code_edit_text.addTextChangedListener(
                object : SimpleTextWatcher() {
                    override fun afterTextChanged(s: Editable?) {
                        if (s != null && s.length == PIN_CODE_LENGTH) {
                            onPinCodeEntered(s.getChars())
                        }
                    }
                }
        )
    }

    protected open fun onPinCodeEntered(pin: CharArray) {
        SoftInputUtil.hideSoftInput(this)
        finishWithKey(pin)
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

        pin_code_edit_text.text.apply {
            replace(0, length, CharBuffer.wrap(savedPinCode))
        }
    }

    protected open fun showFingerprintHint() {
        AnimationUtil.fadeInView(fingerprint_hint_layout)
    }

    protected open fun hideFingerprintHint() {
        fingerprint_hint_layout.visibility = View.GONE
    }
    // endregion

    companion object {
        protected const val PIN_CODE_LENGTH = 4
    }
}
