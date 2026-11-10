package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EditReferenceTextSheet
import com.example.ui.components.MinimalAudioInputCard
import com.example.ui.components.MinimalOutputBox
import com.example.ui.components.MinimalTextInputCard
import com.example.ui.components.MinimalistHeader
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceContainerHigh
import com.example.ui.theme.DarkSurfaceContainerLowest
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TechBlueContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.MainViewModel

/**
 * Minimalist, high-performance SliceVoice screen
 * Clean, compact, zero clutter, no excessive scrolling.
 */
@Composable
fun SliceVoiceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. Minimal Header (SliceVoice + Server Connection Status)
        MinimalistHeader(uiState = uiState)

        // 2. Focused Main Content Container (Compact, perfectly fitting on modern phone screens)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card 1: Audio Input (MP3 / WAV)
            MinimalAudioInputCard(
                uiState = uiState,
                viewModel = viewModel
            )

            // Card 2: Text Script Input (TXT)
            MinimalTextInputCard(
                uiState = uiState,
                viewModel = viewModel
            )

            // 3. Prominent Glowing Main Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 14.dp,
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
                    .clickable { viewModel.generateTimestamps(context) }
                    .padding(vertical = 16.dp, horizontal = 20.dp)
                    .testTag("generate_timestamps_button"),
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
                        text = "توليد ملف التقطيع",
                        color = DarkBackground,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Real-Time Progress Bar when generating or failure status
            AnimatedVisibility(
                visible = uiState.isAligning || uiState.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceContainerHigh)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isFailure = uiState.errorMessage != null ||
                            uiState.statusMessage.contains("تعذر") ||
                            uiState.statusMessage.contains("خطأ") ||
                            uiState.statusMessage.contains("فشل") ||
                            uiState.statusMessage.contains("أولاً")
                        val isWarning = uiState.statusMessage.contains("تنبيه") ||
                            uiState.statusMessage.contains("تحذير")
                        val statusTextColor = when {
                            isFailure -> ErrorRed
                            isWarning -> WarningAmber
                            else -> TextPrimary
                        }
                        Text(
                            text = uiState.statusMessage,
                            color = statusTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (uiState.isAligning) {
                            Text(
                                text = "${(uiState.alignmentProgress * 100).toInt()}%",
                                color = ElectricCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (uiState.isAligning) {
                        LinearProgressIndicator(
                            progress = { uiState.alignmentProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ElectricCyan,
                            trackColor = DarkSurfaceContainerLowest
                        )
                    }
                }
            }

            // 4. Simplified Result Card with Direct Download (only displayed after actual successful alignment)
            AnimatedVisibility(
                visible = uiState.hasResult,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                MinimalOutputBox(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    // Modal sheet for editing or pasting text
    if (uiState.showTextEditSheet) {
        EditReferenceTextSheet(
            initialText = uiState.referenceText,
            onDismiss = { viewModel.showTextEditDialog(false) },
            onSave = { viewModel.updateReferenceText(it) }
        )
    }
}
