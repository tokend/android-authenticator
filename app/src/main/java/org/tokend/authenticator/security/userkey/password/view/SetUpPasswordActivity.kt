package org.tokend.authenticator.security.userkey.password.view

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_password.*
import org.jetbrains.anko.find
import org.jetbrains.anko.textColor
import org.jetbrains.anko.vibrator
import org.tokend.authenticator.R
import org.tokend.authenticator.util.extensions.getChars
import org.tokend.authenticator.util.extensions.hasError
import org.tokend.authenticator.util.validator.PasswordValidator
import org.tokend.crypto.ecdsa.erase
import java.nio.CharBuffer

class SetUpPasswordActivity : PasswordActivity() {
    private var isFirstEnter = true
    private var enteredPassword = charArrayOf()

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        super.onCreateAllowed(savedInstanceState)
        switchToFirstEnter()
    }

    override fun onPasswordEntered() {
        val password = password_edit_text.text.getChars()

        if (isFirstEnter) {
            enteredPassword = password
            switchToConfirmationEnter()
        } else {
            if (enteredPassword.contentEquals(password)) {
                saveUserKeyIfAvailable(password)
                super.onPasswordEntered()
            } else {
                showConfirmationError()
                switchToFirstEnter()
                password.erase()
                enteredPassword.erase()
            }
        }
    }

    private fun switchToFirstEnter() {
        isFirstEnter = true
        resetPasswordInput()
        password_label_text_view.text = getString(R.string.enter_new_password)
        updateContinueAvailability()
    }

    private fun switchToConfirmationEnter() {
        isFirstEnter = false
        resetPasswordInput()
        password_label_text_view.text = getString(R.string.confirm_password)
        updateContinueAvailability()
    }

    private fun resetPasswordInput() {
        password_edit_text.text.clear()
        focus_grabber.requestFocus()
        password_edit_text.post { password_edit_text.requestFocus() }
    }

    private fun showConfirmationError() {
        try {
            vibrator.vibrate(longArrayOf(0, 100), -1)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Snackbar.make(
                password_edit_text,
                R.string.error_password_confirmation_mismatch,
                Snackbar.LENGTH_SHORT
        )
                .also {
                    it.view.find<TextView>(android.support.design.R.id.snackbar_text)
                            .textColor = Color.WHITE
                }
                .show()
    }

    private fun validatePasswordStrength() {
        val password = password_edit_text.text.getChars()
        if (password.isEmpty() || PasswordValidator.isValid(CharBuffer.wrap(password))) {
            password_edit_text.error = null
        } else {
            password_edit_text.error = getString(R.string.error_weak_password)
        }

        password.erase()
    }

    override fun updateContinueAvailability() {
        if (isFirstEnter) {
            validatePasswordStrength()
        } else {
            password_edit_text.error = null
        }

        canContinue = !password_edit_text.hasError() && password_edit_text.text.isNotEmpty()
    }

    override fun requestFingerprintAuthIfAvailable() {
        hideFingerprintHint()
    }

    override fun onDestroy() {
        super.onDestroy()
        enteredPassword.erase()
    }
}