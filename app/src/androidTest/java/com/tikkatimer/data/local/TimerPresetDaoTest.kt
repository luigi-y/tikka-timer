package com.tikkatimer.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tikkatimer.data.local.dao.TimerPresetDao
import com.tikkatimer.data.local.entity.TimerPresetEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TimerPresetDao Integration Test
 * 실제 Room 데이터베이스를 사용하여 DAO 동작 검증
 */
@RunWith(AndroidJUnit4::class)
class TimerPresetDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var timerPresetDao: TimerPresetDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        timerPresetDao = database.timerPresetDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertPreset_returnsGeneratedId() =
        runTest {
            val preset = createTestPreset("3분 타이머", 180)

            val id = timerPresetDao.insertPreset(preset)

            assertTrue(id > 0)
        }

    @Test
    fun getAllPresets_returnsInsertedPresets() =
        runTest {
            timerPresetDao.insertPreset(createTestPreset("1분", 60))
            timerPresetDao.insertPreset(createTestPreset("3분", 180))
            timerPresetDao.insertPreset(createTestPreset("5분", 300))

            val presets = timerPresetDao.getAllPresets().first()

            assertEquals(3, presets.size)
        }

    @Test
    fun getAllPresets_sortedByUsageCount() =
        runTest {
            timerPresetDao.insertPreset(createTestPreset("1분", 60, usageCount = 5))
            timerPresetDao.insertPreset(createTestPreset("3분", 180, usageCount = 10))
            timerPresetDao.insertPreset(createTestPreset("5분", 300, usageCount = 1))

            val presets = timerPresetDao.getAllPresets().first()

            assertEquals("3분", presets[0].name) // usageCount 10
            assertEquals("1분", presets[1].name) // usageCount 5
            assertEquals("5분", presets[2].name) // usageCount 1
        }

    @Test
    fun getPresetById_returnsCorrectPreset() =
        runTest {
            val preset = createTestPreset("테스트 프리셋", 600)
            val id = timerPresetDao.insertPreset(preset)

            val retrieved = timerPresetDao.getPresetById(id)

            assertNotNull(retrieved)
            assertEquals("테스트 프리셋", retrieved?.name)
            assertEquals(600L, retrieved?.durationSeconds)
        }

    @Test
    fun getPresetById_nonExistentId_returnsNull() =
        runTest {
            val retrieved = timerPresetDao.getPresetById(999)

            assertNull(retrieved)
        }

    @Test
    fun updatePreset_updatesCorrectly() =
        runTest {
            val preset = createTestPreset("원래 이름", 60)
            val id = timerPresetDao.insertPreset(preset)

            val updatedPreset = preset.copy(id = id, name = "수정된 이름", durationSeconds = 120)
            timerPresetDao.updatePreset(updatedPreset)

            val retrieved = timerPresetDao.getPresetById(id)
            assertEquals("수정된 이름", retrieved?.name)
            assertEquals(120L, retrieved?.durationSeconds)
        }

    @Test
    fun deletePreset_removesPreset() =
        runTest {
            val preset = createTestPreset("삭제할 프리셋", 60)
            val id = timerPresetDao.insertPreset(preset)

            val toDelete = timerPresetDao.getPresetById(id)!!
            timerPresetDao.deletePreset(toDelete)

            val retrieved = timerPresetDao.getPresetById(id)
            assertNull(retrieved)
        }

    @Test
    fun incrementUsageCount_incrementsCorrectly() =
        runTest {
            val preset = createTestPreset("카운트 테스트", 60, usageCount = 0)
            val id = timerPresetDao.insertPreset(preset)

            timerPresetDao.incrementUsageCount(id)
            timerPresetDao.incrementUsageCount(id)
            timerPresetDao.incrementUsageCount(id)

            val retrieved = timerPresetDao.getPresetById(id)
            assertEquals(3, retrieved?.usageCount)
        }

    @Test
    fun insertPreset_withConflict_replacesExisting() =
        runTest {
            val preset = createTestPreset("원래", 60)
            val id = timerPresetDao.insertPreset(preset)

            val newPreset = preset.copy(id = id, name = "교체됨")
            timerPresetDao.insertPreset(newPreset)

            val retrieved = timerPresetDao.getPresetById(id)
            assertEquals("교체됨", retrieved?.name)
        }

    private fun createTestPreset(
        name: String,
        durationSeconds: Long,
        usageCount: Int = 0,
    ) = TimerPresetEntity(
        name = name,
        durationSeconds = durationSeconds,
        usageCount = usageCount,
    )
}
