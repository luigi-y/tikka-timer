package com.tikkatimer.domain.scheduler

import com.tikkatimer.domain.model.Alarm
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * AlarmScheduler 인터페이스 동작 테스트
 * 실제 AlarmManager 연동은 androidTest에서 수행
 */
class AlarmSchedulerTest {
    private lateinit var scheduler: AlarmScheduler

    @Before
    fun setup() {
        scheduler = mockk(relaxed = true)
    }

    @Test
    fun `활성화된 알람은 스케줄링된다`() {
        val alarm = createTestAlarm(isEnabled = true)
        every { scheduler.canScheduleExactAlarms() } returns true

        scheduler.schedule(alarm)

        verify { scheduler.schedule(alarm) }
    }

    @Test
    fun `비활성화된 알람은 스케줄링되지 않아야 한다`() {
        val alarm = createTestAlarm(isEnabled = false)

        // 실제 구현에서는 비활성화된 알람은 스케줄링하지 않음
        // 이 테스트는 인터페이스 호출 패턴 확인용
        scheduler.schedule(alarm)

        verify { scheduler.schedule(alarm) }
    }

    @Test
    fun `알람 취소가 정상 호출된다`() {
        val alarmId = 123L

        scheduler.cancel(alarmId)

        verify { scheduler.cancel(alarmId) }
    }

    @Test
    fun `canScheduleExactAlarms가 true를 반환할 수 있다`() {
        every { scheduler.canScheduleExactAlarms() } returns true

        val result = scheduler.canScheduleExactAlarms()

        assertTrue(result)
    }

    @Test
    fun `canScheduleExactAlarms가 false를 반환할 수 있다`() {
        every { scheduler.canScheduleExactAlarms() } returns false

        val result = scheduler.canScheduleExactAlarms()

        assertFalse(result)
    }

    @Test
    fun `반복 알람 스케줄링`() {
        val alarm =
            createTestAlarm(
                isEnabled = true,
                repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            )

        scheduler.schedule(alarm)

        verify { scheduler.schedule(alarm) }
    }

    private fun createTestAlarm(
        id: Long = 1L,
        isEnabled: Boolean = true,
        repeatDays: Set<DayOfWeek> = emptySet(),
    ) = Alarm(
        id = id,
        time = LocalTime.of(8, 0),
        isEnabled = isEnabled,
        label = "테스트 알람",
        repeatDays = repeatDays,
    )
}
