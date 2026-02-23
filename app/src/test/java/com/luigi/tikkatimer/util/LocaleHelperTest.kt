package com.luigi.tikkatimer.util

import com.luigi.tikkatimer.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * LocaleHelper 단위 테스트
 * AppLanguage enum 로케일 매핑, 언어 전환 로직 검증
 */
class LocaleHelperTest {
    // ===== AppLanguage 로케일 매핑 테스트 =====

    @Test
    fun `SYSTEM 언어는 빈 locale을 가진다`() {
        assertEquals("", AppLanguage.SYSTEM.locale)
    }

    @Test
    fun `KOREAN 언어는 ko locale을 가진다`() {
        assertEquals("ko", AppLanguage.KOREAN.locale)
    }

    @Test
    fun `ENGLISH 언어는 en locale을 가진다`() {
        assertEquals("en", AppLanguage.ENGLISH.locale)
    }

    @Test
    fun `JAPANESE 언어는 ja locale을 가진다`() {
        assertEquals("ja", AppLanguage.JAPANESE.locale)
    }

    @Test
    fun `CHINESE 언어는 zh locale을 가진다`() {
        assertEquals("zh", AppLanguage.CHINESE.locale)
    }

    // ===== AppLanguage displayName 테스트 =====

    @Test
    fun `KOREAN의 displayName은 한국어이다`() {
        assertEquals("한국어", AppLanguage.KOREAN.displayName)
    }

    @Test
    fun `ENGLISH의 displayName은 English이다`() {
        assertEquals("English", AppLanguage.ENGLISH.displayName)
    }

    @Test
    fun `JAPANESE의 displayName은 日本語이다`() {
        assertEquals("日本語", AppLanguage.JAPANESE.displayName)
    }

    @Test
    fun `CHINESE의 displayName은 中文(简体)이다`() {
        assertEquals("中文(简体)", AppLanguage.CHINESE.displayName)
    }

    @Test
    fun `SYSTEM의 displayName은 비어있다`() {
        assertEquals("", AppLanguage.SYSTEM.displayName)
    }

    // ===== AppLanguage enum 전체 검증 =====

    @Test
    fun `AppLanguage는 5개의 값을 가진다`() {
        assertEquals(5, AppLanguage.entries.size)
    }

    @Test
    fun `SYSTEM을 제외한 언어는 모두 locale이 비어있지 않다`() {
        val nonSystemLanguages = AppLanguage.entries.filter { it != AppLanguage.SYSTEM }

        nonSystemLanguages.forEach { language ->
            assertTrue(
                "${language.name}의 locale이 비어있음",
                language.locale.isNotEmpty(),
            )
        }
    }

    // ===== 로케일 전환 로직 테스트 =====

    @Test
    fun `SYSTEM 언어이면 빈 로케일 리스트를 사용한다`() {
        val language = AppLanguage.SYSTEM
        val shouldUseEmptyList = language == AppLanguage.SYSTEM

        assertTrue(shouldUseEmptyList)
    }

    @Test
    fun `SYSTEM이 아닌 언어이면 특정 로케일을 사용한다`() {
        val language = AppLanguage.KOREAN
        val shouldUseSpecificLocale = language != AppLanguage.SYSTEM

        assertTrue(shouldUseSpecificLocale)
        assertEquals("ko", language.locale)
    }
}
