package org.tokend.authenticator.auth.view.confirmation

import android.content.Context
import android.support.v7.app.AlertDialog
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.jetbrains.anko.runOnUiThread
import org.tokend.authenticator.R
import org.tokend.authenticator.auth.request.AuthRequest
import org.tokend.authenticator.auth.request.AuthRequestConfirmationProvider
import org.tokend.authenticator.auth.view.permission.PermissionListItem
import org.tokend.authenticator.auth.view.permission.PermissionsArrayAdapter
import java.text.DateFormat

class AuthRequestConfirmationDialog(
        private val context: Context,
        private val dateFormat: DateFormat
) : AuthRequestConfirmationProvider {
    override fun confirmAuthRequest(authRequest: AuthRequest): Single<Boolean> {
        val resultSubject = SingleSubject.create<Boolean>()

        context.runOnUiThread {
            showConfirmationDialog(authRequest, resultSubject)
        }

        return resultSubject
    }

    private fun showConfirmationDialog(authRequest: AuthRequest,
                                       resultSubject: SingleSubject<Boolean>) {
        val permissions = PermissionListItem.fromAuthRequest(
                context,
                authRequest,
                dateFormat
        )

        AlertDialog.Builder(context)
                .setTitle(
                        context.getString(R.string.template_auth_confirmation_dialog_title,
                                authRequest.appName)
                )
                .setAdapter(PermissionsArrayAdapter(context, permissions), null)
                .setOnCancelListener {
                    resultSubject.onSuccess(false)
                }
                .setPositiveButton(R.string.confirm_action) { _, _ ->
                    resultSubject.onSuccess(true)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .show()
    }
}