package org.tokend.authenticator.base.activities.general_account_info

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_general_account_info.*
import kotlinx.android.synthetic.main.include_error_empty_view.*
import kotlinx.android.synthetic.main.layout_general_card.*
import okhttp3.HttpUrl
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.auth.revoke.RevokeAccessUseCase
import org.tokend.authenticator.auth.view.AuthorizedAppDetailsDialog
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.base.activities.general_account_info.adapter.SignersAdapter
import org.tokend.authenticator.base.util.Navigator
import org.tokend.authenticator.base.util.ObservableTransformers
import org.tokend.authenticator.base.util.error_handlers.ErrorHandlerFactory
import org.tokend.authenticator.base.view.ProgressDialogFactory
import org.tokend.authenticator.base.view.util.DividerItemDecoration
import org.tokend.authenticator.base.view.util.LoadingIndicatorManager
import org.tokend.authenticator.signers.model.Signer
import org.tokend.authenticator.signers.storage.AccountSignersRepository

class GeneralAccountInfoActivity : BaseActivity() {

    companion object {
        const val EXTRA_UID = "extra_uid"
    }

    private val signersLoadingIndicator = LoadingIndicatorManager(
            showLoading = { progress.show() },
            hideLoading = { progress.hide() }
    )

    private val adapter = SignersAdapter()
    private lateinit var signersRepository: AccountSignersRepository

    private val uid: Long
        get() = intent.getLongExtra(EXTRA_UID, -1)

    private lateinit var account: Account

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_general_account_info)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = getString(R.string.manage_account)

        accountsRepository.itemsList.find { it.uid == uid }?.let {
            account = it
            signersRepository = signersRepositoryProvider.getForAccount(it)
            initGeneralCard()
            initRecoverButton()
            initSwipeRefresh()
            initSignersList()
            subscribeSigners()
            updateErrorVisibility()
            update(force = true)
        }
    }

    private fun initSwipeRefresh() {
        swipe_refresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent))
        swipe_refresh.setOnRefreshListener { update(force = true) }
    }

    private fun update(force: Boolean = false) {
        if (!force) {
            signersRepository.updateIfNotFresh()
        } else {
            signersRepository.update()
        }
    }

    private fun initGeneralCard() {
        network_name.text = account.network.name
        network_host.text = HttpUrl.parse(account.network.rootUrl).host()
        email.text = account.email
    }

    private fun initRecoverButton() {
        recover_button.setOnClickListener {
            Navigator.openRecoveryActivity(this, account.network.rootUrl, account.email)
        }
    }

    private fun initSignersList() {
        adapter.dateFormat = dateTimeDateFormat
        adapter.onItemClick { _, item ->
            showSignerDetailsDialog(item)
        }

        error_empty_view.setPadding(0,
                resources.getDimensionPixelSize(R.dimen.half_standard_margin), 0,
                resources.getDimensionPixelSize(R.dimen.half_standard_margin))
        error_empty_view.observeAdapter(adapter, R.string.no_signers_message)
        error_empty_view.setEmptyViewDenial { signersRepository.isNeverUpdated }

        signers_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        signers_list.adapter = adapter
        signers_list.addItemDecoration(
                DividerItemDecoration(ContextCompat.getDrawable(this, R.drawable.horizontal_divider))
        )
    }

    private var signersDisposable: Disposable? = null
    private var signersLoadingDisposable: Disposable? = null
    private var signersErrorsDisposable: Disposable? = null
    private fun subscribeSigners() {
        signersDisposable?.dispose()
        signersDisposable =
                signersRepository.itemsObservable
                        .compose(ObservableTransformers.defaultSchedulers())
                        .subscribe {
                            onSignersUpdated(it)
                            adapter.setData(it.filter { signer ->
                                signer.name.isNotEmpty()
                            })
                        }
                        .addTo(compositeDisposable)

        signersLoadingDisposable?.dispose()
        signersLoadingDisposable =
                signersRepository.loading
                        .compose(ObservableTransformers.defaultSchedulers())
                        .subscribe { isLoading ->
                            if(!isLoading) {
                                swipe_refresh.isRefreshing = isLoading
                            }
                            signersLoadingIndicator.setLoading(isLoading, "signers")
                        }
                        .addTo(compositeDisposable)

        signersErrorsDisposable?.dispose()
        signersErrorsDisposable =
                signersRepository.errors
                        .compose(ObservableTransformers.defaultSchedulers())
                        .subscribe { error ->
                            if (!adapter.hasData) {
                                error_empty_view.showError(error, errorHandlerFactory.getDefault()) {
                                    update(force = true)
                                }
                            } else {
                                errorHandlerFactory.getDefault().handle(error)
                            }
                        }
                        .addTo(compositeDisposable)
    }

    private fun showSignerDetailsDialog(signer: Signer) {
        AuthorizedAppDetailsDialog(signer, this, dateTimeDateFormat) {
            revokeSignerAccess(signer)
        }.show()
    }

    private fun onSignersUpdated(signers: List<Signer>) {
        if (signersRepository.isNeverUpdated) {
            return
        }
        val noAnySigners = !signers.any { it.publicKey == account.publicKey }
        if (account.isBroken == noAnySigners) {
            return
        }
        account.isBroken = noAnySigners
        accountsRepository.update(account)
        updateErrorVisibility()
        updateListCardVisibility(signers.isNotEmpty())
    }

    private fun updateErrorVisibility() {
        error_info_layout.visibility =
                when(account.isBroken) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
    }

    private fun updateListCardVisibility(visibility: Boolean) {
        list_holder.visibility = when(visibility) {
            true -> View.VISIBLE
            false -> View.GONE
        }
    }

    private fun revokeSignerAccess(signer: Signer) {
        var disposable: Disposable? = null

        val progress = ProgressDialogFactory(this).getDefault() {
            disposable?.dispose()
        }

        disposable = RevokeAccessUseCase(
                signer = signer,
                account = signersRepository.account,
                cipher = dataCipher,
                encryptionKeyProvider = encryptionKeyProvider,
                accountSignersRepositoryProvider = signersRepositoryProvider,
                txManagerFactory = txManagerFactory
        )
                .perform()
                .compose(ObservableTransformers.defaultSchedulersCompletable())
                .subscribeBy(
                        onComplete = {
                            progress.dismiss()
                        },
                        onError = {
                            progress.dismiss()
                            ErrorHandlerFactory(this).getDefault().handle(it)
                        }
                )
                .addTo(compositeDisposable)

        progress.show()
    }
}
