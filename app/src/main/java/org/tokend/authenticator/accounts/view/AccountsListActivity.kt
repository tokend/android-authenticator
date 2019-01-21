package org.tokend.authenticator.accounts.view

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.DisplayMetrics
import android.view.MenuItem
import android.widget.EditText
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_accounts_list.*
import kotlinx.android.synthetic.main.include_error_empty_view.*
import okhttp3.HttpUrl
import org.json.JSONObject
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.add.view.util.AccountLogoFactory
import org.tokend.authenticator.accounts.view.adapter.AccountListItem
import org.tokend.authenticator.accounts.view.adapter.AccountsListAdapter
import org.tokend.authenticator.base.activities.BaseActivity
import org.tokend.authenticator.util.Navigator
import org.tokend.authenticator.util.ObservableTransformers
import org.tokend.authenticator.util.Permission
import org.tokend.authenticator.util.QrScannerUtil
import org.tokend.authenticator.view.util.LoadingIndicatorManager
import org.tokend.authenticator.view.util.ToastManager
import org.tokend.sdk.api.authenticator.model.AuthRequest
import java.util.concurrent.TimeUnit

class AccountsListActivity : BaseActivity() {

    private var searchItem: MenuItem? = null
    private val adapter = AccountsListAdapter(AccountLogoFactory(this))
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

    private val cameraPermission = Permission(Manifest.permission.CAMERA, 404)

    private lateinit var layoutManager: GridLayoutManager

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_accounts_list)
        toolbar.title = getString(R.string.accounts)

        initAccountsList()
        initFab()
        initSwipeRefresh()
        initMenu()

        subscribeAccounts()

        update()
    }

    private val hideFabScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 2) {
                scan_qr_fab.hide()
            } else if (dy < -2 && scan_qr_fab.isEnabled) {
                scan_qr_fab.show()
            }
        }
    }

    private fun initAccountsList() {
        val columns = calculateColumnCount()

        layoutManager = GridLayoutManager(this, columns)

        error_empty_view.setEmptyDrawable(R.drawable.ic_account_key)
        error_empty_view.setPadding(0, 0, 0,
                resources.getDimensionPixelSize(R.dimen.quadra_margin))
        error_empty_view.observeAdapter(adapter, R.string.no_accounts_message)
        error_empty_view.setEmptyViewDenial { accountsRepository.isNeverUpdated }

        adapter.onItemClick { _, item ->
            Navigator.openGeneralAccountInfo(this, item.uid)
        }

        list_account.layoutManager = layoutManager
        list_account.adapter = adapter
        list_account.addOnScrollListener(hideFabScrollListener)
    }

    private fun calculateColumnCount(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels.toDouble()
        return (screenWidth / resources.getDimensionPixelSize(R.dimen.max_content_width))
                .let { Math.ceil(it) }
                .toInt()
    }

    private fun initFab() {
        scan_qr_fab.setOnClickListener {
            tryOpenQrScanner()
        }
    }

    private fun initSwipeRefresh() {
        swipe_refresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent))
        swipe_refresh.setOnRefreshListener { update() }
    }

    private fun update(force: Boolean = false) {
        if (force) {
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

        val addAccountItem = menu.findItem(R.id.add_account)
        val settingsItem = menu.findItem(R.id.settings)

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
                .subscribe { it ->
                    filter = it.trim().toString()
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
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                addAccountItem.isVisible = false
                settingsItem.isVisible = false
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                filter = null
                addAccountItem.isVisible = true
                settingsItem.isVisible = true
                return true
            }
        })

        addAccountItem.setOnMenuItemClickListener {
            Navigator.openAddAccount(this)
            true
        }

        settingsItem.setOnMenuItemClickListener {
            Navigator.openSettings(this)
            true
        }
    }

    private fun onFilterChanged() {
        displayAccounts()
    }

    private fun displayAccounts() {
        val items = accountsRepository.itemsList
                .let { items ->
                    filter?.let { query ->
                        items.filter { item ->
                            item.email.contains(query) || item.network.name.contains(query)
                        }
                    } ?: items
                }
        adapter.setData(items.map { AccountListItem(it) })
    }

    private var accountsDisposable: Disposable? = null
    private var accountsLoadingDisposable: Disposable? = null
    private var accountsErrorsDisposable: Disposable? = null
    private fun subscribeAccounts() {
        accountsDisposable?.dispose()
        accountsDisposable =
                accountsRepository.itemsObservable
                        .compose(ObservableTransformers.defaultSchedulers())
                        .subscribe {
                            adapter.setData(it.map { AccountListItem(it) })
                        }
                        .addTo(compositeDisposable)

        accountsLoadingDisposable?.dispose()
        accountsLoadingDisposable =
                accountsRepository.loading
                        .compose(ObservableTransformers.defaultSchedulers())
                        .subscribe {
                            loadingIndicator.setLoading(it, "account")
                        }
                        .addTo(compositeDisposable)

        accountsErrorsDisposable?.dispose()
        accountsErrorsDisposable =
                accountsRepository.errors
                        .compose(ObservableTransformers.defaultSchedulers())
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

    override fun onBackPressed() {
        (searchItem?.actionView as? SearchView)?.apply {
            if (query.isNotEmpty()) {
                setQuery("", false)
                clearFocus()
                searchItem?.collapseActionView()
            } else super.onBackPressed()
        }
    }

    // region QR
    private fun tryOpenQrScanner() {
        cameraPermission.check(this) {
            QrScannerUtil.openScanner(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraPermission.handlePermissionResult(requestCode, permissions, grantResults)
    }
    // endregion

    override fun onDestroy() {
        super.onDestroy()
        list_account.clearOnScrollListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        QrScannerUtil.getStringFromResult(requestCode, resultCode, data).also {
            try {
                val uri = Uri.parse(it)
                AuthRequest.parse(uri.toString())
                Navigator.openAuthorizeAppActivity(this, uri)
                return@also
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                val apiRoot = JSONObject(it).getString("api")
                        .also { urlString -> HttpUrl.parse(urlString) }
                Navigator.openAddAccount(this, apiRoot)
                return@also
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (it != null) {
                ToastManager(this).short(R.string.error_unknown_qr)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        layoutManager.spanCount = calculateColumnCount()
    }
}
