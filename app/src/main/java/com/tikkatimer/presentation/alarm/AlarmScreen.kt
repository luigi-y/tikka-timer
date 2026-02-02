package com.tikkatimer.presentation.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tikkatimer.R
import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.UpcomingInfo
import com.tikkatimer.domain.model.VibrationPattern
import com.tikkatimer.presentation.alarm.component.AlarmDialog
import com.tikkatimer.presentation.alarm.component.AlarmItem
import com.tikkatimer.presentation.common.UpcomingInfoCard
import com.tikkatimer.presentation.common.UpcomingInfoType
import com.tikkatimer.ui.theme.TikkaTimerTheme
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * 알람 화면
 * 알람 목록 표시 및 알람 추가/편집/삭제 기능 제공
 */
@Suppress("DEPRECATION")
@Composable
fun AlarmScreen(
    modifier: Modifier = Modifier,
    viewModel: AlarmViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    AlarmScreenContent(
        alarms = uiState.alarms,
        isLoading = uiState.isLoading,
        dialogState = uiState.dialogState,
        upcomingInfo = uiState.upcomingInfo,
        onAddClick = viewModel::showAddAlarmDialog,
        onAlarmClick = viewModel::showEditAlarmDialog,
        onAlarmToggle = viewModel::toggleAlarm,
        onTimeChange = viewModel::updateDialogTime,
        onLabelChange = viewModel::updateDialogLabel,
        onRepeatDayToggle = viewModel::toggleDialogRepeatDay,
        onSoundTypeChange = viewModel::updateDialogSoundType,
        onVibrationPatternChange = viewModel::updateDialogVibrationPattern,
        onSnoozeChange = viewModel::updateDialogSnooze,
        onSave = viewModel::saveAlarm,
        onDismiss = viewModel::dismissDialog,
        onDelete = { alarm -> viewModel.deleteAlarm(alarm) },
        modifier = modifier,
    )
}

@Composable
private fun AlarmScreenContent(
    alarms: List<Alarm>,
    isLoading: Boolean,
    dialogState: AlarmDialogState?,
    upcomingInfo: UpcomingInfo,
    onAddClick: () -> Unit,
    onAlarmClick: (Alarm) -> Unit,
    onAlarmToggle: (Alarm) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onLabelChange: (String) -> Unit,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    onSoundTypeChange: (SoundType) -> Unit,
    onVibrationPatternChange: (VibrationPattern) -> Unit,
    onSnoozeChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: (Alarm) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                alarms.isEmpty() -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyAlarmContent()
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        items(
                            items = alarms,
                            key = { it.id },
                        ) { alarm ->
                            AlarmItem(
                                alarm = alarm,
                                onToggle = onAlarmToggle,
                                onClick = onAlarmClick,
                            )
                        }
                        // FAB 공간 확보
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            // 하단: 임박한 알람 정보 카드
            if (upcomingInfo.activeAlarmCount > 0) {
                UpcomingInfoCard(
                    upcomingInfo = upcomingInfo,
                    type = UpcomingInfoType.ALARM,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        // FAB (항상 하단 고정)
        FloatingActionButton(
            onClick = onAddClick,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 72.dp, end = 16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.alarm_add),
            )
        }

        // 다이얼로그
        dialogState?.let { state ->
            val currentAlarm = (state as? AlarmDialogState.Edit)?.alarm
            AlarmDialog(
                dialogState = state,
                onTimeChange = onTimeChange,
                onLabelChange = onLabelChange,
                onRepeatDayToggle = onRepeatDayToggle,
                onSoundTypeChange = onSoundTypeChange,
                onVibrationPatternChange = onVibrationPatternChange,
                onSnoozeChange = onSnoozeChange,
                onSave = onSave,
                onDismiss = onDismiss,
                onDelete =
                    currentAlarm?.let {
                        {
                            onDelete(it)
                            onDismiss()
                        }
                    },
            )
        }
    }
}

@Composable
private fun EmptyAlarmContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Alarm,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Text(
            text = stringResource(R.string.alarm_empty),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.alarm_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmScreenEmptyPreview() {
    TikkaTimerTheme {
        AlarmScreenContent(
            alarms = emptyList(),
            isLoading = false,
            dialogState = null,
            upcomingInfo = UpcomingInfo.EMPTY,
            onAddClick = {},
            onAlarmClick = {},
            onAlarmToggle = {},
            onTimeChange = {},
            onLabelChange = {},
            onRepeatDayToggle = {},
            onSoundTypeChange = {},
            onVibrationPatternChange = {},
            onSnoozeChange = {},
            onSave = {},
            onDismiss = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmScreenWithAlarmsPreview() {
    TikkaTimerTheme {
        AlarmScreenContent(
            alarms =
                listOf(
                    Alarm(
                        id = 1,
                        time = LocalTime.of(7, 30),
                        isEnabled = true,
                        label = "출근 알람",
                        repeatDays =
                            setOf(
                                DayOfWeek.MONDAY,
                                DayOfWeek.TUESDAY,
                                DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY,
                                DayOfWeek.FRIDAY,
                            ),
                    ),
                    Alarm(
                        id = 2,
                        time = LocalTime.of(9, 0),
                        isEnabled = false,
                        label = "",
                        repeatDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                    ),
                    Alarm(
                        id = 3,
                        time = LocalTime.of(22, 0),
                        isEnabled = true,
                        label = "취침 알람",
                    ),
                ),
            isLoading = false,
            dialogState = null,
            upcomingInfo =
                UpcomingInfo(
                    nextAlarmTime = java.time.LocalDateTime.now().plusHours(3),
                    activeAlarmCount = 2,
                ),
            onAddClick = {},
            onAlarmClick = {},
            onAlarmToggle = {},
            onTimeChange = {},
            onLabelChange = {},
            onRepeatDayToggle = {},
            onSoundTypeChange = {},
            onVibrationPatternChange = {},
            onSnoozeChange = {},
            onSave = {},
            onDismiss = {},
            onDelete = {},
        )
    }
}
