package com.tikkatimer.presentation.timer

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tikkatimer.data.local.dao.RunningTimerDao
import com.tikkatimer.data.mapper.RunningTimerMapper
import com.tikkatimer.domain.model.RunningTimer
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.TimerPreset
import com.tikkatimer.domain.model.TimerState
import com.tikkatimer.domain.model.VibrationPattern
import com.tikkatimer.domain.usecase.timer.DeleteTimerPresetUseCase
import com.tikkatimer.domain.usecase.timer.GetTimerPresetsUseCase
import com.tikkatimer.domain.usecase.timer.SaveTimerPresetUseCase
import com.tikkatimer.sync.TimerStateSync
import com.tikkatimer.widget.TimerWidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * 타이머 ViewModel
 * 여러 타이머를 동시에 관리하고 프리셋 기능 제공
 */
@HiltViewModel
class TimerViewModel
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val getTimerPresetsUseCase: GetTimerPresetsUseCase,
        private val saveTimerPresetUseCase: SaveTimerPresetUseCase,
        private val deleteTimerPresetUseCase: DeleteTimerPresetUseCase,
        private val timerStateSync: TimerStateSync,
        private val runningTimerDao: RunningTimerDao,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TimerUiState())
        val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

        private var tickerJob: Job? = null

        companion object {
            private const val TAG = "TimerViewModel"
            private const val TICK_INTERVAL_MS = 1000L
            private const val DEFAULT_TIMER_MINUTES = 5
        }

        init {
            loadPresets()
            restoreTimersFromDb()
        }

        /**
         * Room DB에서 실행 중인 타이머 상태 복원
         * 앱 강제 종료 후에도 타이머 정보 유지
         */
        private fun restoreTimersFromDb() {
            viewModelScope.launch {
                try {
                    val entities = runningTimerDao.getAllTimersSync()
                    if (entities.isNotEmpty()) {
                        val now = System.currentTimeMillis()
                        val restoredTimers =
                            RunningTimerMapper.toDomainList(entities).map { timer ->
                                // RUNNING 상태인데 종료 시간이 지났으면 FINISHED로 변경
                                if (timer.state == TimerState.RUNNING && timer.targetEndTimeMillis > 0) {
                                    if (now >= timer.targetEndTimeMillis) {
                                        timer.copy(
                                            remainingMillis = 0L,
                                            state = TimerState.FINISHED,
                                        )
                                    } else {
                                        // 남은 시간 재계산
                                        timer.copy(
                                            remainingMillis = timer.targetEndTimeMillis - now,
                                        )
                                    }
                                } else {
                                    timer
                                }
                            }

                        _uiState.update { state ->
                            state.copy(runningTimers = restoredTimers)
                        }

                        // DB 업데이트 (FINISHED로 변경된 타이머 반영)
                        saveTimersToDb(restoredTimers)

                        // 실행 중인 타이머가 있으면 ticker 시작
                        if (restoredTimers.any { it.state == TimerState.RUNNING }) {
                            ensureTickerRunning()
                        }

                        // 위젯 동기화
                        val runningTimer = restoredTimers.firstOrNull { it.state == TimerState.RUNNING }
                        if (runningTimer != null) {
                            TimerWidgetUpdater.onTimerStarted(
                                context = context,
                                timerId = runningTimer.instanceId,
                                timerName = runningTimer.name,
                                remainingMillis = runningTimer.remainingMillis,
                                totalMillis = runningTimer.totalDurationMillis,
                            )
                        }

                        Log.d(TAG, "Restored ${restoredTimers.size} timers from DB")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore timers from DB", e)
                }
            }
        }

        /**
         * 실행 중인 타이머들을 DB에 저장
         */
        private fun saveTimersToDb(timers: List<RunningTimer>) {
            viewModelScope.launch {
                try {
                    val entities = RunningTimerMapper.toEntityList(timers)
                    runningTimerDao.deleteAll()
                    runningTimerDao.upsertAll(entities)
                    Log.d(TAG, "Saved ${timers.size} timers to DB")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save timers to DB", e)
                }
            }
        }

        /**
         * 단일 타이머를 DB에 저장/업데이트
         */
        private fun saveTimerToDb(timer: RunningTimer) {
            viewModelScope.launch {
                try {
                    runningTimerDao.upsert(RunningTimerMapper.toEntity(timer))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save timer to DB", e)
                }
            }
        }

        /**
         * DB에서 타이머 삭제
         */
        private fun deleteTimerFromDb(instanceId: String) {
            viewModelScope.launch {
                try {
                    runningTimerDao.delete(instanceId)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete timer from DB", e)
                }
            }
        }

        private fun loadPresets() {
            viewModelScope.launch {
                getTimerPresetsUseCase().collect { presets ->
                    _uiState.update { it.copy(presets = presets) }
                }
            }
        }

        /**
         * 프리셋에서 타이머 시작
         */
        fun startTimerFromPreset(preset: TimerPreset) {
            val instanceId = UUID.randomUUID().toString()
            val totalMillis = preset.durationSeconds * 1000L
            val runningTimer =
                RunningTimer.fromPreset(preset, instanceId)
                    .copy(
                        state = TimerState.RUNNING,
                        targetEndTimeMillis = System.currentTimeMillis() + totalMillis,
                    )

            _uiState.update { state ->
                state.copy(
                    runningTimers = state.runningTimers + runningTimer,
                )
            }

            ensureTickerRunning()
            // 위젯 즉시 업데이트
            TimerWidgetUpdater.onTimerStarted(
                context = context,
                timerId = runningTimer.instanceId,
                timerName = runningTimer.name,
                remainingMillis = runningTimer.remainingMillis,
                totalMillis = runningTimer.totalDurationMillis,
            )
            // 상태 동기화 (알림, 서비스)
            timerStateSync.syncTimers(_uiState.value.runningTimers)
            // DB 저장
            saveTimerToDb(runningTimer)
        }

        /**
         * 새 타이머 추가 모드에서 타이머 시작
         */
        fun startNewTimer(inputName: String = "") {
            val currentState = _uiState.value
            val durationSeconds =
                (currentState.inputHours * 3600L) +
                    (currentState.inputMinutes * 60L) +
                    currentState.inputSeconds

            if (durationSeconds == 0L) return

            val instanceId = UUID.randomUUID().toString()
            // 입력된 이름 사용, 비어있으면 시간으로 표시
            val name = inputName.ifEmpty { formatDuration(durationSeconds) }
            val totalMillis = durationSeconds * 1000L

            val runningTimer =
                RunningTimer(
                    instanceId = instanceId,
                    presetId = 0,
                    name = name,
                    totalDurationMillis = totalMillis,
                    remainingMillis = totalMillis,
                    state = TimerState.RUNNING,
                    soundType = currentState.soundType,
                    vibrationPattern = currentState.vibrationPattern,
                    targetEndTimeMillis = System.currentTimeMillis() + totalMillis,
                )

            _uiState.update { state ->
                state.copy(
                    runningTimers = state.runningTimers + runningTimer,
                    isAddingNewTimer = false,
                )
            }

            ensureTickerRunning()
            // 위젯 즉시 업데이트
            TimerWidgetUpdater.onTimerStarted(
                context = context,
                timerId = runningTimer.instanceId,
                timerName = runningTimer.name,
                remainingMillis = runningTimer.remainingMillis,
                totalMillis = runningTimer.totalDurationMillis,
            )
            // 상태 동기화 (알림, 서비스)
            timerStateSync.syncTimers(_uiState.value.runningTimers)
            // DB 저장
            saveTimerToDb(runningTimer)
        }

        /**
         * 타이머 일시정지
         */
        fun pauseTimer(instanceId: String) {
            _uiState.update { state ->
                state.copy(
                    runningTimers =
                        state.runningTimers.map { timer ->
                            if (timer.instanceId == instanceId && timer.state == TimerState.RUNNING) {
                                val remaining = maxOf(0L, timer.targetEndTimeMillis - System.currentTimeMillis())
                                timer.copy(
                                    remainingMillis = remaining,
                                    state = TimerState.PAUSED,
                                    targetEndTimeMillis = 0L,
                                )
                            } else {
                                timer
                            }
                        },
                )
            }

            stopTickerIfNoRunning()
            // 위젯 즉시 업데이트 (일시정지 상태)
            val pausedTimer = _uiState.value.runningTimers.find { it.instanceId == instanceId }
            pausedTimer?.let {
                TimerWidgetUpdater.onTimerPaused(context, it.remainingMillis)
                // DB 저장
                saveTimerToDb(it)
            }
            // 상태 동기화 (알림, 서비스)
            timerStateSync.syncTimers(_uiState.value.runningTimers)
        }

        /**
         * 일시정지/대기 상태 타이머 재개 (시작)
         */
        fun resumeTimer(instanceId: String) {
            _uiState.update { state ->
                state.copy(
                    runningTimers =
                        state.runningTimers.map { timer ->
                            if (timer.instanceId == instanceId &&
                                (timer.state == TimerState.PAUSED || timer.state == TimerState.IDLE)
                            ) {
                                timer.copy(
                                    state = TimerState.RUNNING,
                                    targetEndTimeMillis = System.currentTimeMillis() + timer.remainingMillis,
                                )
                            } else {
                                timer
                            }
                        },
                )
            }

            ensureTickerRunning()
            // 위젯 즉시 업데이트 (재개 상태)
            val resumedTimer = _uiState.value.runningTimers.find { it.instanceId == instanceId }
            resumedTimer?.let {
                TimerWidgetUpdater.onTimerResumed(
                    context = context,
                    timerId = it.instanceId,
                    timerName = it.name,
                    remainingMillis = it.remainingMillis,
                    totalMillis = it.totalDurationMillis,
                )
                // DB 저장
                saveTimerToDb(it)
            }
            // 상태 동기화 (알림, 서비스)
            timerStateSync.syncTimers(_uiState.value.runningTimers)
        }

        /**
         * 타이머 리셋 (원래 시간으로 복원)
         */
        fun resetTimer(instanceId: String) {
            _uiState.update { state ->
                state.copy(
                    runningTimers =
                        state.runningTimers.map { timer ->
                            if (timer.instanceId == instanceId) {
                                timer.copy(
                                    remainingMillis = timer.totalDurationMillis,
                                    state = TimerState.IDLE,
                                    targetEndTimeMillis = 0L,
                                )
                            } else {
                                timer
                            }
                        },
                )
            }

            stopTickerIfNoRunning()
            // 위젯 즉시 업데이트 (리셋/정지 상태)
            TimerWidgetUpdater.onTimerStopped(context)
            // 상태 동기화 (알림, 서비스)
            timerStateSync.syncTimers(_uiState.value.runningTimers)
            // DB 저장
            val resetTimer = _uiState.value.runningTimers.find { it.instanceId == instanceId }
            resetTimer?.let { saveTimerToDb(it) }
        }

        /**
         * 타이머 삭제 (목록에서 제거)
         */
        fun removeTimer(instanceId: String) {
            _uiState.update { state ->
                state.copy(
                    runningTimers = state.runningTimers.filter { it.instanceId != instanceId },
                )
            }

            stopTickerIfNoRunning()

            // 위젯 즉시 업데이트
            if (_uiState.value.runningTimers.isEmpty()) {
                TimerWidgetUpdater.onTimerStopped(context)
                timerStateSync.clearAll()
            } else {
                // 다른 실행 중인 타이머가 있으면 해당 타이머로 위젯 업데이트
                val nextTimer = _uiState.value.runningTimers.firstOrNull { it.state == TimerState.RUNNING }
                if (nextTimer != null) {
                    TimerWidgetUpdater.onTimerStarted(
                        context = context,
                        timerId = nextTimer.instanceId,
                        timerName = nextTimer.name,
                        remainingMillis = nextTimer.remainingMillis,
                        totalMillis = nextTimer.totalDurationMillis,
                    )
                } else {
                    TimerWidgetUpdater.onTimerStopped(context)
                }
                timerStateSync.syncTimers(_uiState.value.runningTimers)
            }
            // DB에서 삭제
            deleteTimerFromDb(instanceId)
        }

        /**
         * 완료된 타이머 확인 (알림 해제)
         */
        fun acknowledgeFinished(instanceId: String) {
            // 소리/진동 중지
            timerStateSync.stopFinishedAlarm(instanceId)

            _uiState.update { state ->
                state.copy(
                    runningTimers =
                        state.runningTimers.map { timer ->
                            if (timer.instanceId == instanceId && timer.state == TimerState.FINISHED) {
                                timer.copy(
                                    remainingMillis = timer.totalDurationMillis,
                                    state = TimerState.IDLE,
                                    targetEndTimeMillis = 0L,
                                )
                            } else {
                                timer
                            }
                        },
                )
            }
            // 위젯 즉시 업데이트 (완료 확인 후 idle 상태로)
            val hasRunningTimer = _uiState.value.runningTimers.any { it.state == TimerState.RUNNING }
            if (!hasRunningTimer) {
                TimerWidgetUpdater.onTimerStopped(context)
            }
            // 상태 동기화 (알림, 서비스)
            timerStateSync.syncTimers(_uiState.value.runningTimers)
            // DB 저장
            val acknowledgedTimer = _uiState.value.runningTimers.find { it.instanceId == instanceId }
            acknowledgedTimer?.let { saveTimerToDb(it) }
        }

        /**
         * 타이머에 1분 추가
         */
        fun addOneMinute(instanceId: String) {
            val additionalMillis = 60 * 1000L

            _uiState.update { state ->
                state.copy(
                    runningTimers =
                        state.runningTimers.map { timer ->
                            if (timer.instanceId == instanceId) {
                                val newTotal = timer.totalDurationMillis + additionalMillis
                                val newRemaining = timer.remainingMillis + additionalMillis
                                val newTargetEnd =
                                    if (timer.state == TimerState.RUNNING) {
                                        timer.targetEndTimeMillis + additionalMillis
                                    } else {
                                        0L
                                    }

                                timer.copy(
                                    totalDurationMillis = newTotal,
                                    remainingMillis = newRemaining,
                                    targetEndTimeMillis = newTargetEnd,
                                )
                            } else {
                                timer
                            }
                        },
                )
            }
            // DB 저장
            val updatedTimer = _uiState.value.runningTimers.find { it.instanceId == instanceId }
            updatedTimer?.let { saveTimerToDb(it) }
        }

        /**
         * 시간 설정 (새 타이머 추가용)
         */
        fun setTime(
            hours: Int,
            minutes: Int,
            seconds: Int,
        ) {
            _uiState.update {
                it.copy(
                    inputHours = hours,
                    inputMinutes = minutes,
                    inputSeconds = seconds,
                )
            }
        }

        /**
         * 소리 타입 설정
         */
        fun setSoundType(soundType: SoundType) {
            _uiState.update { it.copy(soundType = soundType) }
        }

        /**
         * 진동 패턴 설정
         */
        fun setVibrationPattern(vibrationPattern: VibrationPattern) {
            _uiState.update { it.copy(vibrationPattern = vibrationPattern) }
        }

        /**
         * 현재 시간을 프리셋으로 저장
         */
        fun saveCurrentAsPreset(name: String) {
            val currentState = _uiState.value
            val durationSeconds =
                (currentState.inputHours * 3600L) +
                    (currentState.inputMinutes * 60L) +
                    currentState.inputSeconds

            if (durationSeconds == 0L) return

            viewModelScope.launch {
                val preset =
                    TimerPreset(
                        name = name,
                        durationSeconds = durationSeconds,
                        soundType = currentState.soundType,
                        vibrationPattern = currentState.vibrationPattern,
                    )
                saveTimerPresetUseCase(preset)
                _uiState.update { it.copy(isAddingNewTimer = false) }
            }
        }

        /**
         * 프리셋 삭제
         */
        fun deletePreset(presetId: Long) {
            viewModelScope.launch {
                deleteTimerPresetUseCase(presetId)
            }
        }

        /**
         * 프리셋 수정 모드 시작
         */
        fun startEditingPreset(preset: TimerPreset) {
            _uiState.update {
                it.copy(
                    editingPresetId = preset.id,
                    editingPresetName = preset.name,
                    inputHours = preset.hours,
                    inputMinutes = preset.minutes,
                    inputSeconds = preset.seconds,
                    soundType = preset.soundType,
                    vibrationPattern = preset.vibrationPattern,
                )
            }
        }

        /**
         * 프리셋 수정 저장
         */
        fun saveEditingPreset() {
            val presetId = _uiState.value.editingPresetId ?: return
            val currentState = _uiState.value

            val durationSeconds =
                (currentState.inputHours * 3600L) +
                    (currentState.inputMinutes * 60L) +
                    currentState.inputSeconds

            if (durationSeconds == 0L) return

            viewModelScope.launch {
                val preset =
                    TimerPreset(
                        id = presetId,
                        name = currentState.editingPresetName,
                        durationSeconds = durationSeconds,
                        soundType = currentState.soundType,
                        vibrationPattern = currentState.vibrationPattern,
                    )
                saveTimerPresetUseCase(preset)
                _uiState.update { it.copy(editingPresetId = null, editingPresetName = "") }
            }
        }

        /**
         * 프리셋 수정 취소
         */
        fun cancelEditingPreset() {
            _uiState.update { it.copy(editingPresetId = null, editingPresetName = "") }
        }

        /**
         * 프리셋 이름 변경
         */
        fun setPresetName(name: String) {
            _uiState.update { it.copy(editingPresetName = name) }
        }

        /**
         * 새 타이머 추가 모드 시작
         */
        fun startAddingNewTimer() {
            _uiState.update {
                it.copy(
                    isAddingNewTimer = true,
                    inputHours = 0,
                    inputMinutes = DEFAULT_TIMER_MINUTES,
                    inputSeconds = 0,
                )
            }
        }

        /**
         * 새 타이머 추가 모드 취소
         */
        fun cancelAddingNewTimer() {
            _uiState.update { it.copy(isAddingNewTimer = false, editingTimerId = null) }
        }

        /**
         * 타이머 수정 모드 시작
         */
        fun startEditingTimer(instanceId: String) {
            val timer = _uiState.value.runningTimers.find { it.instanceId == instanceId } ?: return

            // 실행 중이면 일시정지
            if (timer.state == TimerState.RUNNING) {
                pauseTimer(instanceId)
            }

            val totalSeconds = timer.totalDurationMillis / 1000
            _uiState.update {
                it.copy(
                    editingTimerId = instanceId,
                    editingTimerName = timer.name,
                    inputHours = (totalSeconds / 3600).toInt(),
                    inputMinutes = ((totalSeconds % 3600) / 60).toInt(),
                    inputSeconds = (totalSeconds % 60).toInt(),
                    soundType = timer.soundType,
                    vibrationPattern = timer.vibrationPattern,
                )
            }
        }

        /**
         * 타이머 이름 변경
         */
        fun setTimerName(name: String) {
            _uiState.update { it.copy(editingTimerName = name) }
        }

        /**
         * 타이머 수정 저장
         */
        fun saveEditingTimer() {
            val editingId = _uiState.value.editingTimerId ?: return
            val currentState = _uiState.value

            val newDurationMillis =
                (
                    (currentState.inputHours * 3600L) +
                        (currentState.inputMinutes * 60L) +
                        currentState.inputSeconds
                ) * 1000L

            if (newDurationMillis == 0L) return

            // 입력된 이름 사용, 비어있으면 시간으로 표시
            val newName =
                currentState.editingTimerName.ifEmpty {
                    formatDuration(newDurationMillis / 1000)
                }

            _uiState.update { state ->
                state.copy(
                    runningTimers =
                        state.runningTimers.map { timer ->
                            if (timer.instanceId == editingId) {
                                timer.copy(
                                    name = newName,
                                    totalDurationMillis = newDurationMillis,
                                    remainingMillis = newDurationMillis,
                                    soundType = currentState.soundType,
                                    vibrationPattern = currentState.vibrationPattern,
                                    state = TimerState.IDLE,
                                    targetEndTimeMillis = 0L,
                                )
                            } else {
                                timer
                            }
                        },
                    editingTimerId = null,
                    editingTimerName = "",
                )
            }
            // DB 저장
            val editedTimer = _uiState.value.runningTimers.find { it.instanceId == editingId }
            editedTimer?.let { saveTimerToDb(it) }
        }

        /**
         * 타이머 수정 취소
         */
        fun cancelEditingTimer() {
            _uiState.update { it.copy(editingTimerId = null, editingTimerName = "") }
        }

        /**
         * 타이머 수정 저장 후 바로 시작
         */
        fun saveAndStartEditingTimer() {
            val editingId = _uiState.value.editingTimerId ?: return
            val currentState = _uiState.value

            val newDurationMillis =
                (
                    (currentState.inputHours * 3600L) +
                        (currentState.inputMinutes * 60L) +
                        currentState.inputSeconds
                ) * 1000L

            if (newDurationMillis == 0L) return

            // 입력된 이름 사용, 비어있으면 시간으로 표시
            val newName =
                currentState.editingTimerName.ifEmpty {
                    formatDuration(newDurationMillis / 1000)
                }

            _uiState.update { state ->
                state.copy(
                    runningTimers =
                        state.runningTimers.map { timer ->
                            if (timer.instanceId == editingId) {
                                timer.copy(
                                    name = newName,
                                    totalDurationMillis = newDurationMillis,
                                    remainingMillis = newDurationMillis,
                                    soundType = currentState.soundType,
                                    vibrationPattern = currentState.vibrationPattern,
                                    state = TimerState.RUNNING,
                                    targetEndTimeMillis = System.currentTimeMillis() + newDurationMillis,
                                )
                            } else {
                                timer
                            }
                        },
                    editingTimerId = null,
                    editingTimerName = "",
                )
            }

            ensureTickerRunning()
            // DB 저장
            val startedTimer = _uiState.value.runningTimers.find { it.instanceId == editingId }
            startedTimer?.let { saveTimerToDb(it) }
        }

        /**
         * 1초마다 실행 중인 모든 타이머 업데이트
         */
        private fun ensureTickerRunning() {
            if (tickerJob?.isActive == true) return

            tickerJob =
                viewModelScope.launch {
                    while (true) {
                        delay(TICK_INTERVAL_MS)
                        updateRunningTimers()

                        // 실행 중인 타이머가 없으면 중지
                        if (_uiState.value.runningTimers.none { it.state == TimerState.RUNNING }) {
                            break
                        }
                    }
                }
        }

        private fun updateRunningTimers() {
            val now = System.currentTimeMillis()

            _uiState.update { state ->
                state.copy(
                    runningTimers =
                        state.runningTimers.map { timer ->
                            if (timer.state == TimerState.RUNNING) {
                                val remaining = maxOf(0L, timer.targetEndTimeMillis - now)
                                if (remaining <= 0) {
                                    timer.copy(
                                        remainingMillis = 0,
                                        state = TimerState.FINISHED,
                                    )
                                } else {
                                    timer.copy(remainingMillis = remaining)
                                }
                            } else {
                                timer
                            }
                        },
                )
            }

            // 주기적 상태 동기화 (틱마다 알림/위젯 업데이트)
            timerStateSync.syncTick(_uiState.value.runningTimers)
        }

        private fun stopTickerIfNoRunning() {
            if (_uiState.value.runningTimers.none { it.state == TimerState.RUNNING }) {
                tickerJob?.cancel()
                tickerJob = null
            }
        }

        private fun formatDuration(seconds: Long): String {
            val h = (seconds / 3600).toInt()
            val m = ((seconds % 3600) / 60).toInt()
            val s = (seconds % 60).toInt()

            val parts = mutableListOf<String>()
            if (h > 0) parts.add("${h}시간")
            if (m > 0) parts.add("${m}분")
            if (s > 0) parts.add("${s}초")
            return parts.joinToString(" ").ifEmpty { "0초" }
        }

        override fun onCleared() {
            super.onCleared()
            tickerJob?.cancel()
        }

        // ====== 이전 API 호환성 유지 (테스트용) ======

        /**
         * @deprecated 이전 단일 타이머 API - 테스트 호환성용
         */
        @Deprecated("Use startNewTimer() instead")
        fun start() {
            startNewTimer()
        }

        @Deprecated("Use pauseTimer(instanceId) instead")
        fun pause() {
            _uiState.value.runningTimers.firstOrNull { it.state == TimerState.RUNNING }?.let {
                pauseTimer(it.instanceId)
            }
        }

        @Deprecated("Use resetTimer(instanceId) instead")
        fun reset() {
            _uiState.value.runningTimers.firstOrNull()?.let {
                resetTimer(it.instanceId)
            }
        }

        @Deprecated("Use removeTimer(instanceId) instead")
        fun cancel() {
            _uiState.update { state ->
                state.copy(
                    runningTimers = emptyList(),
                    inputHours = 0,
                    inputMinutes = DEFAULT_TIMER_MINUTES,
                    inputSeconds = 0,
                )
            }
            tickerJob?.cancel()
            tickerJob = null
            timerStateSync.clearAll()
            // DB 전체 삭제
            viewModelScope.launch {
                try {
                    runningTimerDao.deleteAll()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete all timers from DB", e)
                }
            }
        }

        @Deprecated("Use addOneMinute(instanceId) instead")
        fun addOneMinute() {
            _uiState.value.runningTimers.firstOrNull()?.let {
                addOneMinute(it.instanceId)
            }
        }

        @Deprecated("Use startTimerFromPreset() instead")
        fun selectPreset(preset: TimerPreset) {
            _uiState.update {
                it.copy(
                    inputHours = preset.hours,
                    inputMinutes = preset.minutes,
                    inputSeconds = preset.seconds,
                    soundType = preset.soundType,
                    vibrationPattern = preset.vibrationPattern,
                )
            }
        }

        @Deprecated("Use acknowledgeFinished(instanceId) instead")
        fun acknowledgeFinished() {
            _uiState.value.runningTimers.firstOrNull { it.state == TimerState.FINISHED }?.let {
                acknowledgeFinished(it.instanceId)
            }
        }

        // 테스트 호환성을 위한 timer 프로퍼티
        val legacyTimer: com.tikkatimer.domain.model.Timer
            get() {
                val first = _uiState.value.runningTimers.firstOrNull()
                return if (first != null) {
                    com.tikkatimer.domain.model.Timer(
                        totalDurationMillis = first.totalDurationMillis,
                        remainingMillis = first.remainingMillis,
                        state = first.state,
                    )
                } else {
                    val state = _uiState.value
                    val totalMillis =
                        ((state.inputHours * 3600L) + (state.inputMinutes * 60L) + state.inputSeconds) * 1000L
                    com.tikkatimer.domain.model.Timer(
                        totalDurationMillis = totalMillis,
                        remainingMillis = totalMillis,
                        state = TimerState.IDLE,
                    )
                }
            }
    }

/**
 * 타이머 UI 상태
 */
data class TimerUiState(
    val presets: List<TimerPreset> = emptyList(),
    /** 실행 중인 타이머들 */
    val runningTimers: List<RunningTimer> = emptyList(),
    val inputHours: Int = 0,
    val inputMinutes: Int = 5,
    val inputSeconds: Int = 0,
    val soundType: SoundType = SoundType.DEFAULT,
    val vibrationPattern: VibrationPattern = VibrationPattern.DEFAULT,
    val isAddingNewTimer: Boolean = false,
    /** 수정 중인 타이머 ID */
    val editingTimerId: String? = null,
    /** 수정 중인 타이머 이름 */
    val editingTimerName: String = "",
    /** 수정 중인 프리셋 ID */
    val editingPresetId: Long? = null,
    /** 수정 중인 프리셋 이름 */
    val editingPresetName: String = "",
) {
    // 이전 API 호환성을 위한 timer 프로퍼티
    @Deprecated("Use runningTimers instead")
    val timer: com.tikkatimer.domain.model.Timer
        get() {
            val first = runningTimers.firstOrNull()
            return if (first != null) {
                com.tikkatimer.domain.model.Timer(
                    totalDurationMillis = first.totalDurationMillis,
                    remainingMillis = first.remainingMillis,
                    state = first.state,
                )
            } else {
                val totalMillis = ((inputHours * 3600L) + (inputMinutes * 60L) + inputSeconds) * 1000L
                com.tikkatimer.domain.model.Timer(
                    totalDurationMillis = totalMillis,
                    remainingMillis = totalMillis,
                    state = TimerState.IDLE,
                )
            }
        }
}
