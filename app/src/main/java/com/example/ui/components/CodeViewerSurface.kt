package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OutputFormat
import com.example.ui.theme.DarkSurfaceContainerHigh
import com.example.ui.theme.DarkSurfaceContainerLowest
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GreenDot
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.RedDot
import com.example.ui.theme.SoftTechBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YellowDot
import java.util.Locale

@Composable
fun CodeViewerSurface(
    codeContent: String,
    format: OutputFormat,
    fileSizeBytes: Int,
    modifier: Modifier = Modifier
) {
    val fileName = when (format) {
        OutputFormat.JSON -> "payload_alignment.json"
        OutputFormat.SRT -> "subtitles.srt"
        OutputFormat.VTT -> "subtitles.vtt"
        OutputFormat.CSV -> "timestamps.csv"
    }

    val formattedSize = if (fileSizeBytes > 1024) {
        String.format(Locale.US, "%.1f KB", fileSizeBytes / 1024.0)
    } else {
        "$fileSizeBytes B"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceContainerLowest)
            .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .testTag("code_editor_surface")
    ) {
        // macOS / Linux style header window bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceContainerHigh)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(RedDot)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(YellowDot)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(GreenDot)
                )

                Text(
                    text = fileName,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            Text(
                text = formattedSize,
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Code Body with syntax styling (rendered LTR for proper code indenting)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            val horizontalScrollState = rememberScrollState()
            val verticalScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 220.dp)
                    .horizontalScroll(horizontalScrollState)
                    .verticalScroll(verticalScrollState)
                    .padding(14.dp)
            ) {
                val annotatedText = buildHighlightedSyntax(codeContent, format)
                Text(
                    text = annotatedText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun buildHighlightedSyntax(raw: String, format: OutputFormat) = buildAnnotatedString {
    when (format) {
        OutputFormat.JSON -> {
            val lines = raw.lines()
            lines.forEachIndexed { i, line ->
                val trimmed = line.trimStart()
                val indent = line.substring(0, line.length - trimmed.length)
                append(indent)

                if (trimmed.startsWith("\"") && trimmed.contains("\":")) {
                    val keyEnd = trimmed.indexOf("\":") + 1
                    val key = trimmed.substring(0, keyEnd)
                    val rest = trimmed.substring(keyEnd)

                    withStyle(SpanStyle(color = ElectricCyan)) {
                        append(key)
                    }

                    if (rest.contains("\"")) {
                        withStyle(SpanStyle(color = NeonEmerald)) {
                            append(rest)
                        }
                    } else if (rest.matches(".*\\d+.*".toRegex())) {
                        withStyle(SpanStyle(color = SoftTechBlue)) {
                            append(rest)
                        }
                    } else {
                        withStyle(SpanStyle(color = TextPrimary)) {
                            append(rest)
                        }
                    }
                } else if (trimmed.matches(".*\\d+.*".toRegex())) {
                    withStyle(SpanStyle(color = SoftTechBlue)) {
                        append(trimmed)
                    }
                } else {
                    withStyle(SpanStyle(color = TextPrimary)) {
                        append(trimmed)
                    }
                }

                if (i < lines.size - 1) append("\n")
            }
        }
        OutputFormat.SRT, OutputFormat.VTT -> {
            val lines = raw.lines()
            lines.forEachIndexed { i, line ->
                when {
                    line.matches("^\\d+$".toRegex()) -> {
                        withStyle(SpanStyle(color = SoftTechBlue)) { append(line) }
                    }
                    line.contains("-->") -> {
                        withStyle(SpanStyle(color = ElectricCyan)) { append(line) }
                    }
                    line.startsWith("WEBVTT") -> {
                        withStyle(SpanStyle(color = NeonEmerald)) { append(line) }
                    }
                    else -> {
                        withStyle(SpanStyle(color = TextPrimary)) { append(line) }
                    }
                }
                if (i < lines.size - 1) append("\n")
            }
        }
        OutputFormat.CSV -> {
            val lines = raw.lines()
            lines.forEachIndexed { i, line ->
                if (i == 0) {
                    withStyle(SpanStyle(color = ElectricCyan)) { append(line) }
                } else {
                    withStyle(SpanStyle(color = TextPrimary)) { append(line) }
                }
                if (i < lines.size - 1) append("\n")
            }
        }
    }
}
