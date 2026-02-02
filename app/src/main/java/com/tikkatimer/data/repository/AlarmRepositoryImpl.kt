package com.tikkatimer.data.repository

import com.tikkatimer.data.local.dao.AlarmDao
import com.tikkatimer.data.mapper.toDomain
import com.tikkatimer.data.mapper.toEntity
import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AlarmRepository 구현체
 * Room DAO를 사용하여 알람 데이터를 관리
 */
@Singleton
class AlarmRepositoryImpl
    @Inject
    constructor(
        private val alarmDao: AlarmDao,
    ) : AlarmRepository {
        override fun getAllAlarms(): Flow<List<Alarm>> {
            return alarmDao.getAllAlarms().map { entities ->
                entities.map { it.toDomain() }
            }
        }

        override fun getEnabledAlarms(): Flow<List<Alarm>> {
            return alarmDao.getEnabledAlarms().map { entities ->
                entities.map { it.toDomain() }
            }
        }

        override suspend fun getAlarmById(alarmId: Long): Alarm? {
            return alarmDao.getAlarmById(alarmId)?.toDomain()
        }

        override suspend fun addAlarm(alarm: Alarm): Long {
            return alarmDao.insertAlarm(alarm.toEntity())
        }

        override suspend fun updateAlarm(alarm: Alarm) {
            alarmDao.updateAlarm(alarm.toEntity())
        }

        override suspend fun deleteAlarm(alarmId: Long) {
            alarmDao.deleteAlarmById(alarmId)
        }

        override suspend fun setAlarmEnabled(
            alarmId: Long,
            isEnabled: Boolean,
        ) {
            alarmDao.setAlarmEnabled(alarmId, isEnabled)
        }
    }
