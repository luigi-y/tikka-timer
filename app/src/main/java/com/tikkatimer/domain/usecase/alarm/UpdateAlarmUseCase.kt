package com.tikkatimer.domain.usecase.alarm

import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.domain.repository.AlarmRepository
import com.tikkatimer.domain.scheduler.AlarmScheduler
import javax.inject.Inject

/**
 * 알람 업데이트 UseCase
 * 알람 정보를 DB에 업데이트하고 시스템 알람도 재등록
 */
class UpdateAlarmUseCase
    @Inject
    constructor(
        private val repository: AlarmRepository,
        private val alarmScheduler: AlarmScheduler,
    ) {
        /**
         * 알람 정보 업데이트
         */
        suspend operator fun invoke(alarm: Alarm) {
            repository.updateAlarm(alarm)
            // 기존 알람 취소 후 재등록
            alarmScheduler.cancel(alarm.id)
            if (alarm.isEnabled) {
                alarmScheduler.schedule(alarm)
            }
        }
    }
