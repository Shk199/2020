package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WordTimelineItem
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceContainerHigh
import com.example.ui.theme.DarkSurfaceContainerLowest
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.OnCyanPrimary
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.SoftTechBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimelineVisualizerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPlaying by viewModel.audioEngine.isPlaying.collectAsState()
    val currentPositionSec by viewModel.audioEngine.currentPositionSec.collectAsState()
    val durationSec by viewModel.audioEngine.durationSec.collectAsState()

    val currentSegment = remember(currentPositionSec, uiState.alignmentPayload.segments) {
        uiState.alignmentPayload.segments.find {
            currentPositionSec >= it.start && currentPositionSec <= it.end
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ViewTimeline,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "الخط الزمني والمزامنة الفورية",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Live Subtitle Display Box (Karaoke-like preview)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceContainer)
                .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(20.dp)
                .testTag("karaoke_subtitle_box"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "معاينة شريط الترجمة والمزامنة",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = currentSegment?.text ?: "— جاهز للمزامنة —",
                    color = if (currentSegment != null) NeonEmerald else TextSecondary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (currentSegment != null) {
                    Text(
                        text = "${currentSegment.formattedTimeRange()} • ثقة ${currentSegment.formattedConfidence()}",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Timeline Scrubber Player Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceContainer)
                .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.audioFileName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${formatSeconds(currentPositionSec)} / ${formatSeconds(durationSec)}",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = currentPositionSec.toFloat(),
                onValueChange = { viewModel.audioEngine.seekTo(it.toDouble()) },
                valueRange = 0f..durationSec.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = ElectricCyan,
                    activeTrackColor = ElectricCyan,
                    inactiveTrackColor = DarkSurfaceContainerHigh
                ),
                modifier = Modifier.testTag("timeline_slider")
            )

            // Playback transport buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceContainerHigh)
                        .clickable {
                            viewModel.audioEngine.seekTo((currentPositionSec - 5.0).coerceAtLeast(0.0))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "تراجع 5 ثوان",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan)
                        .clickable { viewModel.audioEngine.togglePlay() }
                        .testTag("timeline_play_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                        tint = OnCyanPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceContainerHigh)
                        .clickable {
                            viewModel.audioEngine.seekTo((currentPositionSec + 5.0).coerceAtMost(durationSec))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "تقديم 5 ثوان",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Interactive Word Chips Matrix (Karaoke Flow)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceContainer)
                .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "مصفوفة الكلمات التفاعلية (انقر للقفز الزمني)",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.alignmentPayload.segments.forEach { seg ->
                    val isCurrent = currentPositionSec >= seg.start && currentPositionSec <= seg.end
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) ElectricCyan else DarkSurfaceContainerLowest)
                            .border(
                                1.dp,
                                if (isCurrent) NeonEmerald else OutlineBorder.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.playSegment(seg) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = seg.text,
                            color = if (isCurrent) OnCyanPrimary else TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Full Detailed Segment List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceContainer)
                .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "جميع المقاطع (${uiState.alignmentPayload.segments.size} مقطع)",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            uiState.alignmentPayload.segments.forEach { seg ->
                val isActive = currentPositionSec >= seg.start && currentPositionSec <= seg.end
                WordTimelineItem(
                    segment = seg,
                    isActive = isActive,
                    onPlayWord = { viewModel.playSegment(seg) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

private fun formatSeconds(seconds: Double): String {
    val totalSec = seconds.toInt()
    val mins = totalSec / 60
    val secs = totalSec % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
