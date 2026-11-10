package com.example.model

import java.util.Locale

data class AlignmentSegment(
    val id: Int,
    val start: Double,
    val end: Double,
    val text: String,
    val confidence: Double,
    val phonemes: List<String> = emptyList()
) {
    val duration: Double get() = (end - start).coerceAtLeast(0.0)
    
    fun formattedTimeRange(): String {
        return String.format(Locale.US, "%.2fs ➔ %.2fs", start, end)
    }

    fun formattedConfidence(): String {
        val pct = (confidence * 100).coerceIn(0.0, 100.0)
        return String.format(Locale.US, "%.1f%%", pct)
    }
}

enum class OutputFormat(val label: String, val extension: String, val mimeType: String) {
    JSON("JSON (نشط)", "json", "application/json"),
    SRT("SRT", "srt", "text/plain"),
    VTT("VTT", "vtt", "text/vtt"),
    CSV("CSV", "csv", "text/csv")
}

data class HyperParameters(
    val phonemeSlices: Boolean = true,
    val highConfidenceThreshold: Boolean = true,
    val silenceTrimming: Boolean = true,
    val vadSmoothing: Boolean = false
)

data class AlignmentPayload(
    val id: String = java.util.UUID.randomUUID().toString(),
    val status: String = "aligned",
    val engine: String = "whisperx-ctc-ar",
    val sampleRate: Int = 48000,
    val audioFileName: String = "تلاوة_سورة_الفاتحة.wav",
    val audioDurationSec: Double = 222.0,
    val createdAt: Long = System.currentTimeMillis(),
    val executionTimeSec: Double = 0.84,
    val segments: List<AlignmentSegment>
) {
    fun toJsonString(): String {
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"status\": \"${escapeJson(status)}\",\n")
        sb.append("  \"engine\": \"${escapeJson(engine)}\",\n")
        sb.append("  \"sample_rate\": $sampleRate,\n")
        sb.append("  \"audio_file\": \"${escapeJson(audioFileName)}\",\n")
        sb.append("  \"duration_seconds\": ${String.format(Locale.US, "%.3f", audioDurationSec)},\n")
        sb.append("  \"segments_count\": ${segments.size},\n")
        sb.append("  \"segments\": [\n")
        segments.forEachIndexed { index, seg ->
            sb.append("    {\n")
            sb.append("      \"id\": ${seg.id},\n")
            sb.append("      \"start\": ${String.format(Locale.US, "%.3f", seg.start)},\n")
            sb.append("      \"end\": ${String.format(Locale.US, "%.3f", seg.end)},\n")
            sb.append("      \"duration\": ${String.format(Locale.US, "%.3f", seg.duration)},\n")
            sb.append("      \"text\": \"${escapeJson(seg.text)}\",\n")
            sb.append("      \"confidence\": ${String.format(Locale.US, "%.3f", seg.confidence)},\n")
            sb.append("      \"phonemes\": [${seg.phonemes.joinToString(", ") { "\"${escapeJson(it)}\"" }}]\n")
            sb.append("    }${if (index < segments.size - 1) "," else ""}\n")
        }
        sb.append("  ]\n")
        sb.append("}")
        return sb.toString()
    }

    fun toSrtString(): String {
        val sb = StringBuilder()
        segments.forEachIndexed { index, seg ->
            sb.append("${index + 1}\n")
            sb.append("${formatSrtTime(seg.start)} --> ${formatSrtTime(seg.end)}\n")
            sb.append("${seg.text}\n\n")
        }
        return sb.toString().trimEnd()
    }

    fun toVttString(): String {
        val sb = StringBuilder()
        sb.append("WEBVTT - Forced Alignment Output\n\n")
        segments.forEachIndexed { index, seg ->
            sb.append("${index + 1}\n")
            sb.append("${formatVttTime(seg.start)} --> ${formatVttTime(seg.end)}\n")
            sb.append("${seg.text}\n\n")
        }
        return sb.toString().trimEnd()
    }

    fun toCsvString(): String {
        val sb = StringBuilder()
        sb.append("id,start_sec,end_sec,duration_sec,text,confidence\n")
        segments.forEach { seg ->
            val escapedText = seg.text.replace("\"", "\"\"")
            sb.append("${seg.id},${String.format(Locale.US, "%.3f", seg.start)},${String.format(Locale.US, "%.3f", seg.end)},${String.format(Locale.US, "%.3f", seg.duration)},\"$escapedText\",${String.format(Locale.US, "%.3f", seg.confidence)}\n")
        }
        return sb.toString()
    }

    private fun formatSrtTime(seconds: Double): String {
        val totalMs = (seconds * 1000).toLong()
        val hours = totalMs / 3600000
        val mins = (totalMs % 3600000) / 60000
        val secs = (totalMs % 60000) / 1000
        val ms = totalMs % 1000
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", hours, mins, secs, ms)
    }

    private fun formatVttTime(seconds: Double): String {
        val totalMs = (seconds * 1000).toLong()
        val hours = totalMs / 3600000
        val mins = (totalMs % 3600000) / 60000
        val secs = (totalMs % 60000) / 1000
        val ms = totalMs % 1000
        return String.format(Locale.US, "%02d:%02d:%02d.%03d", hours, mins, secs, ms)
    }

    private fun escapeJson(str: String): String {
        val sb = StringBuilder()
        for (c in str) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                '\b' -> sb.append("\\b")
                '\u000c' -> sb.append("\\f")
                else -> {
                    if (c.code in 0x00..0x1F) {
                        sb.append(String.format(Locale.US, "\\u%04x", c.code))
                    } else {
                        sb.append(c)
                    }
                }
            }
        }
        return sb.toString()
    }
}

data class AudioPreset(
    val title: String,
    val fileName: String,
    val formatTag: String,
    val sizeMb: Double,
    val durationSec: Double,
    val sampleRate: Int,
    val referenceText: String,
    val description: String,
    val sampleSegments: List<AlignmentSegment>
)

enum class AlignerTab(val title: String, val iconName: String) {
    STUDIO("المُطابق", "tune"),
    TIMELINE("الخط الزمني", "view_timeline"),
    HISTORY("السجل", "history"),
    SETTINGS("الإعدادات", "settings")
}
