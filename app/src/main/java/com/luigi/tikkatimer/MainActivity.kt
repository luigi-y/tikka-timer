package com.luigi.tikkatimer

import android.Manifest
import android.content.Intent
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
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.luigi.tikkatimer.data.local.SettingsDataStore
import com.luigi.tikkatimer.domain.model.AppSettings
import com.luigi.tikkatimer.domain.model.ThemeMode
import com.luigi.tikkatimer.presentation.main.MainScreen
import com.luigi.tikkatimer.presentation.main.MainTab
import com.luigi.tikkatimer.sync.TimerStateSync
import com.luigi.tikkatimer.ui.theme.TikkaTimerTheme
import com.luigi.tikkatimer.util.LocaleHelper
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

    @Inject
    lateinit var timerStateSync: TimerStateSync

    companion object {
        /** 타이머 탭으로 이동하라는 Intent extra */
        const val EXTRA_NAVIGATE_TO_TIMER = "navigate_to_timer"
    }

    /** 외부에서 탭 변경 요청을 전달하기 위한 State */
    private val navigateToTimerState = mutableStateOf(false)

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

    /** Intent에서 타이머 탭 이동 요청 확인 */
    private fun getInitialTabFromIntent(intent: Intent?): Int? {
        return if (intent?.getBooleanExtra(EXTRA_NAVIGATE_TO_TIMER, false) == true) {
            MainTab.TIMER.ordinal
        } else {
            null
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

        // 앱이 새로 시작될 때만 initialTab 적용 (이미 실행 중이면 null)
        val initialTab =
            if (savedInstanceState == null) {
                getInitialTabFromIntent(intent)
            } else {
                null
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

            // onNewIntent에서 탭 변경 요청 감지
            val navigateToTimer by navigateToTimerState

            TikkaTimerTheme(
                darkTheme = darkTheme,
                colorTheme = settings.colorTheme,
            ) {
                MainScreen(
                    initialTab = initialTab,
                    timerStateSync = timerStateSync,
                    navigateToTimer = navigateToTimer,
                    onNavigateToTimerHandled = { navigateToTimerState.value = false },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // 이미 실행 중인 앱으로 돌아올 때 타이머 탭 이동 요청 처리
        if (intent.getBooleanExtra(EXTRA_NAVIGATE_TO_TIMER, false)) {
            navigateToTimerState.value = true
        }
    }

    override fun onResume() {
        super.onResume()
        // 앱이 포그라운드로 돌아올 때 위젯 상태 동기화
        syncWidgetState()
    }

    /**
     * 현재 타이머 상태를 위젯에 동기화
     */
    private fun syncWidgetState() {
        lifecycleScope.launch {
            timerStateSync.syncCurrentStateToWidget()
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
