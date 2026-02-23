package com.luigi.tikkatimer.presentation.main

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.luigi.tikkatimer.presentation.alarm.AlarmScreen
import com.luigi.tikkatimer.presentation.settings.SettingsScreen
import com.luigi.tikkatimer.presentation.stopwatch.StopwatchScreen
import com.luigi.tikkatimer.presentation.stopwatch.StopwatchViewModel
import com.luigi.tikkatimer.presentation.timer.TimerScreen
import com.luigi.tikkatimer.presentation.timer.TimerViewModel
import com.luigi.tikkatimer.sync.TimerStateSync
import com.luigi.tikkatimer.ui.theme.TikkaTimerTheme
import kotlinx.coroutines.launch

/**
 * 메인 화면
 * 상단 탭을 통해 알람/타이머/스톱워치/설정 화면 전환
 *
 * @param initialTab 초기 탭 인덱스 (null이면 기본값 0)
 * @param timerStateSync 위젯 상태 동기화용 (탭 이동 시 동기화)
 * @param navigateToTimer 외부에서 타이머 탭 이동 요청 (위젯/알림 클릭 시)
 * @param onNavigateToTimerHandled 타이머 탭 이동 처리 완료 콜백
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    initialTab: Int? = null,
    timerStateSync: TimerStateSync? = null,
    navigateToTimer: Boolean = false,
    onNavigateToTimerHandled: () -> Unit = {},
) {
    val tabs = MainTab.entries
    val pagerState =
        rememberPagerState(
            initialPage = initialTab ?: 0,
            pageCount = { tabs.size },
        )
    val coroutineScope = rememberCoroutineScope()

    // 탭 변경 시 위젯 상태 동기화
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            timerStateSync?.syncCurrentStateToWidget()
        }
    }

    // 외부에서 타이머 탭 이동 요청 처리 (위젯/알림 클릭 시)
    LaunchedEffect(navigateToTimer) {
        if (navigateToTimer) {
            pagerState.animateScrollToPage(MainTab.TIMER.ordinal)
            onNavigateToTimerHandled()
        }
    }

    // Activity 스코프에서 ViewModel을 미리 생성하여 탭 이동 시에도 상태 유지
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val timerViewModel: TimerViewModel = hiltViewModel(viewModelStoreOwner)
    val stopwatchViewModel: StopwatchViewModel = hiltViewModel(viewModelStoreOwner)

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
                    MainTab.TIMER -> TimerScreen(viewModel = timerViewModel)
                    MainTab.STOPWATCH -> StopwatchScreen(viewModel = stopwatchViewModel)
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
