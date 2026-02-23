package com.luigi.tikkatimer.data.mapper

import com.luigi.tikkatimer.data.local.entity.AlarmEntity
import com.luigi.tikkatimer.domain.model.Alarm
import com.luigi.tikkatimer.domain.model.SoundType
import com.luigi.tikkatimer.domain.model.VibrationPattern
import java.time.DayOfWeek
import java.time.LocalTime

/** 요일 비트마스크 상수 */
private object DayBitmask {
    const val SUNDAY = 1
    const val MONDAY = 2
    const val TUESDAY = 4
    const val WEDNESDAY = 8
    const val THURSDAY = 16
    const val FRIDAY = 32
    const val SATURDAY = 64
}

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
    if (this and DayBitmask.SUNDAY != 0) days.add(DayOfWeek.SUNDAY)
    if (this and DayBitmask.MONDAY != 0) days.add(DayOfWeek.MONDAY)
    if (this and DayBitmask.TUESDAY != 0) days.add(DayOfWeek.TUESDAY)
    if (this and DayBitmask.WEDNESDAY != 0) days.add(DayOfWeek.WEDNESDAY)
    if (this and DayBitmask.THURSDAY != 0) days.add(DayOfWeek.THURSDAY)
    if (this and DayBitmask.FRIDAY != 0) days.add(DayOfWeek.FRIDAY)
    if (this and DayBitmask.SATURDAY != 0) days.add(DayOfWeek.SATURDAY)
    return days
}

/**
 * DayOfWeek Set을 비트마스크로 변환
 */
private fun Set<DayOfWeek>.toBitmask(): Int {
    var bitmask = 0
    if (DayOfWeek.SUNDAY in this) bitmask = bitmask or DayBitmask.SUNDAY
    if (DayOfWeek.MONDAY in this) bitmask = bitmask or DayBitmask.MONDAY
    if (DayOfWeek.TUESDAY in this) bitmask = bitmask or DayBitmask.TUESDAY
    if (DayOfWeek.WEDNESDAY in this) bitmask = bitmask or DayBitmask.WEDNESDAY
    if (DayOfWeek.THURSDAY in this) bitmask = bitmask or DayBitmask.THURSDAY
    if (DayOfWeek.FRIDAY in this) bitmask = bitmask or DayBitmask.FRIDAY
    if (DayOfWeek.SATURDAY in this) bitmask = bitmask or DayBitmask.SATURDAY
    return bitmask
}
