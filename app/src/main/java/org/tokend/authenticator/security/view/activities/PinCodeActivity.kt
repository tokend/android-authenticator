package org.tokend.authenticator.security.view.activities

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import kotlinx.android.synthetic.main.activity_pin_code.*
import org.tokend.authenticator.R
import org.tokend.authenticator.base.extensions.getChars
import org.tokend.authenticator.security.view.UserKeyActivity
import org.tokend.authenticator.base.view.util.SimpleTextWatcher

open class PinCodeActivity : UserKeyActivity() {
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
        finishWithKey(pin)
    }

    companion object {
        private const val PIN_CODE_LENGTH = 4
    }
}
