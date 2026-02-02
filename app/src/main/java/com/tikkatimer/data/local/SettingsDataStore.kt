package com.tikkatimer.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tikkatimer.domain.model.AppLanguage
import com.tikkatimer.domain.model.AppSettings
import com.tikkatimer.domain.model.ColorTheme
import com.tikkatimer.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * DataStore 기반 설정 저장소
 * 앱 테마, 언어 등의 설정을 영구 저장
 */
@Singleton
class SettingsDataStore
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        private object PreferencesKeys {
            val THEME_MODE = stringPreferencesKey("theme_mode")
            val COLOR_THEME = stringPreferencesKey("color_theme")
            val LANGUAGE = stringPreferencesKey("language")
        }

        /**
         * 설정 Flow
         */
        val settingsFlow: Flow<AppSettings> =
            context.dataStore.data.map { preferences ->
                val themeMode =
                    preferences[PreferencesKeys.THEME_MODE]?.let { value ->
                        ThemeMode.entries.find { it.name == value }
                    } ?: ThemeMode.SYSTEM

                val colorTheme =
                    preferences[PreferencesKeys.COLOR_THEME]?.let { value ->
                        ColorTheme.entries.find { it.name == value }
                    } ?: ColorTheme.DEFAULT

                val language =
                    preferences[PreferencesKeys.LANGUAGE]?.let { value ->
                        AppLanguage.entries.find { it.name == value }
                    } ?: AppLanguage.SYSTEM

                AppSettings(
                    themeMode = themeMode,
                    colorTheme = colorTheme,
                    language = language,
                )
            }

        /**
         * 테마 모드 저장
         */
        suspend fun setThemeMode(themeMode: ThemeMode) {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.THEME_MODE] = themeMode.name
            }
        }

        /**
         * 컬러 테마 저장
         */
        suspend fun setColorTheme(colorTheme: ColorTheme) {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.COLOR_THEME] = colorTheme.name
            }
        }

        /**
         * 언어 설정 저장
         */
        suspend fun setLanguage(language: AppLanguage) {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.LANGUAGE] = language.name
            }
        }

        /**
         * 설정 초기화
         */
        suspend fun resetSettings() {
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }
