package com.tikkatimer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tikkatimer.data.local.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow

/**
 * 알람 CRUD 작업을 위한 DAO
 */
@Dao
interface AlarmDao {
    /**
     * 모든 알람을 생성 시간 역순으로 조회
     */
    @Query("SELECT * FROM alarms ORDER BY createdAt DESC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    /**
     * 활성화된 알람만 조회 (시간순 정렬)
     */
    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour, minute")
    fun getEnabledAlarms(): Flow<List<AlarmEntity>>

    /**
     * ID로 알람 조회
     */
    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    suspend fun getAlarmById(alarmId: Long): AlarmEntity?

    /**
     * 새 알람 추가
     * @return 생성된 알람의 ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    /**
     * 알람 정보 업데이트
     */
    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    /**
     * 알람 삭제
     */
    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    /**
     * ID로 알람 삭제
     */
    @Query("DELETE FROM alarms WHERE id = :alarmId")
    suspend fun deleteAlarmById(alarmId: Long)

    /**
     * 알람 활성화/비활성화 토글
     */
    @Query("UPDATE alarms SET isEnabled = :isEnabled WHERE id = :alarmId")
    suspend fun setAlarmEnabled(
        alarmId: Long,
        isEnabled: Boolean,
    )

    /**
     * 알람 개수 조회
     */
    @Query("SELECT COUNT(*) FROM alarms")
    suspend fun getAlarmCount(): Int
}
