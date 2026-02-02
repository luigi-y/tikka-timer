# Play Store Metadata

This directory contains Play Store listing metadata in Fastlane format.

## Directory Structure

```
fastlane/metadata/android/
├── en-US/                        # English (US) - Default
│   ├── title.txt                 # App title (max 30 chars)
│   ├── short_description.txt     # Short description (max 80 chars)
│   ├── full_description.txt      # Full description (max 4000 chars)
│   ├── changelogs/
│   │   └── {versionCode}.txt     # Release notes for each version
│   └── images/
│       ├── phoneScreenshots/     # Phone screenshots (required)
│       ├── sevenInchScreenshots/ # 7" tablet screenshots (optional)
│       └── tenInchScreenshots/   # 10" tablet screenshots (optional)
├── ko-KR/                        # Korean
├── ja-JP/                        # Japanese
├── zh-CN/                        # Chinese (Simplified)
└── README.md                     # This file
```

## Character Limits

| Field | Limit |
|-------|-------|
| Title | 30 characters |
| Short Description | 80 characters |
| Full Description | 4000 characters |
| Release Notes | 500 characters |

## Changelog Naming

Changelog files should be named with the `versionCode` from `build.gradle.kts`:
- `1.txt` for versionCode 1 (v1.0.0)
- `2.txt` for versionCode 2 (v1.0.1)
- etc.

## Screenshot Requirements

| Spec | Requirement |
|------|-------------|
| Quantity | 4-8 screenshots (min 2) |
| Format | PNG or JPEG |
| Aspect ratio | 16:9 or 9:16 |
| Min dimension | 320px |
| Max dimension | 3840px |
| Max file size | 8MB per image |

### Recommended Screenshots

1. Alarm list screen
2. Alarm edit screen
3. Timer with progress
4. Timer presets
5. Stopwatch with lap times
6. Settings/themes
7. Dark mode

See `en-US/images/README.md` for detailed screenshot guidelines.

## Supported Languages

| Code | Language |
|------|----------|
| en-US | English (United States) |
| ko-KR | Korean |
| ja-JP | Japanese |
| zh-CN | Chinese (Simplified) |

## Adding a New Language

1. Create a new directory with the locale code (e.g., `fr-FR` for French)
2. Copy files from `en-US/` as templates
3. Translate all content
4. Add the language to this README

## Usage with Fastlane

If you set up Fastlane for automated deployment:

```bash
# Upload metadata only
fastlane supply --metadata_path fastlane/metadata/android

# Upload with APK/AAB
fastlane supply --aab app/build/outputs/bundle/release/app-release.aab
```

## Manual Upload

Copy the content from these files when filling out the Play Console:
1. Go to Play Console > Your App > Store presence > Main store listing
2. Paste content from respective files
3. Upload screenshots from `images/phoneScreenshots/`
4. Repeat for each language in "Manage translations"
