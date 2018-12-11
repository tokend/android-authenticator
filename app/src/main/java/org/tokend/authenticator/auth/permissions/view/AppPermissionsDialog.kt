package org.tokend.authenticator.auth.permissions.view

import android.content.Context
import android.support.v7.app.AlertDialog
import java.text.DateFormat

abstract class AppPermissionsDialog(
        private val context: Context,
        private val dateFormat: DateFormat
) {
    protected open fun getAlertDialogBuilder(): AlertDialog.Builder {
        return AlertDialog.Builder(context)
                .setTitle(getTitle())
                .setAdapter(PermissionsArrayAdapter(context, getPermissions()), null)
    }

    fun show(): AlertDialog {
        return getAlertDialogBuilder().show()
    }

    protected abstract fun getTitle(): String

    protected abstract fun getPermissions(): List<PermissionListItem>
}