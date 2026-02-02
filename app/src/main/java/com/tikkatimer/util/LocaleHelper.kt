package com.tikkatimer.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.tikkatimer.domain.model.AppLanguage
import java.util.Locale

/**
 * 앱 언어 변경 유틸리티
 * AppCompatDelegate를 사용하여 모든 Android 버전에서 일관된 동작 보장
 * 언어 변경 시 Activity가 자동으로 재생성됨
 */
object LocaleHelper {
    /**
     * 앱 언어 설정
     * AppCompatDelegate.setApplicationLocales()를 사용하여
     * Activity 재생성과 함께 언어 변경을 적용
     */
    fun setLocale(
        context: Context,
        language: AppLanguage,
    ) {
        val localeList =
            if (language == AppLanguage.SYSTEM) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(language.locale)
            }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    /**
     * 현재 앱 언어 가져오기
     */
    fun getCurrentLocale(context: Context): Locale {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) {
            locales.get(0) ?: Locale.getDefault()
        } else {
            Locale.getDefault()
        }
    }
}
