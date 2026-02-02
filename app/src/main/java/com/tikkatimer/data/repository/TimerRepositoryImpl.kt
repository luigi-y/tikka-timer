package com.tikkatimer.data.repository

import com.tikkatimer.data.local.dao.TimerPresetDao
import com.tikkatimer.data.mapper.toDomain
import com.tikkatimer.data.mapper.toEntity
import com.tikkatimer.domain.model.TimerPreset
import com.tikkatimer.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TimerRepository 구현체
 * Room DAO를 사용하여 타이머 프리셋 데이터를 관리
 */
@Singleton
class TimerRepositoryImpl
    @Inject
    constructor(
        private val timerPresetDao: TimerPresetDao,
    ) : TimerRepository {
        override fun getAllPresets(): Flow<List<TimerPreset>> {
            return timerPresetDao.getAllPresets().map { entities ->
                entities.map { it.toDomain() }
            }
        }

        override suspend fun getPresetById(presetId: Long): TimerPreset? {
            return timerPresetDao.getPresetById(presetId)?.toDomain()
        }

        override suspend fun addPreset(preset: TimerPreset): Long {
            return timerPresetDao.insertPreset(preset.toEntity())
        }

        override suspend fun updatePreset(preset: TimerPreset) {
            timerPresetDao.updatePreset(preset.toEntity())
        }

        override suspend fun deletePreset(presetId: Long) {
            val preset = timerPresetDao.getPresetById(presetId)
            preset?.let { timerPresetDao.deletePreset(it) }
        }

        override suspend fun incrementUsageCount(presetId: Long) {
            timerPresetDao.incrementUsageCount(presetId)
        }
    }
