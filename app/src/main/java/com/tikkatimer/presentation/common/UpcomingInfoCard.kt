package com.tikkatimer.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tikkatimer.R
import com.tikkatimer.domain.model.UpcomingInfo
import com.tikkatimer.ui.theme.TikkaTimerTheme
import java.time.Duration
import java.time.LocalDateTime

/**
 * 임박한 알람/타이머 정보를 표시하는 카드 컴포넌트
 * 화면 상단에 다음 알람 시간과 활성화된 알람 개수를 표시
 */
@Composable
fun UpcomingInfoCard(
    upcomingInfo: UpcomingInfo,
    type: UpcomingInfoType = UpcomingInfoType.ALARM,
    modifier: Modifier = Modifier,
) {
    if (upcomingInfo.activeAlarmCount == 0) {
        return
    }

    val icon =
        when (type) {
            UpcomingInfoType.ALARM -> Icons.Default.Alarm
            UpcomingInfoType.TIMER -> Icons.Default.Timer
        }

    val countText =
        when (type) {
            UpcomingInfoType.ALARM -> stringResource(R.string.alarm_count, upcomingInfo.activeAlarmCount)
            UpcomingInfoType.TIMER -> stringResource(R.string.timer_count, upcomingInfo.activeAlarmCount)
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                // 다음 알람 시간
                formatNextAlarmTimeText(upcomingInfo.nextAlarmTime)?.let { timeText ->
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                // 남은 시간
                formatTimeUntilText(upcomingInfo.nextAlarmTime)?.let { remainingText ->
                    Text(
                        text = remainingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }

            // 활성화된 알람 개수
            Text(
                text = countText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
        }
    }
}

/**
 * 임박 정보 카드 타입
 */
enum class UpcomingInfoType {
    ALARM,
    TIMER,
}

/**
 * 다음 알람 시간을 "오전/오후 HH:MM" 형식으로 반환
 */
@Composable
private fun formatNextAlarmTimeText(nextAlarmTime: LocalDateTime?): String? {
    val targetTime = nextAlarmTime ?: return null

    val hour = targetTime.hour
    val minute = targetTime.minute
    val period = if (hour < 12) stringResource(R.string.alarm_am) else stringResource(R.string.alarm_pm)
    val displayHour =
        when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
    return "$period $displayHour:${minute.toString().padStart(2, '0')}"
}

/**
 * 다음 알람까지 남은 시간을 읽기 쉬운 문자열로 변환
 */
@Composable
private fun formatTimeUntilText(nextAlarmTime: LocalDateTime?): String? {
    val targetTime = nextAlarmTime ?: return null

    val now = LocalDateTime.now()
    if (targetTime.isBefore(now)) return null

    val duration = Duration.between(now, targetTime)
    val days = duration.toDays().toInt()
    val hours = (duration.toHours() % 24).toInt()
    val minutes = (duration.toMinutes() % 60).toInt()

    return when {
        days > 0 && hours > 0 -> stringResource(R.string.time_until_days_hours, days, hours)
        days > 0 -> stringResource(R.string.time_until_days, days)
        hours > 0 && minutes > 0 -> stringResource(R.string.time_until_hours_minutes, hours, minutes)
        hours > 0 -> stringResource(R.string.time_until_hours, hours)
        minutes > 0 -> stringResource(R.string.time_until_minutes, minutes)
        else -> stringResource(R.string.time_until_soon)
    }
}

@Preview(showBackground = true)
@Composable
private fun UpcomingInfoCardPreview() {
    TikkaTimerTheme {
        UpcomingInfoCard(
            upcomingInfo =
                UpcomingInfo(
                    nextAlarmTime = LocalDateTime.now().plusHours(3).plusMinutes(30),
                    activeAlarmCount = 3,
                ),
            type = UpcomingInfoType.ALARM,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UpcomingInfoCardTimerPreview() {
    TikkaTimerTheme {
        UpcomingInfoCard(
            upcomingInfo =
                UpcomingInfo(
                    nextAlarmTime = LocalDateTime.now().plusMinutes(15),
                    activeAlarmCount = 1,
                ),
            type = UpcomingInfoType.TIMER,
            modifier = Modifier.padding(16.dp),
        )
    }
}
