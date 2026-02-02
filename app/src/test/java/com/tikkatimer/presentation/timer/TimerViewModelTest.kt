package com.tikkatimer.presentation.timer

import android.content.Context
import com.tikkatimer.domain.model.TimerPreset
import com.tikkatimer.domain.model.TimerState
import com.tikkatimer.domain.usecase.timer.DeleteTimerPresetUseCase
import com.tikkatimer.domain.usecase.timer.GetTimerPresetsUseCase
import com.tikkatimer.domain.usecase.timer.SaveTimerPresetUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TimerViewModel 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: TimerViewModel
    private lateinit var mockContext: Context
    private lateinit var getTimerPresetsUseCase: GetTimerPresetsUseCase
    private lateinit var saveTimerPresetUseCase: SaveTimerPresetUseCase
    private lateinit var deleteTimerPresetUseCase: DeleteTimerPresetUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockContext = mockk(relaxed = true)

        getTimerPresetsUseCase =
            mockk {
                every { this@mockk() } returns flowOf(emptyList())
            }
        saveTimerPresetUseCase =
            mockk {
                coEvery { this@mockk(any()) } returns 1L
            }
        deleteTimerPresetUseCase =
            mockk {
                coEvery { this@mockk(any()) } returns Unit
            }

        viewModel =
            TimerViewModel(
                mockContext,
                getTimerPresetsUseCase,
                saveTimerPresetUseCase,
                deleteTimerPresetUseCase,
            )
    }

    @After
    fun tearDown() {
        viewModel.cancel() // 타이머 정리
        Dispatchers.resetMain()
    }

    @Test
    fun `초기 상태는 runningTimers가 비어있다`() {
        val state = viewModel.uiState.value

        assertTrue(state.runningTimers.isEmpty())
    }

    @Test
    fun `setTime 호출 시 입력 시간이 설정된다`() {
        viewModel.setTime(1, 30, 0)

        val state = viewModel.uiState.value
        assertEquals(1, state.inputHours)
        assertEquals(30, state.inputMinutes)
        assertEquals(0, state.inputSeconds)
    }

    @Test
    fun `시간 0으로 설정된 상태에서 startNewTimer 호출해도 무시된다`() {
        viewModel.setTime(0, 0, 0)
        viewModel.startNewTimer()

        assertTrue(viewModel.uiState.value.runningTimers.isEmpty())
    }

    @Test
    fun `시간 설정 후 startNewTimer 호출 시 runningTimers에 타이머가 추가된다`() {
        viewModel.setTime(0, 5, 0)
        viewModel.startNewTimer()

        val state = viewModel.uiState.value
        assertEquals(1, state.runningTimers.size)
        assertEquals(TimerState.RUNNING, state.runningTimers[0].state)

        // 정리
        viewModel.removeTimer(state.runningTimers[0].instanceId)
    }

    @Test
    fun `RUNNING 상태에서 pauseTimer 호출 시 PAUSED 상태가 된다`() {
        viewModel.setTime(0, 5, 0)
        viewModel.startNewTimer()

        val instanceId = viewModel.uiState.value.runningTimers[0].instanceId
        viewModel.pauseTimer(instanceId)

        assertEquals(TimerState.PAUSED, viewModel.uiState.value.runningTimers[0].state)
    }

    @Test
    fun `resetTimer 호출 시 원래 시간으로 복원된다`() {
        viewModel.setTime(0, 5, 0) // 5분 = 300000ms
        viewModel.startNewTimer()

        val instanceId = viewModel.uiState.value.runningTimers[0].instanceId
        viewModel.pauseTimer(instanceId)
        viewModel.resetTimer(instanceId)

        val timer = viewModel.uiState.value.runningTimers[0]
        assertEquals(TimerState.IDLE, timer.state)
        assertEquals(300000L, timer.remainingMillis)
        assertEquals(300000L, timer.totalDurationMillis)
    }

    @Test
    fun `removeTimer 호출 시 타이머가 목록에서 제거된다`() {
        viewModel.setTime(0, 5, 0)
        viewModel.startNewTimer()

        val instanceId = viewModel.uiState.value.runningTimers[0].instanceId
        viewModel.removeTimer(instanceId)

        assertTrue(viewModel.uiState.value.runningTimers.isEmpty())
    }

    @Test
    fun `addOneMinute 호출 시 1분이 추가된다`() {
        viewModel.setTime(0, 5, 0) // 5분 = 300000ms
        viewModel.startNewTimer()

        val instanceId = viewModel.uiState.value.runningTimers[0].instanceId
        viewModel.pauseTimer(instanceId) // 일시정지 (시간 고정)
        viewModel.addOneMinute(instanceId)

        val timer = viewModel.uiState.value.runningTimers[0]
        assertEquals(360000L, timer.totalDurationMillis) // 6분
    }

    @Test
    fun `프리셋에서 타이머 시작 시 runningTimers에 추가된다`() {
        // 3분 = 180초
        val preset =
            TimerPreset(
                id = 1,
                name = "테스트 프리셋",
                durationSeconds = 180,
            )

        viewModel.startTimerFromPreset(preset)

        val state = viewModel.uiState.value
        assertEquals(1, state.runningTimers.size)
        assertEquals(180000L, state.runningTimers[0].totalDurationMillis)
        assertEquals(TimerState.RUNNING, state.runningTimers[0].state)

        // 정리
        viewModel.removeTimer(state.runningTimers[0].instanceId)
    }

    @Test
    fun `PAUSED 상태에서 resumeTimer 호출 시 RUNNING 상태로 변경된다`() {
        viewModel.setTime(0, 5, 0)
        viewModel.startNewTimer()

        val instanceId = viewModel.uiState.value.runningTimers[0].instanceId
        viewModel.pauseTimer(instanceId)
        viewModel.resumeTimer(instanceId)

        assertEquals(TimerState.RUNNING, viewModel.uiState.value.runningTimers[0].state)

        // 정리
        viewModel.pauseTimer(instanceId)
    }

    @Test
    fun `여러 타이머를 동시에 실행할 수 있다`() {
        viewModel.setTime(0, 3, 0)
        viewModel.startNewTimer()

        viewModel.setTime(0, 5, 0)
        viewModel.startNewTimer()

        val state = viewModel.uiState.value
        assertEquals(2, state.runningTimers.size)
        assertTrue(state.runningTimers.all { it.state == TimerState.RUNNING })

        // 정리
        state.runningTimers.forEach { viewModel.removeTimer(it.instanceId) }
    }

    @Test
    fun `프리셋 로드 시 UseCase가 호출된다`() =
        runTest {
            val presets =
                listOf(
                    TimerPreset(id = 1, name = "3분", durationSeconds = 180),
                    TimerPreset(id = 2, name = "5분", durationSeconds = 300),
                )

            every { getTimerPresetsUseCase() } returns flowOf(presets)

            val newViewModel =
                TimerViewModel(
                    mockContext,
                    getTimerPresetsUseCase,
                    saveTimerPresetUseCase,
                    deleteTimerPresetUseCase,
                )

            assertEquals(2, newViewModel.uiState.value.presets.size)
            newViewModel.cancel()
        }
}
