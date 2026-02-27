package com.luigi.tikkatimer.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.luigi.tikkatimer.MainActivity
import com.luigi.tikkatimer.R
import com.luigi.tikkatimer.domain.model.ColorTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 1x1 소형 타이머 위젯
 * - 실행 중: 녹색 아이콘 (테마 무관)
 * - 일시정지: 주황색 아이콘 (테마 무관)
 * - 대기: 테마 색상 아이콘
 */
class TimerWidgetSmall : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val state = TimerWidgetStateManager.getState(context).first()
        val colorTheme = WidgetColors.readColorThemeSync(context)
        provideContent {
            GlanceTheme {
                SmallWidgetContent(context = context, state = state, colorTheme = colorTheme)
            }
        }
    }
}

@Composable
private fun SmallWidgetContent(
    context: Context,
    state: TimerWidgetState,
    colorTheme: ColorTheme,
) {
    val openAppIntent =
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

    // 앱 아이콘 라벨 높이(약 14dp)만큼 하단 여백을 두어 정사각형처럼 보이게 함
    // 하단 여백은 투명하게 비워둠
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 위젯 콘텐츠 영역 (배경색 적용)
        Box(
            modifier =
                GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .background(WidgetColors.getBackground(state, colorTheme))
                    .clickable(actionStartActivity(openAppIntent))
                    .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isRunning -> SmallRunningContent(colorTheme)
                state.isPaused -> SmallPausedContent(colorTheme)
                else -> SmallIdleContent(colorTheme)
            }
        }
        // 앱 아이콘 라벨 높이만큼 하단 투명 여백
        Spacer(modifier = GlanceModifier.fillMaxWidth().height(14.dp))
    }
}

@Composable
private fun SmallRunningContent(colorTheme: ColorTheme) {
    // 1x1 위젯은 아이콘만 표시 (앱 아이콘 크기에 맞춤)
    // 앱 설정의 테마 색상으로 아이콘 tint 적용
    Image(
        provider = ImageProvider(R.drawable.ic_hourglass),
        contentDescription = null,
        modifier = GlanceModifier.size(40.dp),
        colorFilter = ColorFilter.tint(WidgetColors.getIdlePalette(colorTheme).accent),
    )
}

@Composable
private fun SmallPausedContent(colorTheme: ColorTheme) {
    // 1x1 위젯은 아이콘만 표시 (앱 아이콘 크기에 맞춤)
    // 앱 설정의 테마 색상으로 아이콘 tint 적용
    Image(
        provider = ImageProvider(R.drawable.ic_timer_paused),
        contentDescription = null,
        modifier = GlanceModifier.size(40.dp),
        colorFilter = ColorFilter.tint(WidgetColors.getIdlePalette(colorTheme).accent),
    )
}

@Composable
private fun SmallIdleContent(colorTheme: ColorTheme) {
    // 1x1 위젯은 아이콘만 표시 (앱 아이콘 크기에 맞춤)
    // idle 상태에서는 테마 색상 적용
    Image(
        provider = ImageProvider(R.drawable.ic_timer_idle),
        contentDescription = null,
        modifier = GlanceModifier.size(40.dp),
        colorFilter = ColorFilter.tint(WidgetColors.getIdlePalette(colorTheme).accent),
    )
}

/**
 * 1x1 소형 위젯 수신자
 */
class TimerWidgetSmallReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimerWidgetSmall()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        // Glance 세션 문제 회피를 위해 코루틴에서 업데이트
        kotlinx.coroutines.MainScope().launch {
            appWidgetIds.forEach { appWidgetId ->
                try {
                    val glanceId =
                        androidx.glance.appwidget.GlanceAppWidgetManager(context)
                            .getGlanceIdBy(appWidgetId)
                    glanceAppWidget.update(context, glanceId)
                } catch (e: Exception) {
                    // 세션 에러 시 부모 클래스 호출
                    super.onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
                }
            }
        }
    }
}
