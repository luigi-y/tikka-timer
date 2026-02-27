package com.luigi.tikkatimer.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import com.luigi.tikkatimer.MainActivity
import com.luigi.tikkatimer.R

/**
 * RemoteViews 기반 타이머 위젯 Provider
 * 완전 동기 방식으로 구현하여 안정성 확보
 */
class TimerWidgetProvider : AppWidgetProvider() {
    companion object {
        private const val TAG = "TimerWidgetProvider"
        private const val ACTION_WIDGET_CLICK = "com.luigi.tikkatimer.ACTION_WIDGET_CLICK"
        private const val ACTION_TIMER_FINISHED = "com.luigi.tikkatimer.ACTION_TIMER_FINISHED"
        private const val ALARM_REQUEST_CODE = 9999

        /**
         * 모든 위젯 인스턴스 업데이트
         */
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TimerWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

            Log.d(TAG, "updateAllWidgets: ${widgetIds.size} widgets found")

            if (widgetIds.isNotEmpty()) {
                val state = TimerWidgetStateManager.getStateSync(context)
                for (widgetId in widgetIds) {
                    updateWidgetInternal(context, appWidgetManager, widgetId, state)
                }
            }
        }

        private fun updateWidgetInternal(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            state: TimerWidgetState,
        ) {
            val layoutId =
                when {
                    state.isFinished -> R.layout.widget_timer_small_finished
                    state.isRunning -> R.layout.widget_timer_small_running
                    state.isPaused -> R.layout.widget_timer_small_paused
                    else -> R.layout.widget_timer_small
                }

            val views = RemoteViews(context.packageName, layoutId)

            // 모든 상태에서 앱 설정의 테마 색상을 아이콘에 적용
            val colorTheme = WidgetColors.readColorThemeSync(context)
            val tintColor = WidgetColors.getIdleAccentArgb(colorTheme)
            views.setInt(R.id.widget_icon, "setColorFilter", tintColor)

            // 실행 중: Chronometer로 카운트다운 (시스템이 자동 업데이트)
            if (state.isRunning && state.targetEndTimeMillis > 0) {
                val now = System.currentTimeMillis()
                val remainingMillis = state.targetEndTimeMillis - now

                if (remainingMillis > 0) {
                    // elapsedRealtime 기준으로 종료 시간 계산
                    val elapsedBase = SystemClock.elapsedRealtime() + remainingMillis
                    views.setChronometer(R.id.widget_timer, elapsedBase, null, true)
                    views.setChronometerCountDown(R.id.widget_timer, true)

                    // 타이머 종료 시간에 위젯 업데이트 알람 예약
                    scheduleTimerFinishedAlarm(context, state.targetEndTimeMillis)
                }
            }

            // 일시정지: TextView로 정지된 시간 표시
            if (state.isPaused) {
                val timeText = formatTime(state.remainingMillis)
                views.setTextViewText(R.id.widget_time, timeText)
            }

            // 클릭 시 위젯 상태 검증 후 앱 실행
            val clickIntent =
                Intent(context, TimerWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_CLICK
                }
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)

            val stateStr =
                when {
                    state.isFinished -> "finished"
                    state.isRunning -> "running"
                    state.isPaused -> "paused"
                    else -> "idle"
                }
            Log.d(
                TAG,
                "updateWidgetInternal: widget $appWidgetId updated, " +
                    "state=$stateStr, time=${state.currentRemainingMillis}",
            )
        }

        /**
         * 시간 포맷팅 (MM:SS 또는 H:MM:SS)
         */
        private fun formatTime(millis: Long): String {
            val totalSeconds = millis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }

        /**
         * 타이머 종료 시간에 위젯 업데이트 알람 예약
         */
        private fun scheduleTimerFinishedAlarm(
            context: Context,
            targetEndTimeMillis: Long,
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent =
                Intent(context, TimerWidgetProvider::class.java).apply {
                    action = ACTION_TIMER_FINISHED
                }
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    ALARM_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            // 기존 알람 취소 후 새로 예약
            alarmManager.cancel(pendingIntent)

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            targetEndTimeMillis,
                            pendingIntent,
                        )
                    } else {
                        // 정확한 알람 권한 없으면 일반 알람 사용
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            targetEndTimeMillis,
                            pendingIntent,
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetEndTimeMillis,
                        pendingIntent,
                    )
                }
                Log.d(TAG, "scheduleTimerFinishedAlarm: alarm scheduled at $targetEndTimeMillis")
            } catch (e: Exception) {
                Log.e(TAG, "scheduleTimerFinishedAlarm: failed", e)
            }
        }

        /**
         * 타이머 종료 알람 취소
         */
        fun cancelTimerFinishedAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent =
                Intent(context, TimerWidgetProvider::class.java).apply {
                    action = ACTION_TIMER_FINISHED
                }
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    ALARM_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "cancelTimerFinishedAlarm: alarm cancelled")
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        Log.d(TAG, "onUpdate: updating ${appWidgetIds.size} widgets")

        val state = TimerWidgetStateManager.getStateSync(context)

        for (appWidgetId in appWidgetIds) {
            updateWidgetInternal(context, appWidgetManager, appWidgetId, state)
        }
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive: ${intent.action}")

        when (intent.action) {
            ACTION_WIDGET_CLICK -> handleWidgetClick(context)
            ACTION_TIMER_FINISHED -> handleTimerFinished(context)
        }
    }

    /**
     * 타이머 종료 시 위젯 업데이트
     */
    private fun handleTimerFinished(context: Context) {
        Log.d(TAG, "handleTimerFinished: timer ended, setting finished state")

        // 위젯 상태를 finished(빨간색 경고)로 변경
        val currentState = TimerWidgetStateManager.getStateSync(context)
        TimerWidgetStateManager.setFinishedSync(context, currentState.timerName)
        updateAllWidgets(context)
    }

    /**
     * 위젯 클릭 처리
     * 상태 검증 후 앱의 타이머 탭으로 이동
     */
    private fun handleWidgetClick(context: Context) {
        Log.d(TAG, "handleWidgetClick: validating state")

        // 위젯 상태 검증
        val state = TimerWidgetStateManager.getStateSync(context)

        if (state.isRunning && state.targetEndTimeMillis > 0) {
            val now = System.currentTimeMillis()
            if (now > state.targetEndTimeMillis) {
                // 타이머 종료 시간이 지났으면 idle로 초기화
                Log.d(TAG, "handleWidgetClick: timer expired, clearing state")
                TimerWidgetStateManager.clearSync(context)
                updateAllWidgets(context)
            }
        }

        // 앱의 타이머 탭으로 이동
        val openAppIntent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(MainActivity.EXTRA_NAVIGATE_TO_TIMER, true)
            }
        context.startActivity(openAppIntent)
    }
}
