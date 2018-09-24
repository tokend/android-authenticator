package org.tokend.authenticator.base.logic.di

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.base.logic.db.AppDatabase
import javax.inject.Singleton

@Module
class AppDatabaseModule(
        private val name: String
) {
    @Provides
    @Singleton
    fun database(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, name)
                .build()
    }
}