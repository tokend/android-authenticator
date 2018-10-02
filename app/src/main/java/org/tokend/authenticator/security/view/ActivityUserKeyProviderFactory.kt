package org.tokend.authenticator.security.view

import android.app.Activity
import android.app.Fragment
import org.tokend.authenticator.security.view.activities.PinCodeActivity
import org.tokend.authenticator.security.view.activities.SetUpPinCodeActivity

class ActivityUserKeyProviderFactory private constructor(
        private val parentActivity: Activity?,
        private val parentFragment: Fragment?
) {
    constructor(parentActivity: Activity) : this(parentActivity, null)

    constructor(parentFragment: Fragment) : this(null, parentFragment)

    fun regularPinCode(): ActivityUserKeyProvider {
        return ActivityUserKeyProvider(
                PinCodeActivity::class.java,
                parentActivity,
                parentFragment
        )
    }

    fun setUpPinCode(): ActivityUserKeyProvider {
        return ActivityUserKeyProvider(
                SetUpPinCodeActivity::class.java,
                parentActivity,
                parentFragment
        )
    }
}