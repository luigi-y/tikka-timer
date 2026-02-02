package com.tikkatimer.presentation.stopwatch.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
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
import com.tikkatimer.R
import com.tikkatimer.domain.model.LapTime
import com.tikkatimer.ui.theme.TikkaTimerTheme

/**
 * 랩 타임 목록 컴포넌트
 * 기록된 랩 타임을 최신순으로 표시
 */
@Composable
fun LapTimeList(
    lapTimes: List<LapTime>,
    modifier: Modifier = Modifier,
) {
    if (lapTimes.isEmpty()) return

    LazyColumn(
        modifier = modifier,
    ) {
        items(lapTimes) { lapTime ->
            // 이전 랩 찾기 (현재 랩 번호 - 1인 랩)
            // 목록은 최신순이므로, 현재 인덱스 + 1이 이전 랩
            val currentIndex = lapTimes.indexOf(lapTime)
            val previousLap =
                if (currentIndex < lapTimes.size - 1) {
                    lapTimes[currentIndex + 1]
                } else {
                    null // 첫 번째 랩은 이전 랩이 없음
                }

            LapTimeItem(
                lapTime = lapTime,
                previousLapTime = previousLap,
                isFastest = lapTimes.size > 1 && lapTime == lapTimes.minByOrNull { it.lapMillis },
                isSlowest = lapTimes.size > 1 && lapTime == lapTimes.maxByOrNull { it.lapMillis },
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun LapTimeItem(
    lapTime: LapTime,
    previousLapTime: LapTime?,
    isFastest: Boolean,
    isSlowest: Boolean,
) {
    val textColor =
        when {
            isFastest -> MaterialTheme.colorScheme.primary
            isSlowest -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurface
        }

    // 이전 랩과의 차이 계산 (첫 번째 랩은 이전 랩이 없음)
    val diffMillis = previousLapTime?.let { lapTime.lapMillis - it.lapMillis }
    val diffText =
        diffMillis?.let { diff ->
            val sign = if (diff >= 0) "+" else ""
            val absDiff = kotlin.math.abs(diff)
            val minutes = (absDiff / 60000).toInt()
            val seconds = ((absDiff % 60000) / 1000).toInt()
            val millis = ((absDiff % 1000) / 10).toInt()
            "$sign${String.format("%02d:%02d.%02d", minutes, seconds, millis)}"
        }
    val diffColor =
        when {
            diffMillis == null -> MaterialTheme.colorScheme.onSurfaceVariant
            diffMillis < 0 -> MaterialTheme.colorScheme.primary // 더 빠름 (좋음)
            diffMillis > 0 -> MaterialTheme.colorScheme.error // 더 느림
            else -> MaterialTheme.colorScheme.onSurfaceVariant // 동일
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 랩 번호
        Text(
            text = stringResource(R.string.stopwatch_lap_number, lapTime.lapNumber),
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                ),
            color = textColor,
            modifier = Modifier.width(72.dp),
        )

        // 랩 구간 시간
        Text(
            text = lapTime.getFormattedLapTime(),
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                ),
            color = textColor,
        )

        // 이전 랩과의 차이 시간 (첫 번째 랩은 "-" 표시)
        Text(
            text = diffText ?: "-",
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                ),
            color =
                if (diffText != null) {
                    diffColor
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.5f,
                    )
                },
            modifier = Modifier.width(80.dp),
        )

        // 총 경과 시간
        Text(
            text = lapTime.getFormattedTotalTime(),
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                ),
            color = textColor.copy(alpha = 0.7f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LapTimeListPreview() {
    TikkaTimerTheme {
        LapTimeList(
            lapTimes =
                listOf(
                    LapTime(lapNumber = 3, lapMillis = 32450, totalMillis = 95670),
                    LapTime(lapNumber = 2, lapMillis = 28120, totalMillis = 63220),
                    LapTime(lapNumber = 1, lapMillis = 35100, totalMillis = 35100),
                ),
        )
    }
}
