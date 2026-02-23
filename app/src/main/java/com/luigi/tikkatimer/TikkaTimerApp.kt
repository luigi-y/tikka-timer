package com.luigi.tikkatimer

import android.app.Application
import com.luigi.tikkatimer.widget.TimerWidgetProvider
import com.luigi.tikkatimer.widget.TimerWidgetStateManager
import dagger.hilt.android.HiltAndroidApp

/**
 * Tikka Timer 애플리케이션 클래스
 * Hilt 의존성 주입을 위한 진입점
 */
@HiltAndroidApp
class TikkaTimerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 앱 시작 시 위젯 상태 검증
        // 강제 종료 후 재시작 시 위젯이 Running 상태로 남아있을 수 있음
        validateWidgetState()
    }

    /**
     * 위젯 상태 검증 및 초기화
     * 앱이 실행 중이 아닌데 위젯이 Running/Paused 상태면 idle로 초기화
     */
    private fun validateWidgetState() {
        val state = TimerWidgetStateManager.getStateSync(this)

        // Running 또는 Paused 상태인데, 타이머가 만료된 경우 (targetEndTime이 지남)
        if (state.isRunning && state.targetEndTimeMillis > 0) {
            val now = System.currentTimeMillis()
            if (now > state.targetEndTimeMillis) {
                // 타이머가 이미 만료됨 - idle로 초기화
                TimerWidgetStateManager.clearSync(this)
                TimerWidgetProvider.updateAllWidgets(this)
            }
        }
    }
}
