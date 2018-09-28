package org.tokend.authenticator.auth.view.permission

import android.content.Context
import org.tokend.authenticator.R
import org.tokend.authenticator.auth.request.AuthRequest
import org.tokend.wallet.xdr.SignerType
import java.text.DateFormat
import java.util.*

class PermissionListItem(
        val name: String,
        val description: String? = null
) {
    companion object {
        private fun fromExpirationDate(context: Context,
                                       expirationDate: Date?,
                                       dateFormat: DateFormat): PermissionListItem {
            val name =
                    if (expirationDate != null)
                        context.getString(R.string.template_permission_finite_access,
                                dateFormat.format(expirationDate))
                    else
                        context.getString(R.string.permission_permanent_access)

            return PermissionListItem(
                    name,
                    context.getString(R.string.permission_access_revoke_description)
            )
        }

        private fun fromAccessType(context: Context, accessType: SignerType): PermissionListItem {
            val name = context.resources.getStringArray(R.array.signer_type_descriptions)
                    .getOrNull(accessType.ordinal)
                    ?: accessType.name

            return PermissionListItem(name)
        }

        fun fromAuthRequest(context: Context,
                            authRequest: AuthRequest,
                            dateFormat: DateFormat): List<PermissionListItem> {
            val accessDurationPermission =
                    fromExpirationDate(
                            context,
                            authRequest.expirationDate,
                            dateFormat
                    )

            return listOf(accessDurationPermission) +
                    authRequest.accessTypes
                            .map {
                                fromAccessType(context, it)
                            }
        }
    }
}