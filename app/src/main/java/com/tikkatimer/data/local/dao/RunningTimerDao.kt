package com.tikkatimer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tikkatimer.data.local.entity.RunningTimerEntity
import kotlinx.coroutines.flow.Flow

/**
 * 실행 중인 타이머 상태를 관리하는 DAO
 */
@Dao
interface RunningTimerDao {
    /**
     * 모든 실행 중인 타이머 조회
     */
    @Query("SELECT * FROM running_timers ORDER BY createdAt ASC")
    fun getAllTimers(): Flow<List<RunningTimerEntity>>

    /**
     * 모든 실행 중인 타이머 동기 조회
     */
    @Query("SELECT * FROM running_timers ORDER BY createdAt ASC")
    suspend fun getAllTimersSync(): List<RunningTimerEntity>

    /**
     * 특정 타이머 조회
     */
    @Query("SELECT * FROM running_timers WHERE instanceId = :instanceId")
    suspend fun getTimer(instanceId: String): RunningTimerEntity?

    /**
     * 타이머 저장 (있으면 업데이트)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(timer: RunningTimerEntity)

    /**
     * 여러 타이머 저장
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(timers: List<RunningTimerEntity>)

    /**
     * 특정 타이머 삭제
     */
    @Query("DELETE FROM running_timers WHERE instanceId = :instanceId")
    suspend fun delete(instanceId: String)

    /**
     * 모든 타이머 삭제
     */
    @Query("DELETE FROM running_timers")
    suspend fun deleteAll()

    /**
     * 타이머 상태 업데이트
     */
    @Query(
        """
        UPDATE running_timers
        SET state = :state, remainingMillis = :remainingMillis, targetEndTimeMillis = :targetEndTimeMillis
        WHERE instanceId = :instanceId
        """,
    )
    suspend fun updateState(
        instanceId: String,
        state: String,
        remainingMillis: Long,
        targetEndTimeMillis: Long,
    )
}
