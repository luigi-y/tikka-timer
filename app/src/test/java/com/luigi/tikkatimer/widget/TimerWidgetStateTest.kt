package com.luigi.tikkatimer.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TimerWidgetState 단위 테스트
 * 위젯 상태 데이터 클래스의 computed property 검증
 */
class TimerWidgetStateTest {
    // ===== isEmpty 테스트 =====

    @Test
    fun `기본 상태는 isEmpty가 true이다`() {
        val state = TimerWidgetState()
        assertTrue(state.isEmpty)
    }

    @Test
    fun `EMPTY 상수는 isEmpty가 true이다`() {
        assertTrue(TimerWidgetState.EMPTY.isEmpty)
    }

    @Test
    fun `실행 중이면 isEmpty가 false이다`() {
        val state = TimerWidgetState(isRunning = true, remainingMillis = 60_000L)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `일시정지 상태이면 isEmpty가 false이다`() {
        val state = TimerWidgetState(isPaused = true, remainingMillis = 30_000L)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `남은 시간만 있으면 isEmpty가 false이다`() {
        val state = TimerWidgetState(remainingMillis = 1000L)
        assertFalse(state.isEmpty)
    }

    // ===== currentRemainingMillis 테스트 =====

    @Test
    fun `실행 중이 아니면 remainingMillis를 그대로 반환한다`() {
        val state = TimerWidgetState(isRunning = false, remainingMillis = 45_000L)
        assertEquals(45_000L, state.currentRemainingMillis)
    }

    @Test
    fun `일시정지 상태에서 remainingMillis를 그대로 반환한다`() {
        val state = TimerWidgetState(isPaused = true, remainingMillis = 30_000L)
        assertEquals(30_000L, state.currentRemainingMillis)
    }

    @Test
    fun `targetEndTimeMillis가 과거이면 0을 반환한다`() {
        val state =
            TimerWidgetState(
                isRunning = true,
                targetEndTimeMillis = System.currentTimeMillis() - 5000L,
            )
        assertEquals(0L, state.currentRemainingMillis)
    }

    @Test
    fun `targetEndTimeMillis가 0이고 lastUpdatedAt도 0이면 remainingMillis를 반환한다`() {
        val state =
            TimerWidgetState(
                isRunning = true,
                remainingMillis = 60_000L,
                targetEndTimeMillis = 0L,
                lastUpdatedAt = 0L,
            )
        assertEquals(60_000L, state.currentRemainingMillis)
    }

    // ===== progress 테스트 =====

    @Test
    fun `totalMillis가 0이면 progress는 0이다`() {
        val state = TimerWidgetState(totalMillis = 0L)
        assertEquals(0f, state.progress, 0.001f)
    }

    @Test
    fun `일시정지 상태에서 progress가 올바르게 계산된다`() {
        val state =
            TimerWidgetState(
                isPaused = true,
                remainingMillis = 30_000L,
                totalMillis = 60_000L,
            )
        assertEquals(0.5f, state.progress, 0.001f)
    }

    @Test
    fun `remainingMillis가 totalMillis와 같으면 progress는 1이다`() {
        val state =
            TimerWidgetState(
                isPaused = true,
                remainingMillis = 60_000L,
                totalMillis = 60_000L,
            )
        assertEquals(1.0f, state.progress, 0.001f)
    }

    // ===== formattedTime 테스트 =====

    @Test
    fun `0초는 00-00으로 포맷된다`() {
        val state = TimerWidgetState(remainingMillis = 0L)
        assertEquals("00:00", state.formattedTime)
    }

    @Test
    fun `59분 59초는 59-59로 포맷된다`() {
        val state = TimerWidgetState(remainingMillis = 3_599_000L)
        assertEquals("59:59", state.formattedTime)
    }

    @Test
    fun `1시간은 1-00-00으로 포맷된다`() {
        val state = TimerWidgetState(remainingMillis = 3_600_000L)
        assertEquals("1:00:00", state.formattedTime)
    }

    @Test
    fun `1시간 1분 1초는 1-01-01로 포맷된다`() {
        val state = TimerWidgetState(remainingMillis = 3_661_000L)
        assertEquals("1:01:01", state.formattedTime)
    }

    // ===== formattedTimeShort 테스트 =====

    @Test
    fun `짧은 포맷에서 0초는 0-00으로 표시된다`() {
        val state = TimerWidgetState(remainingMillis = 0L)
        assertEquals("0:00", state.formattedTimeShort)
    }

    @Test
    fun `짧은 포맷에서 2분 5초는 2-05로 표시된다`() {
        val state = TimerWidgetState(remainingMillis = 125_000L)
        assertEquals("2:05", state.formattedTimeShort)
    }

    @Test
    fun `짧은 포맷에서 60분은 60-00으로 표시된다`() {
        val state = TimerWidgetState(remainingMillis = 3_600_000L)
        assertEquals("60:00", state.formattedTimeShort)
    }

    // ===== 상태 조합 테스트 =====

    @Test
    fun `finished 상태는 isEmpty가 true이다`() {
        val state = TimerWidgetState(isFinished = true, remainingMillis = 0L)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `copy로 상태 전환 시 올바르게 업데이트된다`() {
        val running =
            TimerWidgetState(
                isRunning = true,
                remainingMillis = 60_000L,
                totalMillis = 60_000L,
                timerName = "테스트 타이머",
            )

        val paused = running.copy(isRunning = false, isPaused = true, remainingMillis = 30_000L)

        assertFalse(paused.isRunning)
        assertTrue(paused.isPaused)
        assertEquals(30_000L, paused.remainingMillis)
        assertEquals("테스트 타이머", paused.timerName)
    }
}
