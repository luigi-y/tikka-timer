package com.tikkatimer.presentation.stopwatch

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tikkatimer.domain.model.LapTime
import com.tikkatimer.domain.model.Stopwatch
import com.tikkatimer.domain.model.StopwatchState
import com.tikkatimer.service.TimerForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 스톱워치 ViewModel
 * 시간 측정, 랩 타임 기록, 시작/일시정지/리셋 기능 관리
 */
@HiltViewModel
class StopwatchViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(StopwatchUiState())
        val uiState: StateFlow<StopwatchUiState> = _uiState.asStateFlow()

        private var timerJob: Job? = null
        private var startTimeMillis: Long = 0L
        private var accumulatedMillis: Long = 0L

        companion object {
            private const val UPDATE_INTERVAL_MS = 10L // 10ms 간격으로 업데이트 (센티초 단위)
        }

        /**
         * 스톱워치 시작/재개
         */
        fun start() {
            if (_uiState.value.stopwatch.state == StopwatchState.RUNNING) return

            startTimeMillis = System.currentTimeMillis()

            _uiState.update { currentState ->
                currentState.copy(
                    stopwatch = currentState.stopwatch.copy(state = StopwatchState.RUNNING),
                )
            }

            startTimer()
            startForegroundService()
        }

        /**
         * 스톱워치 일시정지
         */
        fun pause() {
            if (_uiState.value.stopwatch.state != StopwatchState.RUNNING) return

            timerJob?.cancel()
            timerJob = null

            // 경과 시간 누적
            accumulatedMillis += System.currentTimeMillis() - startTimeMillis

            _uiState.update { currentState ->
                currentState.copy(
                    stopwatch = currentState.stopwatch.copy(state = StopwatchState.PAUSED),
                )
            }

            stopForegroundService()
        }

        /**
         * 스톱워치 리셋
         */
        fun reset() {
            timerJob?.cancel()
            timerJob = null
            accumulatedMillis = 0L
            startTimeMillis = 0L

            _uiState.update {
                StopwatchUiState()
            }

            stopForegroundService()
        }

        /**
         * 랩 타임 기록
         */
        fun recordLap() {
            if (_uiState.value.stopwatch.state != StopwatchState.RUNNING) return

            val currentElapsed = calculateCurrentElapsed()
            val lapTimes = _uiState.value.stopwatch.lapTimes
            val previousTotalMillis = lapTimes.firstOrNull()?.totalMillis ?: 0L
            val lapMillis = currentElapsed - previousTotalMillis
            val lapNumber = lapTimes.size + 1

            val newLapTime =
                LapTime(
                    lapNumber = lapNumber,
                    lapMillis = lapMillis,
                    totalMillis = currentElapsed,
                )

            // 최신 랩이 맨 위로
            _uiState.update { currentState ->
                currentState.copy(
                    stopwatch =
                        currentState.stopwatch.copy(
                            lapTimes = listOf(newLapTime) + lapTimes,
                        ),
                )
            }
        }

        private fun startTimer() {
            timerJob =
                viewModelScope.launch {
                    while (true) {
                        val currentElapsed = calculateCurrentElapsed()

                        _uiState.update { currentState ->
                            currentState.copy(
                                stopwatch =
                                    currentState.stopwatch.copy(
                                        elapsedMillis = currentElapsed,
                                    ),
                            )
                        }

                        delay(UPDATE_INTERVAL_MS)
                    }
                }
        }

        private fun calculateCurrentElapsed(): Long {
            return if (_uiState.value.stopwatch.state == StopwatchState.RUNNING) {
                accumulatedMillis + (System.currentTimeMillis() - startTimeMillis)
            } else {
                accumulatedMillis
            }
        }

        override fun onCleared() {
            super.onCleared()
            timerJob?.cancel()
        }

        /**
         * Foreground Service 시작
         */
        private fun startForegroundService() {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = TimerForegroundService.ACTION_START_STOPWATCH
                putExtra(TimerForegroundService.EXTRA_STOPWATCH_ELAPSED, accumulatedMillis)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Foreground Service 중지
         */
        private fun stopForegroundService() {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = TimerForegroundService.ACTION_STOP_STOPWATCH
            }
            context.startService(intent)
        }
    }

/**
 * 스톱워치 UI 상태
 */
data class StopwatchUiState(
    val stopwatch: Stopwatch = Stopwatch.INITIAL,
)
