package com.luigi.tikkatimer.di

import android.content.Context
import androidx.room.Room
import com.luigi.tikkatimer.data.local.AppDatabase
import com.luigi.tikkatimer.data.local.dao.AlarmDao
import com.luigi.tikkatimer.data.local.dao.RunningTimerDao
import com.luigi.tikkatimer.data.local.dao.TimerPresetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Room Database 관련 의존성 제공 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideAlarmDao(database: AppDatabase): AlarmDao {
        return database.alarmDao()
    }

    @Provides
    @Singleton
    fun provideTimerPresetDao(database: AppDatabase): TimerPresetDao {
        return database.timerPresetDao()
    }

    @Provides
    @Singleton
    fun provideRunningTimerDao(database: AppDatabase): RunningTimerDao {
        return database.runningTimerDao()
    }
}
