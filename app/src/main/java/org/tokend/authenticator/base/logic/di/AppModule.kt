package org.tokend.authenticator.base.logic.di

import android.content.Context
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.App

@Module
class AppModule(private val app: App) {

    @Provides
    fun appContext(): Context {
        return app.applicationContext
    }
}