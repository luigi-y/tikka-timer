package com.tikkatimer.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 위젯에 표시할 타이머 상태
 */
data class TimerWidgetState(
    /** 타이머 실행 중 여부 */
    val isRunning: Boolean = false,
    /** 타이머 일시정지 여부 */
    val isPaused: Boolean = false,
    /** 남은 시간 (밀리초) */
    val remainingMillis: Long = 0L,
    /** 전체 시간 (밀리초) */
    val totalMillis: Long = 0L,
    /** 타이머 이름 */
    val timerName: String = "",
    /** 타이머 인스턴스 ID */
    val timerId: String = "",
    /** 마지막 업데이트 시각 */
    val lastUpdatedAt: Long = 0L,
) {
    /** 타이머가 없거나 종료된 상태 */
    val isEmpty: Boolean
        get() = !isRunning && !isPaused && remainingMillis == 0L

    /** 진행률 (0.0 ~ 1.0) */
    val progress: Float
        get() = if (totalMillis > 0) remainingMillis.toFloat() / totalMillis else 0f

    /** 포맷된 남은 시간 (MM:SS 또는 HH:MM:SS) */
    val formattedTime: String
        get() {
            val totalSeconds = remainingMillis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    /** 짧은 포맷의 남은 시간 (소형 위젯용, M:SS) */
    val formattedTimeShort: String
        get() {
            val totalSeconds = remainingMillis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }

    companion object {
        val EMPTY = TimerWidgetState()
    }
}

/**
 * 위젯 상태 관리를 위한 DataStore
 */
private val Context.timerWidgetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timer_widget_state",
)

/**
 * 위젯 상태 저장소
 * 타이머 상태를 DataStore에 저장하고 위젯에서 읽을 수 있도록 함
 */
object TimerWidgetStateManager {
    private val IS_RUNNING = booleanPreferencesKey("is_running")
    private val IS_PAUSED = booleanPreferencesKey("is_paused")
    private val REMAINING_MILLIS = longPreferencesKey("remaining_millis")
    private val TOTAL_MILLIS = longPreferencesKey("total_millis")
    private val TIMER_NAME = stringPreferencesKey("timer_name")
    private val TIMER_ID = stringPreferencesKey("timer_id")
    private val LAST_UPDATED_AT = longPreferencesKey("last_updated_at")

    /**
     * 위젯 상태 Flow
     */
    fun getState(context: Context): Flow<TimerWidgetState> =
        context.timerWidgetDataStore.data.map { prefs ->
            TimerWidgetState(
                isRunning = prefs[IS_RUNNING] ?: false,
                isPaused = prefs[IS_PAUSED] ?: false,
                remainingMillis = prefs[REMAINING_MILLIS] ?: 0L,
                totalMillis = prefs[TOTAL_MILLIS] ?: 0L,
                timerName = prefs[TIMER_NAME] ?: "",
                timerId = prefs[TIMER_ID] ?: "",
                lastUpdatedAt = prefs[LAST_UPDATED_AT] ?: 0L,
            )
        }

    /**
     * 타이머 시작 상태 저장
     */
    suspend fun setRunning(
        context: Context,
        timerId: String,
        timerName: String,
        remainingMillis: Long,
        totalMillis: Long,
    ) {
        context.timerWidgetDataStore.edit { prefs ->
            prefs[IS_RUNNING] = true
            prefs[IS_PAUSED] = false
            prefs[REMAINING_MILLIS] = remainingMillis
            prefs[TOTAL_MILLIS] = totalMillis
            prefs[TIMER_NAME] = timerName
            prefs[TIMER_ID] = timerId
            prefs[LAST_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /**
     * 타이머 일시정지 상태 저장
     */
    suspend fun setPaused(
        context: Context,
        remainingMillis: Long,
    ) {
        context.timerWidgetDataStore.edit { prefs ->
            prefs[IS_RUNNING] = false
            prefs[IS_PAUSED] = true
            prefs[REMAINING_MILLIS] = remainingMillis
            prefs[LAST_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /**
     * 남은 시간 업데이트
     */
    suspend fun updateRemainingTime(
        context: Context,
        remainingMillis: Long,
    ) {
        context.timerWidgetDataStore.edit { prefs ->
            prefs[REMAINING_MILLIS] = remainingMillis
            prefs[LAST_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /**
     * 타이머 종료/초기화
     */
    suspend fun clear(context: Context) {
        context.timerWidgetDataStore.edit { prefs ->
            prefs[IS_RUNNING] = false
            prefs[IS_PAUSED] = false
            prefs[REMAINING_MILLIS] = 0L
            prefs[TOTAL_MILLIS] = 0L
            prefs[TIMER_NAME] = ""
            prefs[TIMER_ID] = ""
            prefs[LAST_UPDATED_AT] = System.currentTimeMillis()
        }
    }
}
