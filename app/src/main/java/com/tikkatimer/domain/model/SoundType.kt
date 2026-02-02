package com.tikkatimer.domain.model

import androidx.annotation.StringRes
import com.tikkatimer.R

/**
 * 알람/타이머 소리 타입
 * 사용자가 알람이 울릴 때 재생될 소리 유형을 선택할 수 있음
 */
enum class SoundType(
    @param:StringRes val displayNameResId: Int,
) {
    /** 기본 알람음 사용 */
    DEFAULT(R.string.sound_default),

    /** 무음 (소리 없음) */
    SILENT(R.string.sound_silent),

    /** 벨소리 1 */
    BELL(R.string.sound_bell),

    /** 디지털 알람음 */
    DIGITAL(R.string.sound_digital),

    /** 부드러운 알람음 */
    GENTLE(R.string.sound_gentle),

    /** 사용자 지정 벨소리 (ringtoneUri 사용) */
    CUSTOM(R.string.sound_custom),
    ;

    companion object {
        fun fromName(name: String): SoundType {
            return entries.find { it.name == name } ?: DEFAULT
        }
    }
}
