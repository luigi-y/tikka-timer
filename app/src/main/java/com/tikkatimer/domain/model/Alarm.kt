package com.tikkatimer.domain.model

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 알람 도메인 모델
 * UI와 비즈니스 로직에서 사용되는 알람 정보
 */
data class Alarm(
    val id: Long = 0,
    val time: LocalTime,
    val isEnabled: Boolean = true,
    val label: String = "",
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val soundType: SoundType = SoundType.DEFAULT,
    val vibrationPattern: VibrationPattern = VibrationPattern.DEFAULT,
    val ringtoneUri: String? = null,
    val snoozeDurationMinutes: Int = 5,
    val isSnoozeEnabled: Boolean = true,
) {
    /**
     * 반복 알람 여부
     */
    val isRepeating: Boolean
        get() = repeatDays.isNotEmpty()

    /**
     * 반복 요일을 읽기 쉬운 문자열로 변환
     * 예: "매일", "평일", "주말", "월, 수, 금"
     */
    fun getRepeatDaysText(): String {
        if (repeatDays.isEmpty()) return "반복 안함"

        val weekdays =
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
            )
        val weekend = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

        return when {
            repeatDays.size == 7 -> "매일"
            repeatDays == weekdays -> "평일"
            repeatDays == weekend -> "주말"
            else ->
                repeatDays
                    .sortedBy { it.value }
                    .joinToString(", ") { it.toKoreanShort() }
        }
    }

    /**
     * 시간을 "오전/오후 HH:MM" 형식으로 반환
     */
    fun getTimeText(): String {
        val hour = time.hour
        val minute = time.minute
        val period = if (hour < 12) "오전" else "오후"
        val displayHour =
            when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
        return "$period $displayHour:${minute.toString().padStart(2, '0')}"
    }

    /**
     * 1회성 알람 여부 (반복 요일이 없는 경우)
     */
    val isOneTime: Boolean
        get() = repeatDays.isEmpty()

    /**
     * 다음 알람 시간 계산
     * 1회성 알람: 오늘 시간이 지났으면 내일
     * 반복 알람: 가장 가까운 반복 요일
     */
    fun getNextAlarmDateTime(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        val todayTime = now.toLocalDate().atTime(time)

        if (repeatDays.isEmpty()) {
            // 1회성 알람: 오늘 시간이 안 지났으면 오늘, 지났으면 내일
            return if (now.isBefore(todayTime)) {
                todayTime
            } else {
                todayTime.plusDays(1)
            }
        }

        // 반복 알람: 가장 가까운 반복 요일 찾기
        val today = now.dayOfWeek
        var daysUntilNext = 0

        // 오늘이 반복 요일이고 아직 시간이 안 지났으면 오늘
        if (today in repeatDays && now.isBefore(todayTime)) {
            return todayTime
        }

        // 다음 반복 요일 찾기
        for (i in 1..7) {
            val checkDay = today.plus(i.toLong())
            if (checkDay in repeatDays) {
                daysUntilNext = i
                break
            }
        }

        return todayTime.plusDays(daysUntilNext.toLong())
    }
}

/**
 * DayOfWeek를 한글 약어로 변환
 */
fun DayOfWeek.toKoreanShort(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }
