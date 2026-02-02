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
import com.tikkatimer.domain.model.VibrationPattern
import com.tikkatimer.ui.theme.TikkaTimerTheme

/**
 * 진동 패턴 선택 다이얼로그
 * 기본 진동, 강한 진동, 심장박동 패턴 등 다양한 진동 옵션 제공
 */
@Composable
fun VibrationPickerDialog(
    selectedPattern: VibrationPattern,
    onPatternSelected: (VibrationPattern) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.alarm_vibration)) },
        text = {
            LazyColumn {
                items(VibrationPattern.entries.toList()) { pattern ->
                    VibrationPatternItem(
                        pattern = pattern,
                        isSelected = pattern == selectedPattern,
                        onClick = { onPatternSelected(pattern) },
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
private fun VibrationPatternItem(
    pattern: VibrationPattern,
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
            text = stringResource(pattern.displayNameResId),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview
@Composable
private fun VibrationPickerDialogPreview() {
    TikkaTimerTheme {
        VibrationPickerDialog(
            selectedPattern = VibrationPattern.DEFAULT,
            onPatternSelected = {},
            onDismiss = {},
        )
    }
}
