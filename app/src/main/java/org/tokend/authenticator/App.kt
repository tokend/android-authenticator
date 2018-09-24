package org.tokend.authenticator

import android.support.multidex.MultiDexApplication
import org.tokend.authenticator.base.logic.di.AppComponent
import org.tokend.authenticator.base.logic.di.AppDatabaseModule
import org.tokend.authenticator.base.logic.di.DaggerAppComponent

class App : MultiDexApplication() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .appDatabaseModule(AppDatabaseModule(DATABASE_NAME))
                .build()
    }

    companion object {
        const val DATABASE_NAME = "app-db"
    }
}