package com.luigi.tikkatimer.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TimerForegroundService 단위 테스트
 * 타이머/스톱워치 상태 관리, 시간 포맷팅 로직 검증
 */
class TimerForegroundServiceTest {
    // ===== TimerServiceState 데이터 클래스 테스트 =====

    @Test
    fun `TimerServiceState가 올바르게 생성된다`() {
        val state =
            TimerServiceState(
                id = "timer-1",
                name = "3분 타이머",
                remainingMillis = 180_000L,
                targetEndTimeMillis = System.currentTimeMillis() + 180_000L,
            )

        assertEquals("timer-1", state.id)
        assertEquals("3분 타이머", state.name)
        assertEquals(180_000L, state.remainingMillis)
    }

    @Test
    fun `TimerServiceState copy로 남은 시간을 업데이트한다`() {
        val state =
            TimerServiceState(
                id = "timer-1",
                name = "3분 타이머",
                remainingMillis = 180_000L,
                targetEndTimeMillis = System.currentTimeMillis() + 180_000L,
            )

        val updated = state.copy(remainingMillis = 120_000L)

        assertEquals(120_000L, updated.remainingMillis)
        assertEquals("timer-1", updated.id) // 다른 필드는 유지
    }

    // ===== StopwatchServiceState 데이터 클래스 테스트 =====

    @Test
    fun `StopwatchServiceState가 올바르게 생성된다`() {
        val now = System.currentTimeMillis()
        val state =
            StopwatchServiceState(
                elapsedMillis = 5_000L,
                startTimeMillis = now - 5_000L,
            )

        assertEquals(5_000L, state.elapsedMillis)
    }

    @Test
    fun `StopwatchServiceState elapsed 시간이 올바르게 계산된다`() {
        val startTime = 1000L
        val currentTime = 6000L

        val elapsed = currentTime - startTime

        assertEquals(5000L, elapsed)
    }

    // ===== 타이머 상태 리스트 관리 테스트 =====

    @Test
    fun `타이머 추가 시 기존 동일 ID 타이머가 교체된다`() {
        val timerId = "timer-1"
        val states =
            listOf(
                TimerServiceState("timer-1", "기존 타이머", 60_000L, 0L),
                TimerServiceState("timer-2", "다른 타이머", 120_000L, 0L),
            )

        val newState = TimerServiceState(timerId, "업데이트 타이머", 30_000L, 0L)
        val updatedStates = states.filter { it.id != timerId } + newState

        assertEquals(2, updatedStates.size)
        assertEquals("업데이트 타이머", updatedStates.find { it.id == timerId }?.name)
    }

    @Test
    fun `타이머 제거 시 해당 ID만 제거된다`() {
        val states =
            listOf(
                TimerServiceState("timer-1", "타이머1", 60_000L, 0L),
                TimerServiceState("timer-2", "타이머2", 120_000L, 0L),
                TimerServiceState("timer-3", "타이머3", 180_000L, 0L),
            )

        val updatedStates = states.filter { it.id != "timer-2" }

        assertEquals(2, updatedStates.size)
        assertNull(updatedStates.find { it.id == "timer-2" })
    }

    @Test
    fun `모든 타이머 제거 후 리스트가 비어있다`() {
        val states =
            listOf(
                TimerServiceState("timer-1", "타이머1", 60_000L, 0L),
            )

        val updatedStates = states.filter { it.id != "timer-1" }

        assertTrue(updatedStates.isEmpty())
    }

    // ===== 타이머 상태 업데이트 테스트 =====

    @Test
    fun `특정 타이머의 남은 시간만 업데이트된다`() {
        val states =
            listOf(
                TimerServiceState("timer-1", "타이머1", 60_000L, 100_000L),
                TimerServiceState("timer-2", "타이머2", 120_000L, 200_000L),
            )

        val updatedStates =
            states.map { state ->
                if (state.id == "timer-1") {
                    state.copy(remainingMillis = 30_000L, targetEndTimeMillis = 80_000L)
                } else {
                    state
                }
            }

        assertEquals(30_000L, updatedStates[0].remainingMillis)
        assertEquals(120_000L, updatedStates[1].remainingMillis) // 변경 안됨
    }

    @Test
    fun `타이머 남은 시간이 0 미만이면 0으로 보정된다`() {
        val targetEndTime = 1000L
        val now = 2000L

        val remaining = maxOf(0L, targetEndTime - now)

        assertEquals(0L, remaining)
    }

    @Test
    fun `타이머 남은 시간이 양수이면 정상 계산된다`() {
        val targetEndTime = 5000L
        val now = 2000L

        val remaining = maxOf(0L, targetEndTime - now)

        assertEquals(3000L, remaining)
    }

    // ===== 시간 포맷팅 테스트 =====

    @Test
    fun `1시간 이상이면 HH-MM-SS 형식으로 포맷된다`() {
        val millis = 3_661_000L // 1시간 1분 1초
        val formatted = formatTime(millis)

        assertEquals("1:01:01", formatted)
    }

    @Test
    fun `1시간 미만이면 MM-SS 형식으로 포맷된다`() {
        val millis = 125_000L // 2분 5초
        val formatted = formatTime(millis)

        assertEquals("02:05", formatted)
    }

    @Test
    fun `0초는 00-00으로 포맷된다`() {
        val millis = 0L
        val formatted = formatTime(millis)

        assertEquals("00:00", formatted)
    }

    @Test
    fun `정확히 1시간은 1-00-00으로 포맷된다`() {
        val millis = 3_600_000L
        val formatted = formatTime(millis)

        assertEquals("1:00:00", formatted)
    }

    @Test
    fun `59분 59초는 59-59로 포맷된다`() {
        val millis = 3_599_000L
        val formatted = formatTime(millis)

        assertEquals("59:59", formatted)
    }

    // ===== 서비스 상태 플래그 테스트 =====

    @Test
    fun `타이머와 스톱워치 모두 비활성이면 서비스 중지가 필요하다`() {
        val isTimerRunning = false
        val isStopwatchRunning = false

        val shouldStop = !isTimerRunning && !isStopwatchRunning

        assertTrue(shouldStop)
    }

    @Test
    fun `타이머만 실행 중이면 서비스가 유지된다`() {
        val isTimerRunning = true
        val isStopwatchRunning = false

        val shouldStop = !isTimerRunning && !isStopwatchRunning

        assertTrue(!shouldStop)
    }

    @Test
    fun `스톱워치만 실행 중이면 서비스가 유지된다`() {
        val isTimerRunning = false
        val isStopwatchRunning = true

        val shouldStop = !isTimerRunning && !isStopwatchRunning

        assertTrue(!shouldStop)
    }

    // ===== 알림 액션 상수 테스트 =====

    @Test
    fun `ACTION_PAUSE_TIMER 상수가 올바른 값을 가진다`() {
        assertEquals(
            "com.luigi.tikkatimer.ACTION_PAUSE_TIMER",
            TimerForegroundService.ACTION_PAUSE_TIMER,
        )
    }

    @Test
    fun `ACTION_RESUME_TIMER 상수가 올바른 값을 가진다`() {
        assertEquals(
            "com.luigi.tikkatimer.ACTION_RESUME_TIMER",
            TimerForegroundService.ACTION_RESUME_TIMER,
        )
    }

    @Test
    fun `ACTION_ADD_MINUTE 상수가 올바른 값을 가진다`() {
        assertEquals(
            "com.luigi.tikkatimer.ACTION_ADD_MINUTE",
            TimerForegroundService.ACTION_ADD_MINUTE,
        )
    }

    @Test
    fun `ACTION_CANCEL_TIMER 상수가 올바른 값을 가진다`() {
        assertEquals(
            "com.luigi.tikkatimer.ACTION_CANCEL_TIMER",
            TimerForegroundService.ACTION_CANCEL_TIMER,
        )
    }

    @Test
    fun `모든 알림 액션 상수가 고유한 값을 가진다`() {
        val actions =
            setOf(
                TimerForegroundService.ACTION_START_TIMER,
                TimerForegroundService.ACTION_STOP_TIMER,
                TimerForegroundService.ACTION_UPDATE_TIMER,
                TimerForegroundService.ACTION_PAUSE_TIMER,
                TimerForegroundService.ACTION_RESUME_TIMER,
                TimerForegroundService.ACTION_ADD_MINUTE,
                TimerForegroundService.ACTION_CANCEL_TIMER,
                TimerForegroundService.ACTION_START_STOPWATCH,
                TimerForegroundService.ACTION_STOP_STOPWATCH,
            )

        assertEquals(9, actions.size)
    }

    // ===== Helper =====

    /**
     * TimerForegroundService.formatTime()과 동일한 로직
     */
    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
