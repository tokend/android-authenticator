package org.tokend.authenticator.accounts.info.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import org.tokend.authenticator.R
import org.tokend.authenticator.view.adapter.BaseRecyclerAdapter
import org.tokend.authenticator.accounts.info.data.model.Signer
import java.text.DateFormat


class SignersAdapter
    : BaseRecyclerAdapter<Signer, SignerViewHolder>() {

    lateinit var dateFormat: DateFormat

    override fun createItemViewHolder(parent: ViewGroup): SignerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_signer, parent, false)
        return SignerViewHolder(view, dateFormat)
    }
}