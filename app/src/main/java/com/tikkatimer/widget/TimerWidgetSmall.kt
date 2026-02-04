package com.tikkatimer.widget

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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.tikkatimer.MainActivity
import com.tikkatimer.R
import kotlinx.coroutines.flow.first

/**
 * 1x1 소형 타이머 위젯
 * - 실행 중: 녹색 아이콘
 * - 일시정지: 주황색 아이콘
 * - 대기: 회색 아이콘
 */
class TimerWidgetSmall : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val state = TimerWidgetStateManager.getState(context).first()
        provideContent {
            GlanceTheme {
                SmallWidgetContent(context = context, state = state)
            }
        }
    }
}

@Composable
private fun SmallWidgetContent(
    context: Context,
    state: TimerWidgetState,
) {
    val openAppIntent =
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(WidgetColors.getBackground(state))
                .clickable(actionStartActivity(openAppIntent))
                .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.isRunning -> SmallRunningContent()
            state.isPaused -> SmallPausedContent()
            else -> SmallIdleContent()
        }
    }
}

@Composable
private fun SmallRunningContent() {
    // 1x1 위젯은 아이콘만 표시 (앱 아이콘 크기에 맞춤)
    Image(
        provider = ImageProvider(R.drawable.ic_timer_running),
        contentDescription = null,
        modifier = GlanceModifier.size(40.dp),
        colorFilter = ColorFilter.tint(WidgetColors.runningAccent),
    )
}

@Composable
private fun SmallPausedContent() {
    // 1x1 위젯은 아이콘만 표시 (앱 아이콘 크기에 맞춤)
    Image(
        provider = ImageProvider(R.drawable.ic_timer_paused),
        contentDescription = null,
        modifier = GlanceModifier.size(40.dp),
        colorFilter = ColorFilter.tint(WidgetColors.pausedAccent),
    )
}

@Composable
private fun SmallIdleContent() {
    // 1x1 위젯은 아이콘만 표시 (앱 아이콘 크기에 맞춤)
    Image(
        provider = ImageProvider(R.drawable.ic_timer_idle),
        contentDescription = null,
        modifier = GlanceModifier.size(40.dp),
        colorFilter = ColorFilter.tint(WidgetColors.idleAccent),
    )
}

/**
 * 1x1 소형 위젯 수신자
 */
class TimerWidgetSmallReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimerWidgetSmall()
}
