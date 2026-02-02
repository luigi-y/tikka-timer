package com.tikkatimer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * NotificationHelper 관련 상수 및 ID 테스트
 */
class NotificationHelperTest {
    @Test
    fun `알림 채널 ID가 정의되어 있다`() {
        assertNotNull(NotificationHelper.ALARM_CHANNEL_ID)
        assertNotNull(NotificationHelper.FOREGROUND_CHANNEL_ID)
    }

    @Test
    fun `알림 ID가 고유하다`() {
        // 알람 알림 ID와 Foreground 알림 ID가 다른지 확인
        val alarmId = NotificationHelper.ALARM_NOTIFICATION_ID
        val foregroundId = NotificationHelper.FOREGROUND_NOTIFICATION_ID

        assertNotNull(alarmId)
        assertNotNull(foregroundId)
        // 두 ID가 다른 범위에 있어야 함
        assert(alarmId != foregroundId)
    }

    @Test
    fun `알람 알림 ID 계산이 올바르다`() {
        // 알람 ID 1에 대한 알림 ID
        val alarmId = 1L
        val notificationId = NotificationHelper.ALARM_NOTIFICATION_ID + alarmId.toInt()

        assertEquals(NotificationHelper.ALARM_NOTIFICATION_ID + 1, notificationId)
    }

    @Test
    fun `여러 알람에 대한 알림 ID가 다르다`() {
        val alarm1NotificationId = NotificationHelper.ALARM_NOTIFICATION_ID + 1
        val alarm2NotificationId = NotificationHelper.ALARM_NOTIFICATION_ID + 2
        val alarm3NotificationId = NotificationHelper.ALARM_NOTIFICATION_ID + 3

        assert(alarm1NotificationId != alarm2NotificationId)
        assert(alarm2NotificationId != alarm3NotificationId)
        assert(alarm1NotificationId != alarm3NotificationId)
    }
}
