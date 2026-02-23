<p align="center">
  <img src="images/feature_graphic.png" alt="Tikka Timer">
</p>

# Tikka Timer

알람시계, 타이머, 스톱워치 기능을 제공하는 안드로이드 앱입니다.

[English](README.md) | [개인정보처리방침](https://luigi-y.github.io/tikka-timer/privacy-policy)

## 주요 기능

### 알람
- 알람 추가/편집/삭제
- 반복 알람 설정 (요일별)
- 스누즈 기능
- 커스텀 알람음 및 진동 패턴
- 잠금 화면 위 전체 화면 알람

### 타이머
- 시/분/초 설정
- 일시정지/재개/리셋
- +1분 추가 기능
- 타이머 프리셋 저장 (소리/진동 설정 포함)
- 원형 진행 표시기
- Foreground Service를 통한 백그라운드 실행

### 스톱워치
- 시작/일시정지/리셋
- 랩 타임 기록
- 밀리초 단위 표시
- 최고/최저 랩 타임 표시

### 위젯
- 1x1 홈 화면 타이머 위젯
- 실시간 상태 표시 (대기/실행/일시정지/완료)

### 설정
- 테마 모드 (라이트/다크/시스템)
- 6가지 컬러 테마
- 4개 언어 지원 (한국어, 영어, 일본어, 중국어)

## 기술 스택

| 구분 | 기술 |
|------|------|
| **언어** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material Design 3 |
| **아키텍처** | MVVM + Clean Architecture |
| **DI** | Hilt |
| **비동기** | Coroutines + Flow |
| **로컬 DB** | Room |
| **위젯** | RemoteViews (AppWidgetProvider) |
| **백그라운드** | AlarmManager + Foreground Service |
| **테스트** | JUnit, Mockk, Turbine |

## 프로젝트 구조

```
app/src/main/java/com/tikkatimer/
├── data/                    # Data 레이어
│   ├── local/               # Room Database (DAO, Entity)
│   ├── mapper/              # Entity <-> Domain 변환
│   └── repository/          # Repository 구현체
├── domain/                  # Domain 레이어
│   ├── model/               # 도메인 모델
│   ├── repository/          # Repository 인터페이스
│   └── usecase/             # UseCase 클래스
├── presentation/            # Presentation 레이어
│   ├── alarm/               # 알람 화면
│   ├── timer/               # 타이머 화면
│   ├── stopwatch/           # 스톱워치 화면
│   ├── settings/            # 설정 화면
│   └── main/                # 메인 화면 (탭 네비게이션)
├── di/                      # Hilt 모듈
├── receiver/                # BroadcastReceiver (알람, 부팅)
├── service/                 # Foreground Service
├── sync/                    # 타이머 상태 동기화
├── util/                    # 알림, 사운드 매니저
└── widget/                  # 홈 화면 위젯
```

## 빌드 및 실행

### 요구사항
- Android Studio Ladybug 이상
- JDK 21
- Android SDK 36 (min SDK 26)

### 빌드
```bash
./gradlew assembleDebug
```

### 테스트 실행
```bash
# Unit Test
./gradlew testDebugUnitTest

# Integration Test (에뮬레이터/디바이스 필요)
./gradlew connectedDebugAndroidTest

# 커버리지 리포트 생성
./gradlew jacocoTestReport
```

### 코드 품질 검사
```bash
# 포맷팅 검사
./gradlew ktlintCheck

# 정적 분석
./gradlew detekt

# Android Lint
./gradlew lintDebug
```

## 테스트 현황

### Unit Test (21개)
- **ViewModel**: AlarmViewModel, TimerViewModel, StopwatchViewModel, SettingsViewModel
- **UseCase**: AlarmUseCase, TimerUseCase, GetUpcomingAlarmUseCase, DisableOneTimeAlarmUseCase
- **Repository**: AlarmRepository, TimerRepository
- **Mapper**: AlarmMapper, TimerMapper
- **Domain Model**: Alarm, Timer, Stopwatch, LapTime
- **Service**: AlarmRingingService
- **Util**: NotificationHelper, AlarmSoundManager, AlarmScheduler
- **Database**: Migration

### Integration Test (3개)
- **Room DAO**: AlarmDao, TimerPresetDao
- **Util**: AlarmSoundManager

## 라이선스

MIT License
