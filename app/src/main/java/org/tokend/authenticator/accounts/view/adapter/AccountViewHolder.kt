package org.tokend.authenticator.accounts.view.adapter

import android.view.View
import kotlinx.android.synthetic.main.item_account.view.*
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.add.view.util.AccountLogoFactory
import org.tokend.authenticator.view.adapter.BaseViewHolder
import org.tokend.authenticator.view.adapter.SimpleItemClickListener

class AccountViewHolder(
        itemView: View,
        private val logoFactory: AccountLogoFactory
) : BaseViewHolder<AccountListItem>(itemView) {

    private val logoSize: Int by lazy {
        itemView.context.resources.getDimensionPixelSize(R.dimen.account_list_item_logo_size)
    }

    override fun bind(item: AccountListItem) {
        if(item.isBroken) {
            itemView.account_logo_image_view.setImageResource(R.drawable.ic_alert_circle)
        } else {
            itemView.account_logo_image_view.setImageBitmap(
                    logoFactory
                            .getWithAutoBackground(item.source, logoSize)
            )
        }
        itemView.network_text_view.text = item.network.name
        itemView.email_text_view.text = item.email
    }

    override fun bind(item: AccountListItem, clickListener: SimpleItemClickListener<AccountListItem>?) {
        bind(item)
        itemView.account_manage_button.setOnClickListener {
            clickListener?.invoke(itemView.account_manage_button, item)
        }
    }
}