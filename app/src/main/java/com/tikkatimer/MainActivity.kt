package com.tikkatimer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
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

    /**
     * 알림 권한 요청 런처 (Android 13+)
     */
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // 권한 결과 처리 (거부해도 앱은 계속 사용 가능)
            if (!isGranted) {
                // 사용자가 알림 권한을 거부함
                // 타이머/알람 알림이 표시되지 않을 수 있음
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 앱 시작 시 저장된 언어 설정 복원
        lifecycleScope.launch {
            val settings = settingsDataStore.settingsFlow.first()
            LocaleHelper.setLocale(this@MainActivity, settings.language)
        }

        // 알림 권한 요청 (Android 13+)
        requestNotificationPermission()

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

    /**
     * Android 13+ 알림 권한 요청
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }
}
