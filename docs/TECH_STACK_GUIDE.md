# Tikka Timer - 기술 스택 가이드

## 1. 2025-2026 안드로이드 개발 트렌드 분석

### 1.1 Kotlin: 표준 언어로 완전히 자리잡음

Kotlin은 이제 안드로이드 공식 언어이며 모든 새 프로젝트의 기본입니다.

**왜 Kotlin인가?**
- **간결성**: Java 대비 40% 적은 코드
- **Null Safety**: 컴파일 타임에 NPE 방지
- **Coroutines**: 구조화된 비동기 프로그래밍
- **Google 공식 지원**: 모든 Jetpack 라이브러리가 Kotlin-first

```kotlin
// Kotlin의 간결함 예시
data class Alarm(
    val id: Long,
    val time: LocalTime,
    val label: String,
    val isEnabled: Boolean = true,
    val repeatDays: Set<DayOfWeek> = emptySet()
)
```

> 참고: [Android app development using Kotlin: A complete guide for 2025](https://powergatesoftware.com/tech-blog/android-app-development-using-kotlin/)

### 1.2 Jetpack Compose: UI 개발의 표준

2026년 기준 Play Store 상위 앱의 60%가 Compose를 사용합니다. Pinterest, Airbnb, Google Pay, Lyft 등 주요 기업들이 이미 도입했습니다.

**Compose의 장점**
- **선언적 UI**: "어떻게" 보다 "무엇을" 그릴지 집중
- **적은 보일러플레이트**: XML + findViewById 불필요
- **State 관리 용이**: 단방향 데이터 흐름
- **강력한 프리뷰**: Android Studio에서 실시간 미리보기

```kotlin
/**
 * 스톱워치 시간 표시 컴포넌트
 * @param elapsedMs 경과 시간 (밀리초)
 */
@Composable
fun StopwatchDisplay(elapsedMs: Long) {
    val hours = elapsedMs / 3600000
    val minutes = (elapsedMs % 3600000) / 60000
    val seconds = (elapsedMs % 60000) / 1000
    val millis = (elapsedMs % 1000) / 10

    Text(
        text = "%02d:%02d:%02d.%02d".format(hours, minutes, seconds, millis),
        style = MaterialTheme.typography.displayLarge,
        fontFamily = FontFamily.Monospace
    )
}
```

> 참고: [Why Jetpack Compose and Kotlin Are Leading Android UI Innovation in 2025](https://medium.com/codeelevation/why-jetpack-compose-and-kotlin-are-leading-android-ui-innovation-in-2025-6f999535357e)

### 1.3 주요 트렌드 요약

| 트렌드 | 설명 | 본 프로젝트 적용 |
|--------|------|------------------|
| Kotlin Multiplatform | 비즈니스 로직 공유 (iOS/Android) | 미적용 (향후 고려) |
| Adaptive UI | 폴더블/태블릿 대응 | 반응형 레이아웃 적용 |
| On-Device AI | 로컬 ML 처리 | 미적용 |
| Material Design 3 | 최신 디자인 시스템 | 전면 적용 |
| Privacy-first | 로컬 데이터 처리 | Room DB 활용 |

> 참고: [Top Trends in Android App Development for 2026](https://www.wildnetedge.com/blogs/top-trends-in-android-app-development)

---

## 2. 기술 스택 상세

### 2.1 언어 및 빌드

| 기술 | 버전 | 용도 |
|------|------|------|
| Kotlin | 1.9.x | 메인 언어 |
| Gradle (Kotlin DSL) | 8.x | 빌드 시스템 |
| Android Gradle Plugin | 8.2.x | 안드로이드 빌드 |
| JDK | 17 | 컴파일 환경 |

### 2.2 Android Jetpack

| 라이브러리 | 용도 | 선택 이유 |
|------------|------|-----------|
| Compose BOM | UI 프레임워크 | 선언적 UI, 적은 코드 |
| Compose Material 3 | 디자인 시스템 | 최신 머티리얼 가이드라인 |
| Navigation Compose | 화면 전환 | 타입 안전한 네비게이션 |
| ViewModel | 상태 관리 | 생명주기 인식, 상태 유지 |
| Room | 로컬 DB | SQLite 추상화, Flow 지원 |
| Hilt | DI | Dagger 기반, 보일러플레이트 감소 |
| DataStore | 설정 저장 | SharedPreferences 대체 |

### 2.3 비동기 처리

| 기술 | 용도 |
|------|------|
| Coroutines | 비동기 작업 처리 |
| Flow | 반응형 스트림 |
| StateFlow | UI 상태 관리 |

---

## 3. 핵심 기술 구현 가이드

### 3.1 스톱워치 구현 (Coroutines + Flow)

스톱워치는 `MutableStateFlow`와 Coroutine Job을 활용하여 구현합니다.

```kotlin
/**
 * 스톱워치 상태를 나타내는 sealed class
 * Paused: 일시정지 상태 (경과 시간 보존)
 * Running: 실행 중 (시작 시간 + 현재 경과 시간)
 */
sealed class StopwatchState {
    data class Paused(val elapsedMs: Long) : StopwatchState()
    data class Running(
        val startTimeMs: Long,
        val elapsedMs: Long
    ) : StopwatchState()
}

class StopwatchViewModel : ViewModel() {
    private val _state = MutableStateFlow<StopwatchState>(StopwatchState.Paused(0L))
    val state: StateFlow<StopwatchState> = _state.asStateFlow()

    private val _lapTimes = MutableStateFlow<List<Long>>(emptyList())
    val lapTimes: StateFlow<List<Long>> = _lapTimes.asStateFlow()

    private var timerJob: Job? = null

    /**
     * 스톱워치 시작
     * 20ms 간격으로 시간 업데이트 (UI 부드러움과 성능 균형)
     */
    fun start() {
        val currentElapsed = when (val s = _state.value) {
            is StopwatchState.Paused -> s.elapsedMs
            is StopwatchState.Running -> s.elapsedMs
        }

        val startTime = SystemClock.elapsedRealtime() - currentElapsed

        timerJob = viewModelScope.launch {
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                _state.value = StopwatchState.Running(startTime, now - startTime)
                delay(20) // 50fps
            }
        }
    }

    /**
     * 스톱워치 일시정지
     * cancelChildren()으로 Job만 취소하고 scope는 유지
     */
    fun pause() {
        timerJob?.cancelChildren()
        val currentElapsed = (_state.value as? StopwatchState.Running)?.elapsedMs ?: 0
        _state.value = StopwatchState.Paused(currentElapsed)
    }

    /** 랩 타임 기록 */
    fun lap() {
        val currentElapsed = when (val s = _state.value) {
            is StopwatchState.Paused -> s.elapsedMs
            is StopwatchState.Running -> s.elapsedMs
        }
        _lapTimes.value = _lapTimes.value + currentElapsed
    }

    /** 스톱워치 리셋 */
    fun reset() {
        timerJob?.cancelChildren()
        _state.value = StopwatchState.Paused(0L)
        _lapTimes.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
```

> 참고: [Building a High-Performance Stopwatch with Kotlin & Jetpack Compose](https://dev.to/blamsa0mine/building-a-high-performance-stopwatch-with-kotlin-jetpack-compose-real-time-state-management--3ik)

### 3.2 타이머 구현 (Flow + Service)

```kotlin
/**
 * 타이머 상태
 */
sealed class TimerState {
    data object Idle : TimerState()
    data class Running(val remainingMs: Long, val totalMs: Long) : TimerState()
    data class Paused(val remainingMs: Long, val totalMs: Long) : TimerState()
    data object Finished : TimerState()
}

class TimerViewModel @Inject constructor(
    private val timerRepository: TimerRepository
) : ViewModel() {

    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var timerJob: Job? = null

    /**
     * 타이머 시작
     * @param durationMs 타이머 시간 (밀리초)
     */
    fun start(durationMs: Long) {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            var remaining = durationMs
            _state.value = TimerState.Running(remaining, durationMs)

            while (remaining > 0 && isActive) {
                delay(100)
                remaining -= 100
                _state.value = TimerState.Running(remaining, durationMs)
            }

            if (remaining <= 0) {
                _state.value = TimerState.Finished
                // TODO: 알림 트리거
            }
        }
    }

    fun pause() {
        timerJob?.cancel()
        val current = _state.value
        if (current is TimerState.Running) {
            _state.value = TimerState.Paused(current.remainingMs, current.totalMs)
        }
    }

    fun resume() {
        val current = _state.value
        if (current is TimerState.Paused) {
            start(current.remainingMs)
        }
    }

    fun reset() {
        timerJob?.cancel()
        _state.value = TimerState.Idle
    }
}
```

> 참고: [Creating a Timer Screen with Kotlin and Jetpack Compose](https://medium.com/@TippuFisalSheriff/creating-a-timer-screen-with-kotlin-and-jetpack-compose-in-android-f7c56952d599)

### 3.3 알람 구현 (AlarmManager + Room)

#### 3.3.1 Room Entity

```kotlin
/**
 * 알람 정보를 저장하는 Entity
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: String = "", // "0,1,2,3,4,5,6" 형식 (일~토)
    val ringtoneUri: String? = null,
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = 10
)

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("UPDATE alarms SET isEnabled = :enabled WHERE id = :id")
    suspend fun setAlarmEnabled(id: Long, enabled: Boolean)
}
```

#### 3.3.2 AlarmManager 스케줄링

```kotlin
/**
 * 알람 스케줄링을 담당하는 클래스
 */
class AlarmScheduler @Inject constructor(
    private val context: Context,
    private val alarmManager: AlarmManager
) {
    /**
     * 알람 예약
     * Android 12+ 에서는 SCHEDULE_EXACT_ALARM 권한 필요
     */
    fun scheduleAlarm(alarm: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            action = "com.tikkatimer.ALARM_TRIGGER"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateNextTriggerTime(alarm)

        // 알람 앱은 setAlarmClock 사용 (정확한 시간에 울림 보장)
        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            triggerTime,
            getAlarmShowIntent(alarm.id)
        )

        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    /**
     * 다음 알람 시간 계산
     * 반복 알람의 경우 가장 가까운 요일 계산
     */
    private fun calculateNextTriggerTime(alarm: AlarmEntity): Long {
        val now = Calendar.getInstance()
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 이미 지난 시간이면 다음 날로
        if (alarmTime.before(now)) {
            alarmTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        // TODO: 반복 요일 처리

        return alarmTime.timeInMillis
    }

    fun cancelAlarm(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
```

#### 3.3.3 부팅 시 알람 복원

```kotlin
/**
 * 기기 부팅 시 저장된 알람을 복원하는 BroadcastReceiver
 */
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmDao: AlarmDao

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Hilt inject
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                BootReceiverEntryPoint::class.java
            )

            CoroutineScope(Dispatchers.IO).launch {
                val enabledAlarms = entryPoint.alarmDao().getEnabledAlarms()
                enabledAlarms.forEach { alarm ->
                    entryPoint.alarmScheduler().scheduleAlarm(alarm)
                }
            }
        }
    }
}
```

> 참고: [Creating a alarm using AlarmManager in Android](https://mubaraknative.medium.com/creating-a-alarm-using-alarmmanager-in-android-e27a4283d39f)

---

## 4. 필수 권한

### AndroidManifest.xml 설정

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- 알람 정확한 시간 예약 (Android 12+) -->
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />

    <!-- 알람 앱 전용 권한 (심사 없이 사용 가능) -->
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />

    <!-- 부팅 시 알람 복원 -->
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <!-- Foreground Service (타이머, 스톱워치 백그라운드 실행) -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />

    <!-- 알림 (Android 13+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- 진동 -->
    <uses-permission android:name="android.permission.VIBRATE" />

    <!-- 화면 켜기 (알람 울릴 때) -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />

    <application ...>
        <!-- 부팅 시 알람 복원 -->
        <receiver
            android:name=".receiver.BootReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>

        <!-- 알람 수신 -->
        <receiver
            android:name=".receiver.AlarmReceiver"
            android:enabled="true"
            android:exported="false" />

        <!-- Foreground Service -->
        <service
            android:name=".service.TimerService"
            android:foregroundServiceType="mediaPlayback" />
    </application>
</manifest>
```

---

## 5. 의존성 목록 (build.gradle.kts)

```kotlin
// Version Catalog 권장 (libs.versions.toml)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.tikkatimer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tikkatimer"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

---

## 6. 참고 리소스

### 공식 문서
- [Android Developers - Schedule Alarms](https://developer.android.com/develop/background-work/services/alarms/schedule)
- [Kotlin for Jetpack Compose](https://developer.android.com/develop/ui/compose/kotlin)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)

### 블로그/튜토리얼
- [Modern Android Development in 2025](https://www.c-sharpcorner.com/article/modern-android-development-in-2025-a-practical-guide-for-developers/)
- [Kotlin Flow - Implementing an Android Timer](https://dev.to/aniketsmk/kotlin-flow-implementing-an-android-timer-ieo)
- [A simple guide to build a stopwatch with Jetpack Compose](https://medium.com/@rodolphefrancotte18/a-simple-guide-to-build-a-stopwatch-with-jetpack-compose-coroutines-6d62e8be4637)

### GitHub 프로젝트
- [Clock - Jetpack Compose clock app](https://github.com/yassineAbou/Clock)
- [InTimeAndroid - Interval timer app](https://github.com/p-hlp/InTimeAndroid)
- [AlarmManagerExample](https://github.com/MubarakNative/AlarmManagerExample)
