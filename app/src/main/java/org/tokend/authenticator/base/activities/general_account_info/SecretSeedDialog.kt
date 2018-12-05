package org.tokend.authenticator.base.activities.general_account_info

import android.content.Context
import android.graphics.Typeface
import android.support.v7.app.AlertDialog
import android.widget.TextView
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.isSelectable
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.base.logic.encryption.DataCipher
import org.tokend.authenticator.base.logic.encryption.EncryptionKeyProvider
import org.tokend.authenticator.base.util.ObservableTransformers
import org.tokend.authenticator.base.view.ProgressDialogFactory

class SecretSeedDialog(
        private val context: Context,
        private val account: Account,
        private val dataCipher: DataCipher,
        private val encryptionKeyProvider: EncryptionKeyProvider
) {

    fun show() {
        showSeedWarningDialog()
    }

    private fun showSeedWarningDialog() {
        AlertDialog.Builder(context, R.style.AlertDialogStyle)
                .setMessage(context.getString(R.string.seed_alert_dialog))
                .setNegativeButton(context.getString(R.string.cancel), null)
                .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                    showSecretSeed()
                }.show()
    }

    private fun showSecretSeed() {
        var disposable: Disposable? = null
        val progress = ProgressDialogFactory(context)
                .get(context.getString(R.string.decryption)) {
                    disposable?.dispose()
                }

        disposable = account.getSeed(dataCipher, encryptionKeyProvider)
                .compose(ObservableTransformers.defaultSchedulersSingle())
                .doOnSubscribe {
                    progress.show()
                }
                .doOnError {
                    progress.dismiss()
                }
                .subscribe { seed ->
                    progress.dismiss()
                    AlertDialog.Builder(context, R.style.AlertDialogStyle)
                            .setTitle(context.getString(R.string.secret_seed))
                            .setMessage(String(seed))
                            .setPositiveButton(android.R.string.ok, null)
                            .show().findViewById<TextView>(android.R.id.message)?.let { textView ->
                                textView.isSelectable = true
                                textView.typeface = Typeface.MONOSPACE
                            }
                }
    }
}