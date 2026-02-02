package com.tikkatimer.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.VibrationPattern
import com.tikkatimer.domain.repository.AlarmRepository
import com.tikkatimer.domain.scheduler.AlarmScheduler
import com.tikkatimer.util.AlarmSoundManager
import com.tikkatimer.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 알람 울림 서비스
 * 알람이 울릴 때 소리/진동을 관리하고 알림을 표시
 */
@AndroidEntryPoint
class AlarmRingingService : Service() {
    @Inject lateinit var alarmSoundManager: AlarmSoundManager

    @Inject lateinit var notificationHelper: NotificationHelper

    @Inject lateinit var alarmRepository: AlarmRepository

    @Inject lateinit var alarmScheduler: AlarmScheduler

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentAlarmId: Long = -1

    companion object {
        const val TAG = "AlarmRingingService"

        const val ACTION_START = "com.tikkatimer.ACTION_START_ALARM"
        const val ACTION_DISMISS = "com.tikkatimer.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE = "com.tikkatimer.ACTION_SNOOZE_ALARM"

        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_SOUND_TYPE = "extra_sound_type"
        const val EXTRA_VIBRATION_PATTERN = "extra_vibration_pattern"
        const val EXTRA_RINGTONE_URI = "extra_ringtone_uri"
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_TIME_TEXT = "extra_time_text"
        const val EXTRA_IS_ONE_TIME = "extra_is_one_time"
        const val EXTRA_SNOOZE_DURATION = "extra_snooze_duration"

        private const val DEFAULT_SNOOZE_MINUTES = 5
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START -> handleStartAlarm(intent)
            ACTION_DISMISS -> handleDismiss(intent)
            ACTION_SNOOZE -> handleSnooze(intent)
            else -> Log.w(TAG, "Unknown action: ${intent?.action}")
        }

        return START_NOT_STICKY
    }

    /**
     * 알람 시작 처리
     */
    private fun handleStartAlarm(intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1)
        if (alarmId == -1L) {
            Log.e(TAG, "Invalid alarm ID")
            stopSelf()
            return
        }

        currentAlarmId = alarmId

        val soundTypeName = intent.getStringExtra(EXTRA_SOUND_TYPE) ?: SoundType.DEFAULT.name
        val vibrationPatternName =
            intent.getStringExtra(EXTRA_VIBRATION_PATTERN)
                ?: VibrationPattern.DEFAULT.name
        val ringtoneUri = intent.getStringExtra(EXTRA_RINGTONE_URI)
        val label = intent.getStringExtra(EXTRA_LABEL) ?: ""
        val timeText = intent.getStringExtra(EXTRA_TIME_TEXT) ?: ""
        val isOneTime = intent.getBooleanExtra(EXTRA_IS_ONE_TIME, false)

        val soundType = SoundType.fromName(soundTypeName)
        val vibrationPattern = VibrationPattern.fromName(vibrationPatternName)

        Log.d(TAG, "Starting alarm $alarmId - sound: $soundType, vibration: $vibrationPattern")

        // Foreground Service로 시작 (에러 핸들링 추가)
        try {
            val notification = notificationHelper.buildAlarmNotification(alarmId, label, timeText)
            startForeground(NotificationHelper.ALARM_NOTIFICATION_ID + alarmId.toInt(), notification.build())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service for alarm $alarmId", e)
            // Foreground 실패해도 소리/진동은 시도
        }

        // 소리/진동 시작 (각각 독립적으로 에러 처리)
        try {
            alarmSoundManager.startAlarmSound(soundType, ringtoneUri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start alarm sound for alarm $alarmId", e)
        }

        try {
            alarmSoundManager.startVibration(vibrationPattern)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start vibration for alarm $alarmId", e)
        }

        // 1회성 알람이면 비활성화
        if (isOneTime) {
            serviceScope.launch {
                try {
                    alarmRepository.setAlarmEnabled(alarmId, false)
                    Log.d(TAG, "One-time alarm $alarmId disabled")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to disable one-time alarm $alarmId", e)
                }
            }
        }
    }

    /**
     * 알람 해제 처리
     */
    private fun handleDismiss(intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, currentAlarmId)
        Log.d(TAG, "Dismissing alarm $alarmId")

        // 소리/진동 중지
        alarmSoundManager.stopAll()

        // 알림 취소
        notificationHelper.cancelNotification(NotificationHelper.ALARM_NOTIFICATION_ID + alarmId.toInt())

        // 반복 알람이면 다음 알람 스케줄링
        serviceScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            alarm?.let {
                if (it.isRepeating && it.isEnabled) {
                    alarmScheduler.schedule(it)
                    Log.d(TAG, "Next alarm scheduled for repeating alarm $alarmId")
                }
            }
        }

        // Activity 종료 브로드캐스트
        sendBroadcast(
            Intent("com.tikkatimer.ALARM_DISMISSED").apply {
                putExtra(EXTRA_ALARM_ID, alarmId)
                setPackage(packageName)
            },
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * 스누즈 처리
     */
    private fun handleSnooze(intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, currentAlarmId)
        val snoozeDuration = intent.getIntExtra(EXTRA_SNOOZE_DURATION, DEFAULT_SNOOZE_MINUTES)
        Log.d(TAG, "Snoozing alarm $alarmId for $snoozeDuration minutes")

        // 소리/진동 중지
        alarmSoundManager.stopAll()

        // 알림 취소
        notificationHelper.cancelNotification(NotificationHelper.ALARM_NOTIFICATION_ID + alarmId.toInt())

        // 스누즈 알람 스케줄링
        serviceScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            alarm?.let {
                // 스누즈 시간 후 다시 울리도록 스케줄링
                val snoozeAlarm =
                    it.copy(
                        time = it.time.plusMinutes(snoozeDuration.toLong()),
                    )
                alarmScheduler.schedule(snoozeAlarm)
                Log.d(TAG, "Snooze alarm scheduled for $snoozeDuration minutes later")
            }
        }

        // Activity 종료 브로드캐스트
        sendBroadcast(
            Intent("com.tikkatimer.ALARM_SNOOZED").apply {
                putExtra(EXTRA_ALARM_ID, alarmId)
                setPackage(packageName)
            },
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        alarmSoundManager.stopAll()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }
}
