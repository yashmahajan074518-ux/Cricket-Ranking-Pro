package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

object CinematicSynth {
    private const val SAMPLE_RATE = 44100

    /**
     * Synthesizes an Unreal Engine / PUBG style cinematic startup sound:
     * - Sub-bass impact with sweeping pitch drop and sub rumble
     * - Shimmering crystal chimes (G6, C7, A6, D7) with harmonics
     */
    suspend fun playStartupSound() = withContext(Dispatchers.Default) {
        try {
            val totalSeconds = 2.8
            val numSamples = (totalSeconds * SAMPLE_RATE).toInt()
            val buffer = ShortArray(numSamples)

            // Synth parameters
            fun addSine(
                startSample: Int,
                durationSeconds: Double,
                startFreq: Double,
                endFreq: Double,
                peakAmp: Double,
                decayRate: Double
            ) {
                val len = (durationSeconds * SAMPLE_RATE).toInt()
                var phase = 0.0
                for (i in 0 until len) {
                    val idx = startSample + i
                    if (idx >= numSamples) break
                    val progress = i.toDouble() / len
                    val currentFreq = startFreq + (endFreq - startFreq) * (progress * progress)
                    phase += 2.0 * Math.PI * currentFreq / SAMPLE_RATE
                    val envelope = exp(-i.toDouble() / (SAMPLE_RATE * decayRate))
                    val sampleVal = sin(phase) * peakAmp * envelope
                    val current = buffer[idx].toInt()
                    val mixed = (current + (sampleVal * Short.MAX_VALUE).toInt()).coerceIn(
                        Short.MIN_VALUE.toInt(),
                        Short.MAX_VALUE.toInt()
                    )
                    buffer[idx] = mixed.toShort()
                }
            }

            fun addNoiseThump(startSample: Int, durationSeconds: Double, peakAmp: Double) {
                val len = (durationSeconds * SAMPLE_RATE).toInt()
                var filterState = 0.0
                for (i in 0 until len) {
                    val idx = startSample + i
                    if (idx >= numSamples) break
                    val white = (Random.nextDouble() * 2.0 - 1.0)
                    filterState += 0.08 * (white - filterState) // Low pass filter
                    val env = (1.0 - i.toDouble() / len) * exp(-i.toDouble() / (SAMPLE_RATE * 0.06))
                    val sampleVal = filterState * peakAmp * env
                    val current = buffer[idx].toInt()
                    val mixed = (current + (sampleVal * Short.MAX_VALUE).toInt()).coerceIn(
                        Short.MIN_VALUE.toInt(),
                        Short.MAX_VALUE.toInt()
                    )
                    buffer[idx] = mixed.toShort()
                }
            }

            // 1. Initial Bass impact + sub drop
            val t0 = (0.05 * SAMPLE_RATE).toInt()
            addNoiseThump(t0, 0.4, 0.35)
            addSine(t0, 1.2, 150.0, 38.0, 0.75, 0.4)

            // 2. High gaming chimes (crystal arpeggio)
            val c1 = (0.08 * SAMPLE_RATE).toInt()
            addSine(c1, 1.4, 1568.0, 1568.0, 0.22, 0.5) // G6
            addSine(c1, 1.4, 3136.0, 3136.0, 0.08, 0.3) // Harmonic 2

            val c2 = (0.22 * SAMPLE_RATE).toInt()
            addSine(c2, 1.4, 2093.0, 2093.0, 0.25, 0.6) // C7
            addSine(c2, 1.4, 4186.0, 4186.0, 0.08, 0.35)

            // 3. Second crescendo impact (around 1.3s)
            val t1 = (1.30 * SAMPLE_RATE).toInt()
            addNoiseThump(t1, 0.3, 0.25)
            addSine(t1, 1.0, 120.0, 45.0, 0.6, 0.35)

            val c3 = (1.35 * SAMPLE_RATE).toInt()
            addSine(c3, 1.3, 1760.0, 1760.0, 0.22, 0.5) // A6
            val c4 = (1.50 * SAMPLE_RATE).toInt()
            addSine(c4, 1.3, 2349.0, 2349.0, 0.26, 0.6) // D7

            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(max(minBufferSize, numSamples * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, numSamples)
            audioTrack.play()

            // Release after playback
            kotlinx.coroutines.delay(2900)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        } catch (_: Exception) {
            // Audio synthesis fallback
        }
    }
}
