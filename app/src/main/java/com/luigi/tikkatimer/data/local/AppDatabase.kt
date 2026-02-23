package com.luigi.tikkatimer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.luigi.tikkatimer.data.local.dao.AlarmDao
import com.luigi.tikkatimer.data.local.dao.RunningTimerDao
import com.luigi.tikkatimer.data.local.dao.TimerPresetDao
import com.luigi.tikkatimer.data.local.entity.AlarmEntity
import com.luigi.tikkatimer.data.local.entity.RunningTimerEntity
import com.luigi.tikkatimer.data.local.entity.TimerPresetEntity

/**
 * Tikka Timer 앱의 Room 데이터베이스
 * Entity 클래스로부터 테이블이 자동 생성되며,
 * 스키마 이력은 app/schemas/ 에 JSON으로 자동 기록됨
 */
@Database(
    entities = [
        AlarmEntity::class,
        TimerPresetEntity::class,
        RunningTimerEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    abstract fun timerPresetDao(): TimerPresetDao

    abstract fun runningTimerDao(): RunningTimerDao

    companion object {
        const val DATABASE_NAME = "tikka_timer_db"
    }
}
