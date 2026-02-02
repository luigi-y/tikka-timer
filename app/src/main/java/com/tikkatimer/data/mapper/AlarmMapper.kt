package com.tikkatimer.data.mapper

import com.tikkatimer.data.local.entity.AlarmEntity
import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.VibrationPattern
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * AlarmEntity를 Alarm 도메인 모델로 변환
 */
fun AlarmEntity.toDomain(): Alarm {
    return Alarm(
        id = id,
        time = LocalTime.of(hour, minute),
        isEnabled = isEnabled,
        label = label,
        repeatDays = repeatDays.toRepeatDaysSet(),
        soundType = SoundType.fromName(soundType),
        vibrationPattern = VibrationPattern.fromName(vibrationPattern),
        ringtoneUri = ringtoneUri,
        snoozeDurationMinutes = snoozeDurationMinutes,
        isSnoozeEnabled = isSnoozeEnabled,
    )
}

/**
 * Alarm 도메인 모델을 AlarmEntity로 변환
 */
fun Alarm.toEntity(): AlarmEntity {
    return AlarmEntity(
        id = id,
        hour = time.hour,
        minute = time.minute,
        isEnabled = isEnabled,
        label = label,
        repeatDays = repeatDays.toBitmask(),
        soundType = soundType.name,
        vibrationPattern = vibrationPattern.name,
        ringtoneUri = ringtoneUri,
        snoozeDurationMinutes = snoozeDurationMinutes,
        isSnoozeEnabled = isSnoozeEnabled,
    )
}

/**
 * 비트마스크를 DayOfWeek Set으로 변환
 * 비트: 일(1), 월(2), 화(4), 수(8), 목(16), 금(32), 토(64)
 */
private fun Int.toRepeatDaysSet(): Set<DayOfWeek> {
    val days = mutableSetOf<DayOfWeek>()
    if (this and 1 != 0) days.add(DayOfWeek.SUNDAY)
    if (this and 2 != 0) days.add(DayOfWeek.MONDAY)
    if (this and 4 != 0) days.add(DayOfWeek.TUESDAY)
    if (this and 8 != 0) days.add(DayOfWeek.WEDNESDAY)
    if (this and 16 != 0) days.add(DayOfWeek.THURSDAY)
    if (this and 32 != 0) days.add(DayOfWeek.FRIDAY)
    if (this and 64 != 0) days.add(DayOfWeek.SATURDAY)
    return days
}

/**
 * DayOfWeek Set을 비트마스크로 변환
 */
private fun Set<DayOfWeek>.toBitmask(): Int {
    var bitmask = 0
    if (DayOfWeek.SUNDAY in this) bitmask = bitmask or 1
    if (DayOfWeek.MONDAY in this) bitmask = bitmask or 2
    if (DayOfWeek.TUESDAY in this) bitmask = bitmask or 4
    if (DayOfWeek.WEDNESDAY in this) bitmask = bitmask or 8
    if (DayOfWeek.THURSDAY in this) bitmask = bitmask or 16
    if (DayOfWeek.FRIDAY in this) bitmask = bitmask or 32
    if (DayOfWeek.SATURDAY in this) bitmask = bitmask or 64
    return bitmask
}
