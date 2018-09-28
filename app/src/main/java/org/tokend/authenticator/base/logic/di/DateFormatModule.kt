package org.tokend.authenticator.base.logic.di

import dagger.Module
import dagger.Provides
import java.text.DateFormat
import java.text.SimpleDateFormat
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DateOnlyDateFormat

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DateTimeDateFormat

@Module
class DateFormatModule {
    @Provides
    @Singleton
    @DateOnlyDateFormat
    fun dateOnlyDateFormat(): DateFormat {
        return SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
    }

    @Provides
    @Singleton
    @DateTimeDateFormat
    fun dateTimeDateFormat(): DateFormat {
        return SimpleDateFormat.getDateTimeInstance(
                SimpleDateFormat.SHORT,
                SimpleDateFormat.SHORT
        )
    }
}