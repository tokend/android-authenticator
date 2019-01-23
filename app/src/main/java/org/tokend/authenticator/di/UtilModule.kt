package org.tokend.authenticator.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.security.userkey.punishment.PunishmentTimer
import org.tokend.authenticator.util.FingerprintUtil
import org.tokend.authenticator.util.errorhandler.ErrorHandlerFactory
import org.tokend.authenticator.view.util.ToastManager
import javax.inject.Singleton

@Module
class UtilModule {
    @Provides
    @Singleton
    fun errorHandlerFactory(context: Context, toastManager: ToastManager): ErrorHandlerFactory {
        return ErrorHandlerFactory(context, toastManager)
    }

    @Provides
    @Singleton
    fun toastManager(context: Context): ToastManager {
        return ToastManager(context)
    }

    @Provides
    @Singleton
    fun fingerprintUtil(context: Context, sharedPreferences: SharedPreferences): FingerprintUtil {
        return FingerprintUtil(context, sharedPreferences)
    }

    @Provides
    @Singleton
    fun punishmentTimer(sharedPreferences: SharedPreferences): PunishmentTimer {
        return PunishmentTimer(sharedPreferences)
    }
}