package com.tikkatimer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tikkatimer.data.local.SettingsDataStore
import com.tikkatimer.domain.model.AppLanguage
import com.tikkatimer.domain.model.AppSettings
import com.tikkatimer.domain.model.ColorTheme
import com.tikkatimer.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
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
         */
        fun setColorTheme(colorTheme: ColorTheme) {
            viewModelScope.launch {
                settingsDataStore.setColorTheme(colorTheme)
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
