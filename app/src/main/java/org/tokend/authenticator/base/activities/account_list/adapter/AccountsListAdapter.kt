package org.tokend.authenticator.base.activities.account_list.adapter


import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.base.view.adapter.BaseRecyclerAdapter

class AccountsListAdapter : BaseRecyclerAdapter<Account, AccountViewHolder>() {

    override fun createItemViewHolder(parent: ViewGroup): AccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun getDiffCallback(newItems: List<Account>): DiffUtil.Callback? {
        return object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].uid == newItems[newItemPosition].uid
            }

            override fun getOldListSize(): Int {
                return itemCount
            }

            override fun getNewListSize(): Int {
                return newItems.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = items[oldItemPosition]
                val newItem = newItems[newItemPosition]
                return oldItem.email == newItem.email &&
                        oldItem.network.rootUrl == newItem.network.rootUrl
            }
        }
    }
}