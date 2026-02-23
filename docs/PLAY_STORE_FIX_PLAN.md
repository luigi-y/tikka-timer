# Play Store 배포 준비 - 3차 검토 수정 계획

**작성일:** 2026-02-23
**상태:** 전체 완료

---

## 수정 대상 요약

| 우선순위 | # | 이슈 | 파일 | 상태 |
|---------|---|------|------|------|
| **HIGH** | H1 | Glance Widget ProGuard 규칙 누락 | `proguard-rules.pro` | ✅ 완료 |
| **MEDIUM** | M1 | TimerWidgetProvider exported 보안 강화 | `AndroidManifest.xml` | ✅ 완료 |
| **MEDIUM** | M2 | `values/` 기본값을 영어로 전환 | `values/strings.xml`, `values-ko/strings.xml` (신규), `values-en/` 삭제 | ✅ 완료 |
| **MEDIUM** | M3 | en-US title 중복 수정 | `fastlane/.../en-US/title.txt` | ✅ 완료 |
| **LOW** | L1 | `onBackPressed()` deprecated 대체 | `AlarmRingingActivity.kt` | ✅ 완료 |
| **LOW** | L2 | Widget 단위 테스트 추가 | `TimerWidgetStateTest.kt` (신규, 21개 테스트) | ✅ 완료 |
| - | V1 | 빌드 및 전체 테스트 검증 | ktlint + 258 tests 통과 | ✅ 완료 |

---

## 상세 수정 계획

### H1. Glance Widget ProGuard 규칙 추가
- **위치:** `app/proguard-rules.pro`
- **내용:** Glance 관련 keep 규칙 추가 (`-keep class androidx.glance.** { *; }`)
- **리스크:** 규칙 없으면 Release 빌드에서 위젯이 동작하지 않을 수 있음

### M1. TimerWidgetProvider exported 보안 강화
- **위치:** `AndroidManifest.xml`, `TimerWidgetProvider.kt`
- **내용:**
  - 커스텀 action(`ACTION_WIDGET_CLICK`, `ACTION_TIMER_FINISHED`)을 Manifest intent-filter에서 제거
  - `TimerWidgetProvider`는 `APPWIDGET_UPDATE`만 intent-filter로 수신
  - 커스텀 action은 explicit intent(ComponentName 지정)로 전달하므로 intent-filter 불필요
- **리스크:** 외부 앱이 위젯 상태를 변경하는 것을 원천 차단

### M2. `values/` 기본값을 영어로 전환
- **위치:** `values/strings.xml`, `values-en/strings.xml`, `values-ko/strings.xml` (신규)
- **내용:**
  - 현재 `values/`(기본)=한국어, `values-en/`=영어
  - `values/`(기본)=영어로 변경, `values-ko/`에 한국어 이동
  - `values-en/` 삭제 (기본값이 영어이므로 불필요)
- **리스크:** 지원 언어(ko/en/ja/zh) 외 기기에서 한국어 대신 영어 표시

### M3. en-US title 수정
- **위치:** `fastlane/metadata/android/en-US/title.txt`
- **내용:** `"Tikka Timer - Alarm & Timer"` → `"Tikka Timer - Alarm & Stopwatch"` (30자 이내)

### L1. onBackPressed() deprecated 대체
- **위치:** `AlarmRingingActivity.kt`
- **내용:** `onBackPressed()` override → `OnBackPressedDispatcher` API로 전환

### L2. Widget 단위 테스트 추가
- **위치:** `app/src/test/java/com/luigi/tikkatimer/widget/TimerWidgetProviderTest.kt` (신규)
- **내용:** TimerWidgetProvider의 formatTime, 상태 판정, 알람 스케줄링 로직 테스트

### V1. 빌드 및 테스트 검증
- `./gradlew ktlintCheck` 통과
- `./gradlew testDebugUnitTest` 전체 통과

---

## 수정 이력

| 항목 | 수정 파일 | 결과 |
|------|----------|------|
| H1 | `proguard-rules.pro` | Glance keep 규칙 추가 |
| M1 | `AndroidManifest.xml` | 커스텀 action을 intent-filter에서 제거, APPWIDGET_UPDATE만 유지 |
| M2 | `values/strings.xml` → 영어, `values-ko/strings.xml` 신규, `values-en/` 삭제 | 기본 locale 영어 전환 |
| M3 | `fastlane/.../en-US/title.txt` | "Alarm & Timer" → "Alarm & Stopwatch" |
| L1 | `AlarmRingingActivity.kt` | `onBackPressed()` 제거 → `OnBackPressedDispatcher` 적용 |
| L2 | `TimerWidgetStateTest.kt` 신규 (21개 테스트) | Widget 상태 로직 테스트 추가 |
| V1 | - | ktlint 통과, 28 클래스 / 258개 테스트 전체 통과 |
