package org.tokend.authenticator.auth.request.confirmation.view

import android.app.Activity
import java.text.DateFormat

class AuthRequestConfirmationDialogFactory(
        private val dateFormat: DateFormat
) {
    fun getForActivity(activity: Activity): AuthRequestConfirmationDialog {
        return AuthRequestConfirmationDialog(activity, dateFormat)
    }
}