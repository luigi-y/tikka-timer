package com.tikkatimer.domain.scheduler

import com.tikkatimer.domain.model.Alarm

/**
 * 알람 스케줄링 인터페이스
 * AlarmManager를 통한 시스템 알람 등록/취소 추상화
 */
interface AlarmScheduler {
    /**
     * 알람을 시스템에 등록
     * @param alarm 등록할 알람
     */
    fun schedule(alarm: Alarm)

    /**
     * 알람을 시스템에서 취소
     * @param alarmId 취소할 알람 ID
     */
    fun cancel(alarmId: Long)

    /**
     * 정확한 알람 권한 확인 (Android 12+)
     * @return 권한 허용 여부
     */
    fun canScheduleExactAlarms(): Boolean
}
