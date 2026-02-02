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
 */
enum class AppLanguage(val displayName: String, val locale: String) {
    SYSTEM("시스템 설정", ""),
    KOREAN("한국어", "ko"),
    ENGLISH("English", "en"),
    JAPANESE("日本語", "ja"),
    CHINESE("中文(简体)", "zh"),
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
