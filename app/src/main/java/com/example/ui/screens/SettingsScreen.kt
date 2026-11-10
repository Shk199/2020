package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OutputFormat
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceContainerHigh
import com.example.ui.theme.DarkSurfaceContainerLowest
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.RedDot
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var serverUrlInput by remember(uiState.serverUrl) { mutableStateOf(uiState.serverUrl) }
    var serverUrlError by remember { mutableStateOf<String?>(null) }

    val engines = listOf(
        "Whisper-X + CTC v2.4 (عصبي)",
        "Montreal Forced Aligner (MFA v2.2)",
        "Wav2Vec2 Arabic CTC (مُسرّع)"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "إعدادات المعالجة ومحرك المطابقة",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Engine Selection Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceContainer)
                .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "محرك المحاذاة القسري (Forced Alignment Engine)",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            engines.forEach { eng ->
                val isSelected = uiState.selectedEngine == eng
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) DarkSurfaceContainerHigh else DarkSurfaceContainerLowest)
                        .border(
                            1.dp,
                            if (isSelected) ElectricCyan else OutlineBorder.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.setEngine(eng) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = eng,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        val desc = when (eng) {
                            "Whisper-X + CTC v2.4 (عصبي)" -> "أعلى دقة فونيمات بالمللي ثانية (±2ms)"
                            "Montreal Forced Aligner (MFA v2.2)" -> "معياري للأبحاث الصوتية والفونولوجية"
                            else -> "خفيف وسريع للمعالجة اللحظية"
                        }
                        Text(
                            text = desc,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "محدد",
                            tint = NeonEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Output Format Settings
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
                text = "صيغة التصدير الافتراضية",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutputFormat.values().forEach { fmt ->
                    val isSelected = uiState.selectedFormat == fmt
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ElectricCyan else DarkSurfaceContainerLowest)
                            .clickable { viewModel.setFormat(fmt) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fmt.name,
                            color = if (isSelected) DarkBackground else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Developer Info Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceContainer)
                .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "عن الأداة والبيئة البرمجية",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "مُطابق الصوت والنصوص (Audio & Text Forced Aligner) مخصص للمطورين وصناع المحتوى، محركات الدبلجة، وتطبيقات تحفيظ القرآن والترجمة التلقائية. يدعم توليد ملفات JSON و SRT و VTT بتوقيتات فائقة الدقة حتى مستوى الفونيمات.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Text(
                text = "الإصدار: v2.4.0-neural • معمارية Jetpack Compose & M3",
                color = NeonEmerald,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Server URL Card (رابط سيرفر المزامنة)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceContainer)
                .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "رابط سيرفر المزامنة",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedTextField(
                value = serverUrlInput,
                onValueChange = {
                    serverUrlInput = it
                    if (serverUrlError != null) serverUrlError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("server_url_input"),
                singleLine = true,
                isError = serverUrlError != null,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = OutlineBorder.copy(alpha = 0.5f),
                    errorBorderColor = RedDot,
                    cursorColor = ElectricCyan,
                    focusedContainerColor = DarkSurfaceContainerLowest,
                    unfocusedContainerColor = DarkSurfaceContainerLowest,
                    errorContainerColor = DarkSurfaceContainerLowest
                ),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            )

            if (serverUrlError != null) {
                Text(
                    text = serverUrlError!!,
                    color = RedDot,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

            Button(
                onClick = {
                    val trimmed = serverUrlInput.trim()
                    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                        serverUrlError = null
                        viewModel.setServerUrl(trimmed)
                    } else {
                        serverUrlError = "يجب أن يبدأ الرابط بـ http:// أو https://"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("save_server_url_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "حفظ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
