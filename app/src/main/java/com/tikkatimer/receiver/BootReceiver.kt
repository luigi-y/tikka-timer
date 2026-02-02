package com.tikkatimer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tikkatimer.domain.repository.AlarmRepository
import com.tikkatimer.domain.scheduler.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 기기 재부팅 시 호출되는 BroadcastReceiver
 * 저장된 알람을 다시 AlarmManager에 등록
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var alarmRepository: AlarmRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler

    companion object {
        const val TAG = "BootReceiver"
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        Log.d(TAG, "Boot completed, restoring alarms...")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 활성화된 알람 목록 조회
                val enabledAlarms = alarmRepository.getEnabledAlarms().first()
                Log.d(TAG, "Found ${enabledAlarms.size} enabled alarms to restore")

                // 각 알람을 AlarmManager에 다시 등록
                enabledAlarms.forEach { alarm ->
                    try {
                        alarmScheduler.schedule(alarm)
                        Log.d(TAG, "Restored alarm ${alarm.id}: ${alarm.getTimeText()}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to restore alarm ${alarm.id}", e)
                    }
                }

                Log.d(TAG, "All alarms restored successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring alarms", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
