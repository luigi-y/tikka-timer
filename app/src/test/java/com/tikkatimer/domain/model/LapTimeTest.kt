package com.tikkatimer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * LapTime 도메인 모델 단위 테스트
 */
class LapTimeTest {
    @Test
    fun `랩 시간 포맷이 올바르다`() {
        // 1분 5초 23 = 65230ms
        val lapTime =
            LapTime(
                lapNumber = 1,
                lapMillis = 65230,
                totalMillis = 65230,
            )

        assertEquals("01:05.23", lapTime.getFormattedLapTime())
    }

    @Test
    fun `총 시간 포맷이 1시간 미만일 때 올바르다`() {
        // 2분 5초 23 = 125230ms
        val lapTime =
            LapTime(
                lapNumber = 1,
                lapMillis = 30000,
                totalMillis = 125230,
            )

        assertEquals("02:05.23", lapTime.getFormattedTotalTime())
    }

    @Test
    fun `총 시간 포맷이 1시간 이상일 때 올바르다`() {
        // 1시간 1분 5초 23 = 3665230ms
        val lapTime =
            LapTime(
                lapNumber = 1,
                lapMillis = 30000,
                totalMillis = 3665230,
            )

        assertEquals("01:01:05.23", lapTime.getFormattedTotalTime())
    }

    @Test
    fun `랩 번호가 올바르게 설정된다`() {
        val lapTime =
            LapTime(
                lapNumber = 5,
                lapMillis = 30000,
                totalMillis = 150000,
            )

        assertEquals(5, lapTime.lapNumber)
    }

    @Test
    fun `0밀리초일 때 포맷이 올바르다`() {
        val lapTime =
            LapTime(
                lapNumber = 1,
                lapMillis = 0,
                totalMillis = 0,
            )

        assertEquals("00:00.00", lapTime.getFormattedLapTime())
        assertEquals("00:00.00", lapTime.getFormattedTotalTime())
    }
}
