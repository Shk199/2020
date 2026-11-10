package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OutputFormat
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.AmiriFamily
import com.example.ui.theme.CyanPrimaryContainer
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceContainerHigh
import com.example.ui.theme.DarkSurfaceContainerLowest
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.OnCyanPrimary
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.SoftTechBlue
import com.example.ui.theme.TechBlueContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.SliceVoiceUiState
import java.util.Locale

/**
 * Minimalist Header for SliceVoice
 */
@Composable
fun MinimalistHeader(
    uiState: SliceVoiceUiState? = null,
    modifier: Modifier = Modifier
) {
    val pulseTransition = rememberInfiniteTransition(label = "serverPulse")
    val serverPulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "serverAlpha"
    )

    val (statusText, statusColor) = when {
        uiState?.isAligning == true -> Pair("جاري المعالجة", ElectricCyan)
        uiState?.hasResult == true -> Pair("آخر مزامنة ناجحة", NeonEmerald)
        else -> Pair("غير متصل", ErrorRed)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("minimal_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Minimal Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(DarkSurfaceContainerHigh, CyanPrimaryContainer)
                        )
                    )
                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "SliceVoice Audio",
                        tint = ElectricCyan,
                        modifier = Modifier.size(17.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = "Slice",
                        tint = NeonEmerald,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Text(
                text = "SliceVoice",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
        }

        // Live Connection Indicator
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(DarkSurfaceContainer)
                .border(1.dp, OutlineBorder.copy(alpha = 0.3f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(if (uiState?.isAligning == true || uiState?.hasResult == true) serverPulseAlpha else 0.7f)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Clean & Minimal Audio Input Card
 */
@Composable
fun MinimalAudioInputCard(
    uiState: SliceVoiceUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.handleAudioPicked(context, uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceContainer)
            .border(1.dp, OutlineBorder.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable { audioPickerLauncher.launch("audio/*") }
            .padding(16.dp)
            .testTag("audio_input_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AudioFile,
                        contentDescription = "ملف صوتي",
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "الملف الصوتي (MP3 / WAV)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = uiState.audioFileName,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceContainerHigh)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "اختيار ملف",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val formatTagParts = uiState.audioFormatTag.replace(" • ", " ").split(" ").filter { it.isNotBlank() }.joinToString(" • ")
            Text(
                text = "${String.format(Locale.US, "%.1f", uiState.audioSizeMb)} MB • $formatTagParts",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                style = TextStyle(
                    textDirection = TextDirection.Ltr
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "جاهز للمزامنة",
                    color = NeonEmerald,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Clean & Minimal Text Input Card
 */
@Composable
fun MinimalTextInputCard(
    uiState: SliceVoiceUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.handleTextFilePicked(context, uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceContainer)
            .border(1.dp, OutlineBorder.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(16.dp)
            .testTag("text_input_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        .clip(RoundedCornerShape(8.dp))
                        .background(TechBlueContainer.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "مستند النص",
                        tint = AccentViolet,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "النص المرجعي (TXT)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = uiState.textFileName,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceContainerHigh)
                        .clickable { textPickerLauncher.launch("text/*") }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("minimal_upload_txt")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(13.dp)
                        )
                        Text("رفع TXT", color = NeonEmerald, fontSize = 11.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceContainerHigh)
                        .clickable { viewModel.showTextEditDialog(true) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("minimal_edit_text")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = SoftTechBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Text("تعديل/لصق", color = SoftTechBlue, fontSize = 11.sp)
                    }
                }
            }
        }

        // Preview snippet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceContainerLowest)
                .clickable { viewModel.showTextEditDialog(true) }
                .padding(10.dp)
        ) {
            val previewText = if (uiState.referenceText.length > 90) {
                uiState.referenceText.take(90) + "..."
            } else {
                uiState.referenceText
            }
            Text(
                text = previewText,
                color = TextPrimary,
                fontSize = 13.sp,
                fontFamily = AmiriFamily,
                lineHeight = 24.sp,
                maxLines = 2
            )
        }
    }
}

/**
 * Clean & Minimal Output Box with Direct Download
 */
@Composable
fun MinimalOutputBox(
    uiState: SliceVoiceUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    if (!uiState.hasResult) return

    val context = LocalContext.current

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.downloadFileToStorage(context)
        } else {
            // Permission rejected on Android 9 or below: fallback to sharing
            viewModel.exportAndShare(context)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceContainer)
            .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(16.dp)
            .testTag("minimal_output_box"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Success Header & Format Selector
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
                    imageVector = Icons.Default.TaskAlt,
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "ملف التقطيع جاهز للتحميل",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick Format Toggle: JSON vs SRT
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceContainerLowest)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf(OutputFormat.JSON, OutputFormat.SRT).forEach { fmt ->
                    val isSelected = uiState.selectedFormat == fmt
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) ElectricCyan else Color.Transparent)
                            .clickable { viewModel.setFormat(fmt) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (fmt == OutputFormat.JSON) "JSON" else "SRT",
                            color = if (isSelected) DarkBackground else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Summary details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${uiState.alignmentPayload.segments.size} مقطع زمني دقيق • دقة ±2ms",
                color = ElectricCyan,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "استغرق ${String.format(Locale.US, "%.2f", uiState.alignmentPayload.executionTimeSec)} ثانية",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        // Action Buttons: Direct Download + Copy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Direct Download Button to Downloads directory
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        viewModel.downloadFileToStorage(context)
                    }
                },
                modifier = Modifier
                    .weight(1.4f)
                    .height(48.dp)
                    .testTag("direct_download_button"),
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
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = "تحميل الملف الناتج (.${uiState.selectedFormat.extension})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Copy to clipboard button
            Button(
                onClick = { viewModel.copyToClipboard(context) },
                modifier = Modifier
                    .weight(0.9f)
                    .height(48.dp)
                    .testTag("copy_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurfaceContainerHigh,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "نسخ",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "نسخ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
