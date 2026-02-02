package com.tikkatimer.domain.usecase.alarm

import com.tikkatimer.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * 1회성 알람 비활성화 UseCase
 * 알람이 울린 후 1회성 알람(반복 요일이 없는 알람)을 자동으로 비활성화
 */
class DisableOneTimeAlarmUseCase
    @Inject
    constructor(
        private val repository: AlarmRepository,
    ) {
        /**
         * 지정된 알람이 1회성인 경우 비활성화
         * @param alarmId 비활성화할 알람 ID
         * @return 비활성화 되었으면 true, 반복 알람이라 비활성화하지 않았으면 false
         */
        suspend operator fun invoke(alarmId: Long): Boolean {
            val alarm = repository.getAlarmById(alarmId) ?: return false

            // 반복 알람이면 비활성화하지 않음
            if (alarm.isRepeating) {
                return false
            }

            // 1회성 알람 비활성화
            repository.setAlarmEnabled(alarmId, false)
            return true
        }
    }
