package com.tikkatimer.data.mapper

import com.tikkatimer.data.local.entity.TimerPresetEntity
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.TimerPreset
import com.tikkatimer.domain.model.VibrationPattern

/**
 * TimerPresetEntity를 TimerPreset 도메인 모델로 변환
 */
fun TimerPresetEntity.toDomain(): TimerPreset {
    return TimerPreset(
        id = id,
        name = name,
        durationSeconds = durationSeconds,
        usageCount = usageCount,
        soundType = SoundType.fromName(soundType),
        vibrationPattern = VibrationPattern.fromName(vibrationPattern),
        ringtoneUri = ringtoneUri,
    )
}

/**
 * TimerPreset 도메인 모델을 TimerPresetEntity로 변환
 */
fun TimerPreset.toEntity(): TimerPresetEntity {
    return TimerPresetEntity(
        id = id,
        name = name,
        durationSeconds = durationSeconds,
        usageCount = usageCount,
        soundType = soundType.name,
        vibrationPattern = vibrationPattern.name,
        ringtoneUri = ringtoneUri,
    )
}
