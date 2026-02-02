package com.tikkatimer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 타이머 프리셋을 저장하는 Room Entity
 * 자주 사용하는 타이머 시간을 저장하여 빠르게 선택 가능
 */
@Entity(tableName = "timer_presets")
data class TimerPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    /** 총 시간(초) */
    val durationSeconds: Long,
    /** 사용 횟수 (자주 사용하는 프리셋 정렬용) */
    val usageCount: Int = 0,
    /** SoundType enum name */
    val soundType: String = "DEFAULT",
    /** VibrationPattern enum name */
    val vibrationPattern: String = "DEFAULT",
    /** 사용자 지정 벨소리 URI */
    val ringtoneUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
