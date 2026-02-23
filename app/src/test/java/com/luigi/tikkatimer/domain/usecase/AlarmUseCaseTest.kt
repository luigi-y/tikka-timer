package com.luigi.tikkatimer.domain.usecase

import com.luigi.tikkatimer.domain.model.Alarm
import com.luigi.tikkatimer.domain.repository.AlarmRepository
import com.luigi.tikkatimer.domain.scheduler.AlarmScheduler
import com.luigi.tikkatimer.domain.usecase.alarm.AddAlarmUseCase
import com.luigi.tikkatimer.domain.usecase.alarm.DeleteAlarmUseCase
import com.luigi.tikkatimer.domain.usecase.alarm.GetAlarmsUseCase
import com.luigi.tikkatimer.domain.usecase.alarm.ToggleAlarmUseCase
import com.luigi.tikkatimer.domain.usecase.alarm.UpdateAlarmUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

/**
 * Alarm UseCase 단위 테스트
 */
class AlarmUseCaseTest {
    private lateinit var repository: AlarmRepository
    private lateinit var scheduler: AlarmScheduler

    @Before
    fun setup() {
        repository = mockk()
        scheduler = mockk(relaxed = true)
    }

    @Test
    fun `GetAlarmsUseCase는 Repository의 getAllAlarms를 호출한다`() =
        runTest {
            val alarms = listOf(createTestAlarm(1), createTestAlarm(2))
            every { repository.getAllAlarms() } returns flowOf(alarms)

            val useCase = GetAlarmsUseCase(repository)
            val result = useCase().first()

            assertEquals(2, result.size)
        }

    @Test
    fun `AddAlarmUseCase는 Repository의 addAlarm을 호출하고 ID를 반환한다`() =
        runTest {
            val alarm = createTestAlarm()
            coEvery { repository.addAlarm(alarm) } returns 1L
            every { scheduler.schedule(any()) } just runs

            val useCase = AddAlarmUseCase(repository, scheduler)
            val result = useCase(alarm)

            assertEquals(1L, result)
            coVerify { repository.addAlarm(alarm) }
            verify { scheduler.schedule(alarm.copy(id = 1L)) }
        }

    @Test
    fun `UpdateAlarmUseCase는 Repository의 updateAlarm을 호출한다`() =
        runTest {
            val alarm = createTestAlarm()
            coEvery { repository.updateAlarm(alarm) } returns Unit

            val useCase = UpdateAlarmUseCase(repository, scheduler)
            useCase(alarm)

            coVerify { repository.updateAlarm(alarm) }
            verify { scheduler.cancel(alarm.id) }
            verify { scheduler.schedule(alarm) }
        }

    @Test
    fun `UpdateAlarmUseCase - 비활성화된 알람은 스케줄러에 등록하지 않는다`() =
        runTest {
            val alarm = createTestAlarm().copy(isEnabled = false)
            coEvery { repository.updateAlarm(alarm) } returns Unit

            val useCase = UpdateAlarmUseCase(repository, scheduler)
            useCase(alarm)

            coVerify { repository.updateAlarm(alarm) }
            verify { scheduler.cancel(alarm.id) }
            verify(exactly = 0) { scheduler.schedule(any()) }
        }

    @Test
    fun `DeleteAlarmUseCase는 Repository의 deleteAlarm을 호출한다`() =
        runTest {
            coEvery { repository.deleteAlarm(1L) } returns Unit

            val useCase = DeleteAlarmUseCase(repository, scheduler)
            useCase(1L)

            verify { scheduler.cancel(1L) }
            coVerify { repository.deleteAlarm(1L) }
        }

    @Test
    fun `ToggleAlarmUseCase - 활성화 시 스케줄러에 등록한다`() =
        runTest {
            val alarm = createTestAlarm().copy(isEnabled = false)
            coEvery { repository.setAlarmEnabled(1L, true) } returns Unit
            coEvery { repository.getAlarmById(1L) } returns alarm

            val useCase = ToggleAlarmUseCase(repository, scheduler)
            useCase(1L, true)

            coVerify { repository.setAlarmEnabled(1L, true) }
            verify { scheduler.schedule(alarm.copy(isEnabled = true)) }
        }

    @Test
    fun `ToggleAlarmUseCase - 비활성화 시 스케줄러에서 취소한다`() =
        runTest {
            coEvery { repository.setAlarmEnabled(1L, false) } returns Unit

            val useCase = ToggleAlarmUseCase(repository, scheduler)
            useCase(1L, false)

            coVerify { repository.setAlarmEnabled(1L, false) }
            verify { scheduler.cancel(1L) }
        }

    private fun createTestAlarm(id: Long = 1) =
        Alarm(
            id = id,
            time = LocalTime.of(8, 0),
            isEnabled = true,
            label = "테스트",
        )
}
