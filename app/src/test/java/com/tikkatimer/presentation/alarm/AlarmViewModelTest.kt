package com.tikkatimer.presentation.alarm

import com.tikkatimer.domain.model.Alarm
import com.tikkatimer.domain.model.UpcomingInfo
import com.tikkatimer.domain.usecase.alarm.AddAlarmUseCase
import com.tikkatimer.domain.usecase.alarm.DeleteAlarmUseCase
import com.tikkatimer.domain.usecase.alarm.GetAlarmsUseCase
import com.tikkatimer.domain.usecase.alarm.GetUpcomingAlarmUseCase
import com.tikkatimer.domain.usecase.alarm.ToggleAlarmUseCase
import com.tikkatimer.domain.usecase.alarm.UpdateAlarmUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * AlarmViewModel 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AlarmViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: AlarmViewModel
    private lateinit var getAlarmsUseCase: GetAlarmsUseCase
    private lateinit var addAlarmUseCase: AddAlarmUseCase
    private lateinit var updateAlarmUseCase: UpdateAlarmUseCase
    private lateinit var deleteAlarmUseCase: DeleteAlarmUseCase
    private lateinit var toggleAlarmUseCase: ToggleAlarmUseCase
    private lateinit var getUpcomingAlarmUseCase: GetUpcomingAlarmUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getAlarmsUseCase =
            mockk {
                every { this@mockk() } returns flowOf(emptyList())
            }
        addAlarmUseCase =
            mockk {
                coEvery { this@mockk(any()) } returns 1L
            }
        updateAlarmUseCase =
            mockk {
                coEvery { this@mockk(any()) } returns Unit
            }
        deleteAlarmUseCase =
            mockk {
                coEvery { this@mockk(any()) } returns Unit
            }
        toggleAlarmUseCase =
            mockk {
                coEvery { this@mockk(any(), any()) } returns Unit
            }
        getUpcomingAlarmUseCase =
            mockk {
                every { this@mockk() } returns flowOf(UpcomingInfo.EMPTY)
            }

        viewModel =
            AlarmViewModel(
                getAlarmsUseCase,
                addAlarmUseCase,
                updateAlarmUseCase,
                deleteAlarmUseCase,
                toggleAlarmUseCase,
                getUpcomingAlarmUseCase,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `알람 로드 완료 후 로딩이 false가 된다`() {
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `showAddAlarmDialog 호출 시 Add 다이얼로그 상태가 설정된다`() {
        viewModel.showAddAlarmDialog()

        val dialogState = viewModel.uiState.value.dialogState
        assertNotNull(dialogState)
        assertTrue(dialogState is AlarmDialogState.Add)
    }

    @Test
    fun `showEditAlarmDialog 호출 시 Edit 다이얼로그 상태가 설정된다`() {
        val alarm = createTestAlarm()

        viewModel.showEditAlarmDialog(alarm)

        val dialogState = viewModel.uiState.value.dialogState
        assertNotNull(dialogState)
        assertTrue(dialogState is AlarmDialogState.Edit)
        assertEquals(alarm, (dialogState as AlarmDialogState.Edit).alarm)
    }

    @Test
    fun `dismissDialog 호출 시 다이얼로그 상태가 null이 된다`() {
        viewModel.showAddAlarmDialog()
        viewModel.dismissDialog()

        assertNull(viewModel.uiState.value.dialogState)
    }

    @Test
    fun `updateDialogTime 호출 시 다이얼로그의 시간이 변경된다`() {
        viewModel.showAddAlarmDialog()
        val newTime = LocalTime.of(9, 30)

        viewModel.updateDialogTime(newTime)

        val dialogState = viewModel.uiState.value.dialogState as AlarmDialogState.Add
        assertEquals(newTime, dialogState.time)
    }

    @Test
    fun `updateDialogLabel 호출 시 다이얼로그의 라벨이 변경된다`() {
        viewModel.showAddAlarmDialog()

        viewModel.updateDialogLabel("테스트 라벨")

        val dialogState = viewModel.uiState.value.dialogState as AlarmDialogState.Add
        assertEquals("테스트 라벨", dialogState.label)
    }

    @Test
    fun `toggleDialogRepeatDay 호출 시 반복 요일이 토글된다`() {
        viewModel.showAddAlarmDialog()

        viewModel.toggleDialogRepeatDay(DayOfWeek.MONDAY)

        var dialogState = viewModel.uiState.value.dialogState as AlarmDialogState.Add
        assertTrue(DayOfWeek.MONDAY in dialogState.repeatDays)

        viewModel.toggleDialogRepeatDay(DayOfWeek.MONDAY)

        dialogState = viewModel.uiState.value.dialogState as AlarmDialogState.Add
        assertTrue(DayOfWeek.MONDAY !in dialogState.repeatDays)
    }

    @Test
    fun `saveAlarm 호출 시 Add 상태에서 addAlarmUseCase가 호출된다`() =
        runTest {
            viewModel.showAddAlarmDialog()
            viewModel.updateDialogTime(LocalTime.of(7, 0))

            viewModel.saveAlarm()

            coVerify { addAlarmUseCase(any()) }
        }

    @Test
    fun `saveAlarm 호출 시 Edit 상태에서 updateAlarmUseCase가 호출된다`() =
        runTest {
            val alarm = createTestAlarm()
            viewModel.showEditAlarmDialog(alarm)
            viewModel.updateDialogLabel("수정된 라벨")

            viewModel.saveAlarm()

            coVerify { updateAlarmUseCase(any()) }
        }

    @Test
    fun `toggleAlarm 호출 시 toggleAlarmUseCase가 호출된다`() =
        runTest {
            val alarm = createTestAlarm()

            viewModel.toggleAlarm(alarm)

            coVerify { toggleAlarmUseCase(alarm.id, !alarm.isEnabled) }
        }

    @Test
    fun `deleteAlarm 호출 시 deleteAlarmUseCase가 호출된다`() =
        runTest {
            val alarm = createTestAlarm()

            viewModel.deleteAlarm(alarm)

            coVerify { deleteAlarmUseCase(alarm.id) }
        }

    @Test
    fun `알람 목록이 로드되면 uiState에 반영된다`() =
        runTest {
            val alarms =
                listOf(
                    createTestAlarm(id = 1),
                    createTestAlarm(id = 2),
                )

            every { getAlarmsUseCase() } returns flowOf(alarms)

            val newViewModel =
                AlarmViewModel(
                    getAlarmsUseCase,
                    addAlarmUseCase,
                    updateAlarmUseCase,
                    deleteAlarmUseCase,
                    toggleAlarmUseCase,
                    getUpcomingAlarmUseCase,
                )

            assertEquals(2, newViewModel.uiState.value.alarms.size)
        }

    private fun createTestAlarm(id: Long = 1) =
        Alarm(
            id = id,
            time = LocalTime.of(8, 0),
            isEnabled = true,
            label = "테스트 알람",
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        )
}
