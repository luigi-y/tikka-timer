<p align="center">
  <img src="images/feature_graphic.png" alt="Tikka Timer">
</p>

# Tikka Timer

一款提供闹钟、计时器和秒表功能的Android应用。

[English](README.md) | [한국어](README.ko.md) | [日本語](README.ja.md) | [隐私政策](https://luigi-y.github.io/tikka-timer/privacy-policy)

## 主要功能

### 闹钟
- 添加/编辑/删除闹钟
- 重复闹钟设置（按星期）
- 贪睡功能
- 自定义闹钟铃声和振动模式
- 锁屏全屏闹钟显示

### 计时器
- 时/分/秒设置
- 暂停/继续/重置
- +1分钟快速添加
- 计时器预设保存（含铃声/振动设置）
- 圆形进度指示器
- 通过Foreground Service后台运行

### 秒表
- 开始/暂停/重置
- 圈速记录
- 毫秒级精度
- 最快/最慢圈速显示

### 小组件
- 1x1主屏幕计时器小组件
- 实时状态显示（待机/运行中/暂停/完成）

### 设置
- 主题模式（浅色/深色/跟随系统）
- 6种颜色主题
- 4种语言支持（韩语、英语、日语、中文）

## 技术栈

| 类别 | 技术 |
|------|------|
| **语言** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material Design 3 |
| **架构** | MVVM + Clean Architecture |
| **DI** | Hilt |
| **异步** | Coroutines + Flow |
| **本地数据库** | Room |
| **小组件** | RemoteViews (AppWidgetProvider) |
| **后台任务** | AlarmManager + Foreground Service |
| **测试** | JUnit, Mockk, Turbine |

## 项目结构

```
app/src/main/java/com/tikkatimer/
├── data/                    # 数据层
│   ├── local/               # Room数据库 (DAO, Entity)
│   ├── mapper/              # Entity <-> Domain转换
│   └── repository/          # Repository实现
├── domain/                  # 领域层
│   ├── model/               # 领域模型
│   ├── repository/          # Repository接口
│   └── usecase/             # UseCase类
├── presentation/            # 展示层
│   ├── alarm/               # 闹钟界面
│   ├── timer/               # 计时器界面
│   ├── stopwatch/           # 秒表界面
│   ├── settings/            # 设置界面
│   └── main/                # 主界面（标签导航）
├── di/                      # Hilt模块
├── receiver/                # BroadcastReceiver（闹钟、启动）
├── service/                 # Foreground Service
├── sync/                    # 计时器状态同步
├── util/                    # 通知、声音管理器
└── widget/                  # 主屏幕小组件
```

## 构建与运行

### 环境要求
- Android Studio Ladybug或更高版本
- JDK 21
- Android SDK 36 (min SDK 26)

### 构建
```bash
./gradlew assembleDebug
```

### 运行测试
```bash
# 单元测试
./gradlew testDebugUnitTest

# 集成测试（需要模拟器/设备）
./gradlew connectedDebugAndroidTest

# 生成覆盖率报告
./gradlew jacocoTestReport
```

### 代码质量检查
```bash
# 格式检查
./gradlew ktlintCheck

# 静态分析
./gradlew detekt

# Android Lint
./gradlew lintDebug
```

## 测试现状

### 单元测试（21个文件）
- **ViewModel**: AlarmViewModel, TimerViewModel, StopwatchViewModel, SettingsViewModel
- **UseCase**: AlarmUseCase, TimerUseCase, GetUpcomingAlarmUseCase, DisableOneTimeAlarmUseCase
- **Repository**: AlarmRepository, TimerRepository
- **Mapper**: AlarmMapper, TimerMapper
- **Domain Model**: Alarm, Timer, Stopwatch, LapTime
- **Service**: AlarmRingingService
- **Util**: NotificationHelper, AlarmSoundManager, AlarmScheduler
- **Database**: Migration

### 集成测试（3个文件）
- **Room DAO**: AlarmDao, TimerPresetDao
- **Util**: AlarmSoundManager

## 许可证

MIT License
