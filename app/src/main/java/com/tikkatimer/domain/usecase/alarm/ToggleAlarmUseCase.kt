package com.tikkatimer.domain.usecase.alarm

import com.tikkatimer.domain.repository.AlarmRepository
import com.tikkatimer.domain.scheduler.AlarmScheduler
import javax.inject.Inject

/**
 * 알람 활성화/비활성화 토글 UseCase
 * 알람 활성화 시 시스템에 등록, 비활성화 시 시스템에서 취소
 */
class ToggleAlarmUseCase
    @Inject
    constructor(
        private val repository: AlarmRepository,
        private val alarmScheduler: AlarmScheduler,
    ) {
        /**
         * 알람 활성화/비활성화 설정
         */
        suspend operator fun invoke(
            alarmId: Long,
            isEnabled: Boolean,
        ) {
            repository.setAlarmEnabled(alarmId, isEnabled)

            if (isEnabled) {
                // 알람 활성화 시 시스템에 등록
                val alarm = repository.getAlarmById(alarmId)
                alarm?.let { alarmScheduler.schedule(it.copy(isEnabled = true)) }
            } else {
                // 알람 비활성화 시 시스템에서 취소
                alarmScheduler.cancel(alarmId)
            }
        }
    }
