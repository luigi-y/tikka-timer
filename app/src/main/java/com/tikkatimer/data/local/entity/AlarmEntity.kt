package com.tikkatimer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 알람 정보를 저장하는 Room Entity
 * 반복 요일, 스누즈 설정, 알람음 등의 정보를 포함
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "",
    /** 비트마스크: 일(1), 월(2), 화(4), 수(8), 목(16), 금(32), 토(64) */
    val repeatDays: Int = 0,
    /** SoundType enum name */
    val soundType: String = "DEFAULT",
    /** VibrationPattern enum name */
    val vibrationPattern: String = "DEFAULT",
    /** null이면 기본 알람음 사용 */
    val ringtoneUri: String? = null,
    val snoozeDurationMinutes: Int = 5,
    val isSnoozeEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)
