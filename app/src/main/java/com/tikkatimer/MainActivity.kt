package com.tikkatimer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.tikkatimer.data.local.SettingsDataStore
import com.tikkatimer.domain.model.AppSettings
import com.tikkatimer.domain.model.ThemeMode
import com.tikkatimer.presentation.main.MainScreen
import com.tikkatimer.ui.theme.TikkaTimerTheme
import com.tikkatimer.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 앱의 메인 Activity
 * 단일 Activity 구조로 Compose Navigation 사용
 * AppCompatActivity를 상속하여 언어 변경 시 자동 Activity 재생성 지원
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 앱 시작 시 저장된 언어 설정 복원
        lifecycleScope.launch {
            val settings = settingsDataStore.settingsFlow.first()
            LocaleHelper.setLocale(this@MainActivity, settings.language)
        }

        enableEdgeToEdge()
        setContent {
            val settings by settingsDataStore.settingsFlow.collectAsState(
                initial = AppSettings.DEFAULT,
            )

            val darkTheme =
                when (settings.themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }

            TikkaTimerTheme(
                darkTheme = darkTheme,
                colorTheme = settings.colorTheme,
            ) {
                MainScreen()
            }
        }
    }
}
