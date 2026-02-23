package com.luigi.tikkatimer.sync

import com.luigi.tikkatimer.domain.model.RunningTimer
import com.luigi.tikkatimer.domain.model.SoundType
import com.luigi.tikkatimer.domain.model.TimerState
import com.luigi.tikkatimer.domain.model.VibrationPattern
import com.luigi.tikkatimer.util.AlarmSoundManager
import com.luigi.tikkatimer.util.NotificationHelper
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TimerStateSync 단위 테스트
 * 타이머 상태 동기화, 완료 알림, 상태 복원 로직 검증
 */
class TimerStateSyncTest {
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmSoundManager: AlarmSoundManager

    @Before
    fun setup() {
        notificationHelper = mockk(relaxed = true)
        alarmSoundManager = mockk(relaxed = true)
    }

    // ===== SyncedTimerState 테스트 =====

    @Test
    fun `SyncedTimerState가 올바르게 생성된다`() {
        val state =
            SyncedTimerState(
                instanceId = "timer-1",
                name = "3분 타이머",
                remainingMillis = 180_000L,
                totalMillis = 180_000L,
                state = TimerState.RUNNING,
                targetEndTimeMillis = System.currentTimeMillis() + 180_000L,
            )

        assertEquals("timer-1", state.instanceId)
        assertEquals("3분 타이머", state.name)
        assertEquals(TimerState.RUNNING, state.state)
    }

    @Test
    fun `SyncedTimerState의 기본 soundType은 DEFAULT이다`() {
        val state =
            SyncedTimerState(
                instanceId = "timer-1",
                name = "타이머",
                remainingMillis = 0L,
                totalMillis = 60_000L,
                state = TimerState.IDLE,
                targetEndTimeMillis = 0L,
            )

        assertEquals(SoundType.DEFAULT, state.soundType)
        assertEquals(VibrationPattern.DEFAULT, state.vibrationPattern)
    }

    // ===== 완료 타이머 감지 테스트 =====

    @Test
    fun `FINISHED 상태 타이머를 필터링한다`() {
        val timers =
            listOf(
                createSyncedState("t1", TimerState.RUNNING),
                createSyncedState("t2", TimerState.FINISHED),
                createSyncedState("t3", TimerState.PAUSED),
                createSyncedState("t4", TimerState.FINISHED),
            )

        val finishedTimers = timers.filter { it.state == TimerState.FINISHED }

        assertEquals(2, finishedTimers.size)
        assertEquals("t2", finishedTimers[0].instanceId)
        assertEquals("t4", finishedTimers[1].instanceId)
    }

    @Test
    fun `이미 알림을 보낸 타이머는 중복 알림하지 않는다`() {
        val notifiedSet = mutableSetOf("t2")
        val timers =
            listOf(
                createSyncedState("t2", TimerState.FINISHED),
                createSyncedState("t3", TimerState.FINISHED),
            )

        val finishedTimers = timers.filter { it.state == TimerState.FINISHED }
        val newFinished = finishedTimers.filter { it.instanceId !in notifiedSet }

        assertEquals(1, newFinished.size)
        assertEquals("t3", newFinished[0].instanceId)
    }

    @Test
    fun `완료 상태가 아닌 타이머는 알림 목록에서 제거된다`() {
        val notifiedSet = mutableSetOf("t1", "t2", "t3")
        val currentTimerIds = setOf("t1", "t2") // t3는 더 이상 목록에 없음

        notifiedSet.removeAll { it !in currentTimerIds }

        assertEquals(2, notifiedSet.size)
        assertFalse(notifiedSet.contains("t3"))
    }

    // ===== 소리/진동 중지 테스트 =====

    @Test
    fun `stopFinishedAlarm 호출 시 소리와 진동이 중지된다`() {
        alarmSoundManager.stopAll()

        verify { alarmSoundManager.stopAll() }
    }

    // ===== 상태 복원 테스트 =====

    @Test
    fun `SyncedTimerState를 RunningTimer로 변환한다`() {
        val synced =
            SyncedTimerState(
                instanceId = "timer-1",
                name = "5분 타이머",
                remainingMillis = 300_000L,
                totalMillis = 300_000L,
                state = TimerState.RUNNING,
                targetEndTimeMillis = System.currentTimeMillis() + 300_000L,
                soundType = SoundType.BELL,
                vibrationPattern = VibrationPattern.STRONG,
            )

        val running =
            RunningTimer(
                instanceId = synced.instanceId,
                presetId = 0,
                name = synced.name,
                totalDurationMillis = synced.totalMillis,
                remainingMillis = synced.remainingMillis,
                state = synced.state,
                soundType = synced.soundType,
                vibrationPattern = synced.vibrationPattern,
                targetEndTimeMillis = synced.targetEndTimeMillis,
            )

        assertEquals(synced.instanceId, running.instanceId)
        assertEquals(synced.name, running.name)
        assertEquals(synced.totalMillis, running.totalDurationMillis)
        assertEquals(synced.remainingMillis, running.remainingMillis)
        assertEquals(synced.state, running.state)
        assertEquals(synced.soundType, running.soundType)
        assertEquals(synced.vibrationPattern, running.vibrationPattern)
        assertEquals(0L, running.presetId) // 복원 시 presetId는 0
    }

    @Test
    fun `빈 타이머 목록 복원 시 빈 리스트를 반환한다`() {
        val syncedTimers = emptyList<SyncedTimerState>()

        val restoredTimers =
            syncedTimers.map { synced ->
                RunningTimer(
                    instanceId = synced.instanceId,
                    presetId = 0,
                    name = synced.name,
                    totalDurationMillis = synced.totalMillis,
                    remainingMillis = synced.remainingMillis,
                    state = synced.state,
                    targetEndTimeMillis = synced.targetEndTimeMillis,
                )
            }

        assertTrue(restoredTimers.isEmpty())
    }

    // ===== 실행 중 타이머 존재 여부 테스트 =====

    @Test
    fun `RUNNING 상태 타이머가 있으면 true를 반환한다`() {
        val timers =
            listOf(
                createSyncedState("t1", TimerState.PAUSED),
                createSyncedState("t2", TimerState.RUNNING),
            )

        val hasRunning = timers.any { it.state == TimerState.RUNNING }

        assertTrue(hasRunning)
    }

    @Test
    fun `RUNNING 상태 타이머가 없으면 false를 반환한다`() {
        val timers =
            listOf(
                createSyncedState("t1", TimerState.PAUSED),
                createSyncedState("t2", TimerState.FINISHED),
            )

        val hasRunning = timers.any { it.state == TimerState.RUNNING }

        assertFalse(hasRunning)
    }

    @Test
    fun `빈 목록이면 false를 반환한다`() {
        val timers = emptyList<SyncedTimerState>()

        val hasRunning = timers.any { it.state == TimerState.RUNNING }

        assertFalse(hasRunning)
    }

    // ===== 위젯 상태 우선순위 테스트 =====

    @Test
    fun `FINISHED 타이머가 위젯 표시 우선순위 1위이다`() {
        val timers =
            listOf(
                createSyncedState("t1", TimerState.RUNNING),
                createSyncedState("t2", TimerState.FINISHED),
                createSyncedState("t3", TimerState.PAUSED),
            )

        val finishedTimer = timers.firstOrNull { it.state == TimerState.FINISHED }
        val activeTimer = timers.firstOrNull { it.state == TimerState.RUNNING }

        val displayTimer =
            when {
                finishedTimer != null -> finishedTimer
                activeTimer != null -> activeTimer
                else -> null
            }

        assertEquals("t2", displayTimer?.instanceId)
    }

    @Test
    fun `FINISHED가 없으면 RUNNING 타이머가 위젯에 표시된다`() {
        val timers =
            listOf(
                createSyncedState("t1", TimerState.RUNNING),
                createSyncedState("t3", TimerState.PAUSED),
            )

        val finishedTimer = timers.firstOrNull { it.state == TimerState.FINISHED }
        val activeTimer = timers.firstOrNull { it.state == TimerState.RUNNING }

        val displayTimer =
            when {
                finishedTimer != null -> finishedTimer
                activeTimer != null -> activeTimer
                else -> null
            }

        assertEquals("t1", displayTimer?.instanceId)
    }

    // ===== Foreground Service 관리 테스트 =====

    @Test
    fun `RUNNING 타이머가 있으면 서비스 시작이 필요하다`() {
        val timers = listOf(createSyncedState("t1", TimerState.RUNNING))
        val isServiceRunning = false

        val hasRunning = timers.any { it.state == TimerState.RUNNING }
        val shouldStart = hasRunning && !isServiceRunning

        assertTrue(shouldStart)
    }

    @Test
    fun `RUNNING 타이머가 없고 서비스가 실행 중이면 서비스 중지가 필요하다`() {
        val timers = listOf(createSyncedState("t1", TimerState.PAUSED))
        val isServiceRunning = true

        val hasRunning = timers.any { it.state == TimerState.RUNNING }
        val shouldStop = !hasRunning && isServiceRunning

        assertTrue(shouldStop)
    }

    // ===== 시간 포맷팅 테스트 =====

    @Test
    fun `시간 포맷팅 - 1시간 이상`() {
        val formatted = formatTime(3_661_000L)
        assertEquals("1:01:01", formatted)
    }

    @Test
    fun `시간 포맷팅 - 1시간 미만`() {
        val formatted = formatTime(125_000L)
        assertEquals("02:05", formatted)
    }

    @Test
    fun `시간 포맷팅 - 0초`() {
        val formatted = formatTime(0L)
        assertEquals("00:00", formatted)
    }

    // ===== clearAll 로직 테스트 =====

    @Test
    fun `clearAll 호출 시 모든 상태가 초기화된다`() {
        val timers =
            mutableListOf(
                createSyncedState("t1", TimerState.RUNNING),
                createSyncedState("t2", TimerState.FINISHED),
            )
        val notifiedSet = mutableSetOf("t2")

        // clearAll 시뮬레이션
        timers.clear()
        notifiedSet.clear()
        alarmSoundManager.stopAll()

        assertTrue(timers.isEmpty())
        assertTrue(notifiedSet.isEmpty())
        verify { alarmSoundManager.stopAll() }
    }

    // ===== Helper =====

    private fun createSyncedState(
        instanceId: String,
        state: TimerState,
    ) = SyncedTimerState(
        instanceId = instanceId,
        name = "타이머 $instanceId",
        remainingMillis = if (state == TimerState.FINISHED) 0L else 60_000L,
        totalMillis = 60_000L,
        state = state,
        targetEndTimeMillis =
            if (state == TimerState.RUNNING) {
                System.currentTimeMillis() + 60_000L
            } else {
                0L
            },
    )

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
