package org.tokend.authenticator

import android.content.Context
import android.content.SharedPreferences
import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import org.tokend.authenticator.base.logic.di.*
import java.io.IOException
import java.net.SocketException

class App : MultiDexApplication() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .appDatabaseModule(AppDatabaseModule(DATABASE_NAME))
                .appEncryptionKeyProviderModule(
                        AppEncryptionKeyProviderModule(getKeystorePreferences())
                )
                .secureStorageModule(
                        SecureStorageModule(getKeystorePreferences())
                )
                .build()

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        initTls()
        initRxErrorHandler()
    }

    private fun initTls() {
        try {
            if (areGooglePlayServicesAvailable()) {
                ProviderInstaller.installIfNeeded(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initRxErrorHandler() {
        RxJavaPlugins.setErrorHandler {
            var e = it
            if (e is UndeliverableException) {
                e = e.cause
            }
            if ((e is IOException) || (e is SocketException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                return@setErrorHandler
            }
            if (e is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return@setErrorHandler
            }
            if ((e is NullPointerException) || (e is IllegalArgumentException)) {
                // that's likely a bug in the application
                Thread.currentThread().uncaughtExceptionHandler
                        .uncaughtException(Thread.currentThread(), e)
                return@setErrorHandler
            }
            if (e is IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().uncaughtExceptionHandler
                        .uncaughtException(Thread.currentThread(), e)
                return@setErrorHandler
            }
            Log.w("RxErrorHandler", "Undeliverable exception received", e)
        }
    }

    fun getKeystorePreferences(): SharedPreferences {
        return getSharedPreferences(KEYSTORE_PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun areGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        return resultCode == ConnectionResult.SUCCESS
    }

    companion object {
        private const val DATABASE_NAME = "app-db"
        private const val KEYSTORE_PREF_NAME = "keystore"
    }
}