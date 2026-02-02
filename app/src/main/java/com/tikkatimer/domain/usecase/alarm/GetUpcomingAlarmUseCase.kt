package com.tikkatimer.domain.usecase.alarm

import com.tikkatimer.domain.model.UpcomingInfo
import com.tikkatimer.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * 가장 임박한 알람 정보 조회 UseCase
 * 활성화된 알람 중 가장 가까운 알람 시간과 활성 알람 개수를 반환
 */
class GetUpcomingAlarmUseCase
    @Inject
    constructor(
        private val repository: AlarmRepository,
    ) {
        /**
         * 임박한 알람 정보를 Flow로 조회
         */
        operator fun invoke(): Flow<UpcomingInfo> {
            return repository.getEnabledAlarms().map { alarms ->
                if (alarms.isEmpty()) {
                    UpcomingInfo.EMPTY
                } else {
                    val now = LocalDateTime.now()
                    val nextAlarmTime =
                        alarms
                            .map { it.getNextAlarmDateTime(now) }
                            .minOrNull()

                    UpcomingInfo(
                        nextAlarmTime = nextAlarmTime,
                        activeAlarmCount = alarms.size,
                    )
                }
            }
        }
    }
