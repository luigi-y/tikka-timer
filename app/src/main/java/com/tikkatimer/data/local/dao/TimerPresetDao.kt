package com.tikkatimer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tikkatimer.data.local.entity.TimerPresetEntity
import kotlinx.coroutines.flow.Flow

/**
 * 타이머 프리셋 CRUD 작업을 위한 DAO
 */
@Dao
interface TimerPresetDao {
    /**
     * 모든 프리셋을 사용 횟수 역순으로 조회 (자주 사용하는 것 먼저)
     */
    @Query("SELECT * FROM timer_presets ORDER BY usageCount DESC, createdAt DESC")
    fun getAllPresets(): Flow<List<TimerPresetEntity>>

    /**
     * ID로 프리셋 조회
     */
    @Query("SELECT * FROM timer_presets WHERE id = :presetId")
    suspend fun getPresetById(presetId: Long): TimerPresetEntity?

    /**
     * 새 프리셋 추가
     * @return 생성된 프리셋의 ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: TimerPresetEntity): Long

    /**
     * 프리셋 정보 업데이트
     */
    @Update
    suspend fun updatePreset(preset: TimerPresetEntity)

    /**
     * 프리셋 삭제
     */
    @Delete
    suspend fun deletePreset(preset: TimerPresetEntity)

    /**
     * 프리셋 사용 횟수 증가
     */
    @Query("UPDATE timer_presets SET usageCount = usageCount + 1 WHERE id = :presetId")
    suspend fun incrementUsageCount(presetId: Long)
}
