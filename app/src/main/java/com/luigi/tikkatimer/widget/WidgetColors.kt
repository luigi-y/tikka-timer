package com.luigi.tikkatimer.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import com.luigi.tikkatimer.data.local.SettingsDataStore
import com.luigi.tikkatimer.domain.model.ColorTheme

/**
 * 위젯 공통 색상 정의
 * running/paused/finished 색상은 상태 시그널이므로 테마 무관 고정
 * idle 상태만 테마 반영
 */
object WidgetColors {
    // 실행 중 상태 (녹색 계열) - 테마 무관 고정
    val runningBackground = ColorProvider(Color(0xFFE8F5E9))
    val runningText = ColorProvider(Color(0xFF1B5E20))
    val runningAccent = ColorProvider(Color(0xFF2E7D32))

    // 일시정지 상태 (주황색 계열) - 테마 무관 고정
    val pausedBackground = ColorProvider(Color(0xFFFFF3E0))
    val pausedText = ColorProvider(Color(0xFFE65100))
    val pausedAccent = ColorProvider(Color(0xFFF57C00))

    // 완료 상태 (빨간색 계열) - 테마 무관 고정
    val finishedBackground = ColorProvider(Color(0xFFFFEBEE))
    val finishedText = ColorProvider(Color(0xFFB71C1C))
    val finishedAccent = ColorProvider(Color(0xFFD32F2F))

    /**
     * 테마별 idle 색상 팔레트
     */
    data class IdlePalette(
        val background: ColorProvider,
        val text: ColorProvider,
        val accent: ColorProvider,
    )

    /** 테마별 idle 팔레트 (Light 모드 기준) */
    private val idlePalettes =
        mapOf(
            ColorTheme.DEFAULT to
                IdlePalette(
                    background = ColorProvider(Color(0xFFD4E3FF)),
                    text = ColorProvider(Color(0xFF43474E)),
                    accent = ColorProvider(Color(0xFF1565C0)),
                ),
            ColorTheme.OCEAN to
                IdlePalette(
                    background = ColorProvider(Color(0xFFE3F2FD)),
                    text = ColorProvider(Color(0xFF43474E)),
                    accent = ColorProvider(Color(0xFF1976D2)),
                ),
            ColorTheme.FOREST to
                IdlePalette(
                    background = ColorProvider(Color(0xFFE8F5E9)),
                    text = ColorProvider(Color(0xFF424940)),
                    accent = ColorProvider(Color(0xFF388E3C)),
                ),
            ColorTheme.SUNSET to
                IdlePalette(
                    background = ColorProvider(Color(0xFFFFF3E0)),
                    text = ColorProvider(Color(0xFF52443C)),
                    accent = ColorProvider(Color(0xFFF57C00)),
                ),
            ColorTheme.CHERRY to
                IdlePalette(
                    background = ColorProvider(Color(0xFFFCE4EC)),
                    text = ColorProvider(Color(0xFF524344)),
                    accent = ColorProvider(Color(0xFFD81B60)),
                ),
            ColorTheme.LAVENDER to
                IdlePalette(
                    background = ColorProvider(Color(0xFFF3E5F5)),
                    text = ColorProvider(Color(0xFF4A454E)),
                    accent = ColorProvider(Color(0xFF8E24AA)),
                ),
        )

    /** 기본 idle 팔레트 (테마를 읽을 수 없는 경우) */
    private val defaultIdlePalette =
        IdlePalette(
            background = ColorProvider(Color(0xFFF5F5F5)),
            text = ColorProvider(Color(0xFF424242)),
            accent = ColorProvider(Color(0xFF9E9E9E)),
        )

    /**
     * SharedPreferences에서 현재 ColorTheme를 동기적으로 읽기
     */
    fun readColorThemeSync(context: Context): ColorTheme {
        val prefs =
            context.getSharedPreferences(
                SettingsDataStore.WIDGET_SETTINGS_PREFS,
                Context.MODE_PRIVATE,
            )
        val name = prefs.getString(SettingsDataStore.KEY_COLOR_THEME, "DEFAULT") ?: "DEFAULT"
        return ColorTheme.entries.find { it.name == name } ?: ColorTheme.DEFAULT
    }

    /**
     * 테마별 idle 팔레트 반환
     */
    fun getIdlePalette(colorTheme: ColorTheme): IdlePalette = idlePalettes[colorTheme] ?: defaultIdlePalette

    /**
     * 테마별 idle accent 색상 반환 (RemoteViews용 ARGB int)
     */
    fun getIdleAccentArgb(colorTheme: ColorTheme): Int =
        when (colorTheme) {
            ColorTheme.DEFAULT -> 0xFF1565C0.toInt()
            ColorTheme.OCEAN -> 0xFF1976D2.toInt()
            ColorTheme.FOREST -> 0xFF388E3C.toInt()
            ColorTheme.SUNSET -> 0xFFF57C00.toInt()
            ColorTheme.CHERRY -> 0xFFD81B60.toInt()
            ColorTheme.LAVENDER -> 0xFF8E24AA.toInt()
        }

    /**
     * 테마별 idle 배경 색상 반환 (RemoteViews용 ARGB int)
     */
    fun getIdleBackgroundArgb(colorTheme: ColorTheme): Int =
        when (colorTheme) {
            ColorTheme.DEFAULT -> 0xFFD4E3FF.toInt()
            ColorTheme.OCEAN -> 0xFFE3F2FD.toInt()
            ColorTheme.FOREST -> 0xFFE8F5E9.toInt()
            ColorTheme.SUNSET -> 0xFFFFF3E0.toInt()
            ColorTheme.CHERRY -> 0xFFFCE4EC.toInt()
            ColorTheme.LAVENDER -> 0xFFF3E5F5.toInt()
        }

    /**
     * 상태에 따른 배경색 반환
     */
    fun getBackground(
        state: TimerWidgetState,
        colorTheme: ColorTheme = ColorTheme.DEFAULT,
    ): ColorProvider =
        when {
            state.isFinished -> finishedBackground
            state.isRunning -> runningBackground
            state.isPaused -> pausedBackground
            else -> getIdlePalette(colorTheme).background
        }

    /**
     * 상태에 따른 텍스트 색상 반환
     */
    fun getText(
        state: TimerWidgetState,
        colorTheme: ColorTheme = ColorTheme.DEFAULT,
    ): ColorProvider =
        when {
            state.isFinished -> finishedText
            state.isRunning -> runningText
            state.isPaused -> pausedText
            else -> getIdlePalette(colorTheme).text
        }

    /**
     * 상태에 따른 강조 색상 반환
     */
    fun getAccent(
        state: TimerWidgetState,
        colorTheme: ColorTheme = ColorTheme.DEFAULT,
    ): ColorProvider =
        when {
            state.isFinished -> finishedAccent
            state.isRunning -> runningAccent
            state.isPaused -> pausedAccent
            else -> getIdlePalette(colorTheme).accent
        }
}
