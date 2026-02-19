package com.tikkatimer.sync

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.tikkatimer.domain.model.RunningTimer
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.TimerState
import com.tikkatimer.domain.model.VibrationPattern
import com.tikkatimer.service.TimerForegroundService
import com.tikkatimer.util.AlarmSoundManager
import com.tikkatimer.util.NotificationHelper
import com.tikkatimer.widget.TimerWidgetProvider
import com.tikkatimer.widget.TimerWidgetStateManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 타이머 상태 동기화 매니저
 * 알림창, 위젯, 앱 UI 상태를 단일 소스로 관리
 *
 * 동기화 대상:
 * 1. 앱 UI (ViewModel)
 * 2. 위젯 (Glance)
 * 3. 알림창 (Foreground Service Notification)
 */
@Singleton
class TimerStateSync
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val notificationHelper: NotificationHelper,
        private val alarmSoundManager: AlarmSoundManager,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /** 현재 동기화된 타이머 상태 */
        private val _currentTimers = MutableStateFlow<List<SyncedTimerState>>(emptyList())
        val currentTimers: StateFlow<List<SyncedTimerState>> = _currentTimers.asStateFlow()

        /** 서비스 실행 여부 */
        private var isServiceRunning = false

        /** 이전 틱에서 완료되지 않았던 타이머 ID 목록 (중복 알림 방지) */
        private val notifiedFinishedTimers = mutableSetOf<String>()

        companion object {
            private const val TAG = "TimerStateSync"
        }

        /**
         * 타이머 상태 업데이트 및 모든 대상에 동기화
         * ViewModel에서 타이머 상태 변경 시 호출
         */
        fun syncTimers(timers: List<RunningTimer>) {
            Log.d(TAG, "Syncing ${timers.size} timers")

            val syncedStates =
                timers.map { timer ->
                    SyncedTimerState(
                        instanceId = timer.instanceId,
                        name = timer.name,
                        remainingMillis = timer.remainingMillis,
                        totalMillis = timer.totalDurationMillis,
                        state = timer.state,
                        targetEndTimeMillis = timer.targetEndTimeMillis,
                        soundType = timer.soundType,
                        vibrationPattern = timer.vibrationPattern,
                    )
                }

            _currentTimers.value = syncedStates

            // 완료된 타이머 소리/진동 처리
            handleFinishedTimers(syncedStates)

            scope.launch {
                // 1. 위젯 업데이트
                updateWidgetState(syncedStates)

                // 2. 알림 업데이트 (실행 중인 타이머가 있을 때만)
                updateNotificationState(syncedStates)

                // 3. Foreground Service 상태 관리
                manageForegroundService(syncedStates)
            }
        }

        /**
         * 남은 시간만 업데이트 (주기적 틱용)
         * 매 초마다 호출되어 위젯과 알림 업데이트, 완료된 타이머 소리/진동 처리
         */
        fun syncTick(timers: List<RunningTimer>) {
            val syncedStates =
                timers.map { timer ->
                    SyncedTimerState(
                        instanceId = timer.instanceId,
                        name = timer.name,
                        remainingMillis = timer.remainingMillis,
                        totalMillis = timer.totalDurationMillis,
                        state = timer.state,
                        targetEndTimeMillis = timer.targetEndTimeMillis,
                        soundType = timer.soundType,
                        vibrationPattern = timer.vibrationPattern,
                    )
                }

            _currentTimers.value = syncedStates

            // 새로 완료된 타이머 감지 및 소리/진동 재생
            handleFinishedTimers(syncedStates)

            scope.launch {
                // 위젯 업데이트
                updateWidgetState(syncedStates)

                // 알림 업데이트
                updateNotificationState(syncedStates)
            }
        }

        /**
         * 완료된 타이머 처리 (소리/진동 재생)
         */
        private fun handleFinishedTimers(timers: List<SyncedTimerState>) {
            val finishedTimers = timers.filter { it.state == TimerState.FINISHED }

            for (timer in finishedTimers) {
                // 이미 알림을 보낸 타이머는 건너뜀
                if (timer.instanceId in notifiedFinishedTimers) continue

                Log.d(
                    TAG,
                    "Timer finished: ${timer.name}, sound=${timer.soundType}, " +
                        "vibration=${timer.vibrationPattern}",
                )

                // 소리 재생
                alarmSoundManager.startAlarmSound(timer.soundType)

                // 진동 재생
                alarmSoundManager.startVibration(timer.vibrationPattern)

                // 알림 완료 표시
                notifiedFinishedTimers.add(timer.instanceId)
            }

            // 완료 상태가 아닌 타이머는 알림 목록에서 제거 (재시작 시 다시 알림)
            val currentTimerIds = timers.map { it.instanceId }.toSet()
            notifiedFinishedTimers.removeAll { it !in currentTimerIds }
        }

        /**
         * 완료된 타이머의 소리/진동 중지
         * 사용자가 타이머 완료를 확인(acknowledge)했을 때 호출
         */
        fun stopFinishedAlarm(instanceId: String) {
            Log.d(TAG, "Stopping alarm for timer: $instanceId")
            alarmSoundManager.stopAll()
            notifiedFinishedTimers.remove(instanceId)
        }

        /**
         * 현재 동기화된 타이머 상태 반환
         * ViewModel 초기화 시 상태 복원에 사용
         */
        fun getRestoredTimers(): List<RunningTimer> {
            return _currentTimers.value.map { synced ->
                RunningTimer(
                    instanceId = synced.instanceId,
                    presetId = 0,
                    name = synced.name,
                    totalDurationMillis = synced.totalMillis,
                    remainingMillis = synced.remainingMillis,
                    state = synced.state,
                    soundType = synced.soundType,
                    vibrationPattern = synced.vibrationPattern,
                    targetEndTimeMillis = synced.targetEndTimeMillis,
                )
            }
        }

        /**
         * 실행 중인 타이머 존재 여부
         */
        fun hasRunningTimers(): Boolean {
            return _currentTimers.value.any { it.state == TimerState.RUNNING }
        }

        /**
         * 모든 타이머 중지 및 정리
         */
        fun clearAll() {
            Log.d(TAG, "Clearing all timer states")
            _currentTimers.value = emptyList()
            notifiedFinishedTimers.clear()

            // 소리/진동 중지
            alarmSoundManager.stopAll()

            scope.launch {
                // 위젯 초기화
                TimerWidgetStateManager.clear(context)
                updateWidgets()

                // 서비스 중지
                stopForegroundService()
            }
        }

        /**
         * 현재 타이머 상태를 위젯에 동기화
         * 앱이 포그라운드로 돌아오거나 탭 이동 시 호출
         */
        suspend fun syncCurrentStateToWidget() {
            Log.d(TAG, "Syncing current state to widget")
            updateWidgetState(_currentTimers.value)
        }

        /**
         * 위젯 상태 업데이트
         */
        private suspend fun updateWidgetState(timers: List<SyncedTimerState>) {
            val finishedTimer = timers.firstOrNull { it.state == TimerState.FINISHED }
            val activeTimer = timers.firstOrNull { it.state == TimerState.RUNNING }
            val pausedTimer = timers.firstOrNull { it.state == TimerState.PAUSED }

            when {
                finishedTimer != null -> {
                    TimerWidgetStateManager.setFinished(
                        context = context,
                        timerName = finishedTimer.name,
                    )
                }
                activeTimer != null -> {
                    TimerWidgetStateManager.setRunning(
                        context = context,
                        timerId = activeTimer.instanceId,
                        timerName = activeTimer.name,
                        remainingMillis = activeTimer.remainingMillis,
                        totalMillis = activeTimer.totalMillis,
                        targetEndTimeMillis = activeTimer.targetEndTimeMillis,
                    )
                }
                pausedTimer != null -> {
                    TimerWidgetStateManager.setPaused(
                        context = context,
                        remainingMillis = pausedTimer.remainingMillis,
                    )
                }
                else -> {
                    TimerWidgetStateManager.clear(context)
                }
            }

            updateWidgets()
        }

        /**
         * 위젯 업데이트
         * RemoteViews 기반 TimerWidgetProvider 사용
         */
        private fun updateWidgets() {
            try {
                TimerWidgetProvider.updateAllWidgets(context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update widgets", e)
            }
        }

        /**
         * 알림 상태 업데이트
         */
        private fun updateNotificationState(timers: List<SyncedTimerState>) {
            val runningTimers = timers.filter { it.state == TimerState.RUNNING }

            if (runningTimers.isEmpty()) {
                // 실행 중인 타이머가 없으면 알림 제거는 서비스에서 처리
                return
            }

            // 실행 중인 첫 번째 타이머로 알림 업데이트
            val firstTimer = runningTimers.first()
            val timeText = formatTime(firstTimer.remainingMillis)
            val title = context.getString(com.tikkatimer.R.string.timer_foreground_title)
            val content = context.getString(com.tikkatimer.R.string.timer_remaining, timeText)

            try {
                val notification = notificationHelper.buildForegroundNotification(title, content)
                notificationHelper.showNotification(
                    NotificationHelper.FOREGROUND_NOTIFICATION_ID,
                    notification,
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update notification", e)
            }
        }

        /**
         * Foreground Service 관리
         */
        private fun manageForegroundService(timers: List<SyncedTimerState>) {
            val hasRunningTimer = timers.any { it.state == TimerState.RUNNING }

            if (hasRunningTimer && !isServiceRunning) {
                startForegroundService(timers.first { it.state == TimerState.RUNNING })
            } else if (!hasRunningTimer && isServiceRunning) {
                stopForegroundService()
            }
        }

        /**
         * Foreground Service 시작
         */
        private fun startForegroundService(timer: SyncedTimerState) {
            val intent =
                Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_START_TIMER
                    putExtra(TimerForegroundService.EXTRA_TIMER_ID, timer.instanceId)
                    putExtra(TimerForegroundService.EXTRA_TIMER_NAME, timer.name)
                    putExtra(TimerForegroundService.EXTRA_REMAINING_MILLIS, timer.remainingMillis)
                    putExtra(TimerForegroundService.EXTRA_TARGET_END_TIME, timer.targetEndTimeMillis)
                }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                isServiceRunning = true
                Log.d(TAG, "Foreground service started")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start foreground service", e)
            }
        }

        /**
         * Foreground Service 중지
         */
        private fun stopForegroundService() {
            try {
                val intent = Intent(context, TimerForegroundService::class.java)
                context.stopService(intent)
                isServiceRunning = false
                Log.d(TAG, "Foreground service stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop foreground service", e)
            }
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
    }

/**
 * 동기화용 타이머 상태
 */
data class SyncedTimerState(
    val instanceId: String,
    val name: String,
    val remainingMillis: Long,
    val totalMillis: Long,
    val state: TimerState,
    val targetEndTimeMillis: Long,
    val soundType: SoundType = SoundType.DEFAULT,
    val vibrationPattern: VibrationPattern = VibrationPattern.DEFAULT,
)
