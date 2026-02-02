# Tikka Timer - Android 알람 앱 프로젝트

## 프로젝트 개요
알람시계, 타이머, 스톱워치 기능을 제공하는 안드로이드 앱

## 기술 스택
- **언어**: Kotlin 2.0
- **UI**: Jetpack Compose + Material Design 3
- **아키텍처**: MVVM + Clean Architecture
- **DI**: Hilt
- **비동기**: Coroutines + Flow
- **로컬 DB**: Room
- **백그라운드 작업**: AlarmManager + Foreground Service
- **테스트**: JUnit, Mockk, Turbine

## 코딩 컨벤션

### 네이밍 규칙
- 클래스: PascalCase (예: `AlarmViewModel`)
- 함수/변수: camelCase (예: `startTimer()`)
- 상수: SCREAMING_SNAKE_CASE (예: `MAX_ALARM_COUNT`)
- 패키지: lowercase (예: `com.tikkatimer.presentation.timer`)

### 아키텍처 레이어
```
app/src/main/java/com/tikkatimer/
├── data/               # Data 레이어
│   ├── local/          # Room (AppDatabase, DAO, Entity)
│   ├── mapper/         # Entity <-> Domain 변환
│   └── repository/     # Repository 구현체
├── domain/             # Domain 레이어
│   ├── model/          # Alarm, Timer, Stopwatch, LapTime
│   ├── repository/     # Repository 인터페이스
│   └── usecase/        # UseCase 클래스
├── presentation/       # Presentation 레이어
│   ├── alarm/          # AlarmScreen, AlarmViewModel
│   ├── timer/          # TimerScreen, TimerViewModel
│   ├── stopwatch/      # StopwatchScreen, StopwatchViewModel
│   ├── settings/       # SettingsScreen
│   └── main/           # MainScreen (탭 네비게이션)
├── di/                 # Hilt 모듈 (AppModule, DatabaseModule, RepositoryModule)
├── receiver/           # AlarmReceiver, BootReceiver
└── ui/theme/           # Compose 테마 (Color, Theme, Type)
```

### Compose 컨벤션
- Composable 함수명은 PascalCase 명사형 (예: `AlarmScreen`, `TimerDisplay`)
- State hoisting 원칙 준수
- Preview 함수 필수 작성
- 재사용 컴포넌트는 `component/` 하위 폴더에 배치

## 테스트 요구사항
- Unit test 커버리지 50% 이상
- ViewModel, UseCase, Repository 테스트 필수
- Room DAO Integration 테스트 필수
- UI 테스트는 핵심 사용자 흐름 위주

### 테스트 실행 명령어
```bash
# Unit Test
./gradlew testDebugUnitTest

# Integration Test (에뮬레이터/디바이스 필요)
./gradlew connectedDebugAndroidTest

# 커버리지 리포트 생성 (SonarQube용 XML 포함)
./gradlew jacocoTestReport
```

### 테스트 파일 위치
- Unit Test: `app/src/test/java/com/tikkatimer/`
- Integration Test: `app/src/androidTest/java/com/tikkatimer/`
- 커버리지 리포트: `app/build/reports/jacoco/test/`

## 커밋 메시지
Conventional Commits 형식:
- `feat:` 새로운 기능
- `fix:` 버그 수정
- `refactor:` 리팩토링
- `test:` 테스트 추가/수정
- `docs:` 문서 수정
- `chore:` 빌드, 설정 변경

## 주요 기능 요구사항

### 알람 (구현 완료)
- [x] 알람 목록 표시 (CRUD)
- [x] 반복 알람 설정 (요일별)
- [x] 스누즈 기능 설정
- [x] 진동/알람음 설정
- [ ] AlarmManager 연동 (스켈레톤 준비됨)
- [ ] 기기 재부팅 시 알람 복원 (스켈레톤 준비됨)

### 타이머 (구현 완료)
- [x] 시/분/초 설정
- [x] 일시정지/재개/리셋
- [x] +1분 추가 기능
- [x] 타이머 프리셋 저장
- [x] 원형 진행 표시기
- [ ] 백그라운드 실행 (Foreground Service)

### 스톱워치 (구현 완료)
- [x] 시작/일시정지/리셋
- [x] 랩 타임 기록
- [x] 밀리초 단위 표시
- [x] 최고/최저 랩 타임 표시

## 빌드 명령어
```bash
# 빌드
./gradlew assembleDebug

# 린트 검사
./gradlew ktlintCheck

# 린트 자동 수정
./gradlew ktlintFormat
```

## 참고 문서
- `/docs/PROJECT_PLAN.md` - 프로젝트 계획서
- `/docs/TECH_STACK_GUIDE.md` - 기술 스택 상세 가이드
- `/README.md` - 프로젝트 소개
