package org.tokend.authenticator.auth.request.accountselection.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_auth_account_selection.*
import kotlinx.android.synthetic.main.layout_progress.*
import org.jetbrains.anko.find
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.data.model.Account
import org.tokend.authenticator.accounts.data.model.Network
import org.tokend.authenticator.auth.request.accountselection.view.adapter.AccountSelectionListAdapter
import org.tokend.authenticator.auth.request.accountselection.view.adapter.AddAccountSelectionListItem
import org.tokend.authenticator.auth.request.accountselection.view.adapter.ExistingAccountSelectionListItem
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.util.Navigator
import org.tokend.authenticator.util.ObservableTransformers
import org.tokend.authenticator.view.util.LoadingIndicatorManager

class AuthAccountSelectionActivity : BaseActivity(
        canShowUserKeyRequest = false
) {
    private lateinit var appName: String
    private lateinit var network: Network

    private val selectionAdapter = AccountSelectionListAdapter()
    private val loadingIndicator = LoadingIndicatorManager(
            showLoading = { progress.show() },
            hideLoading = { progress.hide() }
    )

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_auth_account_selection)

        appName = intent.getStringExtra(APP_NAME_EXTRA)
                ?: throw IllegalArgumentException("$APP_NAME_EXTRA extra is required")
        network = (intent.getSerializableExtra(NETWORK_EXTRA) as? Network)
                ?: throw IllegalArgumentException("$NETWORK_EXTRA extra is required")

        initTitle()
        initButtons()
        initAccountsList()

        subscribeToAccounts()
    }

    // region Init
    private fun initTitle() {
        title = getString(R.string.template_auth_account_selection_title, appName)

        window.decorView.post {
            window.decorView.find<TextView>(R.id.title).apply {
                maxLines = 3
                setSingleLine(false)
            }
        }
    }

    private fun initButtons() {
        cancel_button.setOnClickListener {
            cancel()
        }
    }

    private fun initAccountsList() {
        selectionAdapter.onItemClick { _, item ->
            if (item is ExistingAccountSelectionListItem)
                selectAccount(item.id)
            else
                openAccountAdd()
        }

        account_selection_recycler_view.layoutManager = LinearLayoutManager(this)
        account_selection_recycler_view.adapter = selectionAdapter
    }
    // endregion

    private fun subscribeToAccounts() {
        accountsRepository.updateIfNotFresh()

        accountsRepository.itemsObservable
                .compose(ObservableTransformers.defaultSchedulers())
                .subscribeBy { accounts ->
                    val filteredAccounts = accounts.filter {
                        it.network == network
                    }
                    displayAccounts(filteredAccounts)
                }
                .addTo(compositeDisposable)

        accountsRepository.loading
                .compose(ObservableTransformers.defaultSchedulers())
                .subscribeBy { loadingIndicator.setLoading(it, "accounts") }
    }

    private fun displayAccounts(accounts: List<Account>) {
        val listItems =
                accounts.map {
                    ExistingAccountSelectionListItem.fromAccount(it)
                } +
                        listOf(AddAccountSelectionListItem())
        selectionAdapter.setData(listItems)
    }

    private fun openAccountAdd() {
        Navigator.openAddAccount(this, network.rootUrl)
    }

    // region Result
    private fun cancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun selectAccount(id: Long) {
        setResult(
                Activity.RESULT_OK,
                Intent()
                        .putExtra(ACCOUNT_ID_RESULT_EXTRA, id)
        )
        finish()
    }
    // endregion Result

    companion object {
        const val APP_NAME_EXTRA = "app_name"
        const val NETWORK_EXTRA = "network"
        const val ACCOUNT_ID_RESULT_EXTRA = "account_id"
    }
}
