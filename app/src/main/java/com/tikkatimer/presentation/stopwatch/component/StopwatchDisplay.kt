package com.tikkatimer.presentation.stopwatch.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tikkatimer.domain.model.Stopwatch
import com.tikkatimer.ui.theme.TikkaTimerTheme

/**
 * 스톱워치 시간 표시 컴포넌트
 * 시/분/초/센티초를 큰 폰트로 표시
 */
@Composable
fun StopwatchDisplay(
    stopwatch: Stopwatch,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom,
        ) {
            // 시간 (1시간 이상일 때만 표시)
            if (stopwatch.hours > 0) {
                TimeUnit(
                    value = stopwatch.hours,
                    label = "시간",
                )
                TimeSeparator()
            }

            // 분
            TimeUnit(
                value = stopwatch.minutes,
                label = "분",
            )
            TimeSeparator()

            // 초
            TimeUnit(
                value = stopwatch.seconds,
                label = "초",
            )
            MillisSeparator()

            // 센티초 (밀리초/10)
            Text(
                text = String.format("%02d", stopwatch.milliseconds),
                style =
                    MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        fontSize = 48.sp,
                    ),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun TimeUnit(
    value: Int,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = String.format("%02d", value),
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light,
                    fontSize = 72.sp,
                ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TimeSeparator() {
    Text(
        text = ":",
        style =
            MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                fontSize = 72.sp,
            ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun MillisSeparator() {
    Text(
        text = ".",
        style =
            MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                fontSize = 48.sp,
            ),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun StopwatchDisplayPreview() {
    // 1:01:01.23
    TikkaTimerTheme {
        StopwatchDisplay(
            stopwatch = Stopwatch(elapsedMillis = 3661230),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StopwatchDisplayShortPreview() {
    // 1:05.23
    TikkaTimerTheme {
        StopwatchDisplay(
            stopwatch = Stopwatch(elapsedMillis = 65230),
        )
    }
}
