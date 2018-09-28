package org.tokend.authenticator.auth.view.accounts.selection.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.find
import org.jetbrains.anko.layoutInflater
import org.tokend.authenticator.R
import org.tokend.authenticator.base.view.adapter.BaseRecyclerAdapter
import org.tokend.authenticator.base.view.adapter.BaseViewHolder

class AccountSelectionListAdapter : BaseRecyclerAdapter<AccountSelectionListItem,
        BaseViewHolder<AccountSelectionListItem>>() {
    class AddAccountViewHolder(view: View) : BaseViewHolder<AccountSelectionListItem>(view) {
        override fun bind(item: AccountSelectionListItem) {}
    }

    class ExistingAccountViewHolder(view: View) : BaseViewHolder<AccountSelectionListItem>(view) {
        private val nameTextView: TextView = view.find(android.R.id.text1)

        override fun bind(item: AccountSelectionListItem) {
            val accountItem = (item as ExistingAccountSelectionListItem)
            nameTextView.text = accountItem.name
            nameTextView.setPadding(0, nameTextView.paddingTop, nameTextView.paddingRight,
                    nameTextView.paddingBottom)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is AddAccountSelectionListItem)
            VIEW_TYPE_ADD_ACCOUNT
        else
            super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<AccountSelectionListItem>, position: Int) {
        bindItemViewHolder(holder, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<AccountSelectionListItem> {
        return when (viewType) {
            VIEW_TYPE_ADD_ACCOUNT -> createAddAccountViewHolder(parent)
            else -> createItemViewHolder(parent)
        }
    }

    override fun createItemViewHolder(parent: ViewGroup): BaseViewHolder<AccountSelectionListItem> {
        val view = parent.context.layoutInflater.inflate(
                android.R.layout.simple_list_item_1,
                parent,
                false
        )
        return ExistingAccountViewHolder(view)
    }

    private fun createAddAccountViewHolder(parent: ViewGroup): BaseViewHolder<AccountSelectionListItem> {
        val view = parent.context.layoutInflater.inflate(
                R.layout.list_item_account_selection_add_account,
                parent,
                false
        )
        return AddAccountViewHolder(view)
    }

    companion object {
        private const val VIEW_TYPE_ADD_ACCOUNT = 2
    }
}