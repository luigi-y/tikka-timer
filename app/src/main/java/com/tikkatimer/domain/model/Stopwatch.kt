package com.tikkatimer.domain.model

/**
 * 스톱워치 도메인 모델
 * 경과 시간과 랩 타임 정보를 포함
 */
data class Stopwatch(
    val elapsedMillis: Long = 0,
    val state: StopwatchState = StopwatchState.IDLE,
    val lapTimes: List<LapTime> = emptyList(),
) {
    val hours: Int
        get() = (elapsedMillis / 3600000).toInt()

    val minutes: Int
        get() = ((elapsedMillis % 3600000) / 60000).toInt()

    val seconds: Int
        get() = ((elapsedMillis % 60000) / 1000).toInt()

    val milliseconds: Int
        get() = ((elapsedMillis % 1000) / 10).toInt() // 센티초 (00-99)

    /**
     * 경과 시간을 "MM:SS.mm" 또는 "HH:MM:SS.mm" 형식으로 반환
     */
    fun getFormattedTime(): String {
        return if (hours > 0) {
            String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, milliseconds)
        } else {
            String.format("%02d:%02d.%02d", minutes, seconds, milliseconds)
        }
    }

    companion object {
        val INITIAL = Stopwatch()
    }
}

/**
 * 스톱워치 상태
 */
enum class StopwatchState {
    IDLE, // 초기 상태
    RUNNING, // 실행 중
    PAUSED, // 일시정지
}

/**
 * 랩 타임 도메인 모델
 * 스톱워치에서 기록한 구간 시간
 */
data class LapTime(
    val lapNumber: Int,
    /** 이 랩의 구간 시간 (이전 랩부터 현재까지) */
    val lapMillis: Long,
    /** 시작부터 현재까지의 총 시간 */
    val totalMillis: Long,
) {
    /**
     * 랩 구간 시간을 포맷팅
     */
    fun getFormattedLapTime(): String {
        val minutes = (lapMillis / 60000).toInt()
        val seconds = ((lapMillis % 60000) / 1000).toInt()
        val millis = ((lapMillis % 1000) / 10).toInt()
        return String.format("%02d:%02d.%02d", minutes, seconds, millis)
    }

    /**
     * 총 경과 시간을 포맷팅
     */
    fun getFormattedTotalTime(): String {
        val hours = (totalMillis / 3600000).toInt()
        val minutes = ((totalMillis % 3600000) / 60000).toInt()
        val seconds = ((totalMillis % 60000) / 1000).toInt()
        val millis = ((totalMillis % 1000) / 10).toInt()

        return if (hours > 0) {
            String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, millis)
        } else {
            String.format("%02d:%02d.%02d", minutes, seconds, millis)
        }
    }
}
