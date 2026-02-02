package com.tikkatimer.di

import com.tikkatimer.data.repository.AlarmRepositoryImpl
import com.tikkatimer.data.repository.TimerRepositoryImpl
import com.tikkatimer.domain.repository.AlarmRepository
import com.tikkatimer.domain.repository.TimerRepository
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
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAlarmRepository(alarmRepositoryImpl: AlarmRepositoryImpl): AlarmRepository

    @Binds
    @Singleton
    abstract fun bindTimerRepository(timerRepositoryImpl: TimerRepositoryImpl): TimerRepository
}
