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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.tikkatimer.MainActivity
import com.tikkatimer.R

/**
 * 2x2 중형 타이머 위젯 (기본)
 *
 * 상태별 표시:
 * - 실행 중: 녹색 배경, 남은 시간 표시, 상태 아이콘
 * - 일시정지: 주황색 배경, 남은 시간 표시
 * - 없음/종료: 회색 배경, "타이머 시작" 안내
 */
class TimerWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        TimerWidgetStateManager.getState(context).collect { state ->
            provideContent {
                GlanceTheme {
                    MediumWidgetContent(context = context, state = state)
                }
            }
        }
    }
}

@Composable
private fun MediumWidgetContent(
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
                .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.isRunning -> MediumRunningContent(state)
            state.isPaused -> MediumPausedContent(state)
            else -> MediumIdleContent()
        }
    }
}

@Composable
private fun MediumRunningContent(state: TimerWidgetState) {
    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 타이머 이름
        if (state.timerName.isNotEmpty()) {
            Text(
                text = state.timerName,
                style =
                    TextStyle(
                        color = WidgetColors.runningText,
                        fontSize = 12.sp,
                    ),
                maxLines = 1,
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
        }

        // 아이콘과 시간
        Box(
            contentAlignment = Alignment.Center,
            modifier = GlanceModifier.size(80.dp),
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_timer_running),
                contentDescription = null,
                modifier = GlanceModifier.size(60.dp),
                colorFilter = ColorFilter.tint(WidgetColors.runningAccent),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.formattedTime,
                    style =
                        TextStyle(
                            color = WidgetColors.runningText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // 상태 표시
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_timer_running),
                contentDescription = null,
                modifier = GlanceModifier.size(16.dp),
                colorFilter = ColorFilter.tint(WidgetColors.runningAccent),
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = "실행 중",
                style =
                    TextStyle(
                        color = WidgetColors.runningAccent,
                        fontSize = 11.sp,
                    ),
            )
        }
    }
}

@Composable
private fun MediumPausedContent(state: TimerWidgetState) {
    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 타이머 이름
        if (state.timerName.isNotEmpty()) {
            Text(
                text = state.timerName,
                style =
                    TextStyle(
                        color = WidgetColors.pausedText,
                        fontSize = 12.sp,
                    ),
                maxLines = 1,
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
        }

        // 시간
        Text(
            text = state.formattedTime,
            style =
                TextStyle(
                    color = WidgetColors.pausedText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                ),
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // 상태 표시
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_timer_paused),
                contentDescription = null,
                modifier = GlanceModifier.size(16.dp),
                colorFilter = ColorFilter.tint(WidgetColors.pausedAccent),
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = "일시정지",
                style =
                    TextStyle(
                        color = WidgetColors.pausedAccent,
                        fontSize = 11.sp,
                    ),
            )
        }
    }
}

@Composable
private fun MediumIdleContent() {
    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_timer_idle),
            contentDescription = null,
            modifier = GlanceModifier.size(40.dp),
            colorFilter = ColorFilter.tint(WidgetColors.idleAccent),
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Text(
            text = "타이머",
            style =
                TextStyle(
                    color = WidgetColors.idleText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        Text(
            text = "탭하여 시작",
            style =
                TextStyle(
                    color = WidgetColors.idleAccent,
                    fontSize = 11.sp,
                ),
        )
    }
}

/**
 * 2x2 중형 위젯 수신자
 */
class TimerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimerWidget()
}
