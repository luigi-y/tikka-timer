package com.tikkatimer.presentation.timer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tikkatimer.R
import com.tikkatimer.domain.model.RunningTimer
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.TimerPreset
import com.tikkatimer.domain.model.TimerState
import com.tikkatimer.domain.model.VibrationPattern
import com.tikkatimer.presentation.alarm.component.SoundPickerDialog
import com.tikkatimer.presentation.alarm.component.VibrationPickerDialog
import com.tikkatimer.presentation.timer.component.TimePicker
import com.tikkatimer.ui.theme.TikkaTimerTheme

/**
 * 타이머 화면
 * 프리셋 목록 + 실행 중인 타이머 목록 표시
 */
@Suppress("DEPRECATION")
@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.editingPresetId != null -> {
            // 프리셋 수정 화면
            EditPresetScreen(
                presetName = uiState.editingPresetName,
                inputHours = uiState.inputHours,
                inputMinutes = uiState.inputMinutes,
                inputSeconds = uiState.inputSeconds,
                soundType = uiState.soundType,
                vibrationPattern = uiState.vibrationPattern,
                onNameChange = viewModel::setPresetName,
                onTimeChange = viewModel::setTime,
                onSoundTypeChange = viewModel::setSoundType,
                onVibrationPatternChange = viewModel::setVibrationPattern,
                onSaveClick = viewModel::saveEditingPreset,
                onBackClick = viewModel::cancelEditingPreset,
                modifier = modifier,
            )
        }
        uiState.editingTimerId != null -> {
            // 타이머 수정 화면
            EditTimerScreen(
                timerName = uiState.editingTimerName,
                inputHours = uiState.inputHours,
                inputMinutes = uiState.inputMinutes,
                inputSeconds = uiState.inputSeconds,
                soundType = uiState.soundType,
                vibrationPattern = uiState.vibrationPattern,
                onNameChange = viewModel::setTimerName,
                onTimeChange = viewModel::setTime,
                onSoundTypeChange = viewModel::setSoundType,
                onVibrationPatternChange = viewModel::setVibrationPattern,
                onSaveClick = viewModel::saveEditingTimer,
                onStartClick = viewModel::saveAndStartEditingTimer,
                onBackClick = viewModel::cancelEditingTimer,
                modifier = modifier,
            )
        }
        uiState.isAddingNewTimer -> {
            // 새 타이머 추가 화면
            AddTimerScreen(
                inputHours = uiState.inputHours,
                inputMinutes = uiState.inputMinutes,
                inputSeconds = uiState.inputSeconds,
                soundType = uiState.soundType,
                vibrationPattern = uiState.vibrationPattern,
                onTimeChange = viewModel::setTime,
                onSoundTypeChange = viewModel::setSoundType,
                onVibrationPatternChange = viewModel::setVibrationPattern,
                onStartClick = viewModel::startNewTimer,
                onSavePresetClick = viewModel::saveCurrentAsPreset,
                onBackClick = viewModel::cancelAddingNewTimer,
                modifier = modifier,
            )
        }
        else -> {
            // 메인 타이머 화면
            TimerMainScreen(
                presets = uiState.presets,
                runningTimers = uiState.runningTimers,
                onPresetClick = viewModel::startTimerFromPreset,
                onPresetEdit = viewModel::startEditingPreset,
                onPresetDelete = viewModel::deletePreset,
                onAddClick = viewModel::startAddingNewTimer,
                onTimerPause = viewModel::pauseTimer,
                onTimerResume = viewModel::resumeTimer,
                onTimerReset = viewModel::resetTimer,
                onTimerRemove = viewModel::removeTimer,
                onTimerAddMinute = viewModel::addOneMinute,
                onTimerAcknowledge = viewModel::acknowledgeFinished,
                onTimerEdit = viewModel::startEditingTimer,
                modifier = modifier,
            )
        }
    }
}

/**
 * 메인 타이머 화면 (프리셋 + 실행 중인 타이머)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerMainScreen(
    presets: List<TimerPreset>,
    runningTimers: List<RunningTimer>,
    onPresetClick: (TimerPreset) -> Unit,
    onPresetEdit: (TimerPreset) -> Unit,
    onPresetDelete: (Long) -> Unit,
    onTimerEdit: (String) -> Unit,
    onAddClick: () -> Unit,
    onTimerPause: (String) -> Unit,
    onTimerResume: (String) -> Unit,
    onTimerReset: (String) -> Unit,
    onTimerRemove: (String) -> Unit,
    onTimerAddMinute: (String) -> Unit,
    onTimerAcknowledge: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 실행 중인 타이머를 상태별로 정렬 (RUNNING > PAUSED > FINISHED > IDLE)
    val sortedRunningTimers =
        runningTimers.sortedBy { timer ->
            when (timer.state) {
                TimerState.RUNNING -> 0
                TimerState.PAUSED -> 1
                TimerState.FINISHED -> 2
                TimerState.IDLE -> 3
            }
        }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.timer_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.timer_new))
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 실행 중인 타이머 섹션
            if (sortedRunningTimers.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.timer_running),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }

                items(sortedRunningTimers, key = { it.instanceId }) { timer ->
                    RunningTimerCard(
                        timer = timer,
                        onPause = { onTimerPause(timer.instanceId) },
                        onResume = { onTimerResume(timer.instanceId) },
                        onReset = { onTimerReset(timer.instanceId) },
                        onRemove = { onTimerRemove(timer.instanceId) },
                        onAddMinute = { onTimerAddMinute(timer.instanceId) },
                        onAcknowledge = { onTimerAcknowledge(timer.instanceId) },
                        onEdit = { onTimerEdit(timer.instanceId) },
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // 프리셋 섹션
            if (presets.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.timer_preset),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }

                items(presets, key = { it.id }) { preset ->
                    PresetCard(
                        preset = preset,
                        onClick = { onPresetClick(preset) },
                        onEdit = { onPresetEdit(preset) },
                        onDelete = { onPresetDelete(preset.id) },
                    )
                }
            }

            // 프리셋이 없고 실행 중인 타이머도 없을 때
            if (presets.isEmpty() && runningTimers.isEmpty()) {
                item {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.timer_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FilledTonalButton(onClick = onAddClick) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.timer_add))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 실행 중인 타이머 카드 (컴팩트 가로 레이아웃)
 */
@Composable
private fun RunningTimerCard(
    timer: RunningTimer,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onRemove: () -> Unit,
    onAddMinute: () -> Unit,
    onAcknowledge: () -> Unit,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue =
            when (timer.state) {
                TimerState.RUNNING -> MaterialTheme.colorScheme.primaryContainer
                TimerState.PAUSED -> MaterialTheme.colorScheme.secondaryContainer
                TimerState.FINISHED -> MaterialTheme.colorScheme.errorContainer
                TimerState.IDLE -> MaterialTheme.colorScheme.surfaceVariant
            },
        label = "cardColor",
    )

    val contentColor =
        when (timer.state) {
            TimerState.RUNNING -> MaterialTheme.colorScheme.onPrimaryContainer
            TimerState.PAUSED -> MaterialTheme.colorScheme.onSecondaryContainer
            TimerState.FINISHED -> MaterialTheme.colorScheme.onErrorContainer
            TimerState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 왼쪽: 원형 진행률 + 시간
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp),
            ) {
                CircularProgressIndicator(
                    progress = { timer.progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 4.dp,
                    trackColor = contentColor.copy(alpha = 0.2f),
                    color = contentColor,
                    strokeCap = StrokeCap.Round,
                )
                Text(
                    text = timer.formattedTime,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 중앙: 이름, 소리/진동, 상태
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timer.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(state = timer.state)
                }
                Text(
                    text = "${stringResource(
                        timer.soundType.displayNameResId,
                    )} · ${stringResource(timer.vibrationPattern.displayNameResId)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                )
            }

            // 오른쪽: 제어 버튼
            when (timer.state) {
                TimerState.RUNNING -> {
                    IconButton(onClick = onAddMinute, modifier = Modifier.size(36.dp)) {
                        Text(
                            stringResource(R.string.timer_add_minute),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor,
                        )
                    }
                    IconButton(onClick = onReset, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.timer_reset),
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(onClick = onPause, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Pause,
                            contentDescription = stringResource(R.string.timer_pause),
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                TimerState.PAUSED -> {
                    TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text(
                            stringResource(R.string.timer_edit_btn),
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor,
                        )
                    }
                    IconButton(onClick = onReset, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.timer_reset),
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(onClick = onResume, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.timer_resume),
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                TimerState.FINISHED -> {
                    FilledTonalButton(
                        onClick = onAcknowledge,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Text(stringResource(R.string.confirm), style = MaterialTheme.typography.labelMedium)
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete),
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                TimerState.IDLE -> {
                    TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text(
                            stringResource(R.string.timer_edit_btn),
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor,
                        )
                    }
                    IconButton(onClick = onResume, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.timer_start),
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete),
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 상태 뱃지
 */
@Composable
private fun StatusBadge(state: TimerState) {
    val (textResId, color) =
        when (state) {
            TimerState.RUNNING -> R.string.timer_state_running to MaterialTheme.colorScheme.primary
            TimerState.PAUSED -> R.string.timer_state_paused to MaterialTheme.colorScheme.secondary
            TimerState.FINISHED -> R.string.timer_state_finished to MaterialTheme.colorScheme.error
            TimerState.IDLE -> R.string.timer_state_idle to MaterialTheme.colorScheme.outline
        }

    Text(
        text = stringResource(textResId),
        style = MaterialTheme.typography.labelSmall,
        color = color,
    )
}

/**
 * 프리셋 카드
 */
@Composable
private fun PresetCard(
    preset: TimerPreset,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.name.ifEmpty { preset.formattedDuration },
                    style = MaterialTheme.typography.titleMedium,
                )
                if (preset.name.isNotEmpty()) {
                    Text(
                        text = preset.formattedDuration,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "${stringResource(
                        preset.soundType.displayNameResId,
                    )} · ${stringResource(preset.vibrationPattern.displayNameResId)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
            Row {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.timer_start),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                TextButton(onClick = onEdit) {
                    Text(stringResource(R.string.timer_edit_btn))
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

/**
 * 새 타이머 추가 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTimerScreen(
    inputHours: Int,
    inputMinutes: Int,
    inputSeconds: Int,
    soundType: SoundType,
    vibrationPattern: VibrationPattern,
    onTimeChange: (Int, Int, Int) -> Unit,
    onSoundTypeChange: (SoundType) -> Unit,
    onVibrationPatternChange: (VibrationPattern) -> Unit,
    onStartClick: (String) -> Unit,
    onSavePresetClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canStart = inputHours > 0 || inputMinutes > 0 || inputSeconds > 0

    var showSoundPicker by remember { mutableStateOf(false) }
    var showVibrationPicker by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }

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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.timer_new)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.timer_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            // 버튼들 (하단 고정)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 프리셋으로 저장 버튼
                OutlinedButton(
                    onClick = { onSavePresetClick(presetName) },
                    enabled = canStart,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.timer_preset_save))
                }

                // 시작 버튼
                FilledTonalButton(
                    onClick = { onStartClick(presetName) },
                    enabled = canStart,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.timer_start))
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TimePicker(
                hours = inputHours,
                minutes = inputMinutes,
                seconds = inputSeconds,
                onHoursChange = { onTimeChange(it, inputMinutes, inputSeconds) },
                onMinutesChange = { onTimeChange(inputHours, it, inputSeconds) },
                onSecondsChange = { onTimeChange(inputHours, inputMinutes, it) },
                modifier = Modifier.padding(vertical = 16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 소리/진동 설정
            TimerSettingsCard(
                soundType = soundType,
                vibrationPattern = vibrationPattern,
                onSoundClick = { showSoundPicker = true },
                onVibrationClick = { showVibrationPicker = true },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 프리셋 이름 입력
            OutlinedTextField(
                value = presetName,
                onValueChange = { presetName = it },
                label = { Text(stringResource(R.string.timer_preset_name_hint)) },
                placeholder = { Text(stringResource(R.string.timer_name_example)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * 타이머 수정 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTimerScreen(
    timerName: String,
    inputHours: Int,
    inputMinutes: Int,
    inputSeconds: Int,
    soundType: SoundType,
    vibrationPattern: VibrationPattern,
    onNameChange: (String) -> Unit,
    onTimeChange: (Int, Int, Int) -> Unit,
    onSoundTypeChange: (SoundType) -> Unit,
    onVibrationPatternChange: (VibrationPattern) -> Unit,
    onSaveClick: () -> Unit,
    onStartClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canSave = inputHours > 0 || inputMinutes > 0 || inputSeconds > 0

    var showSoundPicker by remember { mutableStateOf(false) }
    var showVibrationPicker by remember { mutableStateOf(false) }

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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.timer_edit)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.timer_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            // 버튼들 (하단 고정)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 저장 버튼
                OutlinedButton(
                    onClick = onSaveClick,
                    enabled = canSave,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.save))
                }

                // 시작 버튼
                FilledTonalButton(
                    onClick = onStartClick,
                    enabled = canSave,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.timer_start))
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TimePicker(
                hours = inputHours,
                minutes = inputMinutes,
                seconds = inputSeconds,
                onHoursChange = { onTimeChange(it, inputMinutes, inputSeconds) },
                onMinutesChange = { onTimeChange(inputHours, it, inputSeconds) },
                onSecondsChange = { onTimeChange(inputHours, inputMinutes, it) },
                modifier = Modifier.padding(vertical = 16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 소리/진동 설정
            TimerSettingsCard(
                soundType = soundType,
                vibrationPattern = vibrationPattern,
                onSoundClick = { showSoundPicker = true },
                onVibrationClick = { showVibrationPicker = true },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 타이머 이름 입력
            OutlinedTextField(
                value = timerName,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.timer_name_hint)) },
                placeholder = { Text(stringResource(R.string.timer_name_example)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * 프리셋 수정 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPresetScreen(
    presetName: String,
    inputHours: Int,
    inputMinutes: Int,
    inputSeconds: Int,
    soundType: SoundType,
    vibrationPattern: VibrationPattern,
    onNameChange: (String) -> Unit,
    onTimeChange: (Int, Int, Int) -> Unit,
    onSoundTypeChange: (SoundType) -> Unit,
    onVibrationPatternChange: (VibrationPattern) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canSave = inputHours > 0 || inputMinutes > 0 || inputSeconds > 0

    var showSoundPicker by remember { mutableStateOf(false) }
    var showVibrationPicker by remember { mutableStateOf(false) }

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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.timer_preset_edit)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.timer_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            // 저장 버튼 (하단 고정)
            FilledTonalButton(
                onClick = onSaveClick,
                enabled = canSave,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                Text(stringResource(R.string.save))
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 프리셋 이름 입력
            OutlinedTextField(
                value = presetName,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.timer_preset_name)) },
                placeholder = { Text(stringResource(R.string.timer_name_example)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            TimePicker(
                hours = inputHours,
                minutes = inputMinutes,
                seconds = inputSeconds,
                onHoursChange = { onTimeChange(it, inputMinutes, inputSeconds) },
                onMinutesChange = { onTimeChange(inputHours, it, inputSeconds) },
                onSecondsChange = { onTimeChange(inputHours, inputMinutes, it) },
                modifier = Modifier.padding(vertical = 24.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 소리/진동 설정
            TimerSettingsCard(
                soundType = soundType,
                vibrationPattern = vibrationPattern,
                onSoundClick = { showSoundPicker = true },
                onVibrationClick = { showVibrationPicker = true },
            )
        }
    }
}

/**
 * 타이머 설정 카드
 */
@Composable
private fun TimerSettingsCard(
    soundType: SoundType,
    vibrationPattern: VibrationPattern,
    onSoundClick: () -> Unit,
    onVibrationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingRow(
                label = stringResource(R.string.timer_sound),
                value = stringResource(soundType.displayNameResId),
                onClick = onSoundClick,
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingRow(
                label = stringResource(R.string.timer_vibration),
                value = stringResource(vibrationPattern.displayNameResId),
                onClick = onVibrationClick,
            )
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ====== Previews ======

@Preview(showBackground = true)
@Composable
private fun TimerMainScreenPreview() {
    TikkaTimerTheme {
        TimerMainScreen(
            presets =
                listOf(
                    TimerPreset(id = 1, name = "라면", durationSeconds = 180),
                    TimerPreset(id = 2, name = "", durationSeconds = 300),
                ),
            runningTimers =
                listOf(
                    RunningTimer(
                        instanceId = "1",
                        presetId = 1,
                        name = "3분",
                        totalDurationMillis = 180000,
                        remainingMillis = 120000,
                        state = TimerState.RUNNING,
                    ),
                    RunningTimer(
                        instanceId = "2",
                        presetId = 2,
                        name = "5분",
                        totalDurationMillis = 300000,
                        remainingMillis = 250000,
                        state = TimerState.PAUSED,
                    ),
                ),
            onPresetClick = {},
            onPresetEdit = {},
            onPresetDelete = {},
            onAddClick = {},
            onTimerPause = {},
            onTimerResume = {},
            onTimerReset = {},
            onTimerRemove = {},
            onTimerAddMinute = {},
            onTimerAcknowledge = {},
            onTimerEdit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RunningTimerCardRunningPreview() {
    TikkaTimerTheme {
        RunningTimerCard(
            timer =
                RunningTimer(
                    instanceId = "1",
                    presetId = 1,
                    name = "라면 타이머",
                    totalDurationMillis = 180000,
                    remainingMillis = 120000,
                    state = TimerState.RUNNING,
                ),
            onPause = {},
            onResume = {},
            onReset = {},
            onRemove = {},
            onAddMinute = {},
            onAcknowledge = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RunningTimerCardFinishedPreview() {
    TikkaTimerTheme {
        RunningTimerCard(
            timer =
                RunningTimer(
                    instanceId = "1",
                    presetId = 1,
                    name = "완료된 타이머",
                    totalDurationMillis = 180000,
                    remainingMillis = 0,
                    state = TimerState.FINISHED,
                ),
            onPause = {},
            onResume = {},
            onReset = {},
            onRemove = {},
            onAddMinute = {},
            onAcknowledge = {},
        )
    }
}
