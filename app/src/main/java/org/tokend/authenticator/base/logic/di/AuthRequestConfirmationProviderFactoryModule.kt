package org.tokend.authenticator.base.logic.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.auth.view.confirmation.AuthRequestConfirmationDialogFactory
import java.text.DateFormat
import javax.inject.Singleton

@Module
class AuthRequestConfirmationProviderFactoryModule {
    @Provides
    @Singleton
    fun authConfirmationDialogFactory(@DateTimeDateFormat
                                      dateFormat: DateFormat
    ): AuthRequestConfirmationDialogFactory {
        return AuthRequestConfirmationDialogFactory(dateFormat)
    }
}