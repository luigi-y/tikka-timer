package com.tikkatimer.di

import com.tikkatimer.data.scheduler.AlarmSchedulerImpl
import com.tikkatimer.domain.scheduler.AlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 스케줄러 DI 모듈
 * AlarmScheduler 바인딩 제공
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {
    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(impl: AlarmSchedulerImpl): AlarmScheduler
}
