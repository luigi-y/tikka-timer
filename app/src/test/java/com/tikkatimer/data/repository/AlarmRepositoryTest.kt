package com.tikkatimer.data.repository

import com.tikkatimer.data.local.dao.AlarmDao
import com.tikkatimer.data.local.entity.AlarmEntity
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
 * AlarmRepositoryImpl 단위 테스트
 */
class AlarmRepositoryTest {
    private lateinit var repository: AlarmRepositoryImpl
    private lateinit var alarmDao: AlarmDao

    @Before
    fun setup() {
        alarmDao = mockk()
        repository = AlarmRepositoryImpl(alarmDao)
    }

    @Test
    fun `getAllAlarms 호출 시 DAO에서 엔티티를 가져와 도메인 모델로 변환한다`() =
        runTest {
            val entities =
                listOf(
                    createTestEntity(1),
                    createTestEntity(2),
                )
            every { alarmDao.getAllAlarms() } returns flowOf(entities)

            val alarms = repository.getAllAlarms().first()

            assertEquals(2, alarms.size)
            assertEquals(1L, alarms[0].id)
            assertEquals(2L, alarms[1].id)
        }

    @Test
    fun `getEnabledAlarms 호출 시 활성화된 알람만 반환한다`() =
        runTest {
            val entities = listOf(createTestEntity(1))
            every { alarmDao.getEnabledAlarms() } returns flowOf(entities)

            val alarms = repository.getEnabledAlarms().first()

            assertEquals(1, alarms.size)
        }

    @Test
    fun `getAlarmById 호출 시 해당 알람을 반환한다`() =
        runTest {
            val entity = createTestEntity(1)
            coEvery { alarmDao.getAlarmById(1) } returns entity

            val alarm = repository.getAlarmById(1)

            assertNotNull(alarm)
            assertEquals(1L, alarm?.id)
        }

    @Test
    fun `getAlarmById 호출 시 존재하지 않는 알람은 null을 반환한다`() =
        runTest {
            coEvery { alarmDao.getAlarmById(999) } returns null

            val alarm = repository.getAlarmById(999)

            assertNull(alarm)
        }

    @Test
    fun `addAlarm 호출 시 DAO의 insertAlarm이 호출된다`() =
        runTest {
            coEvery { alarmDao.insertAlarm(any()) } returns 1L

            val alarm = createTestAlarm()
            val id = repository.addAlarm(alarm)

            assertEquals(1L, id)
            coVerify { alarmDao.insertAlarm(any()) }
        }

    @Test
    fun `updateAlarm 호출 시 DAO의 updateAlarm이 호출된다`() =
        runTest {
            coEvery { alarmDao.updateAlarm(any()) } returns Unit

            val alarm = createTestAlarm()
            repository.updateAlarm(alarm)

            coVerify { alarmDao.updateAlarm(any()) }
        }

    @Test
    fun `deleteAlarm 호출 시 DAO의 deleteAlarmById가 호출된다`() =
        runTest {
            coEvery { alarmDao.deleteAlarmById(1) } returns Unit

            repository.deleteAlarm(1)

            coVerify { alarmDao.deleteAlarmById(1) }
        }

    @Test
    fun `setAlarmEnabled 호출 시 DAO의 setAlarmEnabled가 호출된다`() =
        runTest {
            coEvery { alarmDao.setAlarmEnabled(1, false) } returns Unit

            repository.setAlarmEnabled(1, false)

            coVerify { alarmDao.setAlarmEnabled(1, false) }
        }

    private fun createTestEntity(id: Long) =
        AlarmEntity(
            id = id,
            hour = 8,
            minute = 0,
            isEnabled = true,
            label = "테스트",
            repeatDays = 0,
            soundType = "DEFAULT",
            vibrationPattern = "DEFAULT",
            ringtoneUri = null,
            snoozeDurationMinutes = 5,
            isSnoozeEnabled = true,
        )

    private fun createTestAlarm() =
        com.tikkatimer.domain.model.Alarm(
            id = 1,
            time = java.time.LocalTime.of(8, 0),
            isEnabled = true,
            label = "테스트",
        )
}
