package com.tikkatimer.presentation.alarm.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tikkatimer.R
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.VibrationPattern
import com.tikkatimer.presentation.alarm.AlarmDialogState
import com.tikkatimer.ui.theme.TikkaTimerTheme
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * 알람 추가/편집 다이얼로그
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDialog(
    dialogState: AlarmDialogState,
    onTimeChange: (LocalTime) -> Unit,
    onLabelChange: (String) -> Unit,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    onSoundTypeChange: (SoundType) -> Unit,
    onVibrationPatternChange: (VibrationPattern) -> Unit,
    onSnoozeChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val isEdit = dialogState is AlarmDialogState.Edit
    val title = stringResource(if (isEdit) R.string.alarm_edit else R.string.alarm_add)

    val time =
        when (dialogState) {
            is AlarmDialogState.Add -> dialogState.time
            is AlarmDialogState.Edit -> dialogState.time
        }
    val label =
        when (dialogState) {
            is AlarmDialogState.Add -> dialogState.label
            is AlarmDialogState.Edit -> dialogState.label
        }
    val repeatDays =
        when (dialogState) {
            is AlarmDialogState.Add -> dialogState.repeatDays
            is AlarmDialogState.Edit -> dialogState.repeatDays
        }
    val soundType =
        when (dialogState) {
            is AlarmDialogState.Add -> dialogState.soundType
            is AlarmDialogState.Edit -> dialogState.soundType
        }
    val vibrationPattern =
        when (dialogState) {
            is AlarmDialogState.Add -> dialogState.vibrationPattern
            is AlarmDialogState.Edit -> dialogState.vibrationPattern
        }
    val isSnoozeEnabled =
        when (dialogState) {
            is AlarmDialogState.Add -> dialogState.isSnoozeEnabled
            is AlarmDialogState.Edit -> dialogState.isSnoozeEnabled
        }

    var showSoundPicker by remember { mutableStateOf(false) }
    var showVibrationPicker by remember { mutableStateOf(false) }

    val timePickerState =
        rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = false,
        )

    // 시간 변경 감지
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
        if (newTime != time) {
            onTimeChange(newTime)
        }
    }

    // 소리 선택 다이얼로그
    if (showSoundPicker) {
        SoundPickerDialog(
            selectedSoundType = soundType,
            onSoundTypeSelected = {
                onSoundTypeChange(it)
                showSoundPicker = false
            },
            onDismiss = { showSoundPicker = false },
        )
    }

    // 진동 선택 다이얼로그
    if (showVibrationPicker) {
        VibrationPickerDialog(
            selectedPattern = vibrationPattern,
            onPatternSelected = {
                onVibrationPatternChange(it)
                showVibrationPicker = false
            },
            onDismiss = { showVibrationPicker = false },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
            ) {
                // 시간 선택 (컴팩트한 TimeInput 사용)
                TimeInput(
                    state = timePickerState,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 반복 요일 선택 (1줄로 표시)
                Text(
                    text = stringResource(R.string.alarm_repeat),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    DayOfWeek.entries.forEach { day ->
                        DayChip(
                            day = getDayOfWeekShort(day),
                            selected = day in repeatDays,
                            onClick = { onRepeatDayToggle(day) },
                        )
                    }
                }

                // 1회성 알람 안내 문구
                if (repeatDays.isEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.alarm_onetime_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 소리 설정
                SettingItem(
                    label = stringResource(R.string.alarm_sound),
                    value = stringResource(soundType.displayNameResId),
                    onClick = { showSoundPicker = true },
                )

                // 진동 설정
                SettingItem(
                    label = stringResource(R.string.alarm_vibration),
                    value = stringResource(vibrationPattern.displayNameResId),
                    onClick = { showVibrationPicker = true },
                )

                // 스누즈 설정
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.alarm_snooze))
                    Switch(
                        checked = isSnoozeEnabled,
                        onCheckedChange = onSnoozeChange,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 라벨 입력 (맨 아래)
                OutlinedTextField(
                    value = label,
                    onValueChange = onLabelChange,
                    label = { Text(stringResource(R.string.alarm_label_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row {
                if (isEdit && onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        },
    )
}

/**
 * 요일 선택 칩 (컴팩트한 원형)
 */
@Composable
private fun DayChip(
    day: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color =
            if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        modifier = Modifier.size(36.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp),
        ) {
            Text(
                text = day,
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
    }
}

/**
 * 클릭 가능한 설정 항목
 */
@Composable
private fun SettingItem(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label)
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 요일을 현재 언어 설정에 맞는 약어로 변환
 */
@Composable
private fun getDayOfWeekShort(day: DayOfWeek): String {
    return when (day) {
        DayOfWeek.MONDAY -> stringResource(R.string.day_mon)
        DayOfWeek.TUESDAY -> stringResource(R.string.day_tue)
        DayOfWeek.WEDNESDAY -> stringResource(R.string.day_wed)
        DayOfWeek.THURSDAY -> stringResource(R.string.day_thu)
        DayOfWeek.FRIDAY -> stringResource(R.string.day_fri)
        DayOfWeek.SATURDAY -> stringResource(R.string.day_sat)
        DayOfWeek.SUNDAY -> stringResource(R.string.day_sun)
    }
}

@Preview
@Composable
private fun AlarmDialogAddPreview() {
    TikkaTimerTheme {
        AlarmDialog(
            dialogState =
                AlarmDialogState.Add(
                    time = LocalTime.of(8, 0),
                    label = "",
                    repeatDays = emptySet(),
                    soundType = SoundType.DEFAULT,
                    vibrationPattern = VibrationPattern.DEFAULT,
                    isSnoozeEnabled = true,
                ),
            onTimeChange = {},
            onLabelChange = {},
            onRepeatDayToggle = {},
            onSoundTypeChange = {},
            onVibrationPatternChange = {},
            onSnoozeChange = {},
            onSave = {},
            onDismiss = {},
        )
    }
}
