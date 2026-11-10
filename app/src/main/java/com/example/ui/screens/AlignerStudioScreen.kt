package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OutputFormat
import com.example.ui.components.CodeViewerSurface
import com.example.ui.components.DiagnosticStrip
import com.example.ui.components.WaveformDeck
import com.example.ui.components.WordTimelineItem
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceContainerHigh
import com.example.ui.theme.DarkSurfaceContainerLow
import com.example.ui.theme.DarkSurfaceContainerLowest
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.OnCyanPrimary
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.SoftTechBlue
import com.example.ui.theme.TechBlueContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AlignerViewModel
import java.util.Locale

@Composable
fun AlignerStudioScreen(
    viewModel: AlignerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPlaying by viewModel.audioEngine.isPlaying.collectAsState()
    val currentPositionSec by viewModel.audioEngine.currentPositionSec.collectAsState()
    val durationSec by viewModel.audioEngine.durationSec.collectAsState()
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    val pulseTransition = rememberInfiniteTransition(label = "serverPulse")
    val serverPulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "serverAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Header matching HTML mockup
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface.copy(alpha = 0.95f))
                .border(0.dp, Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "أيقونة مُطابق الصوت",
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "مُطابق الصوت والنصوص",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .alpha(serverPulseAlpha)
                                .clip(CircleShape)
                                .background(NeonEmerald)
                        )
                        Text(
                            text = "السيرفر جاهز ⚡",
                            color = NeonEmerald,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan)
                    .clickable { viewModel.showPresetPickerDialog(true) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "الملف الشخصي والمحرك",
                    tint = OnCyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Diagnostic Strip
            DiagnosticStrip(
                engineName = uiState.selectedEngine,
                latencyMs = uiState.latencyMs,
                onClick = { viewModel.showPresetPickerDialog(true) }
            )

            // 2. Dual Inputs Bento Container
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Input,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "مدخلات المعالجة الصوتية",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "ملفان جاهزان للمطابقة",
                        color = NeonEmerald,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Audio Source Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceContainer)
                        .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                        .testTag("audio_source_card"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AudioFile,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = uiState.audioFileName,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DarkSurfaceContainerHigh)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = uiState.audioFormatTag,
                                            color = NeonEmerald,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Text(
                                    text = "${String.format(Locale.US, "%.1f", uiState.audioSizeMb)} MB • يدعم MP3, WAV, M4A",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Replace Button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceContainerHigh)
                                .clickable { viewModel.showPresetPickerDialog(true) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("replace_audio_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "استبدال",
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "استبدال",
                                color = ElectricCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Waveform Deck Mini
                    WaveformDeck(
                        isPlaying = isPlaying,
                        currentSec = currentPositionSec,
                        totalDurationSec = durationSec,
                        onTogglePlay = { viewModel.audioEngine.togglePlay() },
                        onSeek = { viewModel.audioEngine.seekTo(it) }
                    )
                }

                // Reference Text Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceContainer)
                        .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                        .testTag("reference_text_card"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TechBlueContainer.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = SoftTechBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "النص المرجعي الصوتي (UTF-8)",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "ملف نصي مطابق للرسم العثماني المصحفي",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Edit Button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceContainerHigh)
                                .clickable { viewModel.showTextEditDialog(true) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("edit_text_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل",
                                tint = SoftTechBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "تعديل",
                                color = SoftTechBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Arabic Script Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceContainerLowest)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = uiState.referenceText,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Meta Token stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tokenCount = uiState.referenceText.split("\\s+".toRegex()).count { it.isNotBlank() }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceContainerHigh)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${tokenCount * 4} رمز / توكن (Tokens)",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceContainerHigh)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$tokenCount كلمة مقطوعة",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "ترميز سليم 100%",
                                color = NeonEmerald,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 3. Hyperparameters Section (2x2 Grid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceContainerLow)
                    .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "معايير دقة المحاذاة (Hyperparameters)",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "ضبط فوري",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // 2x2 Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Toggle 1: Phoneme Slices
                        HyperparamToggleCard(
                            title = "تقطيع الفونيمات",
                            subtitle = "Phoneme Slices",
                            checked = uiState.hyperParameters.phonemeSlices,
                            onToggle = { viewModel.togglePhonemeSlices() },
                            modifier = Modifier.weight(1f)
                        )

                        // Toggle 2: Confidence
                        HyperparamToggleCard(
                            title = "عتبة الثقة σ",
                            subtitle = "Confidence > 95%",
                            subtitleColor = NeonEmerald,
                            checked = uiState.hyperParameters.highConfidenceThreshold,
                            onToggle = { viewModel.toggleHighConfidence() },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Toggle 3: Silence Trimming
                        HyperparamToggleCard(
                            title = "قص فترات الصمت",
                            subtitle = "Silence > 350ms",
                            checked = uiState.hyperParameters.silenceTrimming,
                            onToggle = { viewModel.toggleSilenceTrimming() },
                            modifier = Modifier.weight(1f)
                        )

                        // Toggle 4: VAD Adaptor
                        HyperparamToggleCard(
                            title = "تنعيم كاشف الصوت",
                            subtitle = "VAD Adaptor: تلقائي",
                            subtitleColor = ElectricCyan,
                            checked = uiState.hyperParameters.vadSmoothing,
                            onToggle = { viewModel.toggleVadSmoothing() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Primary Action Area (Electric glowing gradient button)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(14.dp),
                            spotColor = ElectricCyanDark,
                            ambientColor = ElectricCyan
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(ElectricCyan, TechBlueContainer)
                            )
                        )
                        .clickable { viewModel.startForcedAlignment() }
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .testTag("align_trigger_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = DarkBackground,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "توليد التوقيتات وملف التقطيع",
                            color = DarkBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(DarkBackground.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "معالجة فورية",
                                color = DarkBackground,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Progress Bar if aligning
                AnimatedVisibility(visible = uiState.isAligning) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { uiState.alignmentProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ElectricCyan,
                            trackColor = DarkSurfaceContainerHigh,
                        )
                        Text(
                            text = uiState.alignmentStepText,
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "محاذاة متزامنة حتى مستوى أجزاء الألف من الثانية (±2ms)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 5. Output Preview & Timeline Inspection Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceContainer)
                    .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
                    .testTag("output_preview_section"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NeonEmerald)
                        )
                        Text(
                            text = "حزمة المزامنة الناتجة",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(DarkSurfaceContainerLowest)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "تمت المزامنة بنجاح في ${String.format(Locale.US, "%.2f", uiState.alignmentPayload.executionTimeSec)} ثانية ✓",
                            color = NeonEmerald,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Format Switcher Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceContainerLowest)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutputFormat.values().forEach { fmt ->
                        val isSelected = uiState.selectedFormat == fmt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) ElectricCyan else Color.Transparent)
                                .clickable { viewModel.setFormat(fmt) }
                                .padding(vertical = 8.dp)
                                .testTag("format_pill_${fmt.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fmt.label,
                                color = if (isSelected) OnCyanPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Timeline Inspection Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceContainerLow)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "معاينة الخط الزمني للكلمات",
                            color = ElectricCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "انقر للاستماع للمقطع",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // Display sample word pills
                    val sampleSegments = uiState.alignmentPayload.segments.take(4)
                    sampleSegments.forEach { seg ->
                        val isActive = uiState.activeSegmentId == seg.id ||
                                (currentPositionSec >= seg.start && currentPositionSec <= seg.end)
                        WordTimelineItem(
                            segment = seg,
                            isActive = isActive,
                            onPlayWord = { viewModel.playSegment(seg) }
                        )
                    }
                }

                // Code Editor Surface
                val codeText = when (uiState.selectedFormat) {
                    OutputFormat.JSON -> uiState.alignmentPayload.toJsonString()
                    OutputFormat.SRT -> uiState.alignmentPayload.toSrtString()
                    OutputFormat.VTT -> uiState.alignmentPayload.toVttString()
                    OutputFormat.CSV -> uiState.alignmentPayload.toCsvString()
                }

                CodeViewerSurface(
                    codeContent = codeText,
                    format = uiState.selectedFormat,
                    fileSizeBytes = codeText.toByteArray().size
                )

                // Output Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Download / Export Button
                    Button(
                        onClick = { viewModel.exportAndShareFile(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("download_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = OnCyanPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "تحميل",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "تحميل الملف (.${uiState.selectedFormat.extension})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Copy to Clipboard Button
                    Button(
                        onClick = { viewModel.copyOutputToClipboard(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("copy_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceContainerHigh,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "نسخ الحزمة",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Bottom Sheets
    if (uiState.showTextEditSheet) {
        com.example.ui.components.EditReferenceTextSheet(
            initialText = uiState.referenceText,
            onDismiss = { viewModel.showTextEditDialog(false) },
            onSave = { viewModel.updateReferenceText(it) }
        )
    }

    if (uiState.showPresetPickerSheet) {
        com.example.ui.components.PresetPickerSheet(
            currentPreset = uiState.selectedPreset,
            onDismiss = { viewModel.showPresetPickerDialog(false) },
            onSelectPreset = { viewModel.selectPreset(it) },
            onCustomAudioSelected = { name, fmt, size, dur ->
                viewModel.setCustomAudio(name, fmt, size, dur)
            }
        )
    }
}

@Composable
private fun HyperparamToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    subtitleColor: Color = TextSecondary
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceContainer)
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = subtitleColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }

        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = ElectricCyan,
                uncheckedColor = OutlineBorder,
                checkmarkColor = OnCyanPrimary
            )
        )
    }
}
