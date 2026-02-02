package com.tikkatimer.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Room 데이터베이스 마이그레이션 관련 테스트
 * 마이그레이션 구조가 올바르게 정의되어 있는지 검증
 */
class MigrationTest {
    @Test
    fun `데이터베이스 이름이 정의되어 있다`() {
        assertEquals("tikka_timer_db", AppDatabase.DATABASE_NAME)
    }

    @Test
    fun `MIGRATION_1_2가 정의되어 있다`() {
        val migration = AppDatabase.MIGRATION_1_2
        assertNotNull(migration)
        assertEquals(1, migration.startVersion)
        assertEquals(2, migration.endVersion)
    }

    @Test
    fun `ALL_MIGRATIONS 배열이 올바르게 정의되어 있다`() {
        val migrations = AppDatabase.ALL_MIGRATIONS
        assertNotNull(migrations)
        assertTrue(migrations.isNotEmpty())
    }

    @Test
    fun `ALL_MIGRATIONS에 MIGRATION_1_2가 포함되어 있다`() {
        val migrations = AppDatabase.ALL_MIGRATIONS
        val migration1To2 = migrations.find { it.startVersion == 1 && it.endVersion == 2 }
        assertNotNull(migration1To2)
    }

    @Test
    fun `마이그레이션 버전이 순차적이다`() {
        val migrations = AppDatabase.ALL_MIGRATIONS.sortedBy { it.startVersion }

        for (i in 0 until migrations.size - 1) {
            // 각 마이그레이션의 endVersion이 다음 마이그레이션의 startVersion과 일치해야 함
            assertEquals(
                "Migration chain should be continuous",
                migrations[i].endVersion,
                migrations[i + 1].startVersion,
            )
        }
    }

    @Test
    fun `첫 번째 마이그레이션이 버전 1에서 시작한다`() {
        val migrations = AppDatabase.ALL_MIGRATIONS.sortedBy { it.startVersion }
        if (migrations.isNotEmpty()) {
            assertEquals(1, migrations.first().startVersion)
        }
    }

    @Test
    fun `마지막 마이그레이션이 현재 버전 2로 끝난다`() {
        val migrations = AppDatabase.ALL_MIGRATIONS.sortedBy { it.endVersion }
        if (migrations.isNotEmpty()) {
            // 현재 데이터베이스 버전은 2
            assertEquals(2, migrations.last().endVersion)
        }
    }
}
