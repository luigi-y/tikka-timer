# Play Store Screenshots Guide

This directory contains screenshots for Play Store listing.

## Directory Structure

```
images/
├── phoneScreenshots/       # Phone screenshots (required)
│   ├── 1_alarm.png
│   ├── 2_timer.png
│   ├── 3_stopwatch.png
│   └── ...
├── sevenInchScreenshots/   # 7-inch tablet (optional)
└── tenInchScreenshots/     # 10-inch tablet (optional)
```

## Screenshot Requirements

### Phone Screenshots (Required)

| Spec | Requirement |
|------|-------------|
| Quantity | 4-8 screenshots (min 2) |
| Format | PNG or JPEG |
| Size | 16:9 or 9:16 aspect ratio |
| Min dimension | 320px |
| Max dimension | 3840px |
| Max file size | 8MB per image |

### Recommended Sizes

| Device Type | Portrait | Landscape |
|-------------|----------|-----------|
| Phone | 1080 x 1920 | 1920 x 1080 |
| 7" Tablet | 1200 x 1920 | 1920 x 1200 |
| 10" Tablet | 1600 x 2560 | 2560 x 1600 |

## Recommended Screenshots

### Phone (4-8 screenshots)

1. **Alarm List** - Main alarm screen showing alarm list
2. **Alarm Edit** - Alarm creation/edit screen
3. **Timer** - Timer screen with circular progress
4. **Timer Presets** - Timer presets screen
5. **Stopwatch** - Stopwatch with lap times
6. **Settings** - Theme/color customization
7. **Dark Mode** - App in dark theme
8. **Widget** (if available) - Home screen widget

## Naming Convention

Files are sorted alphabetically, so use numbered prefixes:

```
1_alarm_list.png
2_alarm_edit.png
3_timer_running.png
4_timer_presets.png
5_stopwatch_laps.png
6_settings_theme.png
7_dark_mode.png
```

## Tips for Great Screenshots

1. **Use real content** - Show realistic alarms, timers, and lap times
2. **Highlight features** - Each screenshot should show a key feature
3. **Consistent style** - Use same device frame (optional) and status bar time
4. **Clean status bar** - Full battery, good signal, clean time (e.g., 9:41)
5. **Consider localization** - Use language-appropriate content for each locale

## Creating Screenshots

### Using Android Emulator

```bash
# Take screenshot via adb
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png ./1_alarm.png
```

### Using Android Studio

1. Run app on emulator/device
2. `View` → `Tool Windows` → `Device File Explorer`
3. Navigate to desired screen
4. Click camera icon in Logcat toolbar
5. Save screenshot

## Adding Device Frames (Optional)

Use tools like:
- [Android Device Art Generator](https://developer.android.com/distribute/marketing-tools/device-art-generator)
- Figma/Sketch with device mockup templates
- Screenshot apps with built-in frames
