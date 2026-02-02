package com.tikkatimer.presentation.stopwatch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tikkatimer.domain.model.LapTime
import com.tikkatimer.domain.model.Stopwatch
import com.tikkatimer.domain.model.StopwatchState
import com.tikkatimer.presentation.stopwatch.component.LapTimeList
import com.tikkatimer.presentation.stopwatch.component.StopwatchControls
import com.tikkatimer.presentation.stopwatch.component.StopwatchDisplay
import com.tikkatimer.ui.theme.TikkaTimerTheme

/**
 * 스톱워치 화면
 * 시작/일시정지/리셋 및 랩 타임 기록 기능 제공
 */
@Suppress("DEPRECATION")
@Composable
fun StopwatchScreen(
    modifier: Modifier = Modifier,
    viewModel: StopwatchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    StopwatchScreenContent(
        stopwatch = uiState.stopwatch,
        onStartClick = viewModel::start,
        onPauseClick = viewModel::pause,
        onResetClick = viewModel::reset,
        onLapClick = viewModel::recordLap,
        modifier = modifier,
    )
}

@Composable
private fun StopwatchScreenContent(
    stopwatch: Stopwatch,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onLapClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // 시간 표시
        StopwatchDisplay(
            stopwatch = stopwatch,
            modifier = Modifier.padding(vertical = 24.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 제어 버튼
        StopwatchControls(
            state = stopwatch.state,
            onStartClick = onStartClick,
            onPauseClick = onPauseClick,
            onResetClick = onResetClick,
            onLapClick = onLapClick,
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 랩 타임 목록
        if (stopwatch.lapTimes.isNotEmpty()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            LapTimeList(
                lapTimes = stopwatch.lapTimes,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StopwatchScreenPreview() {
    TikkaTimerTheme {
        StopwatchScreenContent(
            stopwatch = Stopwatch.INITIAL,
            onStartClick = {},
            onPauseClick = {},
            onResetClick = {},
            onLapClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StopwatchScreenRunningPreview() {
    TikkaTimerTheme {
        StopwatchScreenContent(
            stopwatch =
                Stopwatch(
                    elapsedMillis = 65230,
                    state = StopwatchState.RUNNING,
                    lapTimes =
                        listOf(
                            LapTime(lapNumber = 2, lapMillis = 32150, totalMillis = 65230),
                            LapTime(lapNumber = 1, lapMillis = 33080, totalMillis = 33080),
                        ),
                ),
            onStartClick = {},
            onPauseClick = {},
            onResetClick = {},
            onLapClick = {},
        )
    }
}
