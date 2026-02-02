package com.tikkatimer.domain.usecase.alarm

import com.tikkatimer.domain.repository.AlarmRepository
import com.tikkatimer.domain.scheduler.AlarmScheduler
import javax.inject.Inject

/**
 * 알람 삭제 UseCase
 * 알람을 DB에서 삭제하고 시스템 알람도 취소
 */
class DeleteAlarmUseCase
    @Inject
    constructor(
        private val repository: AlarmRepository,
        private val alarmScheduler: AlarmScheduler,
    ) {
        /**
         * 알람 삭제
         */
        suspend operator fun invoke(alarmId: Long) {
            // 시스템 알람 취소
            alarmScheduler.cancel(alarmId)
            // DB에서 삭제
            repository.deleteAlarm(alarmId)
        }
    }
