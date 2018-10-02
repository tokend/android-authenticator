package org.tokend.authenticator.base.util

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.tokend.authenticator.R
import org.tokend.authenticator.auth.view.AuthorizeAppActivity
import org.tokend.authenticator.base.activities.general_account_info.GeneralAccountInfoActivity
import org.tokend.authenticator.base.activities.RecoveryActivity
import org.tokend.authenticator.base.activities.RecoverySeedActivity
import org.tokend.authenticator.base.activities.account_list.AccountsListActivity
import org.tokend.authenticator.base.activities.add_account.AddAccountActivity


/**
 * Performs transitions between screens.
 * 'open-' will open related screen as a child.<p>
 * 'to-' will open related screen and finish current.
 */
object Navigator {

    private fun fadeOut(activity: Activity) {
        ActivityCompat.finishAfterTransition(activity)
        activity.overridePendingTransition(0, R.anim.activity_fade_out)
        activity.finish()
    }

    private fun createTransitionBundle(activity: Activity,
                                       vararg pairs: Pair<View?, String>): Bundle {
        val sharedViews = arrayListOf<android.support.v4.util.Pair<View, String>>()

        pairs.forEach {
            val view = it.first
            if (view != null) {
                sharedViews.add(android.support.v4.util.Pair(view, it.second))
            }
        }

        return if (sharedViews.isEmpty()) {
            Bundle.EMPTY
        } else {
            ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                    *sharedViews.toTypedArray()).toBundle() ?: Bundle.EMPTY
        }
    }

    fun openAddAccount(activity: Activity,
                       networkUrl: String? = null) {
        activity.startActivity(activity.intentFor<AddAccountActivity>(
                AddAccountActivity.NETWORK_URL_EXTRA to networkUrl
        ))
    }

    fun openRecoverySeedSaving(activity: Activity, requestCode: Int, seed: String) {
        activity.startActivityForResult(activity.intentFor<RecoverySeedActivity>(
                RecoverySeedActivity.SEED_EXTRA to seed
        ), requestCode)
    }

    fun openRecoveryActivity(activity: Activity, api: String, email: String, requestCode: Int) {
        activity.startActivityForResult(activity.intentFor<RecoveryActivity>(
                RecoveryActivity.EXTRA_API to api,
                RecoveryActivity.EXTRA_EMAIL to email
        ), requestCode)
    }

    fun openRecoveryActivity(activity: Activity, api: String, email: String) {
        activity.startActivity(activity.intentFor<RecoveryActivity>(
                RecoveryActivity.EXTRA_API to api,
                RecoveryActivity.EXTRA_EMAIL to email
        ))
    }

    fun openAuthorizeAppActivity(activity: Activity,
                                 authUri: Uri? = null) {
        activity.startActivity(
                activity.intentFor<AuthorizeAppActivity>()
                        .setData(authUri)
        )
    }

    fun openGeneralAccountInfo(activity: Activity, uid: Long) {
        activity.startActivity(activity.intentFor<GeneralAccountInfoActivity>(
                GeneralAccountInfoActivity.EXTRA_UID to uid
        ))
    }
}