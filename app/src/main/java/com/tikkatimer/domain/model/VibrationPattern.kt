package com.tikkatimer.domain.model

import androidx.annotation.StringRes
import com.tikkatimer.R

/**
 * 진동 패턴 타입
 * 알람/타이머가 울릴 때 사용할 진동 패턴 선택
 */
enum class VibrationPattern(
    @param:StringRes val displayNameResId: Int,
    val pattern: LongArray,
) {
    /** 진동 없음 */
    NONE(R.string.vibration_none, longArrayOf()),

    /** 기본 진동 (짧은 진동 반복) */
    DEFAULT(R.string.vibration_default, longArrayOf(0, 500, 200, 500)),

    /** 강한 진동 (긴 진동 반복) */
    STRONG(R.string.vibration_strong, longArrayOf(0, 800, 200, 800)),

    /** 하트비트 패턴 (두 번씩 진동) */
    HEARTBEAT(R.string.vibration_heartbeat, longArrayOf(0, 200, 100, 200, 400, 200, 100, 200)),

    /** SOS 패턴 (모스 부호) */
    SOS(
        R.string.vibration_sos,
        longArrayOf(0, 100, 100, 100, 100, 100, 300, 300, 100, 300, 100, 300, 300, 100, 100, 100, 100, 100),
    ),

    /** 점점 강해지는 진동 */
    CRESCENDO(R.string.vibration_crescendo, longArrayOf(0, 100, 200, 200, 200, 400, 200, 600)),
    ;

    companion object {
        fun fromName(name: String): VibrationPattern {
            return entries.find { it.name == name } ?: DEFAULT
        }
    }
}
