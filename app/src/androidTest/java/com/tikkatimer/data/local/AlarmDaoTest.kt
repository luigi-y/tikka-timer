package com.tikkatimer.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tikkatimer.data.local.dao.AlarmDao
import com.tikkatimer.data.local.entity.AlarmEntity
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
 * AlarmDao Integration Test
 * 실제 Room 데이터베이스를 사용하여 DAO 동작 검증
 */
@RunWith(AndroidJUnit4::class)
class AlarmDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var alarmDao: AlarmDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        alarmDao = database.alarmDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAlarm_returnsGeneratedId() =
        runTest {
            val alarm = createTestAlarm()

            val id = alarmDao.insertAlarm(alarm)

            assertTrue(id > 0)
        }

    @Test
    fun getAllAlarms_returnsInsertedAlarms() =
        runTest {
            alarmDao.insertAlarm(createTestAlarm(hour = 7))
            alarmDao.insertAlarm(createTestAlarm(hour = 8))
            alarmDao.insertAlarm(createTestAlarm(hour = 9))

            val alarms = alarmDao.getAllAlarms().first()

            assertEquals(3, alarms.size)
        }

    @Test
    fun getAlarmById_returnsCorrectAlarm() =
        runTest {
            val alarm = createTestAlarm(hour = 7, minute = 30, label = "테스트")
            val id = alarmDao.insertAlarm(alarm)

            val retrieved = alarmDao.getAlarmById(id)

            assertNotNull(retrieved)
            assertEquals(7, retrieved?.hour)
            assertEquals(30, retrieved?.minute)
            assertEquals("테스트", retrieved?.label)
        }

    @Test
    fun getAlarmById_nonExistentId_returnsNull() =
        runTest {
            val retrieved = alarmDao.getAlarmById(999)

            assertNull(retrieved)
        }

    @Test
    fun updateAlarm_updatesCorrectly() =
        runTest {
            val alarm = createTestAlarm(hour = 7, label = "원래 라벨")
            val id = alarmDao.insertAlarm(alarm)

            val updatedAlarm = alarm.copy(id = id, hour = 8, label = "수정된 라벨")
            alarmDao.updateAlarm(updatedAlarm)

            val retrieved = alarmDao.getAlarmById(id)
            assertEquals(8, retrieved?.hour)
            assertEquals("수정된 라벨", retrieved?.label)
        }

    @Test
    fun deleteAlarm_removesAlarm() =
        runTest {
            val alarm = createTestAlarm()
            val id = alarmDao.insertAlarm(alarm)

            alarmDao.deleteAlarmById(id)

            val retrieved = alarmDao.getAlarmById(id)
            assertNull(retrieved)
        }

    @Test
    fun setAlarmEnabled_togglesCorrectly() =
        runTest {
            val alarm = createTestAlarm(isEnabled = true)
            val id = alarmDao.insertAlarm(alarm)

            alarmDao.setAlarmEnabled(id, false)

            val retrieved = alarmDao.getAlarmById(id)
            assertEquals(false, retrieved?.isEnabled)
        }

    @Test
    fun getEnabledAlarms_returnsOnlyEnabledAlarms() =
        runTest {
            alarmDao.insertAlarm(createTestAlarm(hour = 7, isEnabled = true))
            alarmDao.insertAlarm(createTestAlarm(hour = 8, isEnabled = false))
            alarmDao.insertAlarm(createTestAlarm(hour = 9, isEnabled = true))

            val enabledAlarms = alarmDao.getEnabledAlarms().first()

            assertEquals(2, enabledAlarms.size)
            assertTrue(enabledAlarms.all { it.isEnabled })
        }

    @Test
    fun getEnabledAlarms_sortedByTime() =
        runTest {
            alarmDao.insertAlarm(createTestAlarm(hour = 9, isEnabled = true))
            alarmDao.insertAlarm(createTestAlarm(hour = 7, isEnabled = true))
            alarmDao.insertAlarm(createTestAlarm(hour = 8, isEnabled = true))

            val enabledAlarms = alarmDao.getEnabledAlarms().first()

            assertEquals(7, enabledAlarms[0].hour)
            assertEquals(8, enabledAlarms[1].hour)
            assertEquals(9, enabledAlarms[2].hour)
        }

    @Test
    fun getAlarmCount_returnsCorrectCount() =
        runTest {
            alarmDao.insertAlarm(createTestAlarm(hour = 7))
            alarmDao.insertAlarm(createTestAlarm(hour = 8))

            val count = alarmDao.getAlarmCount()

            assertEquals(2, count)
        }

    @Test
    fun insertAlarm_withConflict_replacesExisting() =
        runTest {
            val alarm = createTestAlarm(hour = 7, label = "원래")
            val id = alarmDao.insertAlarm(alarm)

            val newAlarm = alarm.copy(id = id, label = "교체됨")
            alarmDao.insertAlarm(newAlarm)

            val retrieved = alarmDao.getAlarmById(id)
            assertEquals("교체됨", retrieved?.label)
        }

    private fun createTestAlarm(
        hour: Int = 8,
        minute: Int = 0,
        isEnabled: Boolean = true,
        label: String = "",
    ) = AlarmEntity(
        hour = hour,
        minute = minute,
        isEnabled = isEnabled,
        label = label,
        repeatDays = 0,
        isVibrate = true,
        ringtoneUri = null,
        snoozeDurationMinutes = 5,
        isSnoozeEnabled = true,
    )
}
