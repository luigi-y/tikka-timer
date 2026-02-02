package com.tikkatimer.domain.model

import java.time.Duration
import java.time.LocalDateTime

/**
 * 임박한 알람/타이머 정보
 * 화면 상단에 다음 알람까지 남은 시간과 활성화된 알람 개수를 표시하기 위한 모델
 */
data class UpcomingInfo(
    /** 다음 알람까지 남은 시간 (없으면 null) */
    val nextAlarmTime: LocalDateTime?,
    /** 활성화된 알람 총 개수 */
    val activeAlarmCount: Int,
) {
    /**
     * 다음 알람까지 남은 시간을 읽기 쉬운 문자열로 변환
     * 예: "3시간 후", "45분 후", "1일 2시간 후"
     */
    fun getTimeUntilText(): String? {
        val targetTime = nextAlarmTime ?: return null

        val now = LocalDateTime.now()
        if (targetTime.isBefore(now)) return null

        val duration = Duration.between(now, targetTime)
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60

        return when {
            days > 0 && hours > 0 -> "${days}일 ${hours}시간 후"
            days > 0 -> "${days}일 후"
            hours > 0 && minutes > 0 -> "${hours}시간 ${minutes}분 후"
            hours > 0 -> "${hours}시간 후"
            minutes > 0 -> "${minutes}분 후"
            else -> "곧"
        }
    }

    /**
     * 다음 알람 시간을 "오전/오후 HH:MM" 형식으로 반환
     */
    fun getNextAlarmTimeText(): String? {
        val targetTime = nextAlarmTime ?: return null

        val hour = targetTime.hour
        val minute = targetTime.minute
        val period = if (hour < 12) "오전" else "오후"
        val displayHour =
            when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
        return "$period $displayHour:${minute.toString().padStart(2, '0')}"
    }

    companion object {
        val EMPTY =
            UpcomingInfo(
                nextAlarmTime = null,
                activeAlarmCount = 0,
            )
    }
}
