package com.luigi.tikkatimer.di

import com.luigi.tikkatimer.data.scheduler.AlarmSchedulerImpl
import com.luigi.tikkatimer.domain.scheduler.AlarmScheduler
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
fun interface SchedulerModule {
    @Binds
    @Singleton
    fun bindAlarmScheduler(impl: AlarmSchedulerImpl): AlarmScheduler
}
