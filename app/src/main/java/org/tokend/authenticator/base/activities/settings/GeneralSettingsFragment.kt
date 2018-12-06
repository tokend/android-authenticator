package org.tokend.authenticator.base.activities.settings

import android.support.v7.preference.SwitchPreferenceCompat
import org.tokend.authenticator.R
import org.tokend.authenticator.base.activities.SettingsFragment
import org.tokend.authenticator.base.logic.fingerprint.FingerprintUtil
import org.tokend.authenticator.base.view.OpenSourceLicensesDialog

class GeneralSettingsFragment : SettingsFragment() {

    private var fingerprintPreference: SwitchPreferenceCompat? = null

    override fun reloadPreferences() {
        super.reloadPreferences()

        initFingerprintItem()
        initInfoCategory()
    }

    private fun initFingerprintItem() {
        fingerprintPreference = findPreference("fingerprint") as? SwitchPreferenceCompat
        fingerprintPreference?.isVisible = FingerprintUtil(requireContext()).isFingerprintAvailable
    }

    // region Information
    private fun initInfoCategory() {
        initOpenSourceLicensesItem()
    }

    private fun initOpenSourceLicensesItem() {
        val openSourceLicensesPreference = findPreference("open_source_licenses")
        openSourceLicensesPreference?.setOnPreferenceClickListener {
            OpenSourceLicensesDialog(requireContext(), R.style.AlertDialogStyle)
                    .show()

            true
        }
    }
    // endregion
}