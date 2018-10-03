package org.tokend.authenticator.base.activities.account_list.adapter

import android.view.View
import kotlinx.android.synthetic.main.item_account.view.*
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.logic.AccountLogoFactory
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.base.view.adapter.BaseViewHolder
import org.tokend.authenticator.base.view.adapter.SimpleItemClickListener

class AccountViewHolder(
        itemView: View,
        private val logoFactory: AccountLogoFactory
) : BaseViewHolder<Account>(itemView) {

    private val logoSize: Int by lazy {
        itemView.context.resources.getDimensionPixelSize(R.dimen.account_list_item_logo_size)
    }

    override fun bind(item: Account) {
        itemView.account_logo_image_view.setImageBitmap(
                logoFactory
                        .getWithAutoBackground(item, logoSize)
        )
        itemView.network_text_view.text = item.network.name
        itemView.email_text_view.text = item.email
    }

    override fun bind(item: Account, clickListener: SimpleItemClickListener<Account>?) {
        bind(item)
        itemView.account_manage_button.setOnClickListener {
            clickListener?.invoke(itemView.account_manage_button, item)
        }
    }
}