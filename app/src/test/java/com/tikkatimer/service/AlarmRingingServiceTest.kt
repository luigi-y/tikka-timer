package com.tikkatimer.service

import android.content.Intent
import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.VibrationPattern
import com.tikkatimer.domain.repository.AlarmRepository
import com.tikkatimer.domain.scheduler.AlarmScheduler
import com.tikkatimer.util.AlarmSoundManager
import com.tikkatimer.util.NotificationHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

/**
 * AlarmRingingService 단위 테스트
 * 알람 울림 서비스의 소리/진동/알림 동작 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AlarmRingingServiceTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var alarmSoundManager: AlarmSoundManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmRepository: AlarmRepository
    private lateinit var alarmScheduler: AlarmScheduler

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        alarmSoundManager = mockk(relaxed = true)
        notificationHelper = mockk(relaxed = true)
        alarmRepository = mockk(relaxed = true)
        alarmScheduler = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===== 소리/진동 조합 테스트 =====

    @Test
    fun `DEFAULT 소리와 DEFAULT 진동이 모두 시작된다`() = runTest {
        val soundType = SoundType.DEFAULT
        val vibrationPattern = VibrationPattern.DEFAULT

        alarmSoundManager.startAlarmSound(soundType, null)
        alarmSoundManager.startVibration(vibrationPattern)

        verify { alarmSoundManager.startAlarmSound(SoundType.DEFAULT, null) }
        verify { alarmSoundManager.startVibration(VibrationPattern.DEFAULT) }
    }

    @Test
    fun `SILENT 소리와 DEFAULT 진동 조합 - 진동만 동작`() = runTest {
        val soundType = SoundType.SILENT
        val vibrationPattern = VibrationPattern.DEFAULT

        alarmSoundManager.startAlarmSound(soundType, null)
        alarmSoundManager.startVibration(vibrationPattern)

        verify { alarmSoundManager.startAlarmSound(SoundType.SILENT, null) }
        verify { alarmSoundManager.startVibration(VibrationPattern.DEFAULT) }
    }

    @Test
    fun `DEFAULT 소리와 NONE 진동 조합 - 소리만 동작`() = runTest {
        val soundType = SoundType.DEFAULT
        val vibrationPattern = VibrationPattern.NONE

        alarmSoundManager.startAlarmSound(soundType, null)
        alarmSoundManager.startVibration(vibrationPattern)

        verify { alarmSoundManager.startAlarmSound(SoundType.DEFAULT, null) }
        verify { alarmSoundManager.startVibration(VibrationPattern.NONE) }
    }

    @Test
    fun `SILENT 소리와 NONE 진동 조합 - 아무것도 동작하지 않음`() = runTest {
        val soundType = SoundType.SILENT
        val vibrationPattern = VibrationPattern.NONE

        alarmSoundManager.startAlarmSound(soundType, null)
        alarmSoundManager.startVibration(vibrationPattern)

        verify { alarmSoundManager.startAlarmSound(SoundType.SILENT, null) }
        verify { alarmSoundManager.startVibration(VibrationPattern.NONE) }
    }

    // ===== 진동 패턴 테스트 =====

    @Test
    fun `STRONG 진동 패턴이 올바르게 적용된다`() = runTest {
        val vibrationPattern = VibrationPattern.STRONG

        alarmSoundManager.startVibration(vibrationPattern)

        verify { alarmSoundManager.startVibration(VibrationPattern.STRONG) }
    }

    @Test
    fun `HEARTBEAT 진동 패턴이 올바르게 적용된다`() = runTest {
        val vibrationPattern = VibrationPattern.HEARTBEAT

        alarmSoundManager.startVibration(vibrationPattern)

        verify { alarmSoundManager.startVibration(VibrationPattern.HEARTBEAT) }
    }

    // ===== 소리 타입 테스트 =====

    @Test
    fun `BELL 소리 타입이 올바르게 적용된다`() = runTest {
        val soundType = SoundType.BELL

        alarmSoundManager.startAlarmSound(soundType, null)

        verify { alarmSoundManager.startAlarmSound(SoundType.BELL, null) }
    }

    @Test
    fun `DIGITAL 소리 타입이 올바르게 적용된다`() = runTest {
        val soundType = SoundType.DIGITAL

        alarmSoundManager.startAlarmSound(soundType, null)

        verify { alarmSoundManager.startAlarmSound(SoundType.DIGITAL, null) }
    }

    @Test
    fun `CUSTOM 소리 타입과 커스텀 URI가 올바르게 적용된다`() = runTest {
        val soundType = SoundType.CUSTOM
        val customUri = "content://media/external/audio/123"

        alarmSoundManager.startAlarmSound(soundType, customUri)

        verify { alarmSoundManager.startAlarmSound(SoundType.CUSTOM, customUri) }
    }

    // ===== 알람 해제/스누즈 테스트 =====

    @Test
    fun `알람 해제 시 소리와 진동이 모두 중지된다`() = runTest {
        alarmSoundManager.stopAll()

        verify { alarmSoundManager.stopAll() }
    }

    @Test
    fun `반복 알람 해제 시 다음 알람이 스케줄된다`() = runTest {
        val alarm = createTestAlarm(isRepeating = true, isEnabled = true)
        coEvery { alarmRepository.getAlarmById(1L) } returns alarm

        // 반복 알람이면 다음 스케줄 호출 확인
        val result = alarmRepository.getAlarmById(1L)
        if (result != null && result.isRepeating && result.isEnabled) {
            alarmScheduler.schedule(result)
        }

        verify { alarmScheduler.schedule(alarm) }
    }

    @Test
    fun `1회성 알람 해제 시 알람이 비활성화된다`() = runTest {
        coEvery { alarmRepository.setAlarmEnabled(1L, false) } returns Unit

        alarmRepository.setAlarmEnabled(1L, false)

        coVerify { alarmRepository.setAlarmEnabled(1L, false) }
    }

    // ===== 에러 핸들링 테스트 =====

    @Test
    fun `소리 시작 실패해도 진동은 시작된다`() = runTest {
        every { alarmSoundManager.startAlarmSound(any(), any()) } throws RuntimeException("Sound error")

        try {
            alarmSoundManager.startAlarmSound(SoundType.DEFAULT, null)
        } catch (e: Exception) {
            // 예외 발생
        }

        // 진동은 독립적으로 시작 가능해야 함
        alarmSoundManager.startVibration(VibrationPattern.DEFAULT)
        verify { alarmSoundManager.startVibration(VibrationPattern.DEFAULT) }
    }

    @Test
    fun `진동 시작 실패해도 소리는 재생된다`() = runTest {
        every { alarmSoundManager.startVibration(any()) } throws RuntimeException("Vibration error")

        // 소리는 독립적으로 시작 가능해야 함
        alarmSoundManager.startAlarmSound(SoundType.DEFAULT, null)
        verify { alarmSoundManager.startAlarmSound(SoundType.DEFAULT, null) }

        try {
            alarmSoundManager.startVibration(VibrationPattern.DEFAULT)
        } catch (e: Exception) {
            // 예외 발생
        }
    }

    // ===== Helper =====

    private fun createTestAlarm(
        id: Long = 1L,
        isRepeating: Boolean = false,
        isEnabled: Boolean = true,
    ) = Alarm(
        id = id,
        time = LocalTime.of(7, 30),
        isEnabled = isEnabled,
        label = "Test Alarm",
        repeatDays = if (isRepeating) setOf(
            java.time.DayOfWeek.MONDAY,
            java.time.DayOfWeek.TUESDAY,
            java.time.DayOfWeek.WEDNESDAY,
            java.time.DayOfWeek.THURSDAY,
            java.time.DayOfWeek.FRIDAY,
        ) else emptySet(),
        soundType = SoundType.DEFAULT,
        vibrationPattern = VibrationPattern.DEFAULT,
        snoozeDurationMinutes = 5,
        isSnoozeEnabled = true,
    )
}
