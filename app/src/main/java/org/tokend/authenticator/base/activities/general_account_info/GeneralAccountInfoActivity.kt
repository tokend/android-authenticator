package org.tokend.authenticator.base.activities.general_account_info

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_general_account_info.*
import kotlinx.android.synthetic.main.include_error_empty_view.*
import kotlinx.android.synthetic.main.layout_general_card.*
import okhttp3.HttpUrl
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.base.activities.general_account_info.adapter.SignersAdapter
import org.tokend.authenticator.base.util.ObservableTransformers
import org.tokend.authenticator.base.view.util.LoadingIndicatorManager
import org.tokend.authenticator.signers.storage.AccountSignersRepository

class GeneralAccountInfoActivity : BaseActivity() {

    companion object {
        const val EXTRA_UID = "extra_uid"
    }

    private val loadingIndicator = LoadingIndicatorManager(
            showLoading = { progress.show() },
            hideLoading = { progress.hide() }
    )

    private val adapter = SignersAdapter()
    private lateinit var signersRepository: AccountSignersRepository

    private val uid: Long
    get() = intent.getLongExtra(EXTRA_UID, -1)

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_general_account_info)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        accountsRepository.itemsList.find { it.uid == uid }?.let {
            signersRepository = signersRepositoryProvider.getForAccount(it)
            initGeneralCard(it)
            initSignersList()
            subscribeSigners()
            update(true)
        }
    }

    private fun update(force: Boolean = false) {
        if (force) {
            signersRepository.updateIfNotFresh()
        } else {
            signersRepository.update()
        }
    }

    private fun initGeneralCard(account: Account) {
        network_name.text = account.network.name
        network_host.text = HttpUrl.parse(account.network.rootUrl).host()
        email.text = account.email
    }

    private fun initSignersList() {
        adapter.dateFormat = dateTimeDateFormat
        adapter.onItemClick { view, item ->

        }

        error_empty_view.setPadding(0,
                resources.getDimensionPixelSize(R.dimen.half_standard_margin), 0,
                resources.getDimensionPixelSize(R.dimen.half_standard_margin))
        error_empty_view.observeAdapter(adapter, R.string.no_signers_message)
        error_empty_view.setEmptyViewDenial { signersRepository.isNeverUpdated }

        signers_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        signers_list.adapter = adapter
        signers_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
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
                            adapter.setData(it.filter { signer ->
                                signer.name.isNotEmpty()
                            })
                        }
                        .addTo(compositeDisposable)

        signersLoadingDisposable?.dispose()
        signersLoadingDisposable =
                signersRepository.loading
                        .compose(ObservableTransformers.defaultSchedulers())
                        .subscribe {
                            loadingIndicator.setLoading(it, "signers")
                        }
                        .addTo(compositeDisposable)

        signersErrorsDisposable?.dispose()
        signersErrorsDisposable =
                signersRepository.errors
                        .compose(ObservableTransformers.defaultSchedulers())
                        .subscribe {error ->
                            if (!adapter.hasData) {
                                error_empty_view.showError(error, errorHandlerFactory.getDefault()) {
                                    update(true)
                                }
                            } else {
                                errorHandlerFactory.getDefault().handle(error)
                            }
                        }
                        .addTo(compositeDisposable)
    }
}
