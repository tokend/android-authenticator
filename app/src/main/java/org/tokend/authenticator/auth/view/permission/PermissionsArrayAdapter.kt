package org.tokend.authenticator.auth.view.permission

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.jetbrains.anko.find
import org.jetbrains.anko.layoutInflater
import org.tokend.authenticator.R

class PermissionsArrayAdapter(
        context: Context,
        items: List<PermissionListItem>
) : ArrayAdapter<PermissionListItem>(context, 0, 0, items) {
    class ViewHolder(
            val nameTextView: TextView,
            val descriptionTextView: TextView
    )

    private val layoutInflater = context.layoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView
                ?: layoutInflater.inflate(R.layout.list_item_app_permission,
                        parent, false)
        val viewHolder =
                (view.tag as? ViewHolder)
                        ?: ViewHolder(
                                view.find(R.id.permission_text_view),
                                view.find(R.id.permission_description_text_view)
                        )
                                .also { view.tag = it }

        val permission = getItem(position)

        viewHolder.nameTextView.text = permission.name

        if (permission.description != null) {
            viewHolder.descriptionTextView.visibility = View.VISIBLE
            viewHolder.descriptionTextView.text = permission.description
        } else {
            viewHolder.descriptionTextView.visibility = View.GONE
        }

        return view
    }
}