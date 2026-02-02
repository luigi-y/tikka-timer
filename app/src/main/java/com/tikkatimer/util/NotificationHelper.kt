package com.tikkatimer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.tikkatimer.R
import com.tikkatimer.presentation.alarm.AlarmRingingActivity
import com.tikkatimer.service.AlarmRingingService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 관련 유틸리티
 * 알림 채널 생성 및 알림 빌더 제공
 */
@Singleton
class NotificationHelper
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        companion object {
            const val ALARM_CHANNEL_ID = "tikka_timer_alarm"
            const val TIMER_CHANNEL_ID = "tikka_timer_timer"
            const val FOREGROUND_CHANNEL_ID = "tikka_timer_foreground"

            const val ALARM_NOTIFICATION_ID = 1000
            const val TIMER_NOTIFICATION_ID = 2000
            const val FOREGROUND_NOTIFICATION_ID = 3000
        }

        init {
            createNotificationChannels()
        }

        /**
         * 알림 채널 생성 (Android 8.0+)
         */
        private fun createNotificationChannels() {
            // 알람 채널 (높은 중요도)
            val alarmChannel =
                NotificationChannel(
                    ALARM_CHANNEL_ID,
                    context.getString(R.string.notification_channel_alarm),
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = context.getString(R.string.notification_channel_alarm_desc)
                    enableVibration(true)
                    setBypassDnd(true)
                }

            // 타이머 채널 (높은 중요도)
            val timerChannel =
                NotificationChannel(
                    TIMER_CHANNEL_ID,
                    context.getString(R.string.notification_channel_timer),
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = context.getString(R.string.notification_channel_timer_desc)
                    enableVibration(true)
                }

            // Foreground Service 채널 (낮은 중요도)
            val foregroundChannel =
                NotificationChannel(
                    FOREGROUND_CHANNEL_ID,
                    context.getString(R.string.notification_channel_foreground),
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = context.getString(R.string.notification_channel_foreground_desc)
                }

            notificationManager.createNotificationChannels(
                listOf(alarmChannel, timerChannel, foregroundChannel),
            )
        }

        /**
         * 알람 울림 알림 생성 (Full-screen intent 포함)
         */
        fun buildAlarmNotification(
            alarmId: Long,
            label: String?,
            timeText: String,
        ): NotificationCompat.Builder {
            // 알람 울림 Activity 실행 Intent
            val fullScreenIntent =
                Intent(context, AlarmRingingActivity::class.java).apply {
                    putExtra(AlarmRingingActivity.EXTRA_ALARM_ID, alarmId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            val fullScreenPendingIntent =
                PendingIntent.getActivity(
                    context,
                    alarmId.toInt(),
                    fullScreenIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            // 해제 버튼 Intent
            val dismissIntent =
                Intent(context, AlarmRingingService::class.java).apply {
                    action = AlarmRingingService.ACTION_DISMISS
                    putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
                }
            val dismissPendingIntent =
                PendingIntent.getService(
                    context,
                    alarmId.toInt() + 1000,
                    dismissIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            // 스누즈 버튼 Intent
            val snoozeIntent =
                Intent(context, AlarmRingingService::class.java).apply {
                    action = AlarmRingingService.ACTION_SNOOZE
                    putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
                }
            val snoozePendingIntent =
                PendingIntent.getService(
                    context,
                    alarmId.toInt() + 2000,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            val title = label?.ifEmpty { null } ?: context.getString(R.string.alarm_notification_title)

            return NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(title)
                .setContentText(timeText)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(false)
                .setOngoing(true)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .addAction(
                    R.drawable.ic_close,
                    context.getString(R.string.alarm_dismiss),
                    dismissPendingIntent,
                )
                .addAction(
                    R.drawable.ic_snooze,
                    context.getString(R.string.alarm_snooze),
                    snoozePendingIntent,
                )
        }

        /**
         * 타이머/스톱워치 Foreground Service 알림 생성
         */
        fun buildForegroundNotification(
            title: String,
            content: String,
        ): NotificationCompat.Builder {
            val packageManager = context.packageManager
            val launchIntent =
                packageManager.getLaunchIntentForPackage(context.packageName)
                    ?: Intent()

            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            return NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
        }

        /**
         * 알림 표시
         */
        fun showNotification(
            notificationId: Int,
            builder: NotificationCompat.Builder,
        ) {
            notificationManager.notify(notificationId, builder.build())
        }

        /**
         * 알림 취소
         */
        fun cancelNotification(notificationId: Int) {
            notificationManager.cancel(notificationId)
        }
    }
