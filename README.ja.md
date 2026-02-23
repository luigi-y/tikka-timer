<p align="center">
  <img src="images/feature_graphic.png" alt="Tikka Timer">
</p>

# Tikka Timer

アラーム、タイマー、ストップウォッチ機能を提供するAndroidアプリです。

[English](README.md) | [한국어](README.ko.md) | [中文](README.zh.md) | [プライバシーポリシー](https://luigi-y.github.io/tikka-timer/privacy-policy)

## 主な機能

### アラーム
- アラームの追加/編集/削除
- 繰り返しアラーム設定（曜日別）
- スヌーズ機能
- カスタムアラーム音＆バイブレーションパターン
- ロック画面上のフルスクリーンアラーム

### タイマー
- 時/分/秒の設定
- 一時停止/再開/リセット
- +1分追加機能
- タイマープリセット保存（音/バイブレーション設定含む）
- 円形プログレスインジケーター
- Foreground Serviceによるバックグラウンド実行

### ストップウォッチ
- スタート/一時停止/リセット
- ラップタイム記録
- ミリ秒単位表示
- 最高/最低ラップタイム表示

### ウィジェット
- 1x1ホーム画面タイマーウィジェット
- リアルタイムステータス表示（待機/実行中/一時停止/完了）

### 設定
- テーマモード（ライト/ダーク/システム）
- 6種類のカラーテーマ
- 4言語対応（韓国語、英語、日本語、中国語）

## 技術スタック

| カテゴリ | 技術 |
|----------|------|
| **言語** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material Design 3 |
| **アーキテクチャ** | MVVM + Clean Architecture |
| **DI** | Hilt |
| **非同期** | Coroutines + Flow |
| **ローカルDB** | Room |
| **ウィジェット** | RemoteViews (AppWidgetProvider) |
| **バックグラウンド** | AlarmManager + Foreground Service |
| **テスト** | JUnit, Mockk, Turbine |

## プロジェクト構成

```
app/src/main/java/com/tikkatimer/
├── data/                    # Dataレイヤー
│   ├── local/               # Room Database (DAO, Entity)
│   ├── mapper/              # Entity <-> Domain変換
│   └── repository/          # Repository実装
├── domain/                  # Domainレイヤー
│   ├── model/               # ドメインモデル
│   ├── repository/          # Repositoryインターフェース
│   └── usecase/             # UseCaseクラス
├── presentation/            # Presentationレイヤー
│   ├── alarm/               # アラーム画面
│   ├── timer/               # タイマー画面
│   ├── stopwatch/           # ストップウォッチ画面
│   ├── settings/            # 設定画面
│   └── main/                # メイン画面（タブナビゲーション）
├── di/                      # Hiltモジュール
├── receiver/                # BroadcastReceiver（アラーム、ブート）
├── service/                 # Foreground Service
├── sync/                    # タイマー状態同期
├── util/                    # 通知、サウンドマネージャー
└── widget/                  # ホーム画面ウィジェット
```

## ビルド＆実行

### 要件
- Android Studio Ladybug以降
- JDK 21
- Android SDK 36 (min SDK 26)

### ビルド
```bash
./gradlew assembleDebug
```

### テスト実行
```bash
# Unit Test
./gradlew testDebugUnitTest

# Integration Test（エミュレータ/デバイスが必要）
./gradlew connectedDebugAndroidTest

# カバレッジレポート生成
./gradlew jacocoTestReport
```

### コード品質チェック
```bash
# フォーマットチェック
./gradlew ktlintCheck

# 静的解析
./gradlew detekt

# Android Lint
./gradlew lintDebug
```

## テスト状況

### Unit Test（21ファイル）
- **ViewModel**: AlarmViewModel, TimerViewModel, StopwatchViewModel, SettingsViewModel
- **UseCase**: AlarmUseCase, TimerUseCase, GetUpcomingAlarmUseCase, DisableOneTimeAlarmUseCase
- **Repository**: AlarmRepository, TimerRepository
- **Mapper**: AlarmMapper, TimerMapper
- **Domain Model**: Alarm, Timer, Stopwatch, LapTime
- **Service**: AlarmRingingService
- **Util**: NotificationHelper, AlarmSoundManager, AlarmScheduler
- **Database**: Migration

### Integration Test（3ファイル）
- **Room DAO**: AlarmDao, TimerPresetDao
- **Util**: AlarmSoundManager

## ライセンス

MIT License
