package com.tikkatimer.domain.model

/**
 * 앱 테마 모드 (라이트/다크)
 */
enum class ThemeMode(val displayNameResId: Int) {
    SYSTEM(0), // R.string.theme_system
    LIGHT(0), // R.string.theme_light
    DARK(0), // R.string.theme_dark
}

/**
 * 앱 컬러 테마
 */
enum class ColorTheme(val displayNameResId: Int) {
    DEFAULT(0), // 기본 (보라)
    OCEAN(0), // 오션 (파랑)
    FOREST(0), // 포레스트 (초록)
    SUNSET(0), // 선셋 (오렌지)
    CHERRY(0), // 체리 (빨강)
    LAVENDER(0), // 라벤더 (연보라)
}

/**
 * 앱 언어 설정
 * displayNameResId가 0이면 displayName을 사용 (언어 이름은 해당 언어로 고정)
 */
enum class AppLanguage(val displayNameResId: Int, val displayName: String, val locale: String) {
    SYSTEM(1, "", ""), // displayNameResId = 1은 R.string.language_system을 의미 (실제 값은 SettingsScreen에서 처리)
    KOREAN(0, "한국어", "ko"),
    ENGLISH(0, "English", "en"),
    JAPANESE(0, "日本語", "ja"),
    CHINESE(0, "中文(简体)", "zh"),
}

/**
 * 앱 설정 도메인 모델
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorTheme: ColorTheme = ColorTheme.DEFAULT,
    val language: AppLanguage = AppLanguage.SYSTEM,
) {
    companion object {
        val DEFAULT = AppSettings()
    }
}
