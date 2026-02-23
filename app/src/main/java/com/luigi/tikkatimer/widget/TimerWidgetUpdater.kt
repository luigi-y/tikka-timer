package com.luigi.tikkatimer.widget

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 타이머 위젯 업데이트 헬퍼
 * ViewModel이나 Service에서 위젯 상태를 업데이트할 때 사용
 */
object TimerWidgetUpdater {
    private const val TAG = "TimerWidgetUpdater"
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
        Log.d(TAG, "onTimerStarted: $timerName, remaining=$remainingMillis")
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
     * 테마 변경 시 위젯 업데이트
     * idle 상태의 아이콘 tint/배경색을 새 테마에 맞게 갱신
     */
    fun onThemeChanged(context: Context) {
        Log.d(TAG, "onThemeChanged: refreshing widget colors")
        scope.launch {
            updateWidgets(context)
            // Glance 위젯도 업데이트
            updateGlanceWidgets(context)
        }
    }

    /**
     * 타이머 위젯 업데이트
     * RemoteViews 기반 TimerWidgetProvider 사용
     */
    private fun updateWidgets(context: Context) {
        try {
            Log.d(TAG, "updateWidgets: calling TimerWidgetProvider")
            TimerWidgetProvider.updateAllWidgets(context)
        } catch (e: Exception) {
            Log.e(TAG, "updateWidgets: failed", e)
        }
    }

    /**
     * Glance 기반 위젯 업데이트
     */
    private suspend fun updateGlanceWidgets(context: Context) {
        try {
            val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
            val widget = TimerWidgetSmall()
            val glanceIds = manager.getGlanceIds(TimerWidgetSmall::class.java)
            glanceIds.forEach { glanceId ->
                widget.update(context, glanceId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateGlanceWidgets: failed", e)
        }
    }
}
