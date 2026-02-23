package com.luigi.tikkatimer.data.mapper

import com.luigi.tikkatimer.data.local.entity.RunningTimerEntity
import com.luigi.tikkatimer.domain.model.RunningTimer
import com.luigi.tikkatimer.domain.model.SoundType
import com.luigi.tikkatimer.domain.model.TimerState
import com.luigi.tikkatimer.domain.model.VibrationPattern

/**
 * RunningTimerEntity <-> RunningTimer 변환 매퍼
 */
object RunningTimerMapper {
    /**
     * Entity -> Domain 변환
     */
    fun toDomain(entity: RunningTimerEntity): RunningTimer {
        return RunningTimer(
            instanceId = entity.instanceId,
            presetId = entity.presetId,
            name = entity.name,
            totalDurationMillis = entity.totalDurationMillis,
            remainingMillis = entity.remainingMillis,
            state = TimerState.valueOf(entity.state),
            soundType = SoundType.valueOf(entity.soundType),
            vibrationPattern = VibrationPattern.valueOf(entity.vibrationPattern),
            targetEndTimeMillis = entity.targetEndTimeMillis,
        )
    }

    /**
     * Domain -> Entity 변환
     */
    fun toEntity(timer: RunningTimer): RunningTimerEntity {
        return RunningTimerEntity(
            instanceId = timer.instanceId,
            presetId = timer.presetId,
            name = timer.name,
            totalDurationMillis = timer.totalDurationMillis,
            remainingMillis = timer.remainingMillis,
            state = timer.state.name,
            targetEndTimeMillis = timer.targetEndTimeMillis,
            soundType = timer.soundType.name,
            vibrationPattern = timer.vibrationPattern.name,
        )
    }

    /**
     * Entity 리스트 -> Domain 리스트 변환
     */
    fun toDomainList(entities: List<RunningTimerEntity>): List<RunningTimer> {
        return entities.map { toDomain(it) }
    }

    /**
     * Domain 리스트 -> Entity 리스트 변환
     */
    fun toEntityList(timers: List<RunningTimer>): List<RunningTimerEntity> {
        return timers.map { toEntity(it) }
    }
}
