package com.tikkatimer.presentation.stopwatch.component

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
import com.tikkatimer.R
import com.tikkatimer.domain.model.StopwatchState
import com.tikkatimer.ui.theme.TikkaTimerTheme

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
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 왼쪽 버튼: 전체 초기화 (항상 표시, IDLE 상태에서만 비활성화)
        FilledTonalIconButton(
            onClick = onResetClick,
            modifier = Modifier.size(64.dp),
            enabled = state != StopwatchState.IDLE,
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
                    if (state != StopwatchState.IDLE) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    },
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // 중앙 버튼: 시작/일시정지
        FilledIconButton(
            onClick = {
                when (state) {
                    StopwatchState.RUNNING -> onPauseClick()
                    else -> onStartClick()
                }
            },
            modifier = Modifier.size(80.dp),
            colors =
                IconButtonDefaults.filledIconButtonColors(
                    containerColor =
                        if (state == StopwatchState.RUNNING) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                ),
        ) {
            Icon(
                imageVector =
                    if (state == StopwatchState.RUNNING) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                contentDescription =
                    if (state == StopwatchState.RUNNING) {
                        stringResource(R.string.stopwatch_pause)
                    } else {
                        stringResource(R.string.stopwatch_start)
                    },
                modifier = Modifier.size(36.dp),
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // 오른쪽 버튼: 랩타임 (RUNNING 상태에서만 활성화)
        FilledTonalIconButton(
            onClick = onLapClick,
            modifier = Modifier.size(64.dp),
            enabled = state == StopwatchState.RUNNING,
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = stringResource(R.string.stopwatch_lap),
                modifier = Modifier.size(28.dp),
            )
        }
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
