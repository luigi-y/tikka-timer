package com.tikkatimer.presentation.timer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.tikkatimer.ui.theme.TikkaTimerTheme

/**
 * 타이머 시간 선택 컴포넌트
 * 시/분/초를 스피너 형태로 선택
 */
@Composable
fun TimePicker(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 시간
        TimeSpinner(
            value = hours,
            label = "시간",
            maxValue = 23,
            onValueChange = onHoursChange,
        )

        TimeSeparator()

        // 분
        TimeSpinner(
            value = minutes,
            label = "분",
            maxValue = 59,
            onValueChange = onMinutesChange,
        )

        TimeSeparator()

        // 초
        TimeSpinner(
            value = seconds,
            label = "초",
            maxValue = 59,
            onValueChange = onSecondsChange,
        )
    }
}

@Composable
private fun TimeSpinner(
    value: Int,
    label: String,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 증가 버튼
        IconButton(
            onClick = {
                val newValue = if (value >= maxValue) 0 else value + 1
                onValueChange(newValue)
            },
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.increase),
            )
        }

        // 값 표시
        Text(
            text = String.format("%02d", value),
            style =
                MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light,
                    fontSize = 48.sp,
                ),
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // 라벨
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 감소 버튼
        IconButton(
            onClick = {
                val newValue = if (value <= 0) maxValue else value - 1
                onValueChange(newValue)
            },
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.decrease),
            )
        }
    }
}

@Composable
private fun TimeSeparator() {
    Text(
        text = ":",
        style =
            MaterialTheme.typography.displayMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                fontSize = 48.sp,
            ),
        modifier = Modifier.width(24.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true)
@Composable
private fun TimePickerPreview() {
    TikkaTimerTheme {
        TimePicker(
            hours = 0,
            minutes = 5,
            seconds = 0,
            onHoursChange = {},
            onMinutesChange = {},
            onSecondsChange = {},
        )
    }
}
