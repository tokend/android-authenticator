package org.tokend.authenticator.base.activities.settings

import android.support.v7.preference.SwitchPreferenceCompat
import org.tokend.authenticator.base.activities.SettingsFragment
import org.tokend.authenticator.base.logic.fingerprint.FingerprintUtil

class GeneralSettingsFragment : SettingsFragment() {

    private var fingerprintPreference: SwitchPreferenceCompat? = null

    override fun reloadPreferences() {
        super.reloadPreferences()

        initFingerprintItem()
    }

    private fun initFingerprintItem() {
        fingerprintPreference = findPreference("fingerprint") as? SwitchPreferenceCompat
        fingerprintPreference?.isVisible = FingerprintUtil(requireContext()).isFingerprintAvailable
    }
}