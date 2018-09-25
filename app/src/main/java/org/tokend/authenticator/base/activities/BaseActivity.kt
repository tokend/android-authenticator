package org.tokend.authenticator.base.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import org.tokend.authenticator.App
import org.tokend.authenticator.accounts.logic.storage.AccountsRepository
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var accountsRepository: AccountsRepository

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        (application as? App)?.appComponent?.inject(this)

        //replace to logic of allow creation
        if(true) {
            onCreateAllowed(savedInstanceState)
        } else {

        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    abstract fun onCreateAllowed(savedInstanceState: Bundle?)
}