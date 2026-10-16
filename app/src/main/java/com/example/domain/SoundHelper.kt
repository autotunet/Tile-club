package com.example.domain

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundHelper(private val context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    var soundEnabled: Boolean = true
    var hapticEnabled: Boolean = true

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Exception) {
            toneGenerator = null
        }

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            vibrator = null
        }
    }

    fun playTileClick() {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
            } catch (_: Exception) {}
        }
        vibrate(30, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun playTripleMatch() {
        if (soundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_3, 60)
                    delay(70)
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_6, 60)
                    delay(70)
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_9, 100)
                } catch (_: Exception) {}
            }
        }
        vibrate(70, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun playBoosterUndo() {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_A, 120)
            } catch (_: Exception) {}
        }
        vibrate(50, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun playBoosterShuffle() {
        if (soundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    for (i in 0..2) {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 50)
                        delay(60)
                    }
                } catch (_: Exception) {}
            }
        }
        vibrate(80, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun playBoosterWand() {
        if (soundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_1, 60)
                    delay(60)
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_5, 60)
                    delay(60)
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_9, 120)
                } catch (_: Exception) {}
            }
        }
        vibrate(100, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun playBoosterHint() {
        if (soundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_B, 70)
                    delay(80)
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 100)
                } catch (_: Exception) {}
            }
        }
        vibrate(60, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun playStarEarned(starIndex: Int) {
        if (soundEnabled) {
            try {
                val tone = when (starIndex) {
                    0 -> ToneGenerator.TONE_DTMF_3
                    1 -> ToneGenerator.TONE_DTMF_6
                    else -> ToneGenerator.TONE_DTMF_9
                }
                toneGenerator?.startTone(tone, 80)
            } catch (_: Exception) {}
        }
        vibrate(40 + (starIndex * 20L), VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun playVictory() {
        if (soundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                val tones = listOf(
                    ToneGenerator.TONE_DTMF_1,
                    ToneGenerator.TONE_DTMF_3,
                    ToneGenerator.TONE_DTMF_5,
                    ToneGenerator.TONE_DTMF_8
                )
                tones.forEach { tone ->
                    try {
                        toneGenerator?.startTone(tone, 90)
                    } catch (_: Exception) {}
                    delay(100)
                }
            }
        }
        vibrate(150, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun playTimerTick() {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
            } catch (_: Exception) {}
        }
        vibrate(20, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun playTimeUp() {
        if (soundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 120)
                    delay(130)
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_MED_L, 120)
                    delay(130)
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_LOW_L, 220)
                } catch (_: Exception) {}
            }
        }
        vibrate(250, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    fun playGameOver() {
        if (soundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 180)
                    delay(150)
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 220)
                } catch (_: Exception) {}
            }
        }
        vibrate(200, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        if (!hapticEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
