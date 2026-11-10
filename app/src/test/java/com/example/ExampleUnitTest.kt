package com.example

import com.example.model.AlignmentPayload
import com.example.model.AlignmentSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSerializationOutput() {
        val segments = listOf(
            AlignmentSegment(1, 0.0, 3.42, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", 0.995),
            AlignmentSegment(2, 3.82, 7.15, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", 0.992)
        )

        val payload = AlignmentPayload(
            id = "test-id",
            status = "aligned",
            engine = "whisperx-ctc-ar",
            sampleRate = 48000,
            audioFileName = "test.wav",
            audioDurationSec = 7.15,
            executionTimeSec = 0.5,
            segments = segments
        )

        val json = payload.toJsonString()
        assertTrue(json.contains("\"status\": \"aligned\""))
        assertTrue(json.contains("\"duration\": 3.420"))
        assertTrue(json.contains("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"))

        val srt = payload.toSrtString()
        assertTrue(srt.contains("00:00:00,000 --> 00:00:03,420"))
        assertTrue(srt.contains("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"))

        val vtt = payload.toVttString()
        assertTrue(vtt.startsWith("WEBVTT"))

        val csv = payload.toCsvString()
        assertTrue(csv.contains("id,start_sec,end_sec,duration_sec,text,confidence"))
    }

    @Test
    fun testAlignFailureMessage() {
        val expectedError = "تعذر الاتصال بسيرفر المزامنة، تحقق من الرابط في الإعدادات"
        val failureResult: Result<AlignmentPayload> = Result.failure(Exception(expectedError))

        assertTrue(failureResult.isFailure)
        assertEquals(expectedError, failureResult.exceptionOrNull()?.message)
    }

    @Test
    fun testMissingAudioErrorMessage() {
        val expectedError = "اختر ملفاً صوتياً أولاً"
        val failureResult: Result<AlignmentPayload> = Result.failure(Exception(expectedError))

        assertTrue(failureResult.isFailure)
        assertEquals(expectedError, failureResult.exceptionOrNull()?.message)
    }

    @Test
    fun testEscapingAndCsvQuotes() {
        val segment = AlignmentSegment(
            id = 1,
            start = 1.0,
            end = 2.5,
            text = "قال \"نعم\"\nسطر جديد\tتبويب\\مسار",
            confidence = 0.98,
            phonemes = listOf("ق", "ا", "ل", "\"ن\"")
        )
        val payload = AlignmentPayload(
            engine = "engine/\"test\"\nline",
            audioFileName = "audio\\\"name\"\t.wav",
            segments = listOf(segment)
        )

        val json = payload.toJsonString()
        assertTrue(json.contains("\\\"نعم\\\""))
        assertTrue(json.contains("\\nسطر جديد"))
        assertTrue(json.contains("\\tتبويب"))
        assertTrue(json.contains("\\\\مسار"))
        assertTrue(json.contains("audio\\\\\\\"name\\\"\\t.wav"))
        assertTrue(json.contains("engine/\\\"test\\\"\\nline"))
        assertTrue(json.contains("\\\"ن\\\""))

        val csv = payload.toCsvString()
        assertTrue(csv.contains("\"قال \"\"نعم\"\"\nسطر جديد\tتبويب\\مسار\""))
    }

    @Test
    fun testInitialStateHasResultIsFalse() {
        val initialState = com.example.viewmodel.SliceVoiceUiState()
        org.junit.Assert.assertFalse(initialState.hasResult)
        org.junit.Assert.assertFalse(initialState.isSuccess)
        assertTrue(initialState.historyList.isEmpty())
    }

    @Test
    fun testLoadFromHistoryResetsAudioUriAndWarns() {
        val payload = AlignmentPayload(
            id = "test-history-item",
            audioFileName = "old_audio.wav",
            audioDurationSec = 45.0,
            segments = emptyList()
        )
        // Simulate loadFromHistory state copy
        val stateWithAudio = com.example.viewmodel.SliceVoiceUiState(
            audioFileName = "current.wav"
        )
        val loadedState = stateWithAudio.copy(
            alignmentPayload = payload,
            audioUri = null,
            audioFileName = payload.audioFileName,
            audioDurationSec = payload.audioDurationSec,
            hasResult = true,
            toastMessage = "تنبيه: الملف الصوتي الأصلي غير محفوظ، يرجى اختيار ملف صوتي جديد للمزامنة"
        )

        org.junit.Assert.assertNull(loadedState.audioUri)
        assertEquals("old_audio.wav", loadedState.audioFileName)
        assertTrue(loadedState.toastMessage?.contains("تنبيه: الملف الصوتي الأصلي غير محفوظ") == true)
        assertTrue(loadedState.hasResult)
    }

    @Test
    fun testExportHistoryItemFormatting() {
        val historyItem = AlignmentPayload(
            id = "history-custom-99",
            audioFileName = "meeting_audio.wav",
            audioDurationSec = 120.0,
            segments = listOf(
                AlignmentSegment(1, 0.0, 2.0, "فقرة من السجل", 0.99)
            )
        )

        val json = historyItem.toJsonString()
        assertTrue(json.contains("meeting_audio.wav"))
        assertTrue(json.contains("فقرة من السجل"))

        val srt = historyItem.toSrtString()
        assertTrue(srt.contains("00:00:00,000 --> 00:00:02,000"))
        assertTrue(srt.contains("فقرة من السجل"))
    }

    @Test
    fun testServerUrlValidation() {
        fun isValidServerUrl(url: String): Boolean {
            val trimmed = url.trim()
            return trimmed.startsWith("http://") || trimmed.startsWith("https://")
        }

        assertTrue(isValidServerUrl("https://api.slicevoice.ai/"))
        assertTrue(isValidServerUrl("http://192.168.1.100:8000"))
        org.junit.Assert.assertFalse(isValidServerUrl("ftp://server.com"))
        org.junit.Assert.assertFalse(isValidServerUrl("api.slicevoice.ai"))
        org.junit.Assert.assertFalse(isValidServerUrl(""))
    }
}
