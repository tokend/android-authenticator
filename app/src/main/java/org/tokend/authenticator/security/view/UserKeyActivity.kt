package org.tokend.authenticator.security.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.View
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.include_punishment_timer_holder.*
import org.tokend.authenticator.R
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.base.extensions.setErrorAndFocus
import org.tokend.authenticator.base.util.SoftInputUtil
import org.tokend.crypto.ecdsa.erase
import org.tokend.wallet.utils.toByteArray
import org.tokend.wallet.utils.toCharArray
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

abstract class UserKeyActivity : BaseActivity(
        canShowUserKeyRequest = false
) {
    abstract val errorMessage: String
    abstract val entryEditText: MaterialEditText

    protected val isRetry
        get() = intent.getBooleanExtra(IS_RETRY_EXTRA, false)

    protected var timerTask: TimerTask? = null

    protected val isPunished
        get() = !punishmentTimer.isExpired()

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
        entryEditText.isFocusableInTouchMode = false
        if (!punishmentTimer.isExpired()) {
            supportActionBar?.hide()
            SoftInputUtil.hideSoftInput(this)
            timer_holder.visibility = View.VISIBLE
            val timerTemplate = getString(R.string.template_timer)

            var timeLeft = punishmentTimer.timeLeft()
            timer_text.text = timerTemplate.format(timeLeft.toString())

            timerTask = Timer().scheduleAtFixedRate(1000, 1000) {
                runOnUiThread {
                    timeLeft--
                    timer_text.text = timerTemplate.format(timeLeft.toString())
                    if (timeLeft == 0) {
                        focusOnEditText()
                        requestFingerprintAuthIfAvailable()
                        cancel()
                    }
                }
            }

        } else {
            focusOnEditText()
            requestFingerprintAuthIfAvailable()
        }
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
                && fingerprintUtil.canFingerPrintBeUsed()
                && !isPunished) {
            requestFingerprintAuth()
        }
    }

    protected open fun requestFingerprintAuth() {
        fingerprintUtil.requestAuth(
                onSuccess = this::onFingerprintAuthSuccess,
                onError = this::onFingerprintAuthMessage,
                onHelp = this::onFingerprintAuthMessage
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
        timerTask?.cancel()
        timerTask = null
    }

    companion object {
        const val USER_KEY_RESULT_EXTRA = "user_key"
        const val IS_RETRY_EXTRA = "is_retry"

        protected const val USER_KEY_STORAGE_KEY = "pin"
    }
}