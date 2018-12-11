package org.tokend.authenticator.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.jetbrains.anko.defaultSharedPreferences
import org.tokend.authenticator.App

@Module
class AppModule(private val app: App) {

    @Provides
    fun appContext(): Context {
        return app.applicationContext
    }

    @Provides
    fun sharedPreferences(): SharedPreferences {
        return app.applicationContext.defaultSharedPreferences
    }
}