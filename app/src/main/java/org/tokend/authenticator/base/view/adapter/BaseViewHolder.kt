package org.tokend.authenticator.base.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class BaseViewHolder<T>(protected val view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: T)

    open fun bind(item: T, clickListener: SimpleItemClickListener<T>?) {
        bind(item)
        view.setOnClickListener { clickListener?.invoke(view, item) }
    }
}