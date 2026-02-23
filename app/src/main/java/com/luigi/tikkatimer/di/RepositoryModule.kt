package com.luigi.tikkatimer.di

import com.luigi.tikkatimer.data.repository.AlarmRepositoryImpl
import com.luigi.tikkatimer.data.repository.TimerRepositoryImpl
import com.luigi.tikkatimer.domain.repository.AlarmRepository
import com.luigi.tikkatimer.domain.repository.TimerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 의존성 바인딩 모듈
 * 인터페이스와 구현체를 연결
 */
@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    @Singleton
    fun bindAlarmRepository(alarmRepositoryImpl: AlarmRepositoryImpl): AlarmRepository

    @Binds
    @Singleton
    fun bindTimerRepository(timerRepositoryImpl: TimerRepositoryImpl): TimerRepository
}
