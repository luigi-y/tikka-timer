package com.tikkatimer.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.tikkatimer.domain.model.SoundType
import com.tikkatimer.domain.model.VibrationPattern
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알람 소리 및 진동 관리
 * 알람음 재생, 진동 패턴 실행
 */
@Singleton
class AlarmSoundManager
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        private var mediaPlayer: MediaPlayer? = null
        private val vibrator: Vibrator? =
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get vibrator service", e)
                null
            }

        companion object {
            private const val TAG = "AlarmSoundManager"
        }

        /**
         * 알람 소리 재생 시작
         */
        fun startAlarmSound(
            soundType: SoundType,
            ringtoneUri: String? = null,
        ) {
            stopAlarmSound()

            if (soundType == SoundType.SILENT) {
                Log.d(TAG, "Sound type is SILENT, skipping playback")
                return
            }

            try {
                val uri = getAlarmSoundUri(soundType, ringtoneUri)
                Log.d(TAG, "Starting alarm sound: $uri")

                mediaPlayer =
                    MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build(),
                        )
                        setDataSource(context, uri)
                        isLooping = true
                        prepare()
                        start()
                    }

                Log.d(TAG, "Alarm sound started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start alarm sound", e)
                // 실패 시 기본 알람음으로 시도
                tryPlayDefaultAlarm()
            }
        }

        /**
         * 알람 소리 중지
         */
        fun stopAlarmSound() {
            try {
                mediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                }
                mediaPlayer = null
                Log.d(TAG, "Alarm sound stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop alarm sound", e)
            }
        }

        /**
         * 진동 시작
         */
        fun startVibration(pattern: VibrationPattern) {
            if (pattern == VibrationPattern.NONE) {
                Log.d(TAG, "Vibration pattern is NONE, skipping")
                return
            }

            if (vibrator == null) {
                Log.w(TAG, "Vibrator is not available on this device")
                return
            }

            try {
                // 진동 기능 지원 여부 확인
                if (!vibrator.hasVibrator()) {
                    Log.w(TAG, "Device does not have vibrator hardware")
                    return
                }

                Log.d(TAG, "Starting vibration: $pattern")

                val vibrationPattern = pattern.pattern
                if (vibrationPattern.isEmpty()) return

                // minSdk=26 (Android O)이므로 VibrationEffect 사용 가능
                val effect = VibrationEffect.createWaveform(vibrationPattern, 0)
                vibrator.vibrate(effect)

                Log.d(TAG, "Vibration started successfully")
            } catch (e: SecurityException) {
                Log.e(TAG, "VIBRATE permission not granted", e)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start vibration", e)
            }
        }

        /**
         * 진동 중지
         */
        fun stopVibration() {
            try {
                vibrator?.cancel()
                Log.d(TAG, "Vibration stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop vibration", e)
            }
        }

        /**
         * 모든 알람 효과 중지
         */
        fun stopAll() {
            stopAlarmSound()
            stopVibration()
        }

        /**
         * 알람 소리 URI 가져오기
         * 커스텀 알람음 파일이 없으면 시스템 기본 알람음 사용
         */
        private fun getAlarmSoundUri(
            soundType: SoundType,
            ringtoneUri: String?,
        ): Uri {
            val defaultAlarmUri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            return when (soundType) {
                SoundType.CUSTOM -> {
                    ringtoneUri?.let { Uri.parse(it) } ?: defaultAlarmUri
                }
                SoundType.DEFAULT -> defaultAlarmUri
                // BELL, DIGITAL, GENTLE은 현재 기본 알람음 사용
                // 추후 res/raw에 알람음 파일 추가 시 해당 리소스 사용 가능
                SoundType.BELL -> defaultAlarmUri
                SoundType.DIGITAL -> defaultAlarmUri
                SoundType.GENTLE -> defaultAlarmUri
                SoundType.SILENT -> defaultAlarmUri // SILENT는 이 함수 호출 전에 처리됨
            }
        }

        /**
         * 기본 알람음 재생 시도
         */
        private fun tryPlayDefaultAlarm() {
            try {
                val uri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                mediaPlayer =
                    MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build(),
                        )
                        setDataSource(context, uri)
                        isLooping = true
                        prepare()
                        start()
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play default alarm", e)
            }
        }
    }
