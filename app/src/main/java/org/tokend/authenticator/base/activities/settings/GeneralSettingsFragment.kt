package org.tokend.authenticator.base.activities.settings

import android.support.v7.preference.SwitchPreferenceCompat
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.tokend.authenticator.R
import org.tokend.authenticator.base.activities.SettingsFragment
import org.tokend.authenticator.base.logic.fingerprint.FingerprintUtil
import org.tokend.authenticator.base.util.ObservableTransformers
import org.tokend.authenticator.base.util.ToastManager
import org.tokend.authenticator.base.view.OpenSourceLicensesDialog
import org.tokend.authenticator.base.view.ProgressDialogFactory

class GeneralSettingsFragment : SettingsFragment() {

    private var fingerprintPreference: SwitchPreferenceCompat? = null

    override fun reloadPreferences() {
        super.reloadPreferences()

        initSecurityCategory()
        initInfoCategory()
    }

    // region Security
    private fun initSecurityCategory() {
        initFingerprintItem()
        initChangePasswordItem()
    }

    private fun initFingerprintItem() {
        fingerprintPreference = findPreference("fingerprint") as? SwitchPreferenceCompat
        fingerprintPreference?.isVisible = FingerprintUtil(requireContext()).isFingerprintAvailable
    }

    private fun initChangePasswordItem() {
        var disposable: Disposable? = null
        val progress = ProgressDialogFactory(requireContext())
                .get(requireContext().getString(R.string.change_password_progress)) {
                    disposable?.dispose()
                }

        findPreference("change_password")?.setOnPreferenceClickListener {
            disposable = (activity as? SettingsActivity)?.let {
                it.encryptionKeyProvider
                        .resetUserKey()
                        .compose(ObservableTransformers.defaultSchedulersCompletable())
                        .doOnSubscribe { progress.show() }
                        .subscribeBy (
                                onComplete = {
                                    progress.dismiss()
                                    ToastManager(requireContext())
                                            .short(getString(R.string.pin_changed_message))
                                },
                                onError = { progress.dismiss() }
                        )
            }

            true
        }
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