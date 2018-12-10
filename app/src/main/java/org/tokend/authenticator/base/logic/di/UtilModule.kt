package org.tokend.authenticator.base.logic.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.base.logic.fingerprint.FingerprintUtil
import org.tokend.authenticator.base.util.ToastManager
import org.tokend.authenticator.base.util.error_handlers.ErrorHandlerFactory
import org.tokend.authenticator.security.logic.PunishmentTimer
import javax.inject.Singleton

@Module
class UtilModule {
    @Provides
    @Singleton
    fun errorHandlerFactory(context: Context): ErrorHandlerFactory {
        return ErrorHandlerFactory(context)
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