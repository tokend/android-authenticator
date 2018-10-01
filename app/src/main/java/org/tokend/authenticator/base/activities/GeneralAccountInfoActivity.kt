package org.tokend.authenticator.base.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_general_account_info.*
import kotlinx.android.synthetic.main.layout_general_card.*
import okhttp3.HttpUrl
import org.tokend.authenticator.R
import org.tokend.authenticator.accounts.logic.model.Account
import org.tokend.authenticator.base.view.util.LoadingIndicatorManager

class GeneralAccountInfoActivity : BaseActivity() {

    companion object {
        const val EXTRA_UID = "extra_uid"
    }

    private val loadingIndicator = LoadingIndicatorManager(
            showLoading = { progress.show() },
            hideLoading = { progress.hide() }
    )

    private val uid: Long
    get() = intent.getLongExtra(EXTRA_UID, -1)

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_general_account_info)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        accountsRepository.itemsList.find { it.uid == uid }?.let {
            initGeneralCard(it)
        }

        loadingIndicator.setLoading(true, "main")
    }

    private fun initGeneralCard(account: Account) {
        network_name.text = account.network.name
        network_host.text = HttpUrl.parse(account.network.rootUrl).host()
        email.text = account.email
    }
}
