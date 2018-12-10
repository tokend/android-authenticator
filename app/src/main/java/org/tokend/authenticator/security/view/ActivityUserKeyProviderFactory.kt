package org.tokend.authenticator.security.view

import android.app.Activity
import android.app.Fragment
import org.tokend.authenticator.security.logic.EnvSecurityStatus
import org.tokend.authenticator.security.logic.UserKeyType
import org.tokend.authenticator.security.logic.persistence.UserKeyTypeStorage
import org.tokend.authenticator.security.view.activities.PasswordActivity
import org.tokend.authenticator.security.view.activities.PinCodeActivity
import org.tokend.authenticator.security.view.activities.SetUpPasswordActivity
import org.tokend.authenticator.security.view.activities.SetUpPinCodeActivity

class ActivityUserKeyProviderFactory(
        envSecurityStatus: EnvSecurityStatus,
        userKeyTypeStorage: UserKeyTypeStorage
) {
    private val keyType =
            userKeyTypeStorage.load()
                    ?: getUserKeyTypeForSecurityStatus(envSecurityStatus)
                            .also { userKeyTypeStorage.save(it) }

    private fun getUserKeyTypeForSecurityStatus(securityStatus: EnvSecurityStatus): UserKeyType {
        return if (securityStatus == EnvSecurityStatus.NORMAL)
            UserKeyType.PIN
        else
            UserKeyType.PASSWORD
    }

    private fun getForRequest(parentActivity: Activity?,
                              parentFragment: Fragment?): ActivityUserKeyProvider {
        val activity =
                when (keyType) {
                    UserKeyType.PASSWORD -> PasswordActivity::class.java
                    UserKeyType.PIN -> PinCodeActivity::class.java
                }

        return ActivityUserKeyProvider(
                activity,
                parentActivity,
                parentFragment
        )
    }

    fun getForRequest(parentActivity: Activity?): ActivityUserKeyProvider {
        return getForRequest(parentActivity, null)
    }

    fun getForRequest(parentFragment: Fragment?): ActivityUserKeyProvider {
        return getForRequest(null, parentFragment)
    }

    private fun getForSetUp(parentActivity: Activity?,
                            parentFragment: Fragment?): ActivityUserKeyProvider {
        val activity =
                when (keyType) {
                    UserKeyType.PASSWORD -> SetUpPasswordActivity::class.java
                    UserKeyType.PIN -> SetUpPinCodeActivity::class.java
                }

        return ActivityUserKeyProvider(
                activity,
                parentActivity,
                parentFragment
        )
    }

    fun getForSetUp(parentActivity: Activity?): ActivityUserKeyProvider {
        return getForSetUp(parentActivity, null)
    }

    fun getForSetUp(parentFragment: Fragment?): ActivityUserKeyProvider {
        return getForSetUp(null, parentFragment)
    }
}