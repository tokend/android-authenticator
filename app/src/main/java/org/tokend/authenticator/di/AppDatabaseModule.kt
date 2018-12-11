package org.tokend.authenticator.di

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import org.tokend.authenticator.logic.db.AppDatabase
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