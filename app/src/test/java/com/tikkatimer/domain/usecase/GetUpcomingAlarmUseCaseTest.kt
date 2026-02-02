package com.tikkatimer.domain.usecase

import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.domain.model.UpcomingInfo
import com.tikkatimer.domain.repository.AlarmRepository
import com.tikkatimer.domain.usecase.alarm.GetUpcomingAlarmUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * GetUpcomingAlarmUseCase 단위 테스트
 */
class GetUpcomingAlarmUseCaseTest {
    private lateinit var repository: AlarmRepository
    private lateinit var useCase: GetUpcomingAlarmUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetUpcomingAlarmUseCase(repository)
    }

    @Test
    fun `활성화된 알람이 없으면 EMPTY를 반환한다`() =
        runTest {
            every { repository.getEnabledAlarms() } returns flowOf(emptyList())

            val result = useCase().first()

            assertEquals(UpcomingInfo.EMPTY, result)
            assertNull(result.nextAlarmTime)
            assertEquals(0, result.activeAlarmCount)
        }

    @Test
    fun `활성화된 알람이 있으면 가장 가까운 알람 시간과 개수를 반환한다`() =
        runTest {
            val alarms = listOf(
                createTestAlarm(1, LocalTime.of(8, 0)),
                createTestAlarm(2, LocalTime.of(7, 0)),
                createTestAlarm(3, LocalTime.of(9, 0)),
            )
            every { repository.getEnabledAlarms() } returns flowOf(alarms)

            val result = useCase().first()

            assertNotNull(result.nextAlarmTime)
            assertEquals(3, result.activeAlarmCount)
        }

    @Test
    fun `알람 한 개만 있을 때 정상 동작한다`() =
        runTest {
            val alarms = listOf(createTestAlarm(1, LocalTime.of(6, 30)))
            every { repository.getEnabledAlarms() } returns flowOf(alarms)

            val result = useCase().first()

            assertNotNull(result.nextAlarmTime)
            assertEquals(1, result.activeAlarmCount)
        }

    @Test
    fun `반복 알람과 1회성 알람이 섞여있어도 정상 동작한다`() =
        runTest {
            val alarms = listOf(
                createTestAlarm(1, LocalTime.of(8, 0)), // 1회성
                createTestAlarm(2, LocalTime.of(7, 0), setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)), // 반복
            )
            every { repository.getEnabledAlarms() } returns flowOf(alarms)

            val result = useCase().first()

            assertNotNull(result.nextAlarmTime)
            assertEquals(2, result.activeAlarmCount)
        }

    private fun createTestAlarm(
        id: Long,
        time: LocalTime,
        repeatDays: Set<DayOfWeek> = emptySet(),
    ) = Alarm(
        id = id,
        time = time,
        isEnabled = true,
        label = "테스트 알람 $id",
        repeatDays = repeatDays,
    )
}
