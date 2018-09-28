package org.tokend.authenticator.base.activities.account_list.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_account.view.*
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.logic.AccountLogoFactory
import org.tokend.authenticator.accounts.logic.model.Account

class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val logoSize: Int by lazy {
        itemView.context.resources.getDimensionPixelSize(R.dimen.account_list_item_logo_size)
    }

    fun bind(account: Account, listener: ManageClickListener?) {
        itemView.account_logo_image_view.setImageBitmap(AccountLogoFactory(itemView.context)
                .getForCode(account.network.name, logoSize))
        itemView.network_text_view.text = account.network.name
        itemView.email_text_view.text = account.email
        itemView.account_manage_button.setOnClickListener {
            listener?.onManageClick(account.uid)
        }
    }
}