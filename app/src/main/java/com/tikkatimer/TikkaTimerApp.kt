package com.tikkatimer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Tikka Timer 애플리케이션 클래스
 * Hilt 의존성 주입을 위한 진입점
 */
@HiltAndroidApp
class TikkaTimerApp : Application()
