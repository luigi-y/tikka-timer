package com.tikkatimer.presentation.alarm.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tikkatimer.R
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.ui.theme.TikkaTimerTheme

/**
 * 소리 타입 선택 다이얼로그
 * 기본 알람음, 무음, 사용자 지정 등 다양한 소리 옵션 제공
 */
@Composable
fun SoundPickerDialog(
    selectedSoundType: SoundType,
    onSoundTypeSelected: (SoundType) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.alarm_sound)) },
        text = {
            LazyColumn {
                items(SoundType.entries.toList()) { soundType ->
                    SoundTypeItem(
                        soundType = soundType,
                        isSelected = soundType == selectedSoundType,
                        onClick = { onSoundTypeSelected(soundType) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.confirm))
            }
        },
    )
}

@Composable
private fun SoundTypeItem(
    soundType: SoundType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(soundType.displayNameResId),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview
@Composable
private fun SoundPickerDialogPreview() {
    TikkaTimerTheme {
        SoundPickerDialog(
            selectedSoundType = SoundType.DEFAULT,
            onSoundTypeSelected = {},
            onDismiss = {},
        )
    }
}
