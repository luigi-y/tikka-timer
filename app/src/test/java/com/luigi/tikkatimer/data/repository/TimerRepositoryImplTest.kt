package com.luigi.tikkatimer.data.repository

import com.luigi.tikkatimer.data.local.dao.TimerPresetDao
import com.luigi.tikkatimer.data.local.entity.TimerPresetEntity
import com.luigi.tikkatimer.domain.model.SoundType
import com.luigi.tikkatimer.domain.model.VibrationPattern
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * TimerRepositoryImpl 단위 테스트
 */
class TimerRepositoryImplTest {
    private lateinit var timerPresetDao: TimerPresetDao
    private lateinit var repository: TimerRepositoryImpl

    @Before
    fun setup() {
        timerPresetDao = mockk()
        repository = TimerRepositoryImpl(timerPresetDao)
    }

    @Test
    fun `getAllPresets는 DAO의 getAllPresets를 호출한다`() =
        runTest {
            val entities =
                listOf(
                    createTestEntity(1, "5분 타이머"),
                    createTestEntity(2, "10분 타이머"),
                )
            every { timerPresetDao.getAllPresets() } returns flowOf(entities)

            val result = repository.getAllPresets().first()

            assertEquals(2, result.size)
            assertEquals("5분 타이머", result[0].name)
            assertEquals("10분 타이머", result[1].name)
        }

    @Test
    fun `getPresetById는 존재하는 프리셋을 반환한다`() =
        runTest {
            val entity = createTestEntity(1, "테스트 프리셋")
            coEvery { timerPresetDao.getPresetById(1L) } returns entity

            val result = repository.getPresetById(1L)

            assertNotNull(result)
            assertEquals("테스트 프리셋", result?.name)
        }

    @Test
    fun `getPresetById는 존재하지 않는 프리셋에 대해 null을 반환한다`() =
        runTest {
            coEvery { timerPresetDao.getPresetById(999L) } returns null

            val result = repository.getPresetById(999L)

            assertNull(result)
        }

    @Test
    fun `addPreset는 DAO의 insertPreset를 호출하고 ID를 반환한다`() =
        runTest {
            coEvery { timerPresetDao.insertPreset(any()) } returns 1L

            val preset =
                com.luigi.tikkatimer.domain.model.TimerPreset(
                    id = 0,
                    name = "새 프리셋",
                    durationSeconds = 300,
                    soundType = SoundType.DEFAULT,
                    vibrationPattern = VibrationPattern.DEFAULT,
                )
            val result = repository.addPreset(preset)

            assertEquals(1L, result)
            coVerify { timerPresetDao.insertPreset(any()) }
        }

    @Test
    fun `updatePreset는 DAO의 updatePreset를 호출한다`() =
        runTest {
            coEvery { timerPresetDao.updatePreset(any()) } returns Unit

            val preset =
                com.luigi.tikkatimer.domain.model.TimerPreset(
                    id = 1,
                    name = "수정된 프리셋",
                    durationSeconds = 600,
                    soundType = SoundType.BELL,
                    vibrationPattern = VibrationPattern.STRONG,
                )
            repository.updatePreset(preset)

            coVerify { timerPresetDao.updatePreset(any()) }
        }

    @Test
    fun `deletePreset는 프리셋이 존재하면 DAO의 deletePreset를 호출한다`() =
        runTest {
            val entity = createTestEntity(1, "삭제할 프리셋")
            coEvery { timerPresetDao.getPresetById(1L) } returns entity
            coEvery { timerPresetDao.deletePreset(entity) } returns Unit

            repository.deletePreset(1L)

            coVerify { timerPresetDao.getPresetById(1L) }
            coVerify { timerPresetDao.deletePreset(entity) }
        }

    @Test
    fun `deletePreset는 프리셋이 존재하지 않으면 deletePreset를 호출하지 않는다`() =
        runTest {
            coEvery { timerPresetDao.getPresetById(999L) } returns null

            repository.deletePreset(999L)

            coVerify { timerPresetDao.getPresetById(999L) }
            coVerify(exactly = 0) { timerPresetDao.deletePreset(any()) }
        }

    @Test
    fun `incrementUsageCount는 DAO의 incrementUsageCount를 호출한다`() =
        runTest {
            coEvery { timerPresetDao.incrementUsageCount(1L) } returns Unit

            repository.incrementUsageCount(1L)

            coVerify { timerPresetDao.incrementUsageCount(1L) }
        }

    @Test
    fun `빈 프리셋 목록 반환`() =
        runTest {
            every { timerPresetDao.getAllPresets() } returns flowOf(emptyList())

            val result = repository.getAllPresets().first()

            assertEquals(0, result.size)
        }

    private fun createTestEntity(
        id: Long,
        name: String,
        durationSeconds: Long = 300,
    ) = TimerPresetEntity(
        id = id,
        name = name,
        durationSeconds = durationSeconds,
        soundType = SoundType.DEFAULT.name,
        vibrationPattern = VibrationPattern.DEFAULT.name,
        usageCount = 0,
        createdAt = System.currentTimeMillis(),
    )
}
