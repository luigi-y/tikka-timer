package com.tikkatimer.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tikkatimer.R
import com.tikkatimer.domain.model.AppLanguage
import com.tikkatimer.domain.model.AppSettings
import com.tikkatimer.domain.model.ColorTheme
import com.tikkatimer.domain.model.ThemeMode
import com.tikkatimer.ui.theme.Cherry40
import com.tikkatimer.ui.theme.Forest40
import com.tikkatimer.ui.theme.Lavender40
import com.tikkatimer.ui.theme.Ocean40
import com.tikkatimer.ui.theme.Purple40
import com.tikkatimer.ui.theme.Sunset40
import com.tikkatimer.ui.theme.TikkaTimerTheme
import com.tikkatimer.util.LocaleHelper

/**
 * 설정 화면
 * 앱 테마, 컬러 테마, 언어 설정 기능 제공
 */
@Suppress("DEPRECATION")
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    SettingsScreenContent(
        settings = settings,
        onThemeModeChange = viewModel::setThemeMode,
        onColorThemeChange = viewModel::setColorTheme,
        onLanguageChange = { language ->
            viewModel.setLanguage(language)
            LocaleHelper.setLocale(context, language)
        },
        onResetSettings = viewModel::resetSettings,
        modifier = modifier,
    )
}

@Composable
private fun SettingsScreenContent(
    settings: AppSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onColorThemeChange: (ColorTheme) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onResetSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val feedbackSubject = stringResource(R.string.settings_feedback_subject)
    val emailNotFoundMessage = stringResource(R.string.settings_feedback_email_not_found)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        // 헤더
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
        )

        HorizontalDivider()

        // 테마 모드 설정
        SettingsItem(
            icon = Icons.Default.DarkMode,
            title = stringResource(R.string.settings_theme),
            subtitle = getThemeModeDisplayName(settings.themeMode),
            onClick = { showThemeDialog = true },
        )

        HorizontalDivider()

        // 컬러 테마 설정
        SettingsItem(
            icon = Icons.Default.Palette,
            title = stringResource(R.string.settings_color_theme),
            subtitle = getColorThemeDisplayName(settings.colorTheme),
            onClick = { showColorThemeDialog = true },
        )

        HorizontalDivider()

        // 언어 설정
        SettingsItem(
            icon = Icons.Default.Language,
            title = stringResource(R.string.settings_language),
            subtitle = settings.language.getDisplayText(),
            onClick = { showLanguageDialog = true },
        )

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // 개발자에게 제안하기
        SettingsItem(
            icon = Icons.Default.Mail,
            title = stringResource(R.string.settings_feedback),
            subtitle = stringResource(R.string.settings_feedback_desc),
            onClick = {
                sendFeedbackEmail(
                    context = context,
                    subject = feedbackSubject,
                    onEmailNotFound = {
                        Toast.makeText(context, emailNotFoundMessage, Toast.LENGTH_SHORT).show()
                    },
                )
            },
        )

        HorizontalDivider()

        // 설정 초기화
        SettingsItem(
            icon = Icons.Default.Refresh,
            title = stringResource(R.string.settings_reset),
            subtitle = "",
            onClick = { showResetDialog = true },
            showChevron = false,
        )

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // 프라이버시 정책
        SettingsItem(
            icon = Icons.Default.Policy,
            title = stringResource(R.string.settings_privacy_policy),
            subtitle = stringResource(R.string.settings_privacy_policy_desc),
            onClick = {
                openPrivacyPolicy(context)
            },
        )

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // 앱 정보
        Text(
            text = stringResource(R.string.settings_app_info),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_version)) },
            supportingContent = { Text("1.0.0") },
        )
    }

    // 테마 모드 선택 다이얼로그
    if (showThemeDialog) {
        ThemeModeSelectionDialog(
            selectedTheme = settings.themeMode,
            onSelect = {
                onThemeModeChange(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    // 컬러 테마 선택 다이얼로그
    if (showColorThemeDialog) {
        ColorThemeSelectionDialog(
            selectedTheme = settings.colorTheme,
            onSelect = {
                onColorThemeChange(it)
                showColorThemeDialog = false
            },
            onDismiss = { showColorThemeDialog = false },
        )
    }

    // 언어 선택 다이얼로그
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = settings.language,
            onSelect = {
                onLanguageChange(it)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false },
        )
    }

    // 설정 초기화 확인 다이얼로그
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_confirm)) },
            text = { Text(stringResource(R.string.settings_reset_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetSettings()
                        showResetDialog = false
                    },
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun getThemeModeDisplayName(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
        ThemeMode.DARK -> stringResource(R.string.theme_dark)
    }
}

@Composable
private fun getColorThemeDisplayName(colorTheme: ColorTheme): String {
    return when (colorTheme) {
        ColorTheme.DEFAULT -> stringResource(R.string.color_default)
        ColorTheme.OCEAN -> stringResource(R.string.color_ocean)
        ColorTheme.FOREST -> stringResource(R.string.color_forest)
        ColorTheme.SUNSET -> stringResource(R.string.color_sunset)
        ColorTheme.CHERRY -> stringResource(R.string.color_cherry)
        ColorTheme.LAVENDER -> stringResource(R.string.color_lavender)
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showChevron: Boolean = true,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent =
            if (subtitle.isNotEmpty()) {
                { Text(subtitle) }
            } else {
                null
            },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun ThemeModeSelectionDialog(
    selectedTheme: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme_select)) },
        text = {
            Column {
                ThemeMode.entries.forEach { theme ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(theme) }
                                .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = getThemeModeDisplayName(theme),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        if (theme == selectedTheme) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.selected),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

/**
 * AppLanguage의 표시 이름을 반환
 * SYSTEM은 현재 로케일에 맞는 문자열 리소스 사용, 나머지는 고정된 언어 이름 사용
 */
@Composable
private fun AppLanguage.getDisplayText(): String =
    if (this == AppLanguage.SYSTEM) {
        stringResource(R.string.language_system)
    } else {
        this.displayName
    }

@Composable
private fun LanguageSelectionDialog(
    selectedLanguage: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language_select)) },
        text = {
            Column {
                AppLanguage.entries.forEach { language ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(language) }
                                .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = language.getDisplayText(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        if (language == selectedLanguage) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.selected),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun ColorThemeSelectionDialog(
    selectedTheme: ColorTheme,
    onSelect: (ColorTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    val colorThemeColors =
        mapOf(
            ColorTheme.DEFAULT to Purple40,
            ColorTheme.OCEAN to Ocean40,
            ColorTheme.FOREST to Forest40,
            ColorTheme.SUNSET to Sunset40,
            ColorTheme.CHERRY to Cherry40,
            ColorTheme.LAVENDER to Lavender40,
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_color_theme_select)) },
        text = {
            Column {
                ColorTheme.entries.forEach { theme ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(theme) }
                                .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // 컬러 프리뷰
                        Box(
                            modifier =
                                Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(colorThemeColors[theme] ?: Purple40)
                                    .then(
                                        if (theme == selectedTheme) {
                                            Modifier.border(2.dp, Color.White, CircleShape)
                                        } else {
                                            Modifier
                                        },
                                    ),
                        )

                        Text(
                            text = getColorThemeDisplayName(theme),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )

                        if (theme == selectedTheme) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.selected),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

/**
 * 개발자에게 피드백 이메일 전송
 */
private fun sendFeedbackEmail(
    context: android.content.Context,
    subject: String,
    onEmailNotFound: () -> Unit,
) {
    val emailIntent =
        Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("herohyohwan@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }

    try {
        context.startActivity(emailIntent)
    } catch (e: ActivityNotFoundException) {
        onEmailNotFound()
    }
}

/**
 * 프라이버시 정책 페이지 열기
 */
private fun openPrivacyPolicy(context: android.content.Context) {
    // GitHub Pages 또는 웹 호스팅에 올린 프라이버시 정책 URL
    val privacyPolicyUrl = "https://yoonhh.github.io/tikka-timer/privacy-policy"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // 브라우저가 없는 경우 (매우 드문 경우)
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    TikkaTimerTheme {
        SettingsScreenContent(
            settings = AppSettings.DEFAULT,
            onThemeModeChange = {},
            onColorThemeChange = {},
            onLanguageChange = {},
            onResetSettings = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenDarkPreview() {
    TikkaTimerTheme(darkTheme = true) {
        SettingsScreenContent(
            settings =
                AppSettings(
                    themeMode = ThemeMode.DARK,
                    colorTheme = ColorTheme.OCEAN,
                    language = AppLanguage.KOREAN,
                ),
            onThemeModeChange = {},
            onColorThemeChange = {},
            onLanguageChange = {},
            onResetSettings = {},
        )
    }
}
