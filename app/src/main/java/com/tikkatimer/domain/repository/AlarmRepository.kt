package com.tikkatimer.domain.repository

import com.tikkatimer.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

/**
 * 알람 저장소 인터페이스
 * Data 레이어의 구현체를 Domain 레이어에서 추상화
 */
interface AlarmRepository {
    /**
     * 모든 알람 조회 (Flow로 실시간 업데이트)
     */
    fun getAllAlarms(): Flow<List<Alarm>>

    /**
     * 활성화된 알람만 조회
     */
    fun getEnabledAlarms(): Flow<List<Alarm>>

    /**
     * ID로 알람 조회
     */
    suspend fun getAlarmById(alarmId: Long): Alarm?

    /**
     * 새 알람 추가
     * @return 생성된 알람의 ID
     */
    suspend fun addAlarm(alarm: Alarm): Long

    /**
     * 알람 정보 업데이트
     */
    suspend fun updateAlarm(alarm: Alarm)

    /**
     * 알람 삭제
     */
    suspend fun deleteAlarm(alarmId: Long)

    /**
     * 알람 활성화/비활성화 토글
     */
    suspend fun setAlarmEnabled(
        alarmId: Long,
        isEnabled: Boolean,
    )
}
