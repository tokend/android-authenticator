package org.tokend.authenticator.accounts.info.view

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_general_account_info.*
import kotlinx.android.synthetic.main.include_error_empty_view.*
import kotlinx.android.synthetic.main.layout_general_card.*
import okhttp3.HttpUrl
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.accounts.info.data.model.Signer
import org.tokend.authenticator.accounts.info.data.storage.AccountSignersRepository
import org.tokend.authenticator.accounts.info.view.adapter.SignersAdapter
import org.tokend.authenticator.auth.manage.logic.RevokeAccessUseCase
import org.tokend.authenticator.auth.manage.view.AuthorizedAppDetailsDialog
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.util.Navigator
import org.tokend.authenticator.util.ObservableTransformers
import org.tokend.authenticator.view.ProgressDialogFactory
import org.tokend.authenticator.view.decoration.DividerItemDecoration
import org.tokend.authenticator.view.util.LoadingIndicatorManager

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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        accountsRepository.itemsList.find { it.uid == uid }?.let {
            account = it
            signersRepository = signersRepositoryProvider.getForAccount(it)
            initViews()
            subscribeSigners()
            updateErrorVisibility()
            update()
        }
    }

    private fun initViews() {
        initGeneralCard()
        initButtons()
        initSwipeRefresh()
        initSignersList()
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

    private fun initButtons() {
        recover_button.setOnClickListener {
            Navigator.openRecoveryActivity(this, account.network.rootUrl, account.email)
        }

        delete_button.setOnClickListener {
            AlertDialog.Builder(this, R.style.AlertDialogStyle)
                    .setTitle(getString(R.string.delete_account_title))
                    .setMessage(getString(R.string.delete_account_message))
                    .setPositiveButton(getString(R.string.delete)) { _, _ ->
                        accountsRepository.delete(account)
                        finish()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
        }
    }

    private fun initSignersList() {
        signers_list.isNestedScrollingEnabled = false
        adapter.dateFormat = dateTimeDateFormat
        adapter.onItemClick { _, item ->
            showSignerDetailsDialog(item)
        }

        error_empty_view.setEmptyDrawable(R.drawable.ic_link)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.general_account_info, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.seed) {
            SecretSeedDialog(
                    this,
                    account,
                    dataCipher,
                    encryptionKeyProvider,
                    errorHandlerFactory
            ).show()
        }
        return super.onOptionsItemSelected(item)
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
                        }
                        .addTo(compositeDisposable)

        signersLoadingDisposable?.dispose()
        signersLoadingDisposable =
                signersRepository.loading
                        .compose(ObservableTransformers.defaultSchedulers())
                        .subscribe { isLoading ->
                            if (!isLoading) {
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
        signers.filter { signer ->
            signer.name.isNotEmpty()
        }.also {
            adapter.setData(it)
            updateListCardVisibility(it.isNotEmpty())
        }

        if (signersRepository.isNeverUpdated) {
            return
        }
        val noAnySigners = signers.isNotEmpty()
                && !signers.any { it.publicKey == account.publicKey }
        if (account.isBroken == noAnySigners) {
            return
        }
        account.isBroken = noAnySigners
        accountsRepository.update(account)
        updateErrorVisibility()
    }

    private fun updateErrorVisibility() {
        error_info_layout.visibility =
                when (account.isBroken) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
    }

    private fun updateListCardVisibility(visibility: Boolean) {
        list_holder.visibility = when (visibility) {
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
                            errorHandlerFactory.getDefault().handle(it)
                        }
                )
                .addTo(compositeDisposable)

        progress.show()
    }
}
