package com.tikkatimer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Stopwatch 도메인 모델 단위 테스트
 */
class StopwatchTest {
    @Test
    fun `경과 시간이 0일 때 모든 값이 0이다`() {
        val stopwatch = Stopwatch(elapsedMillis = 0)

        assertEquals(0, stopwatch.hours)
        assertEquals(0, stopwatch.minutes)
        assertEquals(0, stopwatch.seconds)
        assertEquals(0, stopwatch.milliseconds)
    }

    @Test
    fun `밀리초가 올바르게 계산된다`() {
        // 450ms -> 45 센티초
        val stopwatch = Stopwatch(elapsedMillis = 450)

        assertEquals(45, stopwatch.milliseconds)
    }

    @Test
    fun `초가 올바르게 계산된다`() {
        // 5500ms = 5초 500ms
        val stopwatch = Stopwatch(elapsedMillis = 5500)

        assertEquals(5, stopwatch.seconds)
        assertEquals(50, stopwatch.milliseconds)
    }

    @Test
    fun `분이 올바르게 계산된다`() {
        // 125000ms = 2분 5초
        val stopwatch = Stopwatch(elapsedMillis = 125000)

        assertEquals(2, stopwatch.minutes)
        assertEquals(5, stopwatch.seconds)
    }

    @Test
    fun `시간이 올바르게 계산된다`() {
        // 3661000ms = 1시간 1분 1초
        val stopwatch = Stopwatch(elapsedMillis = 3661000)

        assertEquals(1, stopwatch.hours)
        assertEquals(1, stopwatch.minutes)
        assertEquals(1, stopwatch.seconds)
    }

    @Test
    fun `1시간 미만일 때 포맷이 MM_SS_mm 형식이다`() {
        val stopwatch = Stopwatch(elapsedMillis = 65230) // 1:05.23

        assertEquals("01:05.23", stopwatch.getFormattedTime())
    }

    @Test
    fun `1시간 이상일 때 포맷이 HH_MM_SS_mm 형식이다`() {
        val stopwatch = Stopwatch(elapsedMillis = 3661230) // 1:01:01.23

        assertEquals("01:01:01.23", stopwatch.getFormattedTime())
    }

    @Test
    fun `INITIAL 상태가 올바르다`() {
        val stopwatch = Stopwatch.INITIAL

        assertEquals(0L, stopwatch.elapsedMillis)
        assertEquals(StopwatchState.IDLE, stopwatch.state)
        assertEquals(0, stopwatch.lapTimes.size)
    }
}
