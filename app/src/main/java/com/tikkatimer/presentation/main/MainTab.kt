package com.tikkatimer.presentation.main

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.tikkatimer.R

/**
 * 메인 화면의 탭 메뉴 정의
 * @param titleResId 탭에 표시될 제목 리소스 ID
 * @param selectedIcon 선택 시 아이콘
 * @param unselectedIcon 미선택 시 아이콘
 */
enum class MainTab(
    @param:StringRes val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    ALARM(
        titleResId = R.string.nav_alarm,
        selectedIcon = Icons.Filled.Alarm,
        unselectedIcon = Icons.Outlined.Alarm,
    ),
    TIMER(
        titleResId = R.string.nav_timer,
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer,
    ),
    STOPWATCH(
        titleResId = R.string.nav_stopwatch,
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer,
    ),
    SETTINGS(
        titleResId = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    ),
    ;

    companion object {
        fun fromIndex(index: Int): MainTab = entries[index]
    }
}
