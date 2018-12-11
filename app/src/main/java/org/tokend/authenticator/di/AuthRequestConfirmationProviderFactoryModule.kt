package org.tokend.authenticator.di

import dagger.Module
import dagger.Provides
import org.tokend.authenticator.auth.request.confirmation.view.AuthRequestConfirmationDialogFactory
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