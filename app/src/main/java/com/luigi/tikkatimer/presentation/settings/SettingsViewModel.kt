package com.luigi.tikkatimer.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luigi.tikkatimer.data.local.SettingsDataStore
import com.luigi.tikkatimer.domain.model.AppLanguage
import com.luigi.tikkatimer.domain.model.AppSettings
import com.luigi.tikkatimer.domain.model.ColorTheme
import com.luigi.tikkatimer.domain.model.ThemeMode
import com.luigi.tikkatimer.widget.TimerWidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 설정 ViewModel
 * 앱 테마, 컬러 테마, 언어 설정 관리
 */
@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsDataStore: SettingsDataStore,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        val settings: StateFlow<AppSettings> =
            settingsDataStore.settingsFlow
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = AppSettings.DEFAULT,
                )

        /**
         * 테마 모드 변경
         */
        fun setThemeMode(themeMode: ThemeMode) {
            viewModelScope.launch {
                settingsDataStore.setThemeMode(themeMode)
            }
        }

        /**
         * 컬러 테마 변경
         * 위젯도 새 테마 색상으로 갱신
         */
        fun setColorTheme(colorTheme: ColorTheme) {
            viewModelScope.launch {
                settingsDataStore.setColorTheme(colorTheme)
                TimerWidgetUpdater.onThemeChanged(context)
            }
        }

        /**
         * 언어 설정 변경
         */
        fun setLanguage(language: AppLanguage) {
            viewModelScope.launch {
                settingsDataStore.setLanguage(language)
            }
        }

        /**
         * 설정 초기화
         */
        fun resetSettings() {
            viewModelScope.launch {
                settingsDataStore.resetSettings()
            }
        }
    }
