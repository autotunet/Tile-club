package com.example.domain

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * SoundHelper moderno con síntesis de sonido acústico de alta definición (PCM 44.1kHz),
 * motor de audio SoundPool polifónico y retroalimentación háptica táctil de precisión.
 */
class SoundHelper(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var vibrator: Vibrator? = null

    var soundEnabled: Boolean = true
    var hapticEnabled: Boolean = true

    // IDs de sonidos cargados en SoundPool
    private var soundTileClick: Int = 0
    private var soundTripleMatch: Int = 0
    private var soundVictory: Int = 0
    private var soundGameOver: Int = 0
    private var soundStar1: Int = 0
    private var soundStar2: Int = 0
    private var soundStar3: Int = 0
    private var soundHint: Int = 0
    private var soundUndo: Int = 0
    private var soundShuffle: Int = 0
    private var soundWand: Int = 0
    private var soundTimerTick: Int = 0
    private var soundTimeUp: Int = 0

    private var clickCounter: Int = 0

    init {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build()
        } catch (_: Exception) {
            soundPool = null
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

        // Generar y cargar el catálogo de efectos de sonido modernos en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            initModernAudioCatalog()
        }
    }

    private fun initModernAudioCatalog() {
        val pool = soundPool ?: return
        val soundDir = File(context.cacheDir, "audio_modern_v2")
        if (!soundDir.exists()) {
            soundDir.mkdirs()
        }

        try {
            soundTileClick = loadSoundFile(pool, soundDir, "tile_pop.wav") { generateTilePop() }
            soundTripleMatch = loadSoundFile(pool, soundDir, "triple_match.wav") { generateTripleMatch() }
            soundVictory = loadSoundFile(pool, soundDir, "victory_fanfare.wav") { generateVictory() }
            soundGameOver = loadSoundFile(pool, soundDir, "game_over.wav") { generateGameOver() }
            soundStar1 = loadSoundFile(pool, soundDir, "star_1.wav") { generateStarBell(1046.5) }
            soundStar2 = loadSoundFile(pool, soundDir, "star_2.wav") { generateStarBell(1318.5) }
            soundStar3 = loadSoundFile(pool, soundDir, "star_3.wav") { generateStarBell(1568.0) }
            soundHint = loadSoundFile(pool, soundDir, "booster_hint.wav") { generateBoosterHint() }
            soundUndo = loadSoundFile(pool, soundDir, "booster_undo.wav") { generateBoosterUndo() }
            soundShuffle = loadSoundFile(pool, soundDir, "booster_shuffle.wav") { generateBoosterShuffle() }
            soundWand = loadSoundFile(pool, soundDir, "booster_wand.wav") { generateBoosterWand() }
            soundTimerTick = loadSoundFile(pool, soundDir, "timer_tick.wav") { generateTimerTick() }
            soundTimeUp = loadSoundFile(pool, soundDir, "time_up.wav") { generateTimeUp() }
        } catch (_: Exception) {}
    }

    private fun loadSoundFile(
        pool: SoundPool,
        dir: File,
        filename: String,
        generator: () -> ShortArray
    ): Int {
        val file = File(dir, filename)
        if (!file.exists() || file.length() < 44L) {
            val samples = generator()
            val wavBytes = createWavByteArray(samples)
            file.writeBytes(wavBytes)
        }
        return pool.load(file.absolutePath, 1)
    }

    private fun playSound(soundId: Int, volume: Float = 1.0f, pitch: Float = 1.0f) {
        if (!soundEnabled || soundId == 0) return
        try {
            soundPool?.play(soundId, volume, volume, 1, 0, pitch.coerceIn(0.5f, 2.0f))
        } catch (_: Exception) {}
    }

    // --- Métodos de Interfaz Pública del Juego ---

    /**
     * Clic de ficha: toque pop de marimba orgánica moderno con micro-variación tonal.
     */
    fun playTileClick() {
        if (soundEnabled) {
            val pitch = 0.96f + ((clickCounter++ % 5) * 0.025f)
            playSound(soundTileClick, volume = 0.85f, pitch = pitch)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibratePredefined(VibrationEffect.EFFECT_CLICK)
        } else {
            vibrate(22, 160)
        }
    }

    /**
     * Trío completado: acorde arpegiado cristalino de alta fidelidad (E6-G#6-B6-E7).
     */
    fun playTripleMatch() {
        if (soundEnabled) {
            playSound(soundTripleMatch, volume = 1.0f, pitch = 1.0f)
        }
        vibrateWaveform(longArrayOf(0, 30, 40, 55), intArrayOf(0, 180, 0, 255))
    }

    /**
     * Deshacer: barrido de pitch ascendente suave estilo retroceso.
     */
    fun playBoosterUndo() {
        if (soundEnabled) {
            playSound(soundUndo, volume = 0.9f)
        }
        vibrate(40, 170)
    }

    /**
     * Mezclar: cascada rítmica de fichas de madera.
     */
    fun playBoosterShuffle() {
        if (soundEnabled) {
            playSound(soundShuffle, volume = 0.95f)
        }
        vibrateWaveform(
            longArrayOf(0, 25, 30, 30, 30, 45),
            intArrayOf(0, 140, 0, 190, 0, 255)
        )
    }

    /**
     * Varita mágica: glissando de arpa celestial centelleante.
     */
    fun playBoosterWand() {
        if (soundEnabled) {
            playSound(soundWand, volume = 1.0f)
        }
        vibrateWaveform(
            longArrayOf(0, 35, 30, 45, 25, 70),
            intArrayOf(0, 120, 0, 180, 0, 255)
        )
    }

    /**
     * Pista: campana armónica mística con vibrato brillante.
     */
    fun playBoosterHint() {
        if (soundEnabled) {
            playSound(soundHint, volume = 0.95f)
        }
        vibrateWaveform(longArrayOf(0, 40, 30, 40), intArrayOf(0, 150, 0, 200))
    }

    /**
     * Estrella obtenida: timbre puro de campana dorada escalonada por índice.
     */
    fun playStarEarned(starIndex: Int) {
        if (soundEnabled) {
            val sound = when (starIndex) {
                0 -> soundStar1
                1 -> soundStar2
                else -> soundStar3
            }
            playSound(sound, volume = 0.95f)
        }
        vibrate(35 + (starIndex * 25L), 180 + (starIndex * 25))
    }

    /**
     * Victoria de nivel: fanfarria orquestal triunfal luminosa con halo de campanas.
     */
    fun playVictory() {
        if (soundEnabled) {
            playSound(soundVictory, volume = 1.0f)
        }
        vibrateWaveform(
            longArrayOf(0, 45, 35, 60, 35, 80, 35, 140),
            intArrayOf(0, 140, 0, 180, 0, 220, 0, 255)
        )
    }

    /**
     * Tic del temporizador en zona crítica: golpecito suave de madera minimalista.
     */
    fun playTimerTick() {
        if (soundEnabled) {
            playSound(soundTimerTick, volume = 0.75f)
        }
        vibrate(18, 90)
    }

    /**
     * Tiempo agotado: campanas descendentes suaves y melódicas.
     */
    fun playTimeUp() {
        if (soundEnabled) {
            playSound(soundTimeUp, volume = 0.95f)
        }
        vibrateWaveform(longArrayOf(0, 100, 50, 180), intArrayOf(0, 200, 0, 240))
    }

    /**
     * Fin de partida (bandeja saturada): acorde melancólico suave tipo piano Rhodes.
     */
    fun playGameOver() {
        if (soundEnabled) {
            playSound(soundGameOver, volume = 0.95f)
        }
        vibrateWaveform(longArrayOf(0, 80, 60, 160), intArrayOf(0, 200, 0, 240))
    }

    // --- Retroalimentación Háptica ---

    private fun vibrate(durationMs: Long, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        if (!hapticEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
        if (!hapticEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePredefined(effectId: Int) {
        if (!hapticEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(effectId))
            } else {
                vibrate(22, 160)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
        } catch (_: Exception) {}
    }

    // --- Motores de Síntesis de Sonido Moderno (PCM 44.1kHz, 16-bit Mono) ---

    companion object {
        private const val SAMPLE_RATE = 44100

        private fun createWavByteArray(samples: ShortArray): ByteArray {
            val numChannels = 1
            val bitsPerSample = 16
            val byteRate = SAMPLE_RATE * numChannels * bitsPerSample / 8
            val blockAlign = (numChannels * bitsPerSample / 8).toShort()
            val dataSize = samples.size * 2
            val totalSize = 36 + dataSize

            val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
            buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
            buffer.putInt(totalSize)
            buffer.put("WAVE".toByteArray(Charsets.US_ASCII))

            buffer.put("fmt ".toByteArray(Charsets.US_ASCII))
            buffer.putInt(16)
            buffer.putShort(1.toShort()) // PCM format
            buffer.putShort(numChannels.toShort())
            buffer.putInt(SAMPLE_RATE)
            buffer.putInt(byteRate)
            buffer.putShort(blockAlign)
            buffer.putShort(bitsPerSample.toShort())

            buffer.put("data".toByteArray(Charsets.US_ASCII))
            buffer.putInt(dataSize)
            for (s in samples) {
                buffer.putShort(s)
            }
            return buffer.array()
        }

        /**
         * Pop de madera/marimba moderno (70ms) con ataque nítido y decaimiento cálido.
         */
        private fun generateTilePop(): ShortArray {
            val durationSec = 0.075
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val result = ShortArray(totalSamples)
            var phase = 0.0
            val rand = Random(42)

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // Caída sutil de frecuencia para dar sensación de percusión elástica
                val freq = 740.0 - (120.0 * (t / durationSec))
                phase += 2.0 * PI * freq / SAMPLE_RATE

                // Armónico fundamental + cuerpo de madera + clic transitorio
                val fundamental = sin(phase)
                val overtone = sin(phase * 2.01) * 0.25
                val woodClick = if (t < 0.003) (rand.nextDouble() - 0.5) * 0.35 else 0.0

                val envelope = exp(-t * 52.0)
                val sampleValue = ((fundamental + overtone + woodClick) * envelope).coerceIn(-1.0, 1.0)
                result[i] = (sampleValue * 28000).toInt().toShort()
            }
            return result
        }

        /**
         * Trío completado: acorde arpegiado ascendente en E mayor (1318Hz, 1661Hz, 1975Hz, 2637Hz).
         */
        private fun generateTripleMatch(): ShortArray {
            val durationSec = 0.45
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val result = ShortArray(totalSamples)

            val notes = listOf(
                1318.5 to 0.00, // E6
                1661.2 to 0.06, // G#6
                1975.5 to 0.12, // B6
                2637.0 to 0.18  // E7
            )

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                var sum = 0.0

                for ((freq, startTime) in notes) {
                    if (t >= startTime) {
                        val dt = t - startTime
                        val attack = min(1.0, dt * 500.0)
                        val decay = exp(-dt * 13.0)
                        val notePhase = 2.0 * PI * freq * dt
                        val chime = sin(notePhase) + 0.3 * sin(notePhase * 2.0) + 0.1 * sin(notePhase * 3.0)
                        sum += chime * attack * decay * 0.42
                    }
                }

                result[i] = (sum.coerceIn(-1.0, 1.0) * 29000).toInt().toShort()
            }
            return result
        }

        /**
         * Fanfarria de victoria triunfal con campanas y brillo celestial.
         */
        private fun generateVictory(): ShortArray {
            val durationSec = 0.95
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val result = ShortArray(totalSamples)

            val notes = listOf(
                1046.5 to 0.00, // C6
                1318.5 to 0.11, // E6
                1568.0 to 0.22, // G6
                2093.0 to 0.33, // C7
                2637.0 to 0.44  // E7 campana alta
            )

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                var sum = 0.0

                for ((freq, startTime) in notes) {
                    if (t >= startTime) {
                        val dt = t - startTime
                        val attack = min(1.0, dt * 400.0)
                        val decay = exp(-dt * 6.5)
                        val vibrato = 1.0 + 0.004 * sin(2.0 * PI * 6.0 * dt)
                        val notePhase = 2.0 * PI * freq * vibrato * dt
                        val tone = sin(notePhase) + 0.25 * sin(notePhase * 2.0) + 0.08 * sin(notePhase * 3.5)
                        sum += tone * attack * decay * 0.38
                    }
                }

                result[i] = (sum.coerceIn(-1.0, 1.0) * 29000).toInt().toShort()
            }
            return result
        }

        /**
         * Campana de estrella (C6, E6, G6) con brillo y rica resonancia natural.
         */
        private fun generateStarBell(baseFreq: Double): ShortArray {
            val durationSec = 0.40
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val result = ShortArray(totalSamples)

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val attack = min(1.0, t * 600.0)
                val decay = exp(-t * 9.0)
                val phase = 2.0 * PI * baseFreq * t
                val bell = sin(phase) + 0.32 * sin(phase * 2.76) + 0.12 * sin(phase * 5.4)
                val sampleValue = (bell * attack * decay).coerceIn(-1.0, 1.0)
                result[i] = (sampleValue * 28000).toInt().toShort()
            }
            return result
        }

        /**
         * Pista: dúo de campanas etéreas.
         */
        private fun generateBoosterHint(): ShortArray {
            val durationSec = 0.38
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val result = ShortArray(totalSamples)

            val notes = listOf(880.0 to 0.00, 1174.6 to 0.09) // A5 -> D6

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                var sum = 0.0
                for ((freq, startTime) in notes) {
                    if (t >= startTime) {
                        val dt = t - startTime
                        val attack = min(1.0, dt * 500.0)
                        val decay = exp(-dt * 10.0)
                        val phase = 2.0 * PI * freq * dt
                        sum += (sin(phase) + 0.28 * sin(phase * 2.0)) * attack * decay * 0.52
                    }
                }
                result[i] = (sum.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
            }
            return result
        }

        /**
         * Deshacer: barrido ascendente limpio.
         */
        private fun generateBoosterUndo(): ShortArray {
            val durationSec = 0.20
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val result = ShortArray(totalSamples)
            var phase = 0.0

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / durationSec
                val freq = 340.0 + (380.0 * (t * t)) // curva suave hacia arriba
                phase += 2.0 * PI * freq / SAMPLE_RATE
                val envelope = sin(PI * t.coerceIn(0.0, 1.0))
                val sampleValue = sin(phase) * envelope
                result[i] = (sampleValue.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
            }
            return result
        }

        /**
         * Mezclar: trío percusivo de maderas rápidas.
         */
        private fun generateBoosterShuffle(): ShortArray {
            val durationSec = 0.28
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val result = ShortArray(totalSamples)

            val taps = listOf(560.0 to 0.00, 680.0 to 0.07, 840.0 to 0.14)

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                var sum = 0.0
                for ((freq, startTime) in taps) {
                    if (t >= startTime) {
                        val dt = t - startTime
                        val decay = exp(-dt * 45.0)
                        val phase = 2.0 * PI * freq * dt
                        sum += (sin(phase) + 0.25 * sin(phase * 2.0)) * decay * 0.48
                    }
                }
                result[i] = (sum.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
            }
            return result
        }

        /**
         * Varita: glissando de arpa pentatónica brillante.
         */
        private fun generateBoosterWand(): ShortArray {
            val durationSec = 0.52
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val result = ShortArray(totalSamples)

            val notes = listOf(
                1046.5 to 0.00, // C6
                1318.5 to 0.05, // E6
                1568.0 to 0.10, // G6
                1760.0 to 0.15, // A6
                2093.0 to 0.20, // C7
                2637.0 to 0.25  // E7
            )

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                var sum = 0.0
                for ((freq, startTime) in notes) {
                    if (t >= startTime) {
                        val dt = t - startTime
                        val attack = min(1.0, dt * 500.0)
                        val decay = exp(-dt * 12.0)
                        val phase = 2.0 * PI * freq * dt
                        sum += (sin(phase) + 0.25 * sin(phase * 2.0)) * attack * decay * 0.32
                    }
                }
                result[i] = (sum.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
            }
            return result
        }

        /**
         * Fin de juego: secuencia melódica descendente cálida (G5 -> Eb5 -> C5).
         */
        private fun generateGameOver(): ShortArray {
            val durationSec = 0.60
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val result = ShortArray(totalSamples)

            val notes = listOf(
                783.99 to 0.00, // G5
                622.25 to 0.15, // Eb5
                523.25 to 0.30  // C5
            )

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                var sum = 0.0
                for ((freq, startTime) in notes) {
                    if (t >= startTime) {
                        val dt = t - startTime
                        val attack = min(1.0, dt * 300.0)
                        val decay = exp(-dt * 7.0)
                        val phase = 2.0 * PI * freq * dt
                        sum += (sin(phase) + 0.2 * sin(phase * 2.0)) * attack * decay * 0.45
                    }
                }
                result[i] = (sum.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
            }
            return result
        }

        /**
         * Tic del temporizador: toque sutil de madera minimalista (30ms).
         */
        private fun generateTimerTick(): ShortArray {
            val durationSec = 0.035
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val result = ShortArray(totalSamples)

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val decay = exp(-t * 90.0)
                val phase = 2.0 * PI * 580.0 * t
                val sampleValue = sin(phase) * decay
                result[i] = (sampleValue.coerceIn(-1.0, 1.0) * 22000).toInt().toShort()
            }
            return result
        }

        /**
         * Tiempo agotado: campanas descendentes suaves.
         */
        private fun generateTimeUp(): ShortArray {
            val durationSec = 0.45
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val result = ShortArray(totalSamples)

            val notes = listOf(659.25 to 0.00, 440.0 to 0.14) // E5 -> A4

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                var sum = 0.0
                for ((freq, startTime) in notes) {
                    if (t >= startTime) {
                        val dt = t - startTime
                        val attack = min(1.0, dt * 400.0)
                        val decay = exp(-dt * 9.0)
                        val phase = 2.0 * PI * freq * dt
                        sum += (sin(phase) + 0.25 * sin(phase * 2.0)) * attack * decay * 0.5
                    }
                }
                result[i] = (sum.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
            }
            return result
        }
    }
}
