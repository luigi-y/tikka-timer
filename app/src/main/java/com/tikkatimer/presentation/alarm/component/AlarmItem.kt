package com.tikkatimer.presentation.alarm.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tikkatimer.R
import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.ui.theme.TikkaTimerTheme
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * 알람 목록 아이템 컴포넌트
 * 알람 시간, 라벨, 반복 요일, 활성화 스위치 표시
 */
@Composable
fun AlarmItem(
    alarm: Alarm,
    onToggle: (Alarm) -> Unit,
    onClick: (Alarm) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentAlpha = if (alarm.isEnabled) 1f else 0.5f
    val timeText = formatAlarmTime(alarm.time)
    val repeatDaysText = formatRepeatDays(alarm.repeatDays)

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick(alarm) },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                // 시간 표시
                Text(
                    text = timeText,
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Normal,
                            fontSize = 32.sp,
                        ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 라벨 또는 반복 요일
                Text(
                    text = alarm.label.ifEmpty { repeatDaysText },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                )

                // 라벨이 있고 반복 알람인 경우 반복 요일도 표시
                if (alarm.label.isNotEmpty() && alarm.isRepeating) {
                    Text(
                        text = repeatDaysText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha * 0.8f),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 활성화 스위치
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggle(alarm) },
            )
        }
    }
}

/**
 * 알람 시간을 현재 언어 설정에 맞게 포맷팅
 */
@Composable
private fun formatAlarmTime(time: LocalTime): String {
    val hour = time.hour
    val minute = time.minute
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
 * 반복 요일을 현재 언어 설정에 맞게 포맷팅
 */
@Composable
private fun formatRepeatDays(repeatDays: Set<DayOfWeek>): String {
    if (repeatDays.isEmpty()) return stringResource(R.string.alarm_repeat_none)

    val weekdays =
        setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
        )
    val weekend = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

    // Composable 컨텍스트에서 요일 문자열 맵 생성
    val dayStrings =
        mapOf(
            DayOfWeek.MONDAY to stringResource(R.string.day_mon),
            DayOfWeek.TUESDAY to stringResource(R.string.day_tue),
            DayOfWeek.WEDNESDAY to stringResource(R.string.day_wed),
            DayOfWeek.THURSDAY to stringResource(R.string.day_thu),
            DayOfWeek.FRIDAY to stringResource(R.string.day_fri),
            DayOfWeek.SATURDAY to stringResource(R.string.day_sat),
            DayOfWeek.SUNDAY to stringResource(R.string.day_sun),
        )

    return when {
        repeatDays.size == 7 -> stringResource(R.string.alarm_repeat_everyday)
        repeatDays == weekdays -> stringResource(R.string.alarm_repeat_weekdays)
        repeatDays == weekend -> stringResource(R.string.alarm_repeat_weekend)
        else ->
            repeatDays
                .sortedBy { it.value }
                .joinToString(", ") { dayStrings[it] ?: "" }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmItemEnabledPreview() {
    TikkaTimerTheme {
        AlarmItem(
            alarm =
                Alarm(
                    id = 1,
                    time = LocalTime.of(7, 30),
                    isEnabled = true,
                    label = "출근 알람",
                    repeatDays =
                        setOf(
                            DayOfWeek.MONDAY,
                            DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY,
                            DayOfWeek.FRIDAY,
                        ),
                ),
            onToggle = {},
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmItemDisabledPreview() {
    TikkaTimerTheme {
        AlarmItem(
            alarm =
                Alarm(
                    id = 2,
                    time = LocalTime.of(9, 0),
                    isEnabled = false,
                    label = "",
                    repeatDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                ),
            onToggle = {},
            onClick = {},
        )
    }
}
