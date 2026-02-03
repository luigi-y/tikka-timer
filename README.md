<p align="center">
  <img src="images/feature_graphic.png" alt="Tikka Timer">
</p>

# Tikka Timer

An Android app that provides alarm clock, timer, and stopwatch features.

[한국어](README.ko.md)

## Features

### Alarm
- Add/Edit/Delete alarms
- Repeat alarms (by day of week)
- Snooze functionality
- Vibration settings
- Custom alarm sounds

### Timer
- Hours/Minutes/Seconds setting
- Pause/Resume/Reset
- +1 minute quick add
- Timer presets
- Circular progress indicator

### Stopwatch
- Start/Pause/Reset
- Lap time recording
- Millisecond precision
- Best/Worst lap time display

## Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material Design 3 |
| **Architecture** | MVVM + Clean Architecture |
| **DI** | Hilt |
| **Async** | Coroutines + Flow |
| **Local DB** | Room |
| **Testing** | JUnit, Mockk, Turbine |

## Project Structure

```
app/src/main/java/com/tikkatimer/
├── data/                    # Data Layer
│   ├── local/               # Room Database
│   │   ├── dao/             # DAO Interfaces
│   │   └── entity/          # Entity Classes
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
├── receiver/                # BroadcastReceiver
└── ui/theme/                # Compose Theme
```

## Build & Run

### Requirements
- Android Studio Hedgehog or later
- JDK 21
- Android SDK 36

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

### Coverage Report Location
- HTML: `app/build/reports/jacoco/test/html/index.html`
- XML (for SonarQube): `app/build/reports/jacoco/test/jacocoTestReport.xml`

## Test Coverage

### Unit Tests
- **ViewModel**: StopwatchViewModel, TimerViewModel, AlarmViewModel
- **UseCase**: Alarm UseCase, Timer UseCase
- **Repository**: AlarmRepository
- **Mapper**: AlarmMapper, TimerMapper
- **Domain Model**: Alarm, Timer, Stopwatch, LapTime

### Integration Tests
- **Room DAO**: AlarmDao, TimerPresetDao

## License

MIT License
