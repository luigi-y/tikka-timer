package com.luigi.tikkatimer.presentation.stopwatch.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luigi.tikkatimer.R
import com.luigi.tikkatimer.domain.model.StopwatchState
import com.luigi.tikkatimer.ui.theme.TikkaTimerTheme

/**
 * 스톱워치 제어 버튼 컴포넌트
 * 3버튼 구조: 전체 초기화, 시작/일시정지, 랩타임
 */
@Composable
fun StopwatchControls(
    state: StopwatchState,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onLapClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRunning = state == StopwatchState.RUNNING
    val isIdle = state == StopwatchState.IDLE

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ResetButton(
            enabled = !isIdle,
            onClick = onResetClick,
        )

        Spacer(modifier = Modifier.width(24.dp))

        PlayPauseButton(
            isRunning = isRunning,
            onPlayClick = onStartClick,
            onPauseClick = onPauseClick,
        )

        Spacer(modifier = Modifier.width(24.dp))

        LapButton(
            enabled = isRunning,
            onClick = onLapClick,
        )
    }
}

/**
 * 전체 초기화 버튼 (IDLE 상태에서만 비활성화)
 */
@Composable
private fun ResetButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        enabled = enabled,
        colors =
            IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = stringResource(R.string.stopwatch_reset),
            modifier = Modifier.size(28.dp),
            tint =
                if (enabled) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                },
        )
    }
}

/**
 * 시작/일시정지 토글 버튼
 */
@Composable
private fun PlayPauseButton(
    isRunning: Boolean,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
) {
    val containerColor =
        if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow
    val contentDescriptionRes = if (isRunning) R.string.stopwatch_pause else R.string.stopwatch_start

    FilledIconButton(
        onClick = if (isRunning) onPauseClick else onPlayClick,
        modifier = Modifier.size(80.dp),
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(contentDescriptionRes),
            modifier = Modifier.size(36.dp),
        )
    }
}

/**
 * 랩타임 기록 버튼 (RUNNING 상태에서만 활성화)
 */
@Composable
private fun LapButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        enabled = enabled,
    ) {
        Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = stringResource(R.string.stopwatch_lap),
            modifier = Modifier.size(28.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StopwatchControlsIdlePreview() {
    TikkaTimerTheme {
        StopwatchControls(
            state = StopwatchState.IDLE,
            onStartClick = {},
            onPauseClick = {},
            onResetClick = {},
            onLapClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StopwatchControlsRunningPreview() {
    TikkaTimerTheme {
        StopwatchControls(
            state = StopwatchState.RUNNING,
            onStartClick = {},
            onPauseClick = {},
            onResetClick = {},
            onLapClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StopwatchControlsPausedPreview() {
    TikkaTimerTheme {
        StopwatchControls(
            state = StopwatchState.PAUSED,
            onStartClick = {},
            onPauseClick = {},
            onResetClick = {},
            onLapClick = {},
        )
    }
}
