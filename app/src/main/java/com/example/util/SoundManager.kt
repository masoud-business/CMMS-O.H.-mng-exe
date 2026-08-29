package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * مدیریت افکت‌های صوتی و بازخورد لمسی (Haptic & Sound Feedback)
 * بدون نیاز به فایل‌های سنگین جانبی با استفاده از موتور مولد فرکانس ToneGenerator اندروید
 */
object SoundManager {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    /**
     * صدای ثبت موفقیت‌آمیز پیشرفت، لاگین، صدور پرمیت
     */
    fun playSuccess(context: Context? = null) {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
            vibrate(context, 40)
        } catch (_: Exception) {}
    }

    /**
     * صدای دکمه و کلیک‌های صنعتی
     */
    fun playClick(context: Context? = null) {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 60)
            vibrate(context, 20)
        } catch (_: Exception) {}
    }

    /**
     * صدای اخطار یا خطا / بلاک شدن پیش‌نیاز
     */
    fun playWarning(context: Context? = null) {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250)
            vibrate(context, 100)
        } catch (_: Exception) {}
    }

    /**
     * زنگ و آلارم شیفت و ثبت درصد ساعت ۴ عصر (Daily 4 PM Reminder Alarm)
     */
    fun playDailyAlarm(context: Context? = null, scope: CoroutineScope? = null) {
        val runnable = {
            try {
                // ۳ بوق متوالی و ریتمیک یادآوری
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 400)
                vibratePattern(context, longArrayOf(0, 300, 150, 300, 150, 500))
            } catch (_: Exception) {}
        }

        if (scope != null) {
            scope.launch(Dispatchers.Default) {
                runnable()
                delay(600)
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 400)
                } catch (_: Exception) {}
            }
        } else {
            runnable()
        }
    }

    private fun vibrate(context: Context?, durationMs: Long) {
        if (context == null) return
        try {
            val vibrator = getVibrator(context)
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(context: Context?, pattern: LongArray) {
        if (context == null) return
        try {
            val vibrator = getVibrator(context)
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {}
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
