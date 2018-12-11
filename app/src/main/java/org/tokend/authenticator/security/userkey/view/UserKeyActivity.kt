package org.tokend.authenticator.security.userkey.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.View
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.include_punishment_timer_holder.*
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.security.userkey.punishment.view.PunishmentTimerView
import org.tokend.authenticator.util.extensions.setErrorAndFocus
import org.tokend.authenticator.view.util.input.SoftInputUtil
import org.tokend.crypto.ecdsa.erase
import org.tokend.wallet.utils.toByteArray
import org.tokend.wallet.utils.toCharArray
import java.lang.ref.WeakReference

abstract class UserKeyActivity : BaseActivity(
        canShowUserKeyRequest = false
) {
    abstract val errorMessage: String
    abstract val entryEditText: MaterialEditText

    protected val isRetry
        get() = intent.getBooleanExtra(IS_RETRY_EXTRA, false)

    protected lateinit var timerView: PunishmentTimerView

    protected open fun finishWithKey(key: CharArray) {
        setResult(
                Activity.RESULT_OK,
                Intent().putExtra(USER_KEY_RESULT_EXTRA, key)
        )
        finish()
    }

    protected open fun onUserKeyEntered(key: CharArray) {
        SoftInputUtil.hideSoftInput(this)
        finishWithKey(key)
    }

    protected open fun finishWithCancellation() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishWithCancellation()
    }

    protected open fun initTimerLayout() {
        timerView = PunishmentTimerView(this, punishmentTimer)
        timerView.showTimer(
                onTimerStart = {
                    entryEditText.isFocusableInTouchMode = false
                    supportActionBar?.hide()
                },
                onTimerExpired = {
                    focusOnEditText()
                    requestFingerprintAuthIfAvailable()
                })
    }

    protected open fun focusOnEditText() {
        timer_holder.visibility = View.GONE
        supportActionBar?.show()

        entryEditText.isFocusableInTouchMode = true
        if (isRetry) {
            entryEditText.setErrorAndFocus(errorMessage)
        } else {
            entryEditText.requestFocus()
            SoftInputUtil.showSoftInputOnView(entryEditText)
        }
    }

    // region Fingerprint
    protected open fun requestFingerprintAuthIfAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && secureStorage.hasSecretKey(USER_KEY_STORAGE_KEY)
                && fingerprintUtil.canFingerPrintBeUsed()) {
            requestFingerprintAuth()
        }
    }

    protected open fun requestFingerprintAuth() {
        val weakThis = WeakReference(this)
        fingerprintUtil.requestAuth(
                onSuccess = { weakThis.get()?.onFingerprintAuthSuccess() },
                onError = { weakThis.get()?.onFingerprintAuthMessage(it) },
                onHelp = { weakThis.get()?.onFingerprintAuthMessage(it) }
        )
    }

    protected open fun cancelFingerprintAuth() {
        fingerprintUtil.cancelAuth()
    }

    protected open fun onFingerprintAuthSuccess() {}

    protected open fun onFingerprintAuthMessage(message: String?) {
        message ?: return
        toastManager.short(message)
    }
    // endregion

    // region User key storage
    @RequiresApi(Build.VERSION_CODES.M)
    protected open fun loadUserKey(): CharArray? {
        val bytes = secureStorage.load(USER_KEY_STORAGE_KEY)
        return bytes?.toCharArray()?.also { bytes.erase() }
    }

    protected open fun saveUserKeyIfAvailable(key: CharArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val keyBytes = key.toByteArray()
            secureStorage.save(keyBytes, USER_KEY_STORAGE_KEY)
            keyBytes.erase()
        }
    }
    // endregion

    override fun onResume() {
        super.onResume()
        initTimerLayout()
    }

    override fun onPause() {
        super.onPause()
        cancelFingerprintAuth()
        timerView.cancelTimer()
    }

    companion object {
        const val USER_KEY_RESULT_EXTRA = "user_key"
        const val IS_RETRY_EXTRA = "is_retry"

        protected const val USER_KEY_STORAGE_KEY = "pin"
    }
}