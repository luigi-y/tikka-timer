<p align="center">
  <img src="images/feature_graphic.png" alt="Tikka Timer">
</p>

# Tikka Timer

An Android app that provides alarm clock, timer, and stopwatch features.

[한국어](README.ko.md) | [Privacy Policy](https://luigi-y.github.io/tikka-timer/privacy-policy)

## Features

### Alarm
- Add/Edit/Delete alarms
- Repeat alarms (by day of week)
- Snooze functionality
- Custom alarm sounds & vibration patterns
- Full-screen alarm on lock screen

### Timer
- Hours/Minutes/Seconds setting
- Pause/Resume/Reset
- +1 minute quick add
- Timer presets with sound & vibration settings
- Circular progress indicator
- Background execution via Foreground Service

### Stopwatch
- Start/Pause/Reset
- Lap time recording
- Millisecond precision
- Best/Worst lap time display

### Widget
- 1x1 home screen timer widget
- Real-time status display (idle/running/paused/finished)

### Settings
- Theme mode (Light/Dark/System)
- 6 color themes
- 4 languages (Korean, English, Japanese, Chinese)

## Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material Design 3 |
| **Architecture** | MVVM + Clean Architecture |
| **DI** | Hilt |
| **Async** | Coroutines + Flow |
| **Local DB** | Room |
| **Widget** | RemoteViews (AppWidgetProvider) |
| **Background** | AlarmManager + Foreground Service |
| **Testing** | JUnit, Mockk, Turbine |

## Project Structure

```
app/src/main/java/com/tikkatimer/
├── data/                    # Data Layer
│   ├── local/               # Room Database (DAO, Entity)
│   ├── mapper/              # Entity <-> Domain Mapping
│   └── repository/          # Repository Implementations
├── domain/                  # Domain Layer
│   ├── model/               # Domain Models
│   ├── repository/          # Repository Interfaces
│   └── usecase/             # UseCase Classes
├── presentation/            # Presentation Layer
│   ├── alarm/               # Alarm Screen
│   ├── timer/               # Timer Screen
│   ├── stopwatch/           # Stopwatch Screen
│   ├── settings/            # Settings Screen
│   └── main/                # Main Screen (Tab Navigation)
├── di/                      # Hilt Modules
├── receiver/                # BroadcastReceiver (Alarm, Boot)
├── service/                 # Foreground Service
├── sync/                    # Timer State Sync
├── util/                    # Notification, Sound Manager
└── widget/                  # Home Screen Widget
```

## Build & Run

### Requirements
- Android Studio Ladybug or later
- JDK 21
- Android SDK 36 (min SDK 26)

### Build
```bash
./gradlew assembleDebug
```

### Run Tests
```bash
# Unit Test
./gradlew testDebugUnitTest

# Integration Test (requires emulator/device)
./gradlew connectedDebugAndroidTest

# Generate coverage report
./gradlew jacocoTestReport
```

### Code Quality
```bash
# Formatting check
./gradlew ktlintCheck

# Static analysis
./gradlew detekt

# Android Lint
./gradlew lintDebug
```

## Test Coverage

### Unit Tests (21 files)
- **ViewModel**: AlarmViewModel, TimerViewModel, StopwatchViewModel, SettingsViewModel
- **UseCase**: AlarmUseCase, TimerUseCase, GetUpcomingAlarmUseCase, DisableOneTimeAlarmUseCase
- **Repository**: AlarmRepository, TimerRepository
- **Mapper**: AlarmMapper, TimerMapper
- **Domain Model**: Alarm, Timer, Stopwatch, LapTime
- **Service**: AlarmRingingService
- **Util**: NotificationHelper, AlarmSoundManager, AlarmScheduler
- **Database**: Migration

### Integration Tests (3 files)
- **Room DAO**: AlarmDao, TimerPresetDao
- **Util**: AlarmSoundManager

## License

MIT License
