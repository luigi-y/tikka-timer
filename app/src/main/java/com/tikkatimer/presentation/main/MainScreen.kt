package com.tikkatimer.presentation.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.tikkatimer.presentation.alarm.AlarmScreen
import com.tikkatimer.presentation.settings.SettingsScreen
import com.tikkatimer.presentation.stopwatch.StopwatchScreen
import com.tikkatimer.presentation.timer.TimerScreen
import com.tikkatimer.ui.theme.TikkaTimerTheme
import kotlinx.coroutines.launch

/**
 * 메인 화면
 * 상단 탭을 통해 알람/타이머/스톱워치/설정 화면 전환
 */
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val tabs = MainTab.entries
    val pagerState =
        rememberPagerState(
            initialPage = 0,
            pageCount = { tabs.size },
        )
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            // 상단 탭 메뉴
            @OptIn(ExperimentalMaterial3Api::class)
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = pagerState.currentPage == index
                    val title = stringResource(tab.titleResId)
                    Tab(
                        selected = selected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = title,
                            )
                        },
                    )
                }
            }

            // 탭에 따른 화면 전환 (스와이프 지원)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (MainTab.fromIndex(page)) {
                    MainTab.ALARM -> AlarmScreen()
                    MainTab.TIMER -> TimerScreen()
                    MainTab.STOPWATCH -> StopwatchScreen()
                    MainTab.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    TikkaTimerTheme {
        MainScreen()
    }
}
