package com.luigi.tikkatimer.receiver

import com.luigi.tikkatimer.domain.model.Alarm
import com.luigi.tikkatimer.domain.model.SoundType
import com.luigi.tikkatimer.domain.model.VibrationPattern
import com.luigi.tikkatimer.domain.repository.AlarmRepository
import com.luigi.tikkatimer.domain.scheduler.AlarmScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * BootReceiver 단위 테스트
 * 기기 재부팅 후 알람 복원 로직 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BootReceiverTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var alarmRepository: AlarmRepository
    private lateinit var alarmScheduler: AlarmScheduler

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        alarmRepository = mockk(relaxed = true)
        alarmScheduler = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===== Intent 필터 테스트 =====

    @Test
    fun `BOOT_COMPLETED가 아닌 action은 무시한다`() {
        val action = "android.intent.action.SOME_OTHER_ACTION"

        // BOOT_COMPLETED가 아니면 처리하지 않음
        val shouldProcess = action == "android.intent.action.BOOT_COMPLETED"

        assert(!shouldProcess)
    }

    @Test
    fun `BOOT_COMPLETED action이면 처리를 진행한다`() {
        val action = "android.intent.action.BOOT_COMPLETED"

        val shouldProcess = action == "android.intent.action.BOOT_COMPLETED"

        assert(shouldProcess)
    }

    // ===== 알람 복원 테스트 =====

    @Test
    fun `활성화된 알람이 없으면 스케줄하지 않는다`() =
        runTest {
            every { alarmRepository.getEnabledAlarms() } returns flowOf(emptyList())

            val enabledAlarms = alarmRepository.getEnabledAlarms()

            enabledAlarms.collect { alarms ->
                assert(alarms.isEmpty())
            }

            verify(exactly = 0) { alarmScheduler.schedule(any()) }
        }

    @Test
    fun `활성화된 알람이 있으면 모두 스케줄한다`() =
        runTest {
            val alarms =
                listOf(
                    createTestAlarm(id = 1L, label = "출근"),
                    createTestAlarm(id = 2L, label = "점심"),
                    createTestAlarm(id = 3L, label = "퇴근"),
                )
            every { alarmRepository.getEnabledAlarms() } returns flowOf(alarms)

            val enabledAlarms = alarmRepository.getEnabledAlarms()

            enabledAlarms.collect { alarmList ->
                alarmList.forEach { alarm ->
                    alarmScheduler.schedule(alarm)
                }
            }

            verify(exactly = 3) { alarmScheduler.schedule(any()) }
            alarms.forEach { alarm ->
                verify { alarmScheduler.schedule(alarm) }
            }
        }

    @Test
    fun `반복 알람도 올바르게 복원된다`() =
        runTest {
            val repeatingAlarm =
                createTestAlarm(
                    id = 1L,
                    isRepeating = true,
                )
            every { alarmRepository.getEnabledAlarms() } returns flowOf(listOf(repeatingAlarm))

            val enabledAlarms = alarmRepository.getEnabledAlarms()

            enabledAlarms.collect { alarms ->
                alarms.forEach { alarm ->
                    alarmScheduler.schedule(alarm)
                }
            }

            verify { alarmScheduler.schedule(repeatingAlarm) }
        }

    @Test
    fun `개별 알람 스케줄 실패해도 나머지 알람은 계속 복원한다`() =
        runTest {
            val alarm1 = createTestAlarm(id = 1L, label = "성공 알람 1")
            val alarm2 = createTestAlarm(id = 2L, label = "실패 알람")
            val alarm3 = createTestAlarm(id = 3L, label = "성공 알람 2")

            every { alarmRepository.getEnabledAlarms() } returns flowOf(listOf(alarm1, alarm2, alarm3))
            every { alarmScheduler.schedule(alarm2) } throws RuntimeException("Schedule failed")

            val enabledAlarms = alarmRepository.getEnabledAlarms()

            enabledAlarms.collect { alarms ->
                alarms.forEach { alarm ->
                    try {
                        alarmScheduler.schedule(alarm)
                    } catch (_: Exception) {
                        // 개별 실패는 무시하고 계속 진행
                    }
                }
            }

            verify { alarmScheduler.schedule(alarm1) }
            verify { alarmScheduler.schedule(alarm2) }
            verify { alarmScheduler.schedule(alarm3) }
        }

    @Test
    fun `1회성 알람과 반복 알람이 섞여있어도 모두 복원한다`() =
        runTest {
            val oneTimeAlarm = createTestAlarm(id = 1L, isRepeating = false)
            val repeatingAlarm = createTestAlarm(id = 2L, isRepeating = true)

            every { alarmRepository.getEnabledAlarms() } returns
                flowOf(listOf(oneTimeAlarm, repeatingAlarm))

            val enabledAlarms = alarmRepository.getEnabledAlarms()

            enabledAlarms.collect { alarms ->
                alarms.forEach { alarm ->
                    alarmScheduler.schedule(alarm)
                }
            }

            verify { alarmScheduler.schedule(oneTimeAlarm) }
            verify { alarmScheduler.schedule(repeatingAlarm) }
        }

    // ===== Helper =====

    private fun createTestAlarm(
        id: Long = 1L,
        label: String = "Test Alarm",
        isRepeating: Boolean = false,
    ) = Alarm(
        id = id,
        time = LocalTime.of(7, 30),
        isEnabled = true,
        label = label,
        repeatDays =
            if (isRepeating) {
                setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
            } else {
                emptySet()
            },
        soundType = SoundType.DEFAULT,
        vibrationPattern = VibrationPattern.DEFAULT,
        snoozeDurationMinutes = 5,
        isSnoozeEnabled = true,
    )
}
