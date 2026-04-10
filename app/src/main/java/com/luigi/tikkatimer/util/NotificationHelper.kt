package com.luigi.tikkatimer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.luigi.tikkatimer.MainActivity
import com.luigi.tikkatimer.R
import com.luigi.tikkatimer.presentation.alarm.AlarmRingingActivity
import com.luigi.tikkatimer.service.AlarmRingingService
import com.luigi.tikkatimer.service.TimerForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.media.app.NotificationCompat as MediaNotificationCompat

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

            val builder =
                NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_alarm)
                    .setContentTitle(title)
                    .setContentText(timeText)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(false)
                    .setOngoing(true)

            // Android 14+: full-screen intent 권한 확인 후 설정
            val canUseFullScreen =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    notificationManager.canUseFullScreenIntent()
                } else {
                    true
                }

            if (canUseFullScreen) {
                builder.setFullScreenIntent(fullScreenPendingIntent, true)
            } else {
                // 권한 없으면 일반 알림의 contentIntent로 fallback
                builder.setContentIntent(fullScreenPendingIntent)
            }

            return builder
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
         * 클릭 시 타이머 탭으로 이동, 타이머 실행 중이면 액션 버튼 표시
         */
        fun buildForegroundNotification(
            title: String,
            content: String,
            timerId: String? = null,
            isTimerRunning: Boolean = false,
        ): NotificationCompat.Builder {
            // 타이머 탭으로 이동하는 Intent
            val timerIntent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(MainActivity.EXTRA_NAVIGATE_TO_TIMER, true)
                }

            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    timerIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            val builder =
                NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_hourglass)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)

            // 타이머 ID가 있으면 액션 버튼 추가
            if (timerId != null) {
                addTimerNotificationActions(builder, timerId, isTimerRunning)
            }

            return builder
        }

        /**
         * 타이머 알림 액션 버튼 추가 (MediaStyle 적용)
         * 접힌 상태: Pause/Resume, Cancel 아이콘이 오른쪽에 표시
         * 펼친 상태: +1분 버튼 추가 표시
         *
         * 액션 순서: [0] Pause/Resume, [1] Cancel, [2] +1분
         * compact view: [0, 1] → 접힌 상태에서 Pause/Resume + Cancel 표시
         */
        private fun addTimerNotificationActions(
            builder: NotificationCompat.Builder,
            timerId: String,
            isTimerRunning: Boolean,
        ) {
            val baseRequestCode = FOREGROUND_NOTIFICATION_ID

            // [0] Pause/Resume 토글
            if (isTimerRunning) {
                val pauseIntent =
                    Intent(context, TimerForegroundService::class.java).apply {
                        action = TimerForegroundService.ACTION_PAUSE_TIMER
                        putExtra(TimerForegroundService.EXTRA_TIMER_ID, timerId)
                    }
                val pausePendingIntent =
                    PendingIntent.getService(
                        context,
                        baseRequestCode + 1,
                        pauseIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                builder.addAction(
                    R.drawable.ic_timer_paused,
                    context.getString(R.string.timer_pause),
                    pausePendingIntent,
                )
            } else {
                val resumeIntent =
                    Intent(context, TimerForegroundService::class.java).apply {
                        action = TimerForegroundService.ACTION_RESUME_TIMER
                        putExtra(TimerForegroundService.EXTRA_TIMER_ID, timerId)
                    }
                val resumePendingIntent =
                    PendingIntent.getService(
                        context,
                        baseRequestCode + 1,
                        resumeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                builder.addAction(
                    R.drawable.ic_timer_running,
                    context.getString(R.string.timer_resume),
                    resumePendingIntent,
                )
            }

            // [1] 취소 버튼
            val cancelIntent =
                Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_CANCEL_TIMER
                    putExtra(TimerForegroundService.EXTRA_TIMER_ID, timerId)
                }
            val cancelPendingIntent =
                PendingIntent.getService(
                    context,
                    baseRequestCode + 3,
                    cancelIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            builder.addAction(
                R.drawable.ic_close,
                context.getString(R.string.notification_timer_cancel),
                cancelPendingIntent,
            )

            // [2] +1분 버튼 (펼친 상태에서만 표시)
            val addMinuteIntent =
                Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_ADD_MINUTE
                    putExtra(TimerForegroundService.EXTRA_TIMER_ID, timerId)
                }
            val addMinutePendingIntent =
                PendingIntent.getService(
                    context,
                    baseRequestCode + 2,
                    addMinuteIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            builder.addAction(
                0,
                context.getString(R.string.timer_add_minute),
                addMinutePendingIntent,
            )

            // MediaStyle 적용: 접힌 상태에서 [0] Pause/Resume, [1] Cancel을 오른쪽 아이콘으로 표시
            builder.setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1),
            )
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
