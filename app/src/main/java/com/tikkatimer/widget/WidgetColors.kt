package com.tikkatimer.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

/**
 * 위젯 공통 색상 정의
 */
object WidgetColors {
    // 실행 중 상태 (녹색 계열)
    val runningBackground = ColorProvider(Color(0xFFE8F5E9))
    val runningText = ColorProvider(Color(0xFF1B5E20))
    val runningAccent = ColorProvider(Color(0xFF2E7D32))

    // 일시정지 상태 (주황색 계열)
    val pausedBackground = ColorProvider(Color(0xFFFFF3E0))
    val pausedText = ColorProvider(Color(0xFFE65100))
    val pausedAccent = ColorProvider(Color(0xFFF57C00))

    // 대기 상태 (회색 계열)
    val idleBackground = ColorProvider(Color(0xFFF5F5F5))
    val idleText = ColorProvider(Color(0xFF424242))
    val idleAccent = ColorProvider(Color(0xFF9E9E9E))

    /**
     * 상태에 따른 배경색 반환
     */
    fun getBackground(state: TimerWidgetState): ColorProvider =
        when {
            state.isRunning -> runningBackground
            state.isPaused -> pausedBackground
            else -> idleBackground
        }

    /**
     * 상태에 따른 텍스트 색상 반환
     */
    fun getText(state: TimerWidgetState): ColorProvider =
        when {
            state.isRunning -> runningText
            state.isPaused -> pausedText
            else -> idleText
        }

    /**
     * 상태에 따른 강조 색상 반환
     */
    fun getAccent(state: TimerWidgetState): ColorProvider =
        when {
            state.isRunning -> runningAccent
            state.isPaused -> pausedAccent
            else -> idleAccent
        }
}
