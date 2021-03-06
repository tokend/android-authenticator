package org.tokend.authenticator.settings.view

import android.support.v7.app.AlertDialog
import android.support.v7.preference.SwitchPreferenceCompat
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.tokend.authenticator.R
import org.tokend.authenticator.security.environment.model.EnvSecurityStatus
import org.tokend.authenticator.util.ObservableTransformers
import org.tokend.authenticator.view.ProgressDialogFactory
import org.tokend.authenticator.view.util.ToastManager
import javax.inject.Inject

class GeneralSettingsFragment : SettingsFragment() {
    @Inject
    lateinit var toastManager: ToastManager

    private val isDeviceSecure
        get() = envSecurityStatusProvider.getEnvSecurityStatus() == EnvSecurityStatus.NORMAL

    override fun reloadPreferences() {
        super.reloadPreferences()

        initSecurityCategory()
        initInfoCategory()
    }

    // region Security
    private fun initSecurityCategory() {
        initUnsecuredDeviceItem()
        initFingerprintItem()
        initChangePasswordItem()
    }

    private fun initUnsecuredDeviceItem() {
        val preference = findPreference("unsecured_device") ?: return

        if (!isDeviceSecure) {
            preference.isVisible = true

            preference.setOnPreferenceClickListener {
                AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                        .setMessage(R.string.security_limitations_explanation)
                        .setPositiveButton(R.string.ok, null)
                        .show()

                true
            }
        } else {
            preference.isVisible = false
        }
    }

    private fun initFingerprintItem() {
        val preference = findPreference("fingerprint") as? SwitchPreferenceCompat
        preference?.isVisible = (activity as SettingsActivity)
                .fingerprintUtil.isFingerprintAvailable
        preference?.isEnabled = isDeviceSecure
    }

    private fun initChangePasswordItem() {
        var disposable: Disposable? = null
        val progress = ProgressDialogFactory(requireContext())
                .get(requireContext().getString(R.string.change_security_code_progress)) {
                    disposable?.dispose()
                }

        findPreference("change_password")?.setOnPreferenceClickListener {
            disposable = (activity as? SettingsActivity)?.let {
                it.encryptionKeyProvider
                        .resetUserKey()
                        .compose(ObservableTransformers.defaultSchedulersCompletable())
                        .doOnSubscribe { progress.show() }
                        .subscribeBy(
                                onComplete = {
                                    progress.dismiss()
                                    toastManager
                                            .short(getString(R.string.security_code_changed_message))
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