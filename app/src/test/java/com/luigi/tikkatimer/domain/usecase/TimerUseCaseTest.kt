package com.luigi.tikkatimer.domain.usecase

import com.luigi.tikkatimer.domain.model.TimerPreset
import com.luigi.tikkatimer.domain.repository.TimerRepository
import com.luigi.tikkatimer.domain.usecase.timer.DeleteTimerPresetUseCase
import com.luigi.tikkatimer.domain.usecase.timer.GetTimerPresetsUseCase
import com.luigi.tikkatimer.domain.usecase.timer.SaveTimerPresetUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Timer UseCase 단위 테스트
 */
class TimerUseCaseTest {
    private lateinit var repository: TimerRepository

    @Before
    fun setup() {
        repository = mockk()
    }

    @Test
    fun `GetTimerPresetsUseCase는 Repository의 getAllPresets를 호출한다`() =
        runTest {
            val presets =
                listOf(
                    createTestPreset(1, "3분", 180),
                    createTestPreset(2, "5분", 300),
                )
            every { repository.getAllPresets() } returns flowOf(presets)

            val useCase = GetTimerPresetsUseCase(repository)
            val result = useCase().first()

            assertEquals(2, result.size)
            assertEquals("3분", result[0].name)
        }

    @Test
    fun `SaveTimerPresetUseCase는 Repository의 addPreset을 호출하고 ID를 반환한다`() =
        runTest {
            val preset = createTestPreset(0, "새 프리셋", 600)
            coEvery { repository.addPreset(preset) } returns 1L

            val useCase = SaveTimerPresetUseCase(repository)
            val result = useCase(preset)

            assertEquals(1L, result)
            coVerify { repository.addPreset(preset) }
        }

    @Test
    fun `DeleteTimerPresetUseCase는 Repository의 deletePreset을 호출한다`() =
        runTest {
            coEvery { repository.deletePreset(1L) } returns Unit

            val useCase = DeleteTimerPresetUseCase(repository)
            useCase(1L)

            coVerify { repository.deletePreset(1L) }
        }

    private fun createTestPreset(
        id: Long,
        name: String,
        durationSeconds: Long,
    ) = TimerPreset(
        id = id,
        name = name,
        durationSeconds = durationSeconds,
    )
}
