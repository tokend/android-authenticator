package org.tokend.authenticator.base.activities.account_list.adapter

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.logic.model.Account

class AccountsListAdapter : RecyclerView.Adapter<AccountViewHolder>() {

    companion object {
        private const val ITEM_ACCOUNT = 0
    }

    private val items = mutableListOf<Account>()
    var listener: ManageClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        when(getItemViewType(position)){
            ITEM_ACCOUNT -> holder.bind(items[position], listener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return ITEM_ACCOUNT
    }

    val hasData: Boolean
        get() = items.isNotEmpty()

    fun setData(data: Collection<Account>?) {
        val newItems = data?.toList() ?: listOf()
        val diffCallback = getDiffCallback(newItems)

        if (items.isEmpty() && newItems.isEmpty()) {
            notifyDataSetChanged()
        } else {
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            items.clear()
            items.addAll(newItems)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    fun addData(data: Collection<Account>?) {
        items.clear()
        if(data != null) {
            items.addAll(data)
        }
        notifyDataSetChanged()
    }

    private fun getDiffCallback(newItems: List<Account>) = object: DiffUtil.Callback() {

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