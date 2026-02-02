package com.tikkatimer.data.local

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tikkatimer.data.local.dao.AlarmDao
import com.tikkatimer.data.local.dao.TimerPresetDao
import com.tikkatimer.data.local.entity.AlarmEntity
import com.tikkatimer.data.local.entity.TimerPresetEntity

/**
 * Tikka Timer 앱의 Room 데이터베이스
 * 알람과 타이머 프리셋 정보를 저장
 */
@Database(
    entities = [
        AlarmEntity::class,
        TimerPresetEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    abstract fun timerPresetDao(): TimerPresetDao

    companion object {
        const val DATABASE_NAME = "tikka_timer_db"
        private const val TAG = "AppDatabase"

        /**
         * Migration v1 -> v2
         * - alarms: isVibrate 제거, soundType/vibrationPattern 추가
         * - timer_presets: soundType/vibrationPattern/ringtoneUri 추가
         *
         * SQLite는 ALTER TABLE DROP COLUMN을 지원하지 않으므로 테이블 재생성 필요
         */
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    Log.d(TAG, "Starting migration from v1 to v2")

                    try {
                        // 트랜잭션 내에서 마이그레이션 수행
                        db.beginTransaction()
                        try {
                            migrateAlarmsTable(db)
                            migrateTimerPresetsTable(db)
                            db.setTransactionSuccessful()
                            Log.d(TAG, "Migration v1 -> v2 completed successfully")
                        } finally {
                            db.endTransaction()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Migration v1 -> v2 failed", e)
                        throw e // Room이 처리하도록 다시 throw
                    }
                }

                /**
                 * alarms 테이블 마이그레이션
                 */
                private fun migrateAlarmsTable(db: SupportSQLiteDatabase) {
                    // 1. 새 테이블 생성 (isVibrate 제거, soundType/vibrationPattern 추가)
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS alarms_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            hour INTEGER NOT NULL,
                            minute INTEGER NOT NULL,
                            isEnabled INTEGER NOT NULL,
                            label TEXT NOT NULL,
                            repeatDays INTEGER NOT NULL,
                            soundType TEXT NOT NULL DEFAULT 'DEFAULT',
                            vibrationPattern TEXT NOT NULL DEFAULT 'DEFAULT',
                            ringtoneUri TEXT,
                            snoozeDurationMinutes INTEGER NOT NULL,
                            isSnoozeEnabled INTEGER NOT NULL,
                            createdAt INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // 2. 기존 데이터 복사 (isVibrate 값을 vibrationPattern으로 변환)
                    db.execSQL(
                        """
                        INSERT INTO alarms_new (id, hour, minute, isEnabled, label, repeatDays,
                            soundType, vibrationPattern, ringtoneUri, snoozeDurationMinutes,
                            isSnoozeEnabled, createdAt)
                        SELECT id, hour, minute, isEnabled, label, repeatDays,
                            'DEFAULT',
                            CASE WHEN isVibrate = 0 THEN 'NONE' ELSE 'DEFAULT' END,
                            ringtoneUri, snoozeDurationMinutes, isSnoozeEnabled, createdAt
                        FROM alarms
                        """.trimIndent(),
                    )

                    // 3. 기존 테이블 삭제 및 새 테이블 이름 변경
                    db.execSQL("DROP TABLE IF EXISTS alarms")
                    db.execSQL("ALTER TABLE alarms_new RENAME TO alarms")
                    Log.d(TAG, "alarms table migrated successfully")
                }

                /**
                 * timer_presets 테이블 마이그레이션
                 */
                private fun migrateTimerPresetsTable(db: SupportSQLiteDatabase) {
                    // 1. 새 테이블 생성
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS timer_presets_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            durationSeconds INTEGER NOT NULL,
                            usageCount INTEGER NOT NULL,
                            soundType TEXT NOT NULL DEFAULT 'DEFAULT',
                            vibrationPattern TEXT NOT NULL DEFAULT 'DEFAULT',
                            ringtoneUri TEXT,
                            createdAt INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // 2. 기존 데이터 복사
                    db.execSQL(
                        """
                        INSERT INTO timer_presets_new (id, name, durationSeconds, usageCount,
                            soundType, vibrationPattern, ringtoneUri, createdAt)
                        SELECT id, name, durationSeconds, usageCount,
                            'DEFAULT', 'DEFAULT', NULL, createdAt
                        FROM timer_presets
                        """.trimIndent(),
                    )

                    // 3. 기존 테이블 삭제 및 새 테이블 이름 변경
                    db.execSQL("DROP TABLE IF EXISTS timer_presets")
                    db.execSQL("ALTER TABLE timer_presets_new RENAME TO timer_presets")
                    Log.d(TAG, "timer_presets table migrated successfully")
                }
            }

        // 향후 마이그레이션 예시 (v2 -> v3)
        // val MIGRATION_2_3 = object : Migration(2, 3) { ... }

        /**
         * 모든 마이그레이션 목록 (순차 적용용)
         */
        val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
    }
}
