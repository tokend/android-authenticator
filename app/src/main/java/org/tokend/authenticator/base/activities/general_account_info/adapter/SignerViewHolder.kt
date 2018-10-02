package org.tokend.authenticator.base.activities.general_account_info.adapter

import android.view.View
import kotlinx.android.synthetic.main.item_signer.view.*
import org.tokend.authenticator.R
import org.tokend.authenticator.base.view.adapter.BaseViewHolder
import org.tokend.authenticator.signers.model.Signer
import java.text.DateFormat

class SignerViewHolder(itemView: View, private val dateTimeDateFormat: DateFormat)
    : BaseViewHolder<Signer>(itemView) {

    override fun bind(item: Signer) {
        var date = ""
        item.expirationDate?.let {
            date = dateTimeDateFormat.format(item.expirationDate)
        }
        val expires = when(date.isEmpty()) {
            true -> itemView.context.getString(R.string.permanent)
            else -> itemView.context.getString(R.string.expires)
        }
        val finalText = "$expires $date"

        itemView.app_name_text_view.text = item.name
        itemView.expiration_date_text_view.text = finalText
    }
}