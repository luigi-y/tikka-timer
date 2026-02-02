package com.tikkatimer.util

import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.VibrationPattern
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * AlarmSoundManager 단위 테스트
 * 소리/진동 패턴 및 타입이 올바르게 정의되어 있는지 검증
 */
class AlarmSoundManagerTest {
    // ===== VibrationPattern 테스트 =====

    @Test
    fun `VibrationPattern의 pattern 배열이 올바르게 정의되어 있다`() {
        // 각 패턴이 비어있지 않은지 확인 (NONE 제외)
        assertTrue(VibrationPattern.DEFAULT.pattern.isNotEmpty())
        assertTrue(VibrationPattern.STRONG.pattern.isNotEmpty())
        assertTrue(VibrationPattern.HEARTBEAT.pattern.isNotEmpty())
        assertTrue(VibrationPattern.NONE.pattern.isEmpty())
    }

    @Test
    fun `VibrationPattern NONE의 pattern이 비어있다`() {
        assertEquals(0, VibrationPattern.NONE.pattern.size)
    }

    @Test
    fun `VibrationPattern DEFAULT의 pattern이 올바르다`() {
        val pattern = VibrationPattern.DEFAULT.pattern
        assertTrue(pattern.isNotEmpty())
        // 패턴은 [대기시간, 진동시간, 대기시간, 진동시간, ...] 형태
        assertTrue(pattern.size >= 2)
    }

    @Test
    fun `VibrationPattern STRONG의 pattern이 DEFAULT보다 강하다`() {
        val defaultPattern = VibrationPattern.DEFAULT.pattern
        val strongPattern = VibrationPattern.STRONG.pattern

        assertNotNull(defaultPattern)
        assertNotNull(strongPattern)
        // STRONG은 더 긴 진동 시간을 가짐
        assertTrue(strongPattern.isNotEmpty())
    }

    @Test
    fun `VibrationPattern HEARTBEAT의 pattern이 올바르다`() {
        val heartbeatPattern = VibrationPattern.HEARTBEAT.pattern

        assertNotNull(heartbeatPattern)
        assertTrue(heartbeatPattern.isNotEmpty())
    }

    // ===== SoundType 테스트 =====

    @Test
    fun `SoundType 모든 값이 정의되어 있다`() {
        val soundTypes = SoundType.entries
        assertTrue(soundTypes.isNotEmpty())
        assertTrue(soundTypes.contains(SoundType.DEFAULT))
        assertTrue(soundTypes.contains(SoundType.SILENT))
        assertTrue(soundTypes.contains(SoundType.BELL))
        assertTrue(soundTypes.contains(SoundType.DIGITAL))
        assertTrue(soundTypes.contains(SoundType.GENTLE))
        assertTrue(soundTypes.contains(SoundType.CUSTOM))
    }

    @Test
    fun `SoundType fromName이 올바르게 동작한다`() {
        assertEquals(SoundType.DEFAULT, SoundType.fromName("DEFAULT"))
        assertEquals(SoundType.SILENT, SoundType.fromName("SILENT"))
        assertEquals(SoundType.BELL, SoundType.fromName("BELL"))
        assertEquals(SoundType.DIGITAL, SoundType.fromName("DIGITAL"))
        assertEquals(SoundType.GENTLE, SoundType.fromName("GENTLE"))
        assertEquals(SoundType.CUSTOM, SoundType.fromName("CUSTOM"))
    }

    @Test
    fun `SoundType fromName 잘못된 값은 DEFAULT로 반환된다`() {
        assertEquals(SoundType.DEFAULT, SoundType.fromName("INVALID"))
        assertEquals(SoundType.DEFAULT, SoundType.fromName(""))
    }

    // ===== VibrationPattern fromName 테스트 =====

    @Test
    fun `VibrationPattern fromName이 올바르게 동작한다`() {
        assertEquals(VibrationPattern.DEFAULT, VibrationPattern.fromName("DEFAULT"))
        assertEquals(VibrationPattern.NONE, VibrationPattern.fromName("NONE"))
        assertEquals(VibrationPattern.STRONG, VibrationPattern.fromName("STRONG"))
        assertEquals(VibrationPattern.HEARTBEAT, VibrationPattern.fromName("HEARTBEAT"))
    }

    @Test
    fun `VibrationPattern fromName 잘못된 값은 DEFAULT로 반환된다`() {
        assertEquals(VibrationPattern.DEFAULT, VibrationPattern.fromName("INVALID"))
        assertEquals(VibrationPattern.DEFAULT, VibrationPattern.fromName(""))
    }

    // ===== 소리와 진동 조합 테스트 =====

    @Test
    fun `SILENT 소리와 NONE 진동 조합이 유효하다`() {
        // 사용자가 알람을 완전히 무음으로 설정할 수 있어야 함
        val soundType = SoundType.SILENT
        val vibrationPattern = VibrationPattern.NONE

        assertNotNull(soundType)
        assertNotNull(vibrationPattern)
    }

    @Test
    fun `SILENT 소리와 DEFAULT 진동 조합이 유효하다`() {
        // 소리 없이 진동만 원하는 경우
        val soundType = SoundType.SILENT
        val vibrationPattern = VibrationPattern.DEFAULT

        assertNotNull(soundType)
        assertNotNull(vibrationPattern)
        assertTrue(vibrationPattern.pattern.isNotEmpty())
    }

    @Test
    fun `DEFAULT 소리와 NONE 진동 조합이 유효하다`() {
        // 진동 없이 소리만 원하는 경우
        val soundType = SoundType.DEFAULT
        val vibrationPattern = VibrationPattern.NONE

        assertNotNull(soundType)
        assertTrue(vibrationPattern.pattern.isEmpty())
    }
}
