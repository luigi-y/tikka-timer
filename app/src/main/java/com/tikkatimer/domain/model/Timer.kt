package com.tikkatimer.domain.model

/**
 * 타이머 도메인 모델
 * 타이머의 현재 상태와 설정을 나타냄
 */
data class Timer(
    val totalDurationMillis: Long,
    val remainingMillis: Long,
    val state: TimerState = TimerState.IDLE,
) {
    /**
     * 남은 시간을 시/분/초로 분해
     */
    val hours: Int
        get() = (remainingMillis / 3600000).toInt()

    val minutes: Int
        get() = ((remainingMillis % 3600000) / 60000).toInt()

    val seconds: Int
        get() = ((remainingMillis % 60000) / 1000).toInt()

    /**
     * 진행률 (0.0 ~ 1.0)
     */
    val progress: Float
        get() =
            if (totalDurationMillis > 0) {
                remainingMillis.toFloat() / totalDurationMillis
            } else {
                0f
            }

    /**
     * 남은 시간을 "HH:MM:SS" 또는 "MM:SS" 형식으로 반환
     */
    fun getFormattedTime(): String {
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    companion object {
        val EMPTY =
            Timer(
                totalDurationMillis = 0,
                remainingMillis = 0,
                state = TimerState.IDLE,
            )
    }
}

/**
 * 타이머 상태
 */
enum class TimerState {
    IDLE, // 초기 상태 (시간 설정 대기)
    RUNNING, // 실행 중
    PAUSED, // 일시정지
    FINISHED, // 완료
}

/**
 * 타이머 프리셋 도메인 모델
 * 자주 사용하는 타이머 설정을 저장
 */
data class TimerPreset(
    val id: Long = 0,
    val name: String,
    val durationSeconds: Long,
    val usageCount: Int = 0,
    val soundType: SoundType = SoundType.DEFAULT,
    val vibrationPattern: VibrationPattern = VibrationPattern.DEFAULT,
    val ringtoneUri: String? = null,
) {
    val hours: Int
        get() = (durationSeconds / 3600).toInt()

    val minutes: Int
        get() = ((durationSeconds % 3600) / 60).toInt()

    val seconds: Int
        get() = (durationSeconds % 60).toInt()

    /**
     * 시간을 읽기 쉬운 형식으로 반환
     * 예: "3분", "1시간 30분", "45초"
     */
    val formattedDuration: String
        get() {
            val parts = mutableListOf<String>()
            if (hours > 0) parts.add("${hours}시간")
            if (minutes > 0) parts.add("${minutes}분")
            if (seconds > 0) parts.add("${seconds}초")
            return parts.joinToString(" ").ifEmpty { "0초" }
        }
}

/**
 * 실행 중인 타이머 인스턴스
 * 프리셋 기반으로 생성되며 독립적으로 실행됨
 */
data class RunningTimer(
    /** 고유 인스턴스 ID (UUID) */
    val instanceId: String,
    /** 원본 프리셋 ID (0이면 일회성) */
    val presetId: Long,
    /** 타이머 이름 */
    val name: String,
    /** 전체 시간 */
    val totalDurationMillis: Long,
    /** 남은 시간 */
    val remainingMillis: Long,
    val state: TimerState = TimerState.IDLE,
    val soundType: SoundType = SoundType.DEFAULT,
    val vibrationPattern: VibrationPattern = VibrationPattern.DEFAULT,
    /** 실행 중일 때 종료 예정 시각 */
    val targetEndTimeMillis: Long = 0L,
) {
    val hours: Int
        get() = (remainingMillis / 3600000).toInt()

    val minutes: Int
        get() = ((remainingMillis % 3600000) / 60000).toInt()

    val seconds: Int
        get() = ((remainingMillis % 60000) / 1000).toInt()

    val progress: Float
        get() =
            if (totalDurationMillis > 0) {
                remainingMillis.toFloat() / totalDurationMillis
            } else {
                0f
            }

    /**
     * 남은 시간을 "HH:MM:SS" 또는 "MM:SS" 형식으로 반환
     */
    val formattedTime: String
        get() =
            if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }

    companion object {
        /**
         * 프리셋에서 실행 타이머 생성
         */
        fun fromPreset(
            preset: TimerPreset,
            instanceId: String,
        ): RunningTimer {
            val totalMillis = preset.durationSeconds * 1000L
            return RunningTimer(
                instanceId = instanceId,
                presetId = preset.id,
                name = preset.name.ifEmpty { preset.formattedDuration },
                totalDurationMillis = totalMillis,
                remainingMillis = totalMillis,
                state = TimerState.IDLE,
                soundType = preset.soundType,
                vibrationPattern = preset.vibrationPattern,
            )
        }

        /**
         * 일회성 타이머 생성
         */
        fun createOneTime(
            instanceId: String,
            name: String,
            durationSeconds: Long,
            soundType: SoundType = SoundType.DEFAULT,
            vibrationPattern: VibrationPattern = VibrationPattern.DEFAULT,
        ): RunningTimer {
            val totalMillis = durationSeconds * 1000L
            return RunningTimer(
                instanceId = instanceId,
                presetId = 0,
                name = name,
                totalDurationMillis = totalMillis,
                remainingMillis = totalMillis,
                state = TimerState.IDLE,
                soundType = soundType,
                vibrationPattern = vibrationPattern,
            )
        }
    }
}
