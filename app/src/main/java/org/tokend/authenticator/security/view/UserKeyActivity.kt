package org.tokend.authenticator.security.view

import android.app.Activity
import android.content.Intent
import org.tokend.authenticator.base.activities.BaseActivity

abstract class UserKeyActivity : BaseActivity(
        canShowUserKeyRequest = false
) {
    protected open fun finishWithKey(key: CharArray) {
        setResult(
                Activity.RESULT_OK,
                Intent().putExtra(USER_KEY_RESULT_EXTRA, key)
        )
        finish()
    }

    protected open fun finishWithCancellation() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishWithCancellation()
    }

    companion object {
        const val USER_KEY_RESULT_EXTRA = "user_key"
    }
}