package com.luigi.tikkatimer.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Room 데이터베이스 설정 테스트
 */
class MigrationTest {
    @Test
    fun `데이터베이스 이름이 정의되어 있다`() {
        assertEquals("tikka_timer_db", AppDatabase.DATABASE_NAME)
    }
}
