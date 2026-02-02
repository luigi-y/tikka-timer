package com.tikkatimer.domain.usecase

import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.domain.repository.AlarmRepository
import com.tikkatimer.domain.usecase.alarm.DisableOneTimeAlarmUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * DisableOneTimeAlarmUseCase 단위 테스트
 */
class DisableOneTimeAlarmUseCaseTest {
    private lateinit var repository: AlarmRepository
    private lateinit var useCase: DisableOneTimeAlarmUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DisableOneTimeAlarmUseCase(repository)
    }

    @Test
    fun `1회성 알람은 비활성화된다`() =
        runTest {
            val oneTimeAlarm = createTestAlarm(id = 1, repeatDays = emptySet())
            coEvery { repository.getAlarmById(1L) } returns oneTimeAlarm
            coEvery { repository.setAlarmEnabled(1L, false) } returns Unit

            val result = useCase(1L)

            assertTrue(result)
            coVerify { repository.setAlarmEnabled(1L, false) }
        }

    @Test
    fun `반복 알람은 비활성화되지 않는다`() =
        runTest {
            val repeatingAlarm = createTestAlarm(
                id = 2,
                repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            )
            coEvery { repository.getAlarmById(2L) } returns repeatingAlarm

            val result = useCase(2L)

            assertFalse(result)
            coVerify(exactly = 0) { repository.setAlarmEnabled(any(), any()) }
        }

    @Test
    fun `존재하지 않는 알람 ID는 false를 반환한다`() =
        runTest {
            coEvery { repository.getAlarmById(999L) } returns null

            val result = useCase(999L)

            assertFalse(result)
            coVerify(exactly = 0) { repository.setAlarmEnabled(any(), any()) }
        }

    @Test
    fun `매일 반복 알람은 비활성화되지 않는다`() =
        runTest {
            val everydayAlarm = createTestAlarm(
                id = 3,
                repeatDays = DayOfWeek.entries.toSet(),
            )
            coEvery { repository.getAlarmById(3L) } returns everydayAlarm

            val result = useCase(3L)

            assertFalse(result)
        }

    @Test
    fun `평일 반복 알람은 비활성화되지 않는다`() =
        runTest {
            val weekdayAlarm = createTestAlarm(
                id = 4,
                repeatDays = setOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                ),
            )
            coEvery { repository.getAlarmById(4L) } returns weekdayAlarm

            val result = useCase(4L)

            assertFalse(result)
        }

    private fun createTestAlarm(
        id: Long,
        repeatDays: Set<DayOfWeek>,
    ) = Alarm(
        id = id,
        time = LocalTime.of(8, 0),
        isEnabled = true,
        label = "테스트",
        repeatDays = repeatDays,
    )
}
