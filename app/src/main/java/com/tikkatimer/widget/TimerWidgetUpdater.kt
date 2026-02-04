package com.tikkatimer.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 타이머 위젯 업데이트 헬퍼
 * ViewModel이나 Service에서 위젯 상태를 업데이트할 때 사용
 */
object TimerWidgetUpdater {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 타이머 시작 시 위젯 업데이트
     */
    fun onTimerStarted(
        context: Context,
        timerId: String,
        timerName: String,
        remainingMillis: Long,
        totalMillis: Long,
    ) {
        scope.launch {
            TimerWidgetStateManager.setRunning(
                context = context,
                timerId = timerId,
                timerName = timerName,
                remainingMillis = remainingMillis,
                totalMillis = totalMillis,
            )
            updateWidgets(context)
        }
    }

    /**
     * 타이머 일시정지 시 위젯 업데이트
     */
    fun onTimerPaused(
        context: Context,
        remainingMillis: Long,
    ) {
        scope.launch {
            TimerWidgetStateManager.setPaused(
                context = context,
                remainingMillis = remainingMillis,
            )
            updateWidgets(context)
        }
    }

    /**
     * 타이머 재개 시 위젯 업데이트
     */
    fun onTimerResumed(
        context: Context,
        timerId: String,
        timerName: String,
        remainingMillis: Long,
        totalMillis: Long,
    ) {
        scope.launch {
            TimerWidgetStateManager.setRunning(
                context = context,
                timerId = timerId,
                timerName = timerName,
                remainingMillis = remainingMillis,
                totalMillis = totalMillis,
            )
            updateWidgets(context)
        }
    }

    /**
     * 남은 시간 업데이트 (주기적 호출용)
     */
    fun onTimerTick(
        context: Context,
        remainingMillis: Long,
    ) {
        scope.launch {
            TimerWidgetStateManager.updateRemainingTime(
                context = context,
                remainingMillis = remainingMillis,
            )
            updateWidgets(context)
        }
    }

    /**
     * 타이머 종료/리셋 시 위젯 업데이트
     */
    fun onTimerStopped(context: Context) {
        scope.launch {
            TimerWidgetStateManager.clear(context)
            updateWidgets(context)
        }
    }

    /**
     * 타이머 위젯 업데이트
     */
    private suspend fun updateWidgets(context: Context) {
        try {
            TimerWidgetSmall().updateAll(context)
        } catch (ignored: Exception) {
            // 위젯이 없거나 업데이트 실패 시 무시
        }
    }
}
