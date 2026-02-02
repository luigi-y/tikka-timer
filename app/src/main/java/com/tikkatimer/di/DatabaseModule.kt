package com.tikkatimer.di

import android.content.Context
import androidx.room.Room
import com.tikkatimer.data.local.AppDatabase
import com.tikkatimer.data.local.dao.AlarmDao
import com.tikkatimer.data.local.dao.TimerPresetDao
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
            // 모든 마이그레이션 순차 적용
            .addMigrations(*AppDatabase.ALL_MIGRATIONS)
            // 마이그레이션 실패 시 데이터 삭제 후 재생성 (최후의 수단)
            .fallbackToDestructiveMigration()
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
}
