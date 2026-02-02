package com.tikkatimer.presentation.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.UpcomingInfo
import com.tikkatimer.domain.model.VibrationPattern
import com.tikkatimer.domain.usecase.alarm.AddAlarmUseCase
import com.tikkatimer.domain.usecase.alarm.DeleteAlarmUseCase
import com.tikkatimer.domain.usecase.alarm.GetAlarmsUseCase
import com.tikkatimer.domain.usecase.alarm.GetUpcomingAlarmUseCase
import com.tikkatimer.domain.usecase.alarm.ToggleAlarmUseCase
import com.tikkatimer.domain.usecase.alarm.UpdateAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

/**
 * 알람 ViewModel
 * 알람 목록 관리 및 CRUD 기능 제공
 */
@HiltViewModel
class AlarmViewModel
    @Inject
    constructor(
        private val getAlarmsUseCase: GetAlarmsUseCase,
        private val addAlarmUseCase: AddAlarmUseCase,
        private val updateAlarmUseCase: UpdateAlarmUseCase,
        private val deleteAlarmUseCase: DeleteAlarmUseCase,
        private val toggleAlarmUseCase: ToggleAlarmUseCase,
        private val getUpcomingAlarmUseCase: GetUpcomingAlarmUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AlarmUiState())
        val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

        init {
            loadAlarms()
            loadUpcomingInfo()
        }

        private fun loadAlarms() {
            viewModelScope.launch {
                getAlarmsUseCase().collect { alarms ->
                    _uiState.update { it.copy(alarms = alarms, isLoading = false) }
                }
            }
        }

        private fun loadUpcomingInfo() {
            viewModelScope.launch {
                getUpcomingAlarmUseCase().collect { upcomingInfo ->
                    _uiState.update { it.copy(upcomingInfo = upcomingInfo) }
                }
            }
        }

        /**
         * 알람 추가 다이얼로그 열기
         */
        fun showAddAlarmDialog() {
            _uiState.update { currentState ->
                currentState.copy(
                    dialogState =
                        AlarmDialogState.Add(
                            time = LocalTime.of(8, 0),
                            label = "",
                            repeatDays = emptySet(),
                            soundType = SoundType.DEFAULT,
                            vibrationPattern = VibrationPattern.DEFAULT,
                            isSnoozeEnabled = true,
                        ),
                )
            }
        }

        /**
         * 알람 편집 다이얼로그 열기
         */
        fun showEditAlarmDialog(alarm: Alarm) {
            _uiState.update { currentState ->
                currentState.copy(
                    dialogState =
                        AlarmDialogState.Edit(
                            alarm = alarm,
                            time = alarm.time,
                            label = alarm.label,
                            repeatDays = alarm.repeatDays,
                            soundType = alarm.soundType,
                            vibrationPattern = alarm.vibrationPattern,
                            isSnoozeEnabled = alarm.isSnoozeEnabled,
                        ),
                )
            }
        }

        /**
         * 다이얼로그 닫기
         */
        fun dismissDialog() {
            _uiState.update { it.copy(dialogState = null) }
        }

        /**
         * 다이얼로그에서 시간 변경
         */
        fun updateDialogTime(time: LocalTime) {
            _uiState.update { currentState ->
                when (val dialog = currentState.dialogState) {
                    is AlarmDialogState.Add ->
                        currentState.copy(
                            dialogState = dialog.copy(time = time),
                        )
                    is AlarmDialogState.Edit ->
                        currentState.copy(
                            dialogState = dialog.copy(time = time),
                        )
                    null -> currentState
                }
            }
        }

        /**
         * 다이얼로그에서 라벨 변경
         */
        fun updateDialogLabel(label: String) {
            _uiState.update { currentState ->
                when (val dialog = currentState.dialogState) {
                    is AlarmDialogState.Add ->
                        currentState.copy(
                            dialogState = dialog.copy(label = label),
                        )
                    is AlarmDialogState.Edit ->
                        currentState.copy(
                            dialogState = dialog.copy(label = label),
                        )
                    null -> currentState
                }
            }
        }

        /**
         * 다이얼로그에서 반복 요일 토글
         */
        fun toggleDialogRepeatDay(day: DayOfWeek) {
            _uiState.update { currentState ->
                when (val dialog = currentState.dialogState) {
                    is AlarmDialogState.Add -> {
                        val newDays =
                            if (day in dialog.repeatDays) {
                                dialog.repeatDays - day
                            } else {
                                dialog.repeatDays + day
                            }
                        currentState.copy(dialogState = dialog.copy(repeatDays = newDays))
                    }
                    is AlarmDialogState.Edit -> {
                        val newDays =
                            if (day in dialog.repeatDays) {
                                dialog.repeatDays - day
                            } else {
                                dialog.repeatDays + day
                            }
                        currentState.copy(dialogState = dialog.copy(repeatDays = newDays))
                    }
                    null -> currentState
                }
            }
        }

        /**
         * 다이얼로그에서 소리 타입 변경
         */
        fun updateDialogSoundType(soundType: SoundType) {
            _uiState.update { currentState ->
                when (val dialog = currentState.dialogState) {
                    is AlarmDialogState.Add ->
                        currentState.copy(
                            dialogState = dialog.copy(soundType = soundType),
                        )
                    is AlarmDialogState.Edit ->
                        currentState.copy(
                            dialogState = dialog.copy(soundType = soundType),
                        )
                    null -> currentState
                }
            }
        }

        /**
         * 다이얼로그에서 진동 패턴 변경
         */
        fun updateDialogVibrationPattern(vibrationPattern: VibrationPattern) {
            _uiState.update { currentState ->
                when (val dialog = currentState.dialogState) {
                    is AlarmDialogState.Add ->
                        currentState.copy(
                            dialogState = dialog.copy(vibrationPattern = vibrationPattern),
                        )
                    is AlarmDialogState.Edit ->
                        currentState.copy(
                            dialogState = dialog.copy(vibrationPattern = vibrationPattern),
                        )
                    null -> currentState
                }
            }
        }

        /**
         * 다이얼로그에서 스누즈 설정 변경
         */
        fun updateDialogSnooze(isSnoozeEnabled: Boolean) {
            _uiState.update { currentState ->
                when (val dialog = currentState.dialogState) {
                    is AlarmDialogState.Add ->
                        currentState.copy(
                            dialogState = dialog.copy(isSnoozeEnabled = isSnoozeEnabled),
                        )
                    is AlarmDialogState.Edit ->
                        currentState.copy(
                            dialogState = dialog.copy(isSnoozeEnabled = isSnoozeEnabled),
                        )
                    null -> currentState
                }
            }
        }

        /**
         * 알람 저장 (추가 또는 수정)
         */
        fun saveAlarm() {
            val dialogState = _uiState.value.dialogState ?: return

            viewModelScope.launch {
                when (dialogState) {
                    is AlarmDialogState.Add -> {
                        val newAlarm =
                            Alarm(
                                time = dialogState.time,
                                label = dialogState.label,
                                repeatDays = dialogState.repeatDays,
                                soundType = dialogState.soundType,
                                vibrationPattern = dialogState.vibrationPattern,
                                isSnoozeEnabled = dialogState.isSnoozeEnabled,
                            )
                        addAlarmUseCase(newAlarm)
                    }
                    is AlarmDialogState.Edit -> {
                        val updatedAlarm =
                            dialogState.alarm.copy(
                                time = dialogState.time,
                                label = dialogState.label,
                                repeatDays = dialogState.repeatDays,
                                soundType = dialogState.soundType,
                                vibrationPattern = dialogState.vibrationPattern,
                                isSnoozeEnabled = dialogState.isSnoozeEnabled,
                            )
                        updateAlarmUseCase(updatedAlarm)
                    }
                }
                dismissDialog()
            }
        }

        /**
         * 알람 활성화/비활성화 토글
         */
        fun toggleAlarm(alarm: Alarm) {
            viewModelScope.launch {
                toggleAlarmUseCase(alarm.id, !alarm.isEnabled)
            }
        }

        /**
         * 알람 삭제
         */
        fun deleteAlarm(alarm: Alarm) {
            viewModelScope.launch {
                deleteAlarmUseCase(alarm.id)
            }
        }
    }

/**
 * 알람 UI 상태
 */
data class AlarmUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = true,
    val dialogState: AlarmDialogState? = null,
    val upcomingInfo: UpcomingInfo = UpcomingInfo.EMPTY,
)

/**
 * 알람 다이얼로그 상태
 */
sealed class AlarmDialogState {
    data class Add(
        val time: LocalTime,
        val label: String,
        val repeatDays: Set<DayOfWeek>,
        val soundType: SoundType,
        val vibrationPattern: VibrationPattern,
        val isSnoozeEnabled: Boolean,
    ) : AlarmDialogState()

    data class Edit(
        val alarm: Alarm,
        val time: LocalTime,
        val label: String,
        val repeatDays: Set<DayOfWeek>,
        val soundType: SoundType,
        val vibrationPattern: VibrationPattern,
        val isSnoozeEnabled: Boolean,
    ) : AlarmDialogState()
}
