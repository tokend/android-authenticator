package org.tokend.authenticator.security.view

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import io.reactivex.Maybe
import io.reactivex.subjects.MaybeSubject
import org.tokend.authenticator.security.logic.UserKeyProvider
import java.util.*

class ActivityUserKeyProvider(
        private val activityClass: Class<out UserKeyActivity>,
        private val parentActivity: Activity? = null,
        private val parentFragment: Fragment? = null
) : UserKeyProvider {
    private var resultSubject: MaybeSubject<CharArray>? = null
    private var requestCode = 0

    override fun getUserKey(): Maybe<CharArray> {
        return resultSubject ?: (MaybeSubject.create<CharArray>()
                .also {
                    resultSubject = it
                    openInputActivity()
                })
    }

    private fun getRequestCode(renew: Boolean = false): Int {
        if (renew) {
            requestCode = Random().nextInt() and 0xffff
        }
        return requestCode
    }

    private fun openInputActivity() {
        if (parentActivity != null) {
            parentActivity.startActivityForResult(
                    Intent(parentActivity, activityClass),
                    getRequestCode(true)
            )
        } else parentFragment?.startActivityForResult(
                Intent(parentFragment.activity, activityClass),
                getRequestCode(true)
        )
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != getRequestCode()) {
            return false
        }

        val key = data?.getCharArrayExtra(UserKeyActivity.USER_KEY_RESULT_EXTRA)

        return if (resultCode == Activity.RESULT_CANCELED || key == null) {
            cancelInput()
            true
        } else {
            postKey(key)
            true
        }
    }

    private fun cancelInput() {
        resultSubject?.onComplete()
    }

    private fun postKey(key: CharArray) {
        resultSubject?.onSuccess(key)
    }
}