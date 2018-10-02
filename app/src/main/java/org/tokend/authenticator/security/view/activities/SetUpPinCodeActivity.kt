package org.tokend.authenticator.security.view.activities

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_pin_code.*
import org.jetbrains.anko.find
import org.jetbrains.anko.textColor
import org.jetbrains.anko.vibrator
import org.tokend.authenticator.R

class SetUpPinCodeActivity : PinCodeActivity() {
    private var isFirstEnter = true
    private var enteredPin = charArrayOf()

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        super.onCreateAllowed(savedInstanceState)
        switchToFirstEnter()
    }

    override fun onPinCodeEntered(pin: CharArray) {
        if (isFirstEnter) {
            enteredPin = pin
            switchToConfirmationEnter()
        } else {
            if (enteredPin.contentEquals(pin)) {
                super.onPinCodeEntered(pin)
            } else {
                showConfirmationError()
                switchToFirstEnter()
            }
        }
    }

    private fun switchToFirstEnter() {
        isFirstEnter = true
        pin_code_edit_text.text.clear()
        pin_code_label_text_view.text = getString(R.string.enter_pin_code)
    }

    private fun switchToConfirmationEnter() {
        isFirstEnter = false
        pin_code_edit_text.text.clear()
        pin_code_label_text_view.text = getString(R.string.confirm_pin_code)
    }

    private fun showConfirmationError() {
        try {
            vibrator.vibrate(longArrayOf(0, 100), -1)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Snackbar.make(
                pin_code_edit_text,
                R.string.error_pin_confirmation_mismatch,
                Snackbar.LENGTH_SHORT
        )
                .also {
                    it.view.find<TextView>(android.support.design.R.id.snackbar_text)
                            .textColor = Color.WHITE
                }
                .show()
    }
}