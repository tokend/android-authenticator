package org.tokend.authenticator.base.activities.settings

import android.os.Bundle
import org.tokend.authenticator.R
import org.tokend.authenticator.base.activities.BaseActivity

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateAllowed(savedInstanceState: Bundle?) {
        displaySettings()
    }

    private fun displaySettings() {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.stay_visible, R.anim.activity_fade_out)
                .replace(R.id.root_layout, GeneralSettingsFragment())
                .commit()
    }
}
