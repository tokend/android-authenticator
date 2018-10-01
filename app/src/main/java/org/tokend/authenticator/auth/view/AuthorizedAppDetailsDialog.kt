package org.tokend.authenticator.auth.view

import android.content.Context
import android.support.v7.app.AlertDialog
import org.tokend.authenticator.R
import org.tokend.authenticator.auth.view.permission.AppPermissionsDialog
import org.tokend.authenticator.auth.view.permission.PermissionListItem
import org.tokend.authenticator.signers.model.Signer
import java.text.DateFormat

class AuthorizedAppDetailsDialog(
        private val signer: Signer,
        private val context: Context,
        private val dateFormat: DateFormat,
        private val onAccessRevokeClicked: () -> Unit
) : AppPermissionsDialog(context, dateFormat) {

    override fun getTitle(): String {
        return context.getString(
                R.string.template_authorized_app_dialog_title,
                signer.name
        )
    }

    override fun getPermissions(): List<PermissionListItem> {
        return PermissionListItem.fromSigner(context, signer, dateFormat)
    }

    override fun getAlertDialogBuilder(): AlertDialog.Builder {
        return super.getAlertDialogBuilder()
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.revoke_access_action) { _, _ ->
                    onAccessRevokeClicked()
                }
    }
}