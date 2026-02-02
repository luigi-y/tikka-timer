package com.tikkatimer.presentation.alarm

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.tikkatimer.R
import com.tikkatimer.service.AlarmRingingService
import com.tikkatimer.ui.theme.TikkaTimerTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 알람 울림 화면 Activity
 * 잠금 화면에서도 표시되며, 해제/스누즈 버튼 제공
 */
@AndroidEntryPoint
class AlarmRingingActivity : ComponentActivity() {
    private var alarmId: Long = -1
    private var alarmLabel by mutableStateOf("")
    private var alarmTime by mutableStateOf("")
    private var snoozeDuration: Int = 5

    private val dismissReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?,
        ) {
            val dismissedAlarmId = intent?.getLongExtra(AlarmRingingService.EXTRA_ALARM_ID, -1) ?: -1
            if (dismissedAlarmId == alarmId || dismissedAlarmId == -1L) {
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_TIME_TEXT = "extra_time_text"
        const val EXTRA_SNOOZE_DURATION = "extra_snooze_duration"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 잠금 화면 위에 표시 설정
        setupLockScreenFlags()

        // Intent에서 알람 정보 추출
        alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1)
        alarmLabel = intent.getStringExtra(EXTRA_LABEL) ?: ""
        alarmTime = intent.getStringExtra(EXTRA_TIME_TEXT)
            ?: LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        snoozeDuration = intent.getIntExtra(EXTRA_SNOOZE_DURATION, 5)

        // 알람 해제/스누즈 브로드캐스트 수신 등록
        val filter = IntentFilter().apply {
            addAction("com.tikkatimer.ALARM_DISMISSED")
            addAction("com.tikkatimer.ALARM_SNOOZED")
        }
        ContextCompat.registerReceiver(
            this,
            dismissReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        setContent {
            TikkaTimerTheme {
                AlarmRingingScreen(
                    label = alarmLabel,
                    timeText = alarmTime,
                    onDismiss = ::dismissAlarm,
                    onSnooze = ::snoozeAlarm,
                )
            }
        }
    }

    /**
     * 잠금 화면 위에 표시하기 위한 플래그 설정
     */
    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
            )
        }

        // 화면 켜진 상태 유지
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * 알람 해제
     */
    private fun dismissAlarm() {
        val dismissIntent = Intent(this, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_DISMISS
            putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
        }
        startService(dismissIntent)
    }

    /**
     * 스누즈
     */
    private fun snoozeAlarm() {
        val snoozeIntent = Intent(this, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_SNOOZE
            putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmRingingService.EXTRA_SNOOZE_DURATION, snoozeDuration)
        }
        startService(snoozeIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(dismissReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }

    override fun onBackPressed() {
        // 뒤로 가기 비활성화 - 사용자가 명시적으로 해제/스누즈 해야 함
    }
}

/**
 * 알람 울림 화면 UI
 */
@Composable
private fun AlarmRingingScreen(
    label: String,
    timeText: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // 알람 아이콘
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 시간 표시
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            // 라벨 표시
            if (label.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                // 스누즈 버튼
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(R.string.alarm_snooze))
                }

                // 해제 버튼
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(R.string.alarm_dismiss))
                }
            }
        }
    }
}
