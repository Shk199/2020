package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class AudioPlayerEngine(private val scope: CoroutineScope) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionSec = MutableStateFlow(1.76) // matching mockup initial time
    val currentPositionSec: StateFlow<Double> = _currentPositionSec.asStateFlow()

    private val _durationSec = MutableStateFlow(222.0)
    val durationSec: StateFlow<Double> = _durationSec.asStateFlow()

    private var playbackJob: Job? = null
    private var soundJob: Job? = null

    fun setDuration(duration: Double) {
        _durationSec.value = duration
    }

    fun togglePlay() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = scope.launch(Dispatchers.Default) {
            startAudioToneStream()
            val startTime = System.currentTimeMillis() - (_currentPositionSec.value * 1000).toLong()
            while (isActive && _isPlaying.value) {
                val elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0
                if (elapsedSec >= _durationSec.value) {
                    _currentPositionSec.value = 0.0
                    _isPlaying.value = false
                    break
                } else {
                    _currentPositionSec.value = elapsedSec
                }
                delay(40) // ~25 fps update for smooth waveform animation
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun seekTo(seconds: Double) {
        val clamped = seconds.coerceIn(0.0, _durationSec.value)
        _currentPositionSec.value = clamped
        if (_isPlaying.value) {
            play()
        }
    }

    fun playSegment(startSec: Double, endSec: Double) {
        _isPlaying.value = true
        _currentPositionSec.value = startSec
        playbackJob?.cancel()
        playbackJob = scope.launch(Dispatchers.Default) {
            playToneSnippet((endSec - startSec).coerceAtLeast(0.2))
            val durationMs = ((endSec - startSec) * 1000).toLong().coerceAtLeast(200)
            val startTime = System.currentTimeMillis()
            while (isActive && System.currentTimeMillis() - startTime < durationMs) {
                val elapsedFraction = (System.currentTimeMillis() - startTime).toDouble() / durationMs
                _currentPositionSec.value = startSec + elapsedFraction * (endSec - startSec)
                delay(30)
            }
            _currentPositionSec.value = endSec
            _isPlaying.value = false
        }
    }

    private fun playToneSnippet(durationSeconds: Double) {
        soundJob?.cancel()
        soundJob = scope.launch(Dispatchers.IO) {
            try {
                val sampleRate = 22050
                val numSamples = (durationSeconds * sampleRate).toInt().coerceIn(1000, 44100 * 5)
                val buffer = ShortArray(numSamples)
                val freq = 440.0 // pleasant tech beep tone A4
                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i * freq / sampleRate
                    // Apply envelope to avoid clicks
                    val envelope = if (i < 500) i / 500.0 else if (i > numSamples - 500) (numSamples - i) / 500.0 else 1.0
                    buffer[i] = (sin(angle) * 12000 * envelope).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                delay((durationSeconds * 1000).toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Graceful fallback if device audio permission/state restricts
            }
        }
    }

    private fun startAudioToneStream() {
        // Subtle background speech simulation acoustic resonance
        playToneSnippet(0.5)
    }

    fun release() {
        pause()
        soundJob?.cancel()
    }
}
