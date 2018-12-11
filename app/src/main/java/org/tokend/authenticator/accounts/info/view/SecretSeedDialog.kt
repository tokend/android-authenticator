package org.tokend.authenticator.accounts.info.view

import android.content.Context
import android.graphics.Typeface
import android.support.v7.app.AlertDialog
import android.widget.TextView
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.isSelectable
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.security.encryption.cipher.DataCipher
import org.tokend.authenticator.security.encryption.logic.EncryptionKeyProvider
import org.tokend.authenticator.util.ObservableTransformers
import org.tokend.authenticator.util.errorhandler.ErrorHandlerFactory
import org.tokend.authenticator.view.ProgressDialogFactory
import org.tokend.crypto.ecdsa.erase
import java.nio.CharBuffer

class SecretSeedDialog(
        private val context: Context,
        private val account: Account,
        private val dataCipher: DataCipher,
        private val encryptionKeyProvider: EncryptionKeyProvider,
        private val errorHandlerFactory: ErrorHandlerFactory
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
                .get(context.getString(R.string.decryption_progress)) {
                    disposable?.dispose()
                }

        disposable = account.getSeed(dataCipher, encryptionKeyProvider)
                .compose(ObservableTransformers.defaultSchedulersSingle())
                .doOnSubscribe {
                    progress.show()
                }
                .doOnError { error ->
                    progress.dismiss()
                    errorHandlerFactory.getDefault().handle(error)
                }
                .subscribe { seed ->
                    progress.dismiss()
                    AlertDialog.Builder(context, R.style.AlertDialogStyle)
                            .setTitle(context.getString(R.string.secret_seed))
                            .setMessage(CharBuffer.wrap(seed))
                            .setPositiveButton(R.string.ok, null)
                            .setOnDismissListener {
                                seed.erase()
                            }
                            .show().findViewById<TextView>(android.R.id.message)?.let { textView ->
                                textView.isSelectable = true
                                textView.typeface = Typeface.MONOSPACE
                            }
                }
    }
}