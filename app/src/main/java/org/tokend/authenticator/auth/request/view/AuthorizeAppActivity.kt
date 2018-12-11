package org.tokend.authenticator.auth.request.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import org.tokend.authenticator.R
import org.tokend.authenticator.auth.request.logic.AuthorizeAppUseCase
import org.tokend.authenticator.auth.request.accountselection.view.ActivityAuthAccountSelector
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.util.ObservableTransformers
import org.tokend.authenticator.view.util.ToastManager

class AuthorizeAppActivity : BaseActivity() {
    private lateinit var accountSelector: ActivityAuthAccountSelector

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_authorize_app)

        val uri = intent.dataString
                ?: return

        accountSelector = authAccountSelectorFactory.getForActivity(this)

        AuthorizeAppUseCase(
                uri,
                accountSelector,
                signersRepositoryProvider,
                authRequestConfirmationDialogFactory.getForActivity(this),
                dataCipher,
                encryptionKeyProvider,
                apiFactory,
                txManagerFactory
        )
                .perform()
                .compose(ObservableTransformers.defaultSchedulersCompletable())
                .subscribeBy(
                        onComplete = {
                            if (!isFinishing) {
                                ToastManager(this).short(R.string.app_authorized)
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        },
                        onError = {
                            it.printStackTrace()
                            if (!isFinishing) {
                                errorHandlerFactory.getDefault().handle(it)
                                finish()
                            }
                        }
                )
                .addTo(compositeDisposable)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        accountSelector.handleActivityResult(requestCode, resultCode, data)
    }
}
