package com.tikkatimer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Alarm 도메인 모델 단위 테스트
 */
class AlarmTest {
    @Test
    fun `반복 요일이 없으면 isRepeating은 false이다`() {
        val alarm =
            Alarm(
                time = LocalTime.of(8, 0),
                repeatDays = emptySet(),
            )

        assertFalse(alarm.isRepeating)
    }

    @Test
    fun `반복 요일이 있으면 isRepeating은 true이다`() {
        val alarm =
            Alarm(
                time = LocalTime.of(8, 0),
                repeatDays = setOf(DayOfWeek.MONDAY),
            )

        assertTrue(alarm.isRepeating)
    }

    @Test
    fun `모든 요일이 선택되면 '매일'을 반환한다`() {
        val alarm =
            Alarm(
                time = LocalTime.of(8, 0),
                repeatDays = DayOfWeek.entries.toSet(),
            )

        assertEquals("매일", alarm.getRepeatDaysText())
    }

    @Test
    fun `평일만 선택되면 '평일'을 반환한다`() {
        val alarm =
            Alarm(
                time = LocalTime.of(8, 0),
                repeatDays =
                    setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                    ),
            )

        assertEquals("평일", alarm.getRepeatDaysText())
    }

    @Test
    fun `주말만 선택되면 '주말'을 반환한다`() {
        val alarm =
            Alarm(
                time = LocalTime.of(8, 0),
                repeatDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
            )

        assertEquals("주말", alarm.getRepeatDaysText())
    }

    @Test
    fun `반복 요일이 없으면 '반복 안함'을 반환한다`() {
        val alarm =
            Alarm(
                time = LocalTime.of(8, 0),
                repeatDays = emptySet(),
            )

        assertEquals("반복 안함", alarm.getRepeatDaysText())
    }

    @Test
    fun `특정 요일만 선택되면 요일 약어를 반환한다`() {
        val alarm =
            Alarm(
                time = LocalTime.of(8, 0),
                repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            )

        assertEquals("월, 수, 금", alarm.getRepeatDaysText())
    }

    @Test
    fun `오전 시간 포맷이 올바르다`() {
        val alarm =
            Alarm(
                time = LocalTime.of(8, 30),
            )

        assertEquals("오전 8:30", alarm.getTimeText())
    }

    @Test
    fun `오후 시간 포맷이 올바르다`() {
        val alarm =
            Alarm(
                time = LocalTime.of(14, 45),
            )

        assertEquals("오후 2:45", alarm.getTimeText())
    }

    @Test
    fun `자정 시간 포맷이 올바르다`() {
        val alarm =
            Alarm(
                time = LocalTime.of(0, 0),
            )

        assertEquals("오전 12:00", alarm.getTimeText())
    }

    @Test
    fun `정오 시간 포맷이 올바르다`() {
        val alarm =
            Alarm(
                time = LocalTime.of(12, 0),
            )

        assertEquals("오후 12:00", alarm.getTimeText())
    }

    @Test
    fun `분이 한 자리일 때 0이 패딩된다`() {
        val alarm =
            Alarm(
                time = LocalTime.of(9, 5),
            )

        assertEquals("오전 9:05", alarm.getTimeText())
    }
}
