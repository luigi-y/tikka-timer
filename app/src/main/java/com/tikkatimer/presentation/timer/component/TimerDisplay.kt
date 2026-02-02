package com.tikkatimer.presentation.timer.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tikkatimer.R
import com.tikkatimer.domain.model.Timer
import com.tikkatimer.domain.model.TimerState
import com.tikkatimer.ui.theme.TikkaTimerTheme

/**
 * 타이머 시간 표시 컴포넌트
 * 원형 진행 표시기와 함께 남은 시간 표시
 */
@Composable
fun TimerDisplay(
    timer: Timer,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = timer.progress,
        label = "timer_progress",
    )

    val progressColor =
        when (timer.state) {
            TimerState.FINISHED -> MaterialTheme.colorScheme.error
            TimerState.PAUSED -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.primary
        }

    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 배경 원
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(280.dp),
            strokeWidth = 12.dp,
            color = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )

        // 진행 원
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(280.dp),
            strokeWidth = 12.dp,
            color = progressColor,
            strokeCap = StrokeCap.Round,
        )

        // 시간 표시
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = timer.getFormattedTime(),
                style =
                    MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        fontSize = if (timer.hours > 0) 48.sp else 64.sp,
                    ),
                color =
                    if (timer.state == TimerState.FINISHED) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )

            if (timer.state == TimerState.FINISHED) {
                Text(
                    text = stringResource(R.string.timer_state_finished),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerDisplayPreview() {
    TikkaTimerTheme {
        TimerDisplay(
            timer =
                Timer(
                    totalDurationMillis = 300000,
                    remainingMillis = 180000,
                    state = TimerState.RUNNING,
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerDisplayFinishedPreview() {
    TikkaTimerTheme {
        TimerDisplay(
            timer =
                Timer(
                    totalDurationMillis = 300000,
                    remainingMillis = 0,
                    state = TimerState.FINISHED,
                ),
        )
    }
}
