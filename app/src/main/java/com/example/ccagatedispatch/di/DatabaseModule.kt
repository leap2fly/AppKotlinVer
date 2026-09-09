package com.example.ccagatedispatch.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.ccagatedispatch.data.local.AppDatabase
import com.example.ccagatedispatch.data.local.dao.AppDao
import com.example.ccagatedispatch.security.KeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSupportFactory(keyProvider: KeyProvider): SupportSQLiteOpenHelper.Factory {
        val passphrase = keyProvider.getOrCreateDatabasePassphrase()
        return SupportFactory(passphrase)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        factory: SupportSQLiteOpenHelper.Factory
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cca_gate_dispatch_encrypted.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAppDao(database: AppDatabase): AppDao {
        return database.appDao()
    }
}
