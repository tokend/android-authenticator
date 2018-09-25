package org.tokend.authenticator.base.activities.account_list

import android.os.Bundle
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.MenuItem
import android.widget.EditText
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_accounts_list.*
import kotlinx.android.synthetic.main.include_error_empty_view.*
import org.tokend.authenticator.R
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.base.activities.account_list.adapter.AccountsListAdapter
import org.tokend.authenticator.base.activities.account_list.adapter.ManageClickListener
import org.tokend.authenticator.base.util.SearchUtil
import org.tokend.authenticator.base.view.util.LoadingIndicatorManager
import java.util.concurrent.TimeUnit

class AccountsListActivity : BaseActivity(), ManageClickListener {

    private var searchItem: MenuItem? = null
    private val adapter = AccountsListAdapter()
    private var filter: String? = null
        set(value) {
            if (value != field) {
                field = value
                onFilterChanged()
            }
        }

    private val loadingIndicator = LoadingIndicatorManager(
            showLoading = { swipe_refresh.isRefreshing = true },
            hideLoading = { swipe_refresh.isRefreshing = false }
    )

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_accounts_list)
        toolbar.title = getString(R.string.accounts)

        initAccountsList()
        initFab()
        initSwipeRefresh()
        initMenu()

        subscribeAccounts()
    }

    private val hideFabScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 2) {
                add_account_fab.hide()
            } else if (dy < -2 && add_account_fab.isEnabled) {
                add_account_fab.show()
            }
        }
    }

    private fun initAccountsList() {
        error_empty_view.setPadding(0, 0, 0,
                resources.getDimensionPixelSize(R.dimen.quadra_margin))
        error_empty_view.observeAdapter(adapter, R.string.no_accounts_message)
        error_empty_view.setEmptyViewDenial { accountsRepository.isNeverUpdated }

        list_account.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter.listener = this
        list_account.adapter = adapter
        list_account.addOnScrollListener(hideFabScrollListener)
    }

    override fun onManageClick(uid: Long) {

    }

    private fun initFab() {
        add_account_fab.setOnClickListener {

        }
    }

    private fun initSwipeRefresh() {
        swipe_refresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent))
        swipe_refresh.setOnRefreshListener { update(true) }
    }

    private fun update(force: Boolean = false) {
        if(force) {
            accountsRepository.updateIfNotFresh()
        } else {
            accountsRepository.update()
        }
    }

    private var searchDisposable: Disposable? = null
    private fun initMenu() {
        toolbar.inflateMenu(R.menu.accounts)
        val menu = toolbar.menu

        searchItem = menu?.findItem(R.id.search) ?: return
        val searchView = searchItem?.actionView as? SearchView ?: return

        (searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as? EditText)
                ?.apply {
                    setHintTextColor(ContextCompat.getColor(context!!, R.color.white_almost))
                    setTextColor(ContextCompat.getColor(context!!, R.color.white))
                }
        searchView.queryHint = getString(R.string.search)
        searchView.setOnQueryTextFocusChangeListener { _, focused ->
            if (!focused && searchView.query.isBlank()) {
                searchItem?.collapseActionView()
            }
        }

        searchDisposable = RxSearchView.queryTextChanges(searchView)
                .skipInitialValue()
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    filter = it.trim().toString().takeIf { it.isNotEmpty() }
                }
                .addTo(compositeDisposable)

        searchItem?.setOnMenuItemClickListener {
            TransitionManager.beginDelayedTransition(toolbar, Fade().setDuration(
                    resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            ))
            searchItem?.expandActionView()
            true
        }

        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean = true

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                filter = null
                return true
            }
        })
    }

    private fun onFilterChanged() {
        displayAccounts()
    }

    private fun displayAccounts() {
        val items = accountsRepository.itemsList
                .let { items ->
                    filter?.let {
                        items.filter { item ->
                            SearchUtil.isMatchGeneralCondition(it, item.network.name, item.email)
                        }
                    }
                }
        adapter.filter(items)
    }

    private var accountsDisposable: Disposable? = null
    private var accountsLoadingDisposable: Disposable? = null
    private var accountsErrorsDisposable: Disposable? = null
    private fun subscribeAccounts() {
        accountsDisposable?.dispose()
        accountsDisposable =
                accountsRepository.itemsObservable
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            adapter.addData(it)
                        }
                        .addTo(compositeDisposable)

        accountsLoadingDisposable?.dispose()
        accountsLoadingDisposable =
                accountsRepository.loading
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            loadingIndicator.setLoading(it, "accounts")
                        }
                        .addTo(compositeDisposable)

        accountsErrorsDisposable?.dispose()
        accountsErrorsDisposable =
                accountsRepository.errors
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { error ->
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
