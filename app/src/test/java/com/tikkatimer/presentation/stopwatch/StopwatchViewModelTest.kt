package com.tikkatimer.presentation.stopwatch

import android.content.Context
import com.tikkatimer.domain.model.StopwatchState
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * StopwatchViewModel 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StopwatchViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: StopwatchViewModel
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockContext = mockk(relaxed = true)
        viewModel = StopwatchViewModel(mockContext)
    }

    @After
    fun tearDown() {
        viewModel.reset() // 타이머 정리
        Dispatchers.resetMain()
    }

    @Test
    fun `초기 상태는 IDLE이다`() {
        val state = viewModel.uiState.value

        assertEquals(StopwatchState.IDLE, state.stopwatch.state)
        assertEquals(0L, state.stopwatch.elapsedMillis)
        assertTrue(state.stopwatch.lapTimes.isEmpty())
    }

    @Test
    fun `start 호출 시 상태가 RUNNING으로 변경된다`() {
        viewModel.start()

        assertEquals(StopwatchState.RUNNING, viewModel.uiState.value.stopwatch.state)
        viewModel.pause() // 타이머 정지
    }

    @Test
    fun `pause 호출 시 상태가 PAUSED로 변경된다`() {
        viewModel.start()
        viewModel.pause()

        assertEquals(StopwatchState.PAUSED, viewModel.uiState.value.stopwatch.state)
    }

    @Test
    fun `reset 호출 시 초기 상태로 돌아간다`() {
        viewModel.start()
        viewModel.pause()
        viewModel.reset()

        val state = viewModel.uiState.value
        assertEquals(StopwatchState.IDLE, state.stopwatch.state)
        assertEquals(0L, state.stopwatch.elapsedMillis)
        assertTrue(state.stopwatch.lapTimes.isEmpty())
    }

    @Test
    fun `IDLE 상태에서 pause 호출해도 상태가 변경되지 않는다`() {
        viewModel.pause()

        assertEquals(StopwatchState.IDLE, viewModel.uiState.value.stopwatch.state)
    }

    @Test
    fun `RUNNING 상태가 아닐 때 recordLap 호출해도 랩이 기록되지 않는다`() {
        viewModel.recordLap()

        assertTrue(viewModel.uiState.value.stopwatch.lapTimes.isEmpty())
    }

    @Test
    fun `RUNNING 상태에서 recordLap 호출 시 랩이 기록된다`() {
        viewModel.start()
        viewModel.recordLap()

        assertEquals(1, viewModel.uiState.value.stopwatch.lapTimes.size)
        assertEquals(1, viewModel.uiState.value.stopwatch.lapTimes.first().lapNumber)
        viewModel.pause()
    }

    @Test
    fun `여러 번 recordLap 호출 시 랩 번호가 순차적으로 증가한다`() {
        viewModel.start()

        viewModel.recordLap()
        viewModel.recordLap()
        viewModel.recordLap()

        val lapTimes = viewModel.uiState.value.stopwatch.lapTimes
        assertEquals(3, lapTimes.size)
        // 최신 랩이 맨 위에 있으므로 역순
        assertEquals(3, lapTimes[0].lapNumber)
        assertEquals(2, lapTimes[1].lapNumber)
        assertEquals(1, lapTimes[2].lapNumber)
        viewModel.pause()
    }

    @Test
    fun `pause 후 start 호출 시 RUNNING 상태가 된다`() {
        viewModel.start()
        viewModel.pause()
        viewModel.start()

        assertEquals(StopwatchState.RUNNING, viewModel.uiState.value.stopwatch.state)
        viewModel.pause()
    }

    @Test
    fun `이미 RUNNING 상태에서 start 호출해도 무시된다`() {
        viewModel.start()
        viewModel.start() // 중복 호출

        assertEquals(StopwatchState.RUNNING, viewModel.uiState.value.stopwatch.state)
        viewModel.pause()
    }
}
