package com.tikkatimer.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.domain.scheduler.AlarmScheduler
import com.tikkatimer.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AlarmScheduler 구현체
 * AlarmManager를 사용하여 시스템 알람을 등록/취소
 */
@Singleton
class AlarmSchedulerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : AlarmScheduler {
        private val alarmManager: AlarmManager? =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        companion object {
            private const val TAG = "AlarmSchedulerImpl"
            private const val MAX_RETRY_COUNT = 3
            private const val RETRY_DELAY_MS = 100L
        }

        /**
         * 알람을 시스템에 등록
         * setAlarmClock 사용 - Doze 모드에서도 정확한 시간에 울림
         */
        override fun schedule(alarm: Alarm) {
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is not available on this device")
                return
            }

            if (!alarm.isEnabled) {
                Log.d(TAG, "Alarm ${alarm.id} is disabled, skipping schedule")
                return
            }

            // Android 12+ 정확한 알람 권한 확인
            if (!canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms - permission not granted")
                // 권한이 없어도 setAlarmClock은 시도 (시스템이 허용할 수 있음)
            }

            // 다음 알람 시간 계산
            val nextAlarmDateTime = alarm.getNextAlarmDateTime()
            val triggerTimeMillis = nextAlarmDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            Log.d(TAG, "Scheduling alarm ${alarm.id} at $nextAlarmDateTime (${triggerTimeMillis}ms)")

            scheduleWithRetry(alarm, triggerTimeMillis, MAX_RETRY_COUNT)
        }

        /**
         * 재시도 로직이 포함된 알람 스케줄링
         */
        private fun scheduleWithRetry(alarm: Alarm, triggerTimeMillis: Long, retryCount: Int) {
            try {
                // PendingIntent 생성
                val pendingIntent = createAlarmPendingIntent(alarm)

                // 알람 정보 표시용 PendingIntent (알람 클릭 시 앱 열기)
                val showIntent = createShowPendingIntent(alarm.id)

                // setAlarmClock 사용 - Doze 모드 예외 적용됨
                // 알람 앱은 SCHEDULE_EXACT_ALARM 없이도 setAlarmClock 사용 가능
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMillis, showIntent)
                alarmManager?.setAlarmClock(alarmClockInfo, pendingIntent)

                Log.d(TAG, "Alarm ${alarm.id} scheduled successfully")
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException scheduling alarm ${alarm.id}", e)
            } catch (e: IllegalStateException) {
                // 리소스 부족 등의 상황에서 재시도
                if (retryCount > 0) {
                    Log.w(TAG, "Retrying alarm schedule for ${alarm.id}, remaining attempts: $retryCount")
                    Thread.sleep(RETRY_DELAY_MS)
                    scheduleWithRetry(alarm, triggerTimeMillis, retryCount - 1)
                } else {
                    Log.e(TAG, "Failed to schedule alarm ${alarm.id} after retries", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error scheduling alarm ${alarm.id}", e)
            }
        }

        /**
         * 알람을 시스템에서 취소
         */
        override fun cancel(alarmId: Long) {
            Log.d(TAG, "Cancelling alarm $alarmId")

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.tikkatimer.ALARM_$alarmId"
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )

            pendingIntent?.let {
                alarmManager?.cancel(it)
                it.cancel()
                Log.d(TAG, "Alarm $alarmId cancelled successfully")
            } ?: Log.d(TAG, "No pending intent found for alarm $alarmId")
        }

        /**
         * 정확한 알람 권한 확인 (Android 12+)
         */
        override fun canScheduleExactAlarms(): Boolean {
            if (alarmManager == null) return false
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    alarmManager.canScheduleExactAlarms()
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking exact alarm permission", e)
                    false
                }
            } else {
                true
            }
        }

        /**
         * 알람 트리거용 PendingIntent 생성
         */
        private fun createAlarmPendingIntent(alarm: Alarm): PendingIntent {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.tikkatimer.ALARM_${alarm.id}"
                putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
                putExtra(AlarmReceiver.EXTRA_SOUND_TYPE, alarm.soundType.name)
                putExtra(AlarmReceiver.EXTRA_VIBRATION_PATTERN, alarm.vibrationPattern.name)
                alarm.ringtoneUri?.let { putExtra(AlarmReceiver.EXTRA_RINGTONE_URI, it) }
            }

            return PendingIntent.getBroadcast(
                context,
                alarm.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        /**
         * 알람 클릭 시 앱 표시용 PendingIntent 생성
         */
        private fun createShowPendingIntent(alarmId: Long): PendingIntent {
            val packageManager = context.packageManager
            val launchIntent = packageManager.getLaunchIntentForPackage(context.packageName)
                ?: Intent()

            return PendingIntent.getActivity(
                context,
                alarmId.toInt(),
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
