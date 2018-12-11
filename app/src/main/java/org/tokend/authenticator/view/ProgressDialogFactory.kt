package org.tokend.authenticator.view

import android.app.ProgressDialog
import android.content.Context
import org.tokend.authenticator.R

class ProgressDialogFactory(
        private val context: Context
) {
    /**
     * @return Progress dialog with the default message, cancellable if listener specified.
     */
    fun getDefault(cancelListener: (() -> Unit)? = null): ProgressDialog {
        return get(context.getString(R.string.processing_progress), cancelListener)
    }

    /**
     * @return Progress dialog with given message, cancellable if listener specified.
     */
    fun get(message: String, cancelListener: (() -> Unit)?): ProgressDialog {
        val progress = ProgressDialog(context)
        progress.isIndeterminate = true
        progress.setMessage(message)

        if (cancelListener != null) {
            progress.setCancelable(true)
            progress.setOnCancelListener { cancelListener() }
        } else {
            progress.setCancelable(false)
        }

        return progress
    }
}