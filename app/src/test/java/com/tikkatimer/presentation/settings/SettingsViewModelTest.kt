package com.tikkatimer.presentation.settings

import com.tikkatimer.data.local.SettingsDataStore
import com.tikkatimer.domain.model.AppLanguage
import com.tikkatimer.domain.model.AppSettings
import com.tikkatimer.domain.model.ColorTheme
import com.tikkatimer.domain.model.ThemeMode
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
import org.junit.Before
import org.junit.Test

/**
 * SettingsViewModel 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        settingsDataStore =
            mockk {
                every { settingsFlow } returns flowOf(AppSettings.DEFAULT)
                coEvery { setThemeMode(any()) } returns Unit
                coEvery { setColorTheme(any()) } returns Unit
                coEvery { setLanguage(any()) } returns Unit
                coEvery { resetSettings() } returns Unit
            }

        viewModel = SettingsViewModel(settingsDataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `초기 설정값이 DEFAULT로 설정된다`() =
        runTest {
            assertEquals(AppSettings.DEFAULT, viewModel.settings.value)
        }

    @Test
    fun `setThemeMode 호출 시 DataStore의 setThemeMode가 호출된다`() =
        runTest {
            viewModel.setThemeMode(ThemeMode.DARK)

            coVerify { settingsDataStore.setThemeMode(ThemeMode.DARK) }
        }

    @Test
    fun `setColorTheme 호출 시 DataStore의 setColorTheme가 호출된다`() =
        runTest {
            viewModel.setColorTheme(ColorTheme.OCEAN)

            coVerify { settingsDataStore.setColorTheme(ColorTheme.OCEAN) }
        }

    @Test
    fun `setLanguage 호출 시 DataStore의 setLanguage가 호출된다`() =
        runTest {
            viewModel.setLanguage(AppLanguage.ENGLISH)

            coVerify { settingsDataStore.setLanguage(AppLanguage.ENGLISH) }
        }

    @Test
    fun `resetSettings 호출 시 DataStore의 resetSettings가 호출된다`() =
        runTest {
            viewModel.resetSettings()

            coVerify { settingsDataStore.resetSettings() }
        }

    @Test
    fun `다크 모드 설정 변경`() =
        runTest {
            viewModel.setThemeMode(ThemeMode.DARK)

            coVerify { settingsDataStore.setThemeMode(ThemeMode.DARK) }
        }

    @Test
    fun `라이트 모드 설정 변경`() =
        runTest {
            viewModel.setThemeMode(ThemeMode.LIGHT)

            coVerify { settingsDataStore.setThemeMode(ThemeMode.LIGHT) }
        }

    @Test
    fun `시스템 테마 모드 설정 변경`() =
        runTest {
            viewModel.setThemeMode(ThemeMode.SYSTEM)

            coVerify { settingsDataStore.setThemeMode(ThemeMode.SYSTEM) }
        }

    @Test
    fun `모든 컬러 테마 설정 가능`() =
        runTest {
            ColorTheme.entries.forEach { colorTheme ->
                viewModel.setColorTheme(colorTheme)
                coVerify { settingsDataStore.setColorTheme(colorTheme) }
            }
        }

    @Test
    fun `모든 언어 설정 가능`() =
        runTest {
            AppLanguage.entries.forEach { language ->
                viewModel.setLanguage(language)
                coVerify { settingsDataStore.setLanguage(language) }
            }
        }
}
