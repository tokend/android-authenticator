package org.tokend.authenticator.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import org.jetbrains.anko.intentFor
import org.tokend.authenticator.R
import org.tokend.authenticator.auth.request.view.AuthorizeAppActivity
import org.tokend.authenticator.accounts.add.recovery.view.RecoveryActivity
import org.tokend.authenticator.accounts.add.view.RecoverySeedActivity
import org.tokend.authenticator.accounts.add.view.AddAccountActivity
import org.tokend.authenticator.accounts.info.view.GeneralAccountInfoActivity
import org.tokend.authenticator.settings.view.SettingsActivity


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

    fun openRecoverySeedSaving(activity: Activity, requestCode: Int, seed: CharArray) {
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

    fun openSettings(activity: Activity) {
        activity.startActivity(Intent(activity, SettingsActivity::class.java))
    }
}