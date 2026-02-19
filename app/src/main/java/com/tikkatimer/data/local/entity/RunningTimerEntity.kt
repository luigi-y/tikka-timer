package com.tikkatimer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 실행 중인 타이머 상태를 저장하는 Room Entity
 * 앱 강제 종료 후에도 타이머 상태 복원 가능
 */
@Entity(tableName = "running_timers")
data class RunningTimerEntity(
    @PrimaryKey
    val instanceId: String,
    /** 프리셋 ID (0이면 커스텀 타이머) */
    val presetId: Long = 0,
    /** 타이머 이름 */
    val name: String,
    /** 총 시간 (밀리초) */
    val totalDurationMillis: Long,
    /** 남은 시간 (밀리초) - 일시정지 시점 기준 */
    val remainingMillis: Long,
    /** 타이머 상태 (RUNNING, PAUSED, FINISHED) */
    val state: String,
    /** 타이머 종료 예정 시각 (실행 중일 때) */
    val targetEndTimeMillis: Long,
    /** SoundType enum name */
    val soundType: String = "DEFAULT",
    /** VibrationPattern enum name */
    val vibrationPattern: String = "DEFAULT",
    /** 생성 시각 */
    val createdAt: Long = System.currentTimeMillis(),
)
