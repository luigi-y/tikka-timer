package com.tikkatimer.domain.usecase.alarm

import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.domain.repository.AlarmRepository
import com.tikkatimer.domain.scheduler.AlarmScheduler
import javax.inject.Inject

/**
 * 알람 추가 UseCase
 * 알람을 DB에 저장하고 시스템 AlarmManager에 등록
 */
class AddAlarmUseCase
    @Inject
    constructor(
        private val repository: AlarmRepository,
        private val alarmScheduler: AlarmScheduler,
    ) {
        /**
         * 새 알람 추가
         * @return 생성된 알람의 ID
         */
        suspend operator fun invoke(alarm: Alarm): Long {
            val alarmId = repository.addAlarm(alarm)
            // 저장된 알람을 시스템에 등록
            val savedAlarm = alarm.copy(id = alarmId)
            alarmScheduler.schedule(savedAlarm)
            return alarmId
        }
    }
