# Tikka Timer - 프로젝트 계획서

## 1. 프로젝트 개요

### 1.1 목표
- 알람시계, 타이머, 스톱워치 기능을 제공하는 안드로이드 앱 개발
- 최신 안드로이드 개발 트렌드(Kotlin, Jetpack Compose)를 활용한 현대적인 앱 구현

### 1.2 대상 플랫폼
- Android 8.0 (API 26) 이상
- 타겟 SDK: Android 14 (API 34)

---

## 2. 핵심 기능 명세

### 2.1 알람 (Alarm)

| 기능 | 설명 | 우선순위 |
|------|------|----------|
| 알람 생성/편집/삭제 | 시간, 라벨, 알람음 설정 | P0 |
| 반복 알람 | 요일별 반복 설정 | P0 |
| 스누즈 | 5/10/15분 간격 스누즈 | P0 |
| 알람음 선택 | 기본 벨소리 + 커스텀 음원 | P1 |
| 진동 설정 | 알람 시 진동 on/off | P1 |
| 점진적 볼륨 | 알람음 볼륨 점진적 증가 | P2 |
| 부팅 시 복원 | 기기 재부팅 후 알람 자동 복원 | P0 |

### 2.2 타이머 (Timer)

| 기능 | 설명 | 우선순위 |
|------|------|----------|
| 시간 설정 | 시/분/초 입력 | P0 |
| 시작/일시정지/리셋 | 기본 타이머 컨트롤 | P0 |
| 백그라운드 실행 | 앱 종료 후에도 타이머 동작 | P0 |
| 완료 알림 | 타이머 종료 시 알림 + 소리 | P0 |
| 프리셋 저장 | 자주 쓰는 시간 저장 | P1 |
| 다중 타이머 | 여러 타이머 동시 실행 | P2 |

### 2.3 스톱워치 (Stopwatch)

| 기능 | 설명 | 우선순위 |
|------|------|----------|
| 시작/일시정지/리셋 | 기본 스톱워치 컨트롤 | P0 |
| 밀리초 표시 | 00:00:00.00 형식 표시 | P0 |
| 랩 타임 | 랩 타임 기록 및 목록 표시 | P0 |
| 랩 타임 비교 | 최고/최저 랩 하이라이트 | P1 |
| 백그라운드 유지 | 앱 백그라운드에서도 계속 실행 | P1 |

---

## 3. 개발 단계 (Phase)

### Phase 1: 프로젝트 설정 및 기반 구축
**목표**: 프로젝트 구조 설정, 기본 아키텍처 구현

- [ ] Android 프로젝트 생성 (Kotlin + Compose)
- [ ] 패키지 구조 설계 (Clean Architecture)
- [ ] Hilt DI 설정
- [ ] Room 데이터베이스 설정
- [ ] Navigation 설정 (Bottom Navigation)
- [ ] 테마 및 Material Design 3 적용

### Phase 2: 스톱워치 기능 구현
**목표**: 가장 단순한 기능부터 시작하여 아키텍처 검증

- [ ] 스톱워치 UI 구현 (시간 표시, 버튼)
- [ ] StopwatchViewModel 구현 (Coroutines + Flow)
- [ ] 랩 타임 기능 구현
- [ ] 백그라운드 유지 (Foreground Service)
- [ ] Unit 테스트 작성

### Phase 3: 타이머 기능 구현
**목표**: 백그라운드 작업 및 알림 시스템 학습

- [ ] 타이머 UI 구현 (시간 입력, 진행 표시)
- [ ] TimerViewModel 구현
- [ ] 백그라운드 타이머 (WorkManager/Service)
- [ ] 타이머 완료 알림
- [ ] 프리셋 저장 기능 (Room)
- [ ] Unit 테스트 작성

### Phase 4: 알람 기능 구현
**목표**: AlarmManager를 활용한 정확한 알람 구현

- [ ] 알람 목록 UI 구현
- [ ] 알람 생성/편집 UI 구현
- [ ] Alarm Entity 및 Repository 구현
- [ ] AlarmManager 연동
- [ ] 알람 울림 화면 (Full-screen Intent)
- [ ] 스누즈 기능
- [ ] 부팅 시 알람 복원 (BroadcastReceiver)
- [ ] Unit 테스트 작성

### Phase 5: 마무리 및 최적화
**목표**: 품질 개선 및 출시 준비

- [ ] UI/UX 개선 및 애니메이션 추가
- [ ] 다크 모드 지원
- [ ] 접근성(Accessibility) 개선
- [ ] 성능 최적화 (메모리, 배터리)
- [ ] 통합 테스트 작성
- [ ] 버그 수정 및 안정화
- [ ] Play Store 출시 준비

---

## 4. 프로젝트 구조

```
tikka-timer/
├── app/
│   ├── src/main/
│   │   ├── java/com/tikkatimer/
│   │   │   ├── TikkaTimerApp.kt           # Application 클래스
│   │   │   ├── MainActivity.kt
│   │   │   │
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── AlarmDao.kt
│   │   │   │   │   │   └── TimerPresetDao.kt
│   │   │   │   │   └── entity/
│   │   │   │   │       ├── AlarmEntity.kt
│   │   │   │   │       └── TimerPresetEntity.kt
│   │   │   │   └── repository/
│   │   │   │       ├── AlarmRepositoryImpl.kt
│   │   │   │       └── TimerRepositoryImpl.kt
│   │   │   │
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── Alarm.kt
│   │   │   │   │   ├── Timer.kt
│   │   │   │   │   └── LapTime.kt
│   │   │   │   ├── repository/
│   │   │   │   │   ├── AlarmRepository.kt
│   │   │   │   │   └── TimerRepository.kt
│   │   │   │   └── usecase/
│   │   │   │       ├── alarm/
│   │   │   │       ├── timer/
│   │   │   │       └── stopwatch/
│   │   │   │
│   │   │   ├── presentation/
│   │   │   │   ├── navigation/
│   │   │   │   │   └── AppNavigation.kt
│   │   │   │   ├── alarm/
│   │   │   │   │   ├── AlarmScreen.kt
│   │   │   │   │   ├── AlarmViewModel.kt
│   │   │   │   │   └── components/
│   │   │   │   ├── timer/
│   │   │   │   │   ├── TimerScreen.kt
│   │   │   │   │   ├── TimerViewModel.kt
│   │   │   │   │   └── components/
│   │   │   │   ├── stopwatch/
│   │   │   │   │   ├── StopwatchScreen.kt
│   │   │   │   │   ├── StopwatchViewModel.kt
│   │   │   │   │   └── components/
│   │   │   │   └── theme/
│   │   │   │       ├── Theme.kt
│   │   │   │       ├── Color.kt
│   │   │   │       └── Type.kt
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── AlarmService.kt
│   │   │   │   ├── TimerService.kt
│   │   │   │   └── StopwatchService.kt
│   │   │   │
│   │   │   ├── receiver/
│   │   │   │   ├── AlarmReceiver.kt
│   │   │   │   └── BootReceiver.kt
│   │   │   │
│   │   │   └── di/
│   │   │       ├── AppModule.kt
│   │   │       ├── DatabaseModule.kt
│   │   │       └── RepositoryModule.kt
│   │   │
│   │   └── res/
│   │       ├── values/
│   │       ├── drawable/
│   │       └── raw/                        # 알람 사운드
│   │
│   └── src/test/                           # Unit Tests
│   └── src/androidTest/                    # UI Tests
│
├── docs/
│   ├── PROJECT_PLAN.md
│   └── TECH_STACK_GUIDE.md
│
├── CLAUDE.md
└── README.md
```

---

## 5. 테스트 전략

### 5.1 Unit Test (목표: 50% 커버리지)
- **ViewModel**: 상태 변화, 이벤트 처리 테스트
- **UseCase**: 비즈니스 로직 테스트
- **Repository**: 데이터 조작 테스트 (Mock 활용)

### 5.2 Integration Test
- Room Database CRUD 테스트
- AlarmManager 스케줄링 테스트

### 5.3 UI Test
- 핵심 사용자 흐름 (알람 생성, 타이머 시작 등)
- Compose Testing 라이브러리 활용

---

## 6. 품질 관리

### 6.1 코드 품질
- Ktlint / Detekt 적용
- GitHub Actions CI/CD 설정
- PR 리뷰 필수

### 6.2 성능 목표
- 앱 시작 시간: 1초 이내
- 알람 정확도: ±1초 이내
- 메모리 사용: 100MB 이하

---

## 7. 참고 자료

### 공식 문서
- [Android Developers - Schedule Alarms](https://developer.android.com/develop/background-work/services/alarms/schedule)
- [Jetpack Compose](https://developer.android.com/develop/ui/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)

### 오픈소스 참고
- [Clock App (Jetpack Compose)](https://github.com/yassineAbou/Clock)
- [AlarmManager Example](https://github.com/MubarakNative/AlarmManagerExample)
- [InTime Android](https://github.com/p-hlp/InTimeAndroid)
