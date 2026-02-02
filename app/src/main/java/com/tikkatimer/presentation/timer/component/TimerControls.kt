package com.tikkatimer.presentation.timer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tikkatimer.R
import com.tikkatimer.domain.model.TimerState
import com.tikkatimer.ui.theme.TikkaTimerTheme

/**
 * 타이머 제어 버튼 컴포넌트
 * 시작/일시정지, 리셋, +1분 추가 버튼 제공
 */
@Composable
fun TimerControls(
    state: TimerState,
    canStart: Boolean,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onCancelClick: () -> Unit,
    onAddMinuteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (state) {
            TimerState.IDLE -> {
                // 취소 버튼 (비활성)
                FilledTonalIconButton(
                    onClick = onCancelClick,
                    modifier = Modifier.size(56.dp),
                    enabled = canStart,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancel),
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // 시작 버튼
                FilledIconButton(
                    onClick = onStartClick,
                    modifier = Modifier.size(72.dp),
                    enabled = canStart,
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.timer_start),
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // 자리 유지
                Spacer(modifier = Modifier.size(56.dp))
            }

            TimerState.RUNNING -> {
                // 리셋 버튼
                FilledTonalIconButton(
                    onClick = onResetClick,
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.timer_reset),
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // 일시정지 버튼
                FilledIconButton(
                    onClick = onPauseClick,
                    modifier = Modifier.size(72.dp),
                    colors =
                        IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = stringResource(R.string.timer_pause),
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // +1분 버튼
                FilledTonalIconButton(
                    onClick = onAddMinuteClick,
                    modifier = Modifier.size(56.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.timer_add_minute),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            TimerState.PAUSED -> {
                // 취소 버튼
                FilledTonalIconButton(
                    onClick = onCancelClick,
                    modifier = Modifier.size(56.dp),
                    colors =
                        IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancel),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // 재개 버튼
                FilledIconButton(
                    onClick = onStartClick,
                    modifier = Modifier.size(72.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.timer_resume),
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // +1분 버튼
                FilledTonalIconButton(
                    onClick = onAddMinuteClick,
                    modifier = Modifier.size(56.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.timer_add_minute),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            TimerState.FINISHED -> {
                // 완료 확인 버튼
                FilledIconButton(
                    onClick = onCancelClick,
                    modifier = Modifier.size(72.dp),
                    colors =
                        IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.confirm),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerControlsIdlePreview() {
    TikkaTimerTheme {
        TimerControls(
            state = TimerState.IDLE,
            canStart = true,
            onStartClick = {},
            onPauseClick = {},
            onResetClick = {},
            onCancelClick = {},
            onAddMinuteClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerControlsRunningPreview() {
    TikkaTimerTheme {
        TimerControls(
            state = TimerState.RUNNING,
            canStart = true,
            onStartClick = {},
            onPauseClick = {},
            onResetClick = {},
            onCancelClick = {},
            onAddMinuteClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerControlsPausedPreview() {
    TikkaTimerTheme {
        TimerControls(
            state = TimerState.PAUSED,
            canStart = true,
            onStartClick = {},
            onPauseClick = {},
            onResetClick = {},
            onCancelClick = {},
            onAddMinuteClick = {},
        )
    }
}
