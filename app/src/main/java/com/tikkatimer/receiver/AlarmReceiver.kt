package com.tikkatimer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.VibrationPattern
import com.tikkatimer.domain.repository.AlarmRepository
import com.tikkatimer.presentation.alarm.AlarmRingingActivity
import com.tikkatimer.service.AlarmRingingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 알람 시간이 되었을 때 호출되는 BroadcastReceiver
 * AlarmRingingService를 시작하여 알람 울림 처리
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var alarmRepository: AlarmRepository

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_SOUND_TYPE = "extra_sound_type"
        const val EXTRA_VIBRATION_PATTERN = "extra_vibration_pattern"
        const val EXTRA_RINGTONE_URI = "extra_ringtone_uri"
        const val TAG = "AlarmReceiver"
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1)

        if (alarmId == -1L) {
            Log.e(TAG, "Invalid alarm ID received")
            return
        }

        Log.d(TAG, "Alarm triggered: $alarmId")

        // 소리/진동 정보 추출
        val soundTypeName = intent.getStringExtra(EXTRA_SOUND_TYPE) ?: SoundType.DEFAULT.name
        val vibrationPatternName =
            intent.getStringExtra(EXTRA_VIBRATION_PATTERN)
                ?: VibrationPattern.DEFAULT.name
        val ringtoneUri = intent.getStringExtra(EXTRA_RINGTONE_URI)

        Log.d(TAG, "Sound type: $soundTypeName, Vibration: $vibrationPatternName")

        // 알람 정보 조회 후 서비스 시작
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarm = alarmRepository.getAlarmById(alarmId)

                if (alarm == null) {
                    Log.e(TAG, "Alarm $alarmId not found in database")
                    pendingResult.finish()
                    return@launch
                }

                // AlarmRingingService 시작
                val serviceIntent =
                    Intent(context, AlarmRingingService::class.java).apply {
                        action = AlarmRingingService.ACTION_START
                        putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
                        putExtra(AlarmRingingService.EXTRA_SOUND_TYPE, soundTypeName)
                        putExtra(AlarmRingingService.EXTRA_VIBRATION_PATTERN, vibrationPatternName)
                        putExtra(AlarmRingingService.EXTRA_RINGTONE_URI, ringtoneUri)
                        putExtra(AlarmRingingService.EXTRA_LABEL, alarm.label)
                        putExtra(AlarmRingingService.EXTRA_TIME_TEXT, alarm.getTimeText())
                        putExtra(AlarmRingingService.EXTRA_IS_ONE_TIME, alarm.isOneTime)
                        putExtra(AlarmRingingService.EXTRA_SNOOZE_DURATION, alarm.snoozeDurationMinutes)
                    }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                // 알람 울림 Activity 시작
                val activityIntent =
                    Intent(context, AlarmRingingActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra(AlarmRingingActivity.EXTRA_ALARM_ID, alarmId)
                        putExtra(AlarmRingingActivity.EXTRA_LABEL, alarm.label)
                        putExtra(AlarmRingingActivity.EXTRA_TIME_TEXT, alarm.getTimeText())
                        putExtra(AlarmRingingActivity.EXTRA_SNOOZE_DURATION, alarm.snoozeDurationMinutes)
                    }
                context.startActivity(activityIntent)

                Log.d(TAG, "Alarm service and activity started for alarm $alarmId")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling alarm $alarmId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
