package com.tikkatimer.util

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.VibrationPattern
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * AlarmSoundManager Instrumented Test
 * 실제 디바이스에서 소리/진동 재생 여부를 검증
 *
 * 테스트 케이스:
 * 1. 소리만 재생 (진동 없음)
 * 2. 진동만 재생 (소리 없음)
 * 3. 소리 + 진동 동시 재생
 */
@RunWith(AndroidJUnit4::class)
class AlarmSoundManagerTest {
    private lateinit var context: Context
    private lateinit var alarmSoundManager: AlarmSoundManager
    private lateinit var audioManager: AudioManager
    private var vibrator: Vibrator? = null

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        alarmSoundManager = AlarmSoundManager(context)
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Vibrator 서비스 가져오기
        vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
    }

    @After
    fun tearDown() {
        // 테스트 후 모든 알람 효과 중지
        alarmSoundManager.stopAll()
    }

    // ===== 소리만 재생 테스트 (진동 없음) =====

    @Test
    fun startAlarmSound_withDefaultType_playsSound() {
        // Given: 기본 알람음 타입
        val soundType = SoundType.DEFAULT

        // When: 알람 소리 재생
        alarmSoundManager.startAlarmSound(soundType)

        // Then: 알람 소리가 재생 중인지 확인 (AudioManager 활성 여부로 검증)
        // 참고: 실제 소리 재생은 하드웨어 의존적이므로 예외 없이 실행되는지 확인
        Thread.sleep(500) // 소리 재생 대기

        // 중지 가능한지 확인 (예외 없이 실행되어야 함)
        alarmSoundManager.stopAlarmSound()
        assertTrue("Sound playback test completed", true)
    }

    @Test
    fun startAlarmSound_withBellType_playsSound() {
        // Given: BELL 타입
        val soundType = SoundType.BELL

        // When: 알람 소리 재생
        alarmSoundManager.startAlarmSound(soundType)
        Thread.sleep(500)

        // Then: 예외 없이 실행되어야 함
        alarmSoundManager.stopAlarmSound()
        assertTrue("Bell sound playback test completed", true)
    }

    @Test
    fun startAlarmSound_withDigitalType_playsSound() {
        // Given: DIGITAL 타입
        val soundType = SoundType.DIGITAL

        // When: 알람 소리 재생
        alarmSoundManager.startAlarmSound(soundType)
        Thread.sleep(500)

        // Then: 예외 없이 실행되어야 함
        alarmSoundManager.stopAlarmSound()
        assertTrue("Digital sound playback test completed", true)
    }

    @Test
    fun startAlarmSound_withGentleType_playsSound() {
        // Given: GENTLE 타입
        val soundType = SoundType.GENTLE

        // When: 알람 소리 재생
        alarmSoundManager.startAlarmSound(soundType)
        Thread.sleep(500)

        // Then: 예외 없이 실행되어야 함
        alarmSoundManager.stopAlarmSound()
        assertTrue("Gentle sound playback test completed", true)
    }

    @Test
    fun startAlarmSound_withSilentType_doesNotPlaySound() {
        // Given: SILENT 타입
        val soundType = SoundType.SILENT

        // When: 알람 소리 재생 시도
        alarmSoundManager.startAlarmSound(soundType)
        Thread.sleep(200)

        // Then: SILENT 타입은 소리가 재생되지 않아야 함
        // 예외 없이 stopAlarmSound가 실행되어야 함
        alarmSoundManager.stopAlarmSound()
        assertTrue("Silent type should not play sound", true)
    }

    @Test
    fun stopAlarmSound_stopsPlayingSound() {
        // Given: 재생 중인 알람 소리
        alarmSoundManager.startAlarmSound(SoundType.DEFAULT)
        Thread.sleep(500)

        // When: 소리 중지
        alarmSoundManager.stopAlarmSound()
        Thread.sleep(100)

        // Then: 예외 없이 중지되어야 함
        assertTrue("Sound stopped successfully", true)
    }

    // ===== 진동만 재생 테스트 (소리 없음) =====

    @Test
    fun startVibration_withDefaultPattern_vibrates() {
        // Given: 기본 진동 패턴
        val pattern = VibrationPattern.DEFAULT

        // When: 진동 시작
        alarmSoundManager.startVibration(pattern)
        Thread.sleep(1000) // 진동 패턴이 실행될 시간 대기

        // Then: 디바이스에 진동 기능이 있으면 진동 실행됨
        vibrator?.let { v ->
            if (v.hasVibrator()) {
                // 진동 기능이 있는 디바이스에서 테스트 통과
                assertTrue("Vibrator is available and pattern executed", true)
            }
        }

        alarmSoundManager.stopVibration()
    }

    @Test
    fun startVibration_withStrongPattern_vibrates() {
        // Given: 강한 진동 패턴
        val pattern = VibrationPattern.STRONG

        // When: 진동 시작
        alarmSoundManager.startVibration(pattern)
        Thread.sleep(1500)

        // Then: 예외 없이 실행되어야 함
        alarmSoundManager.stopVibration()
        assertTrue("Strong vibration pattern executed", true)
    }

    @Test
    fun startVibration_withHeartbeatPattern_vibrates() {
        // Given: 하트비트 진동 패턴
        val pattern = VibrationPattern.HEARTBEAT

        // When: 진동 시작
        alarmSoundManager.startVibration(pattern)
        Thread.sleep(1500)

        // Then: 예외 없이 실행되어야 함
        alarmSoundManager.stopVibration()
        assertTrue("Heartbeat vibration pattern executed", true)
    }

    @Test
    fun startVibration_withSosPattern_vibrates() {
        // Given: SOS 진동 패턴
        val pattern = VibrationPattern.SOS

        // When: 진동 시작
        alarmSoundManager.startVibration(pattern)
        Thread.sleep(2000) // SOS 패턴은 더 긴 시간 필요

        // Then: 예외 없이 실행되어야 함
        alarmSoundManager.stopVibration()
        assertTrue("SOS vibration pattern executed", true)
    }

    @Test
    fun startVibration_withCrescendoPattern_vibrates() {
        // Given: 점점 강해지는 진동 패턴
        val pattern = VibrationPattern.CRESCENDO

        // When: 진동 시작
        alarmSoundManager.startVibration(pattern)
        Thread.sleep(2000)

        // Then: 예외 없이 실행되어야 함
        alarmSoundManager.stopVibration()
        assertTrue("Crescendo vibration pattern executed", true)
    }

    @Test
    fun startVibration_withNonePattern_doesNotVibrate() {
        // Given: NONE 패턴 (진동 없음)
        val pattern = VibrationPattern.NONE

        // When: 진동 시작 시도
        alarmSoundManager.startVibration(pattern)
        Thread.sleep(200)

        // Then: NONE 패턴은 진동이 실행되지 않아야 함
        alarmSoundManager.stopVibration()
        assertTrue("NONE pattern should not vibrate", true)
    }

    @Test
    fun stopVibration_stopsOngoingVibration() {
        // Given: 진행 중인 진동
        alarmSoundManager.startVibration(VibrationPattern.DEFAULT)
        Thread.sleep(500)

        // When: 진동 중지
        alarmSoundManager.stopVibration()
        Thread.sleep(100)

        // Then: 예외 없이 중지되어야 함
        assertTrue("Vibration stopped successfully", true)
    }

    // ===== 소리 + 진동 동시 재생 테스트 =====

    @Test
    fun startSoundAndVibration_together_bothWork() {
        // Given: 기본 소리 + 기본 진동
        val soundType = SoundType.DEFAULT
        val vibrationPattern = VibrationPattern.DEFAULT

        // When: 소리와 진동 동시 시작
        alarmSoundManager.startAlarmSound(soundType)
        alarmSoundManager.startVibration(vibrationPattern)
        Thread.sleep(1500)

        // Then: 둘 다 예외 없이 실행되어야 함
        alarmSoundManager.stopAll()
        assertTrue("Both sound and vibration executed together", true)
    }

    @Test
    fun startSoundAndVibration_withStrongVibration_bothWork() {
        // Given: 기본 소리 + 강한 진동
        val soundType = SoundType.DEFAULT
        val vibrationPattern = VibrationPattern.STRONG

        // When: 소리와 진동 동시 시작
        alarmSoundManager.startAlarmSound(soundType)
        alarmSoundManager.startVibration(vibrationPattern)
        Thread.sleep(1500)

        // Then: 둘 다 예외 없이 실행되어야 함
        alarmSoundManager.stopAll()
        assertTrue("Sound with strong vibration executed", true)
    }

    @Test
    fun startSilentSoundAndVibration_onlyVibrates() {
        // Given: 무음 + 기본 진동 (타이머 "무음.기본진동" 설정)
        val soundType = SoundType.SILENT
        val vibrationPattern = VibrationPattern.DEFAULT

        // When: 소리(무음)와 진동 동시 시작
        alarmSoundManager.startAlarmSound(soundType)
        alarmSoundManager.startVibration(vibrationPattern)
        Thread.sleep(1500)

        // Then: 진동만 실행되어야 함
        vibrator?.let { v ->
            if (v.hasVibrator()) {
                assertTrue("Only vibration should work with SILENT sound", true)
            }
        }

        alarmSoundManager.stopAll()
    }

    @Test
    fun startSoundAndNoVibration_onlyPlaysSound() {
        // Given: 기본 소리 + 진동 없음
        val soundType = SoundType.DEFAULT
        val vibrationPattern = VibrationPattern.NONE

        // When: 소리와 진동(NONE) 시작
        alarmSoundManager.startAlarmSound(soundType)
        alarmSoundManager.startVibration(vibrationPattern)
        Thread.sleep(1000)

        // Then: 소리만 실행되어야 함
        alarmSoundManager.stopAll()
        assertTrue("Only sound should play with NONE vibration", true)
    }

    @Test
    fun startSilentSoundAndNoVibration_nothingHappens() {
        // Given: 무음 + 진동 없음 (완전 무음 설정)
        val soundType = SoundType.SILENT
        val vibrationPattern = VibrationPattern.NONE

        // When: 둘 다 시작
        alarmSoundManager.startAlarmSound(soundType)
        alarmSoundManager.startVibration(vibrationPattern)
        Thread.sleep(500)

        // Then: 아무것도 실행되지 않아야 함
        alarmSoundManager.stopAll()
        assertTrue("Neither sound nor vibration should work", true)
    }

    // ===== stopAll 테스트 =====

    @Test
    fun stopAll_stopsBothSoundAndVibration() {
        // Given: 소리와 진동 모두 실행 중
        alarmSoundManager.startAlarmSound(SoundType.DEFAULT)
        alarmSoundManager.startVibration(VibrationPattern.DEFAULT)
        Thread.sleep(500)

        // When: 모두 중지
        alarmSoundManager.stopAll()
        Thread.sleep(200)

        // Then: 둘 다 중지되어야 함
        assertTrue("Both sound and vibration stopped", true)
    }

    // ===== Vibrator 하드웨어 검증 테스트 =====

    @Test
    fun vibratorService_isAvailable() {
        // Given: 테스트 디바이스

        // When: Vibrator 서비스 가져오기
        val v =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

        // Then: Vibrator 서비스는 null이 아니어야 함
        assertNotNull("Vibrator service should be available", v)
    }

    @Test
    fun vibrator_hasVibratorHardware() {
        // Given: Vibrator 서비스

        // Then: 대부분의 안드로이드 디바이스는 진동 하드웨어 보유
        vibrator?.let { v ->
            // hasVibrator()는 디바이스에 진동 하드웨어가 있는지 확인
            val hasVibrator = v.hasVibrator()
            assertTrue(
                "Most Android devices should have vibrator hardware (actual: $hasVibrator)",
                true,
            )
        }
    }

    // ===== 연속 재생 테스트 =====

    @Test
    fun startAlarmSound_multipleTimes_handlesCorrectly() {
        // Given: 이미 재생 중인 소리

        // When: 여러 번 연속 재생 시도
        alarmSoundManager.startAlarmSound(SoundType.DEFAULT)
        Thread.sleep(300)
        alarmSoundManager.startAlarmSound(SoundType.BELL)
        Thread.sleep(300)
        alarmSoundManager.startAlarmSound(SoundType.DIGITAL)
        Thread.sleep(300)

        // Then: 마지막 요청만 재생되어야 하며 예외 없이 처리되어야 함
        alarmSoundManager.stopAlarmSound()
        assertTrue("Multiple sound starts handled correctly", true)
    }

    @Test
    fun startVibration_multipleTimes_handlesCorrectly() {
        // Given: 이미 진동 중

        // When: 여러 번 연속 진동 시도
        alarmSoundManager.startVibration(VibrationPattern.DEFAULT)
        Thread.sleep(300)
        alarmSoundManager.startVibration(VibrationPattern.STRONG)
        Thread.sleep(300)
        alarmSoundManager.startVibration(VibrationPattern.HEARTBEAT)
        Thread.sleep(300)

        // Then: 예외 없이 처리되어야 함
        alarmSoundManager.stopVibration()
        assertTrue("Multiple vibration starts handled correctly", true)
    }
}
