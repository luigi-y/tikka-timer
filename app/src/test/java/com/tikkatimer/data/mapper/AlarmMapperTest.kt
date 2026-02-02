package com.tikkatimer.data.mapper

import com.tikkatimer.data.local.entity.AlarmEntity
import com.tikkatimer.domain.model.Alarm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * AlarmMapper 단위 테스트
 */
class AlarmMapperTest {
    @Test
    fun `AlarmEntity를 Alarm으로 변환한다`() {
        val entity =
            AlarmEntity(
                id = 1,
                hour = 8,
                minute = 30,
                isEnabled = true,
                label = "테스트 알람",
                repeatDays = 0,
                soundType = "DEFAULT",
                vibrationPattern = "DEFAULT",
                ringtoneUri = null,
                snoozeDurationMinutes = 5,
                isSnoozeEnabled = true,
            )

        val alarm = entity.toDomain()

        assertEquals(1L, alarm.id)
        assertEquals(LocalTime.of(8, 30), alarm.time)
        assertEquals(true, alarm.isEnabled)
        assertEquals("테스트 알람", alarm.label)
        assertTrue(alarm.repeatDays.isEmpty())
    }

    @Test
    fun `Alarm을 AlarmEntity로 변환한다`() {
        val alarm =
            Alarm(
                id = 1,
                time = LocalTime.of(8, 30),
                isEnabled = true,
                label = "테스트 알람",
                repeatDays = emptySet(),
                ringtoneUri = null,
                snoozeDurationMinutes = 5,
                isSnoozeEnabled = true,
            )

        val entity = alarm.toEntity()

        assertEquals(1L, entity.id)
        assertEquals(8, entity.hour)
        assertEquals(30, entity.minute)
        assertEquals(true, entity.isEnabled)
        assertEquals("테스트 알람", entity.label)
        assertEquals(0, entity.repeatDays)
    }

    @Test
    fun `반복 요일 비트마스크가 올바르게 변환된다 - 월수금`() {
        val alarm =
            Alarm(
                id = 1,
                time = LocalTime.of(8, 0),
                repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            )

        val entity = alarm.toEntity()
        // 월(2) + 수(8) + 금(32) = 42
        assertEquals(42, entity.repeatDays)

        // 다시 변환
        val converted = entity.toDomain()
        assertEquals(3, converted.repeatDays.size)
        assertTrue(DayOfWeek.MONDAY in converted.repeatDays)
        assertTrue(DayOfWeek.WEDNESDAY in converted.repeatDays)
        assertTrue(DayOfWeek.FRIDAY in converted.repeatDays)
    }

    @Test
    fun `반복 요일 비트마스크가 올바르게 변환된다 - 매일`() {
        val alarm =
            Alarm(
                id = 1,
                time = LocalTime.of(8, 0),
                repeatDays = DayOfWeek.entries.toSet(),
            )

        val entity = alarm.toEntity()
        // 일(1) + 월(2) + 화(4) + 수(8) + 목(16) + 금(32) + 토(64) = 127
        assertEquals(127, entity.repeatDays)

        val converted = entity.toDomain()
        assertEquals(7, converted.repeatDays.size)
    }

    @Test
    fun `반복 요일 비트마스크가 올바르게 변환된다 - 주말`() {
        val alarm =
            Alarm(
                id = 1,
                time = LocalTime.of(9, 0),
                repeatDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
            )

        val entity = alarm.toEntity()
        // 일(1) + 토(64) = 65
        assertEquals(65, entity.repeatDays)

        val converted = entity.toDomain()
        assertEquals(2, converted.repeatDays.size)
        assertTrue(DayOfWeek.SATURDAY in converted.repeatDays)
        assertTrue(DayOfWeek.SUNDAY in converted.repeatDays)
    }

    @Test
    fun `ringtoneUri가 null이면 null로 유지된다`() {
        val alarm =
            Alarm(
                id = 1,
                time = LocalTime.of(8, 0),
                ringtoneUri = null,
            )

        val entity = alarm.toEntity()
        assertEquals(null, entity.ringtoneUri)

        val converted = entity.toDomain()
        assertEquals(null, converted.ringtoneUri)
    }

    @Test
    fun `ringtoneUri가 있으면 유지된다`() {
        val uri = "content://media/alarm/test"
        val alarm =
            Alarm(
                id = 1,
                time = LocalTime.of(8, 0),
                ringtoneUri = uri,
            )

        val entity = alarm.toEntity()
        assertEquals(uri, entity.ringtoneUri)

        val converted = entity.toDomain()
        assertEquals(uri, converted.ringtoneUri)
    }
}
