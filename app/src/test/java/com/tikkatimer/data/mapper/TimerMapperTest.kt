package com.tikkatimer.data.mapper

import com.tikkatimer.data.local.entity.TimerPresetEntity
import com.tikkatimer.domain.model.TimerPreset
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * TimerMapper 단위 테스트
 */
class TimerMapperTest {
    @Test
    fun `TimerPresetEntity를 TimerPreset으로 변환한다`() {
        val entity =
            TimerPresetEntity(
                id = 1,
                name = "3분 타이머",
                durationSeconds = 180,
                usageCount = 5,
            )

        val preset = entity.toDomain()

        assertEquals(1L, preset.id)
        assertEquals("3분 타이머", preset.name)
        assertEquals(180L, preset.durationSeconds)
        assertEquals(5, preset.usageCount)
    }

    @Test
    fun `TimerPreset을 TimerPresetEntity로 변환한다`() {
        val preset =
            TimerPreset(
                id = 1,
                name = "5분 타이머",
                durationSeconds = 300,
                usageCount = 10,
            )

        val entity = preset.toEntity()

        assertEquals(1L, entity.id)
        assertEquals("5분 타이머", entity.name)
        assertEquals(300L, entity.durationSeconds)
        assertEquals(10, entity.usageCount)
    }

    @Test
    fun `새 프리셋 변환 시 id가 0이면 유지된다`() {
        val preset =
            TimerPreset(
                id = 0,
                name = "새 타이머",
                durationSeconds = 60,
            )

        val entity = preset.toEntity()

        assertEquals(0L, entity.id)
    }

    @Test
    fun `usageCount 기본값이 0이다`() {
        val preset =
            TimerPreset(
                id = 1,
                name = "테스트",
                durationSeconds = 60,
            )

        assertEquals(0, preset.usageCount)

        val entity = preset.toEntity()
        assertEquals(0, entity.usageCount)
    }

    @Test
    fun `양방향 변환 시 데이터가 유지된다`() {
        val originalPreset =
            TimerPreset(
                id = 5,
                name = "1시간 30분",
                durationSeconds = 5400,
                usageCount = 15,
            )

        val entity = originalPreset.toEntity()
        val convertedPreset = entity.toDomain()

        assertEquals(originalPreset.id, convertedPreset.id)
        assertEquals(originalPreset.name, convertedPreset.name)
        assertEquals(originalPreset.durationSeconds, convertedPreset.durationSeconds)
        assertEquals(originalPreset.usageCount, convertedPreset.usageCount)
    }
}
