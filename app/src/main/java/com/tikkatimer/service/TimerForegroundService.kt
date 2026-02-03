package com.tikkatimer.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.tikkatimer.R
import com.tikkatimer.util.NotificationHelper
import com.tikkatimer.widget.TimerWidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 타이머/스톱워치 백그라운드 실행을 위한 Foreground Service
 * 앱이 백그라운드에 있어도 타이머/스톱워치가 계속 동작하도록 보장
 */
@AndroidEntryPoint
class TimerForegroundService : Service() {
    @Inject lateinit var notificationHelper: NotificationHelper

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var updateJob: Job? = null

    // 타이머 상태
    private val _timerStates = MutableStateFlow<List<TimerServiceState>>(emptyList())
    val timerStates: StateFlow<List<TimerServiceState>> = _timerStates.asStateFlow()

    // 스톱워치 상태
    private val _stopwatchState = MutableStateFlow<StopwatchServiceState?>(null)
    val stopwatchState: StateFlow<StopwatchServiceState?> = _stopwatchState.asStateFlow()

    private var isTimerRunning = false
    private var isStopwatchRunning = false

    companion object {
        const val TAG = "TimerForegroundService"

        const val ACTION_START_TIMER = "com.tikkatimer.ACTION_START_TIMER"
        const val ACTION_STOP_TIMER = "com.tikkatimer.ACTION_STOP_TIMER"
        const val ACTION_START_STOPWATCH = "com.tikkatimer.ACTION_START_STOPWATCH"
        const val ACTION_STOP_STOPWATCH = "com.tikkatimer.ACTION_STOP_STOPWATCH"
        const val ACTION_UPDATE_TIMER = "com.tikkatimer.ACTION_UPDATE_TIMER"

        const val EXTRA_TIMER_ID = "extra_timer_id"
        const val EXTRA_TIMER_NAME = "extra_timer_name"
        const val EXTRA_REMAINING_MILLIS = "extra_remaining_millis"
        const val EXTRA_TARGET_END_TIME = "extra_target_end_time"
        const val EXTRA_STOPWATCH_ELAPSED = "extra_stopwatch_elapsed"

        private const val UPDATE_INTERVAL_MS = 1000L
    }

    inner class LocalBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_TIMER -> handleStartTimer(intent)
            ACTION_STOP_TIMER -> handleStopTimer(intent)
            ACTION_UPDATE_TIMER -> handleUpdateTimer(intent)
            ACTION_START_STOPWATCH -> handleStartStopwatch(intent)
            ACTION_STOP_STOPWATCH -> handleStopStopwatch()
        }

        return START_STICKY
    }

    /**
     * 타이머 시작 처리
     */
    private fun handleStartTimer(intent: Intent) {
        val timerId = intent.getStringExtra(EXTRA_TIMER_ID) ?: return
        val timerName = intent.getStringExtra(EXTRA_TIMER_NAME) ?: ""
        val remainingMillis = intent.getLongExtra(EXTRA_REMAINING_MILLIS, 0L)
        val targetEndTime = intent.getLongExtra(EXTRA_TARGET_END_TIME, 0L)

        Log.d(TAG, "Starting timer $timerId: $timerName, remaining: $remainingMillis")

        val newState =
            TimerServiceState(
                id = timerId,
                name = timerName,
                remainingMillis = remainingMillis,
                targetEndTimeMillis = targetEndTime,
            )

        _timerStates.value = _timerStates.value
            .filter { it.id != timerId } + newState

        isTimerRunning = true
        startForegroundIfNeeded()
        startUpdateLoop()

        // 위젯 업데이트
        TimerWidgetUpdater.onTimerStarted(
            context = this,
            timerId = timerId,
            timerName = timerName,
            remainingMillis = remainingMillis,
            totalMillis = remainingMillis,
        )
    }

    /**
     * 타이머 중지 처리
     */
    private fun handleStopTimer(intent: Intent) {
        val timerId = intent.getStringExtra(EXTRA_TIMER_ID) ?: return
        Log.d(TAG, "Stopping timer $timerId")

        _timerStates.value = _timerStates.value.filter { it.id != timerId }

        if (_timerStates.value.isEmpty()) {
            isTimerRunning = false
            stopForegroundIfNotNeeded()

            // 위젯 업데이트 - 타이머 중지
            TimerWidgetUpdater.onTimerStopped(this)
        } else {
            // 다른 실행 중인 타이머가 있으면 해당 타이머로 위젯 업데이트
            _timerStates.value.firstOrNull()?.let { timer ->
                TimerWidgetUpdater.onTimerStarted(
                    context = this,
                    timerId = timer.id,
                    timerName = timer.name,
                    remainingMillis = timer.remainingMillis,
                    totalMillis = timer.remainingMillis,
                )
            }
        }
    }

    /**
     * 타이머 업데이트 처리
     */
    private fun handleUpdateTimer(intent: Intent) {
        val timerId = intent.getStringExtra(EXTRA_TIMER_ID) ?: return
        val remainingMillis = intent.getLongExtra(EXTRA_REMAINING_MILLIS, 0L)
        val targetEndTime = intent.getLongExtra(EXTRA_TARGET_END_TIME, 0L)

        _timerStates.value =
            _timerStates.value.map { state ->
                if (state.id == timerId) {
                    state.copy(
                        remainingMillis = remainingMillis,
                        targetEndTimeMillis = targetEndTime,
                    )
                } else {
                    state
                }
            }

        updateNotification()
    }

    /**
     * 스톱워치 시작 처리
     */
    private fun handleStartStopwatch(intent: Intent) {
        val elapsedMillis = intent.getLongExtra(EXTRA_STOPWATCH_ELAPSED, 0L)
        Log.d(TAG, "Starting stopwatch with elapsed: $elapsedMillis")

        _stopwatchState.value =
            StopwatchServiceState(
                elapsedMillis = elapsedMillis,
                startTimeMillis = System.currentTimeMillis() - elapsedMillis,
            )

        isStopwatchRunning = true
        startForegroundIfNeeded()
        startUpdateLoop()
    }

    /**
     * 스톱워치 중지 처리
     */
    private fun handleStopStopwatch() {
        Log.d(TAG, "Stopping stopwatch")

        _stopwatchState.value = null
        isStopwatchRunning = false
        stopForegroundIfNotNeeded()
    }

    /**
     * Foreground Service 시작 (필요한 경우)
     */
    private fun startForegroundIfNeeded() {
        try {
            val notification = buildNotification()
            startForeground(NotificationHelper.FOREGROUND_NOTIFICATION_ID, notification.build())
            Log.d(TAG, "Started foreground service")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
            // Android 12+ ForegroundServiceStartNotAllowedException 등 처리
            // 백그라운드에서 시작 제한 시 서비스 중지
            stopSelf()
        }
    }

    /**
     * Foreground Service 중지 (필요 없는 경우)
     */
    private fun stopForegroundIfNotNeeded() {
        if (!isTimerRunning && !isStopwatchRunning) {
            updateJob?.cancel()
            updateJob = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            Log.d(TAG, "Stopped foreground service")
        }
    }

    /**
     * 업데이트 루프 시작
     */
    private fun startUpdateLoop() {
        if (updateJob?.isActive == true) return

        updateJob =
            serviceScope.launch {
                while (isTimerRunning || isStopwatchRunning) {
                    updateStates()
                    updateNotification()
                    delay(UPDATE_INTERVAL_MS)
                }
            }
    }

    /**
     * 상태 업데이트
     */
    private fun updateStates() {
        val now = System.currentTimeMillis()

        // 타이머 상태 업데이트
        if (isTimerRunning) {
            _timerStates.value =
                _timerStates.value.map { state ->
                    if (state.targetEndTimeMillis > 0) {
                        state.copy(
                            remainingMillis = maxOf(0L, state.targetEndTimeMillis - now),
                        )
                    } else {
                        state
                    }
                }

            // 위젯 업데이트 - 실행 중인 첫 번째 타이머 기준
            _timerStates.value.firstOrNull()?.let { timer ->
                TimerWidgetUpdater.onTimerTick(
                    context = this,
                    remainingMillis = timer.remainingMillis,
                )
            }
        }

        // 스톱워치 상태 업데이트
        if (isStopwatchRunning) {
            _stopwatchState.value?.let { state ->
                _stopwatchState.value =
                    state.copy(
                        elapsedMillis = now - state.startTimeMillis,
                    )
            }
        }
    }

    /**
     * 알림 업데이트
     */
    private fun updateNotification() {
        try {
            val notification = buildNotification()
            notificationHelper.showNotification(
                NotificationHelper.FOREGROUND_NOTIFICATION_ID,
                notification,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification", e)
        }
    }

    /**
     * 알림 빌드
     */
    private fun buildNotification(): androidx.core.app.NotificationCompat.Builder {
        val (title, content) =
            when {
                isTimerRunning && isStopwatchRunning -> {
                    val timerCount = _timerStates.value.size
                    val content =
                        "${getString(R.string.timer_count, timerCount)}, " +
                            getString(R.string.stopwatch_foreground_title)
                    getString(R.string.timer_foreground_title) to content
                }
                isTimerRunning -> {
                    val firstTimer = _timerStates.value.firstOrNull()
                    val timerText = firstTimer?.let { formatTime(it.remainingMillis) } ?: ""
                    getString(R.string.timer_foreground_title) to
                        getString(R.string.timer_remaining, timerText)
                }
                isStopwatchRunning -> {
                    val elapsed = _stopwatchState.value?.elapsedMillis ?: 0L
                    getString(R.string.stopwatch_foreground_title) to
                        getString(R.string.stopwatch_elapsed, formatTime(elapsed))
                }
                else -> {
                    "Tikka Timer" to ""
                }
            }

        return notificationHelper.buildForegroundNotification(title, content)
    }

    /**
     * 시간 포맷팅
     */
    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }
}

/**
 * 서비스 내 타이머 상태
 */
data class TimerServiceState(
    val id: String,
    val name: String,
    val remainingMillis: Long,
    val targetEndTimeMillis: Long,
)

/**
 * 서비스 내 스톱워치 상태
 */
data class StopwatchServiceState(
    val elapsedMillis: Long,
    val startTimeMillis: Long,
)
