package com.tikkatimer.domain.repository

import com.tikkatimer.domain.model.TimerPreset
import kotlinx.coroutines.flow.Flow

/**
 * 타이머 프리셋 저장소 인터페이스
 */
interface TimerRepository {
    /**
     * 모든 프리셋 조회 (사용 빈도순)
     */
    fun getAllPresets(): Flow<List<TimerPreset>>

    /**
     * ID로 프리셋 조회
     */
    suspend fun getPresetById(presetId: Long): TimerPreset?

    /**
     * 새 프리셋 추가
     * @return 생성된 프리셋의 ID
     */
    suspend fun addPreset(preset: TimerPreset): Long

    /**
     * 프리셋 업데이트
     */
    suspend fun updatePreset(preset: TimerPreset)

    /**
     * 프리셋 삭제
     */
    suspend fun deletePreset(presetId: Long)

    /**
     * 프리셋 사용 횟수 증가 (자주 사용하는 프리셋 정렬용)
     */
    suspend fun incrementUsageCount(presetId: Long)
}
