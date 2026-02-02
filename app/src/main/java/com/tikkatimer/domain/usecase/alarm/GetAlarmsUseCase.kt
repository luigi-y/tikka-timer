package com.tikkatimer.domain.usecase.alarm

import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 알람 목록 조회 UseCase
 */
class GetAlarmsUseCase
    @Inject
    constructor(
        private val repository: AlarmRepository,
    ) {
        /**
         * 모든 알람을 Flow로 조회
         */
        operator fun invoke(): Flow<List<Alarm>> {
            return repository.getAllAlarms()
        }
    }
