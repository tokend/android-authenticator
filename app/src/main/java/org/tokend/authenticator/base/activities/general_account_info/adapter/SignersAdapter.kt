package org.tokend.authenticator.base.activities.general_account_info.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import org.tokend.authenticator.R
import org.tokend.authenticator.base.view.adapter.BaseRecyclerAdapter
import org.tokend.authenticator.signers.model.Signer
import java.text.DateFormat


class SignersAdapter
    : BaseRecyclerAdapter<Signer, SignerViewHolder>() {

    lateinit var dateFormat: DateFormat

    override fun createItemViewHolder(parent: ViewGroup): SignerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_signer, parent, false)
        return SignerViewHolder(view, dateFormat)
    }
}