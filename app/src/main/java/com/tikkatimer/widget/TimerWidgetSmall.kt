package com.tikkatimer.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.tikkatimer.MainActivity
import com.tikkatimer.R

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
        TimerWidgetStateManager.getState(context).collect { state ->
            provideContent {
                GlanceTheme {
                    SmallWidgetContent(context = context, state = state)
                }
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
            state.isRunning -> SmallRunningContent(state)
            state.isPaused -> SmallPausedContent(state)
            else -> SmallIdleContent()
        }
    }
}

@Composable
private fun SmallRunningContent(state: TimerWidgetState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_timer_running),
            contentDescription = null,
            modifier = GlanceModifier.size(24.dp),
            colorFilter = ColorFilter.tint(WidgetColors.runningAccent),
        )
        Text(
            text = state.formattedTimeShort,
            style =
                TextStyle(
                    color = WidgetColors.runningText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                ),
        )
    }
}

@Composable
private fun SmallPausedContent(state: TimerWidgetState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_timer_paused),
            contentDescription = null,
            modifier = GlanceModifier.size(24.dp),
            colorFilter = ColorFilter.tint(WidgetColors.pausedAccent),
        )
        Text(
            text = state.formattedTimeShort,
            style =
                TextStyle(
                    color = WidgetColors.pausedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                ),
        )
    }
}

@Composable
private fun SmallIdleContent() {
    Image(
        provider = ImageProvider(R.drawable.ic_timer_idle),
        contentDescription = null,
        modifier = GlanceModifier.size(32.dp),
        colorFilter = ColorFilter.tint(WidgetColors.idleAccent),
    )
}

/**
 * 1x1 소형 위젯 수신자
 */
class TimerWidgetSmallReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimerWidgetSmall()
}
