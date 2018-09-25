package org.tokend.authenticator.base.logic.di

import android.content.Context
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.base.util.ToastManager
import org.tokend.authenticator.base.util.error_handlers.ErrorHandlerFactory
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
}