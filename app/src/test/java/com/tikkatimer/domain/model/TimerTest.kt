package com.tikkatimer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Timer 도메인 모델 단위 테스트
 */
class TimerTest {
    @Test
    fun `시간 분해가 올바르게 동작한다`() {
        // 1시간 30분 45초 = 5445000ms
        val timer =
            Timer(
                totalDurationMillis = 5445000,
                remainingMillis = 5445000,
            )

        assertEquals(1, timer.hours)
        assertEquals(30, timer.minutes)
        assertEquals(45, timer.seconds)
    }

    @Test
    fun `진행률이 올바르게 계산된다`() {
        val timer =
            Timer(
                totalDurationMillis = 100000,
                remainingMillis = 50000,
            )

        assertEquals(0.5f, timer.progress, 0.001f)
    }

    @Test
    fun `총 시간이 0일 때 진행률은 0이다`() {
        val timer =
            Timer(
                totalDurationMillis = 0,
                remainingMillis = 0,
            )

        assertEquals(0f, timer.progress, 0.001f)
    }

    @Test
    fun `남은 시간이 0일 때 진행률은 0이다`() {
        val timer =
            Timer(
                totalDurationMillis = 100000,
                remainingMillis = 0,
            )

        assertEquals(0f, timer.progress, 0.001f)
    }

    @Test
    fun `1시간 미만일 때 포맷이 MM_SS 형식이다`() {
        // 5분 5초 = 305000ms
        val timer =
            Timer(
                totalDurationMillis = 305000,
                remainingMillis = 305000,
            )

        assertEquals("05:05", timer.getFormattedTime())
    }

    @Test
    fun `1시간 이상일 때 포맷이 HH_MM_SS 형식이다`() {
        // 1시간 1분 5초 = 3665000ms
        val timer =
            Timer(
                totalDurationMillis = 3665000,
                remainingMillis = 3665000,
            )

        assertEquals("01:01:05", timer.getFormattedTime())
    }

    @Test
    fun `EMPTY 상태가 올바르다`() {
        val timer = Timer.EMPTY

        assertEquals(0L, timer.totalDurationMillis)
        assertEquals(0L, timer.remainingMillis)
        assertEquals(TimerState.IDLE, timer.state)
    }
}

/**
 * TimerPreset 도메인 모델 단위 테스트
 */
class TimerPresetTest {
    @Test
    fun `시간 분해가 올바르게 동작한다`() {
        // 1시간 30분 45초 = 5445초
        val preset =
            TimerPreset(
                name = "테스트",
                durationSeconds = 5445,
            )

        assertEquals(1, preset.hours)
        assertEquals(30, preset.minutes)
        assertEquals(45, preset.seconds)
    }

    @Test
    fun `시간만 있을 때 포맷이 올바르다`() {
        val preset =
            TimerPreset(
                name = "1시간",
                durationSeconds = 3600,
            )

        assertEquals("1시간", preset.formattedDuration)
    }

    @Test
    fun `분만 있을 때 포맷이 올바르다`() {
        val preset =
            TimerPreset(
                name = "30분",
                durationSeconds = 1800,
            )

        assertEquals("30분", preset.formattedDuration)
    }

    @Test
    fun `초만 있을 때 포맷이 올바르다`() {
        val preset =
            TimerPreset(
                name = "45초",
                durationSeconds = 45,
            )

        assertEquals("45초", preset.formattedDuration)
    }

    @Test
    fun `복합 시간 포맷이 올바르다`() {
        val preset =
            TimerPreset(
                name = "1시간 30분 45초",
                durationSeconds = 5445,
            )

        assertEquals("1시간 30분 45초", preset.formattedDuration)
    }

    @Test
    fun `0초일 때 포맷이 올바르다`() {
        val preset =
            TimerPreset(
                name = "0",
                durationSeconds = 0,
            )

        assertEquals("0초", preset.formattedDuration)
    }
}
