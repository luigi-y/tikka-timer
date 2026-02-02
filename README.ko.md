<p align="center">
  <img src="feature_graphic.png" alt="Tikka Timer">
</p>

# Tikka Timer

알람시계, 타이머, 스톱워치 기능을 제공하는 안드로이드 앱입니다.

[English](README.md)

## 주요 기능

### 알람
- 알람 추가/편집/삭제
- 반복 알람 설정 (요일별)
- 스누즈 기능
- 진동 설정
- 커스텀 알람음 지원

### 타이머
- 시/분/초 설정
- 일시정지/재개/리셋
- +1분 추가 기능
- 타이머 프리셋 저장
- 원형 진행 표시기

### 스톱워치
- 시작/일시정지/리셋
- 랩 타임 기록
- 밀리초 단위 표시
- 최고/최저 랩 타임 표시

## 기술 스택

| 구분 | 기술 |
|------|------|
| **언어** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material Design 3 |
| **아키텍처** | MVVM + Clean Architecture |
| **DI** | Hilt |
| **비동기** | Coroutines + Flow |
| **로컬 DB** | Room |
| **테스트** | JUnit, Mockk, Turbine |

## 프로젝트 구조

```
app/src/main/java/com/tikkatimer/
├── data/                    # Data 레이어
│   ├── local/               # Room Database
│   │   ├── dao/             # DAO 인터페이스
│   │   └── entity/          # Entity 클래스
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
├── receiver/                # BroadcastReceiver
└── ui/theme/                # Compose 테마
```

## 빌드 및 실행

### 요구사항
- Android Studio Hedgehog 이상
- JDK 21
- Android SDK 36

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

### 커버리지 리포트 위치
- HTML: `app/build/reports/jacoco/test/html/index.html`
- XML (SonarQube용): `app/build/reports/jacoco/test/jacocoTestReport.xml`

## 테스트 현황

### Unit Test
- **ViewModel**: StopwatchViewModel, TimerViewModel, AlarmViewModel
- **UseCase**: Alarm UseCase, Timer UseCase
- **Repository**: AlarmRepository
- **Mapper**: AlarmMapper, TimerMapper
- **Domain Model**: Alarm, Timer, Stopwatch, LapTime

### Integration Test
- **Room DAO**: AlarmDao, TimerPresetDao

## 라이선스

MIT License
