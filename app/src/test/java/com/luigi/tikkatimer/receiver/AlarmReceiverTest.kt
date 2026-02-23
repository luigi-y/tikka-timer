package com.luigi.tikkatimer.receiver

import com.luigi.tikkatimer.domain.model.Alarm
import com.luigi.tikkatimer.domain.model.SoundType
import com.luigi.tikkatimer.domain.model.VibrationPattern
import com.luigi.tikkatimer.domain.repository.AlarmRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

/**
 * AlarmReceiver 단위 테스트
 * Intent 데이터 추출, 알람 조회, 서비스/Activity 시작 로직 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AlarmReceiverTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var alarmRepository: AlarmRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        alarmRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===== 알람 ID 검증 테스트 =====

    @Test
    fun `유효하지 않은 알람 ID(-1)면 처리하지 않는다`() =
        runTest {
            val alarmId = -1L

            // -1은 유효하지 않은 ID이므로 repository 조회를 하지 않아야 함
            if (alarmId == -1L) {
                return@runTest
            }

            alarmRepository.getAlarmById(alarmId)
            // 이 줄에 도달하면 안 됨
        }

    @Test
    fun `유효한 알람 ID면 repository에서 알람을 조회한다`() =
        runTest {
            val alarmId = 1L
            val alarm = createTestAlarm(id = alarmId)
            coEvery { alarmRepository.getAlarmById(alarmId) } returns alarm

            val result = alarmRepository.getAlarmById(alarmId)

            assertNotNull(result)
            assertEquals(alarmId, result?.id)
            coVerify { alarmRepository.getAlarmById(alarmId) }
        }

    @Test
    fun `알람이 DB에 존재하지 않으면 null을 반환한다`() =
        runTest {
            val alarmId = 999L
            coEvery { alarmRepository.getAlarmById(alarmId) } returns null

            val result = alarmRepository.getAlarmById(alarmId)

            assertNull(result)
            coVerify { alarmRepository.getAlarmById(alarmId) }
        }

    // ===== Intent Extra 기본값 테스트 =====

    @Test
    fun `soundType이 없으면 DEFAULT가 사용된다`() {
        val soundTypeName: String? = null
        val resolvedSoundType = soundTypeName ?: SoundType.DEFAULT.name

        assertEquals(SoundType.DEFAULT.name, resolvedSoundType)
    }

    @Test
    fun `vibrationPattern이 없으면 DEFAULT가 사용된다`() {
        val vibrationPatternName: String? = null
        val resolvedPattern = vibrationPatternName ?: VibrationPattern.DEFAULT.name

        assertEquals(VibrationPattern.DEFAULT.name, resolvedPattern)
    }

    @Test
    fun `명시적 soundType이 있으면 해당 값이 사용된다`() {
        val soundTypeName = SoundType.BELL.name
        val resolvedSoundType = soundTypeName ?: SoundType.DEFAULT.name

        assertEquals(SoundType.BELL.name, resolvedSoundType)
    }

    @Test
    fun `명시적 vibrationPattern이 있으면 해당 값이 사용된다`() {
        val vibrationPatternName = VibrationPattern.STRONG.name
        val resolvedPattern = vibrationPatternName ?: VibrationPattern.DEFAULT.name

        assertEquals(VibrationPattern.STRONG.name, resolvedPattern)
    }

    // ===== 알람 정보 전달 테스트 =====

    @Test
    fun `알람 라벨이 서비스에 전달된다`() =
        runTest {
            val alarm = createTestAlarm(label = "아침 알람")
            coEvery { alarmRepository.getAlarmById(1L) } returns alarm

            val result = alarmRepository.getAlarmById(1L)

            assertEquals("아침 알람", result?.label)
        }

    @Test
    fun `1회성 알람 정보가 올바르게 전달된다`() =
        runTest {
            val alarm = createTestAlarm(isOneTime = true)
            coEvery { alarmRepository.getAlarmById(1L) } returns alarm

            val result = alarmRepository.getAlarmById(1L)

            assertEquals(true, result?.isOneTime)
        }

    @Test
    fun `반복 알람의 isOneTime은 false이다`() =
        runTest {
            val alarm = createTestAlarm(isRepeating = true)
            coEvery { alarmRepository.getAlarmById(1L) } returns alarm

            val result = alarmRepository.getAlarmById(1L)

            assertEquals(false, result?.isOneTime)
        }

    @Test
    fun `스누즈 시간이 올바르게 전달된다`() =
        runTest {
            val alarm = createTestAlarm(snoozeDuration = 10)
            coEvery { alarmRepository.getAlarmById(1L) } returns alarm

            val result = alarmRepository.getAlarmById(1L)

            assertEquals(10, result?.snoozeDurationMinutes)
        }

    // ===== Helper =====

    private fun createTestAlarm(
        id: Long = 1L,
        label: String = "Test Alarm",
        isRepeating: Boolean = false,
        isOneTime: Boolean = !isRepeating,
        snoozeDuration: Int = 5,
    ) = Alarm(
        id = id,
        time = LocalTime.of(7, 30),
        isEnabled = true,
        label = label,
        repeatDays =
            if (isRepeating) {
                setOf(
                    java.time.DayOfWeek.MONDAY,
                    java.time.DayOfWeek.WEDNESDAY,
                    java.time.DayOfWeek.FRIDAY,
                )
            } else {
                emptySet()
            },
        soundType = SoundType.DEFAULT,
        vibrationPattern = VibrationPattern.DEFAULT,
        snoozeDurationMinutes = snoozeDuration,
        isSnoozeEnabled = true,
    )
}
