package org.tokend.authenticator.auth.view.confirmation

import android.app.Activity
import java.text.DateFormat

class AuthRequestConfirmationDialogFactory(
        private val dateFormat: DateFormat
) {
    fun getForActivity(activity: Activity): AuthRequestConfirmationDialog {
        return AuthRequestConfirmationDialog(activity, dateFormat)
    }
}