package org.tokend.authenticator.accounts.add.view

import android.app.Activity
import android.content.ClipData
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputType
import kotlinx.android.synthetic.main.activity_recovery_seed.*
import org.jetbrains.anko.clipboardManager
import org.jetbrains.anko.dip
import org.tokend.authenticator.R
import org.tokend.authenticator.util.extensions.getChars
import org.tokend.authenticator.util.extensions.onEditorAction
import org.tokend.authenticator.view.util.ToastManager
import org.tokend.authenticator.view.util.input.SimpleTextWatcher
import org.tokend.crypto.ecdsa.erase
import java.nio.CharBuffer


class RecoverySeedActivity : AppCompatActivity() {
    companion object {
        const val SEED_EXTRA = "seed"
    }

    private val seed: CharArray?
        get() = intent.getCharArrayExtra(SEED_EXTRA)

    private var canContinue: Boolean = false
        set(value) {
            field = value
            continue_button.isEnabled = value
        }

    private var seedsMatch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (seed == null) {
            finish()
            return
        }

        setContentView(R.layout.activity_recovery_seed)

        initFields()
        initButtons()

        canContinue = false
    }

    // region Init
    private fun initFields() {
        seed_edit_text.apply {
            setText(CharBuffer.wrap(seed))
            setSingleLine()
            setPaddings(0, 0, dip(40), 0)
            inputType = InputType.TYPE_NULL
            setTextIsSelectable(true)
        }

        confirm_seed_edit_text.apply {
            addTextChangedListener(object : SimpleTextWatcher() {
                override fun afterTextChanged(s: Editable?) {
                    checkSeedsMatch()
                    updateContinueAvailability()
                }
            })
            requestFocus()
            onEditorAction {
                if (canContinue) {
                    finishWithSuccess()
                }
            }
        }
    }

    private fun initButtons() {
        continue_button.setOnClickListener {
            finishWithSuccess()
        }

        copy_button.setOnClickListener {
            clipboardManager.primaryClip = ClipData.newPlainText(
                    getString(R.string.recovery_seed),
                    CharBuffer.wrap(seed)
            )
            ToastManager(this).short(R.string.seed_copied)
        }
    }
    // endregion

    private fun checkSeedsMatch() {
        val confirmation = confirm_seed_edit_text.text.getChars()
        seedsMatch = seed?.contentEquals(confirmation) ?: true
        confirmation.erase()

        if (!seedsMatch && !confirm_seed_edit_text.text.isEmpty()) {
            confirm_seed_edit_text.error = getString(R.string.error_seed_mismatch)
        } else {
            confirm_seed_edit_text.error = null
        }
    }

    private fun updateContinueAvailability() {
        canContinue = seedsMatch
    }

    private fun finishWithSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun displayConfirmationIsRequiredDialog() {
        AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.save_recovery_seed)
                .setMessage(R.string.seed_not_copied_confirmation)
                .setPositiveButton(R.string.i_understand, null)
                .show()
    }

    override fun onBackPressed() {
        val primaryClipText = clipboardManager.primaryClip?.getItemAt(0)?.text

        if (seedsMatch
                || primaryClipText?.equals(CharBuffer.wrap(seed)) == true) {
            finishWithSuccess()
        } else {
            displayConfirmationIsRequiredDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        seed?.erase()
    }
}
