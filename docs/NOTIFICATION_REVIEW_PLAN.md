# 알림/소리/진동 검토 및 개선 작업 플랜

**작성일:** 2026-04-10
**상태:** 전체 완료

---

## 발견된 이슈

### 버그

| # | 심각도 | 이슈 | 파일 | 상태 |
|---|--------|------|------|------|
| B1 | **HIGH** | 알람 자동 타임아웃 미구현 (무한 울림) | `AlarmRingingService.kt` | ✅ 완료 |
| B2 | **HIGH** | 타이머 완료 소리 자동 정지 미구현 (무한 반복) | `TimerStateSync.kt` | ✅ 완료 |
| B3 | **MEDIUM** | MediaPlayer 리소스 누수 (`tryPlayDefaultAlarm`) | `AlarmSoundManager.kt` | ✅ 완료 |

### 개선사항

| # | 우선순위 | 이슈 | 파일 | 상태 |
|---|---------|------|------|------|
| I1 | **MEDIUM** | TIMER_CHANNEL_ID 미사용 → 타이머 완료 알림 추가 | `NotificationHelper.kt`, `TimerStateSync.kt` | ✅ 완료 |
| I2 | **MEDIUM** | PAUSED 상태에서 Foreground Service 종료 | `TimerStateSync.kt` | ✅ 완료 |

---

## 상세 수정 계획

### B1. 알람 자동 타임아웃 (5분)
- **문제:** `AlarmRingingService.handleStartAlarm()` 이후 사용자가 해제/스누즈를 누르지 않으면 소리+진동이 영원히 울림
- **해결:** 소리/진동 시작 후 `Handler.postDelayed()`로 5분 후 자동 해제. 자동 해제 시 스누즈로 처리 (다시 알람이 울리도록)
- **리스크:** 없음. Google Clock 앱도 동일한 동작 (10분 후 자동 스누즈)

### B2. 타이머 완료 소리 자동 정지 (30초)
- **문제:** `TimerStateSync.handleFinishedTimers()`에서 소리/진동 시작 후, `stopFinishedAlarm()`은 사용자가 앱에서 확인해야만 호출됨
- **해결:** 소리/진동 시작 후 30초 타이머를 걸어 자동 정지. 알림은 유지.
- **리스크:** 없음. 타이머 완료음은 짧게 울리는 것이 표준.

### B3. MediaPlayer 리소스 누수 수정
- **문제:** `tryPlayDefaultAlarm()`에서 이전 `mediaPlayer`를 release하지 않고 새로 할당
- **해결:** `tryPlayDefaultAlarm()` 시작 시 `stopAlarmSound()` 호출 추가
- **리스크:** 없음.

### I1. 타이머 완료 알림 추가
- **문제:** TIMER_CHANNEL_ID 채널이 생성되어 있지만 사용되지 않음. 타이머 완료 시 사용자에게 명시적 알림이 없음 (Foreground 알림만 업데이트).
- **해결:** 타이머 완료 시 TIMER_CHANNEL_ID로 별도 알림 발송 (타이머 이름 + "완료!" 메시지)
- **리스크:** 없음.

### I2. PAUSED 상태 Foreground Service 유지
- **문제:** `manageForegroundService()`가 `TimerState.RUNNING`만 체크. 모든 타이머가 PAUSED면 서비스가 종료되어 알림 사라짐.
- **해결:** RUNNING 또는 PAUSED 상태의 타이머가 있으면 서비스 유지.
- **리스크:** 없음. 이미 `updateNotificationState()`는 PAUSED를 고려하고 있음.

---

## 수정 이력

| 항목 | 수정 파일 | 결과 |
|------|----------|------|
| B1 | `AlarmRingingService.kt` | Handler.postDelayed로 5분 자동 스누즈 구현, dismiss/snooze 시 타이머 취소 |
| B2 | `TimerStateSync.kt` | 완료 소리 30초 후 자동 정지 (coroutine delay), 수동 정지 시 Job 취소 |
| B3 | `AlarmSoundManager.kt` | `tryPlayDefaultAlarm()` 시작 시 `stopAlarmSound()` 호출 추가 |
| I1 | `NotificationHelper.kt`, `TimerStateSync.kt`, `strings.xml` (4개 언어) | 타이머 완료 알림 (TIMER_CHANNEL_ID) 발송 구현 |
| I2 | `TimerStateSync.kt` | `manageForegroundService()`에서 PAUSED 상태도 체크하도록 수정 |
| 테스트 | `AlarmRingingServiceTest.kt`, `TimerStateSyncTest.kt` | 자동 타임아웃 상수, 서비스 유지 조건 테스트 추가 |
| A11y | `AlarmRingingActivity.kt`, `TimerScreen.kt`, `TimerControls.kt` | 8개 기능 버튼 Icon에 contentDescription 추가 |
| 검증 | - | ktlintCheck 통과, testDebugUnitTest 전체 통과 |
