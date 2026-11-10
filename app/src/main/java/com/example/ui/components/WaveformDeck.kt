package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceContainerLowest
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.OnCyanPrimary
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun WaveformDeck(
    isPlaying: Boolean,
    currentSec: Double,
    totalDurationSec: Double,
    onTogglePlay: () -> Unit,
    onSeek: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val barAmplitudes = remember {
        listOf(
            0.25f, 0.45f, 0.35f, 0.70f, 0.85f, 0.60f, 0.95f, 0.50f,
            0.30f, 0.65f, 0.80f, 0.40f, 0.30f, 0.75f, 0.90f, 1.00f,
            0.65f, 0.35f, 0.50f, 0.75f, 0.25f, 0.45f, 0.85f, 0.40f
        )
    }

    val progressFraction = if (totalDurationSec > 0) {
        (currentSec / totalDurationSec).coerceIn(0.0, 1.0).toFloat()
    } else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "wavePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseWave"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceContainerLowest)
            .border(1.dp, OutlineBorder.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("waveform_deck"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Play/Pause circular button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ElectricCyan)
                .clickable { onTogglePlay() }
                .testTag("play_pause_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل الصوت",
                tint = OnCyanPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Interactive Waveform visualizer bars
        Row(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .pointerInput(totalDurationSec) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(fraction * totalDurationSec)
                    }
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            barAmplitudes.forEachIndexed { index, amp ->
                val barFraction = index.toFloat() / barAmplitudes.size
                val isPassed = barFraction <= progressFraction
                val isPeak = index == 6 || index == 15

                val currentAmp = if (isPlaying && isPassed) {
                    (amp * pulseScale).coerceIn(0.2f, 1.0f)
                } else {
                    amp
                }

                val barColor = when {
                    isPeak && isPassed -> NeonEmerald
                    isPassed -> ElectricCyan
                    else -> ElectricCyan.copy(alpha = 0.25f)
                }

                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .fillMaxHeight(currentAmp)
                        .clip(CircleShape)
                        .background(barColor)
                )
            }
        }

        // Timecode display (Monospace)
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatTimecode(currentSec),
                color = ElectricCyan,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatTimecode(totalDurationSec),
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun formatTimecode(seconds: Double): String {
    val totalSec = seconds.toInt()
    val mins = totalSec / 60
    val secs = totalSec % 60
    val millis = ((seconds - totalSec) * 100).toInt()
    return String.format(Locale.US, "%02d:%02d.%02d", mins, secs, millis)
}
