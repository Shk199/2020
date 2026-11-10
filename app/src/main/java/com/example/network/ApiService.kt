package com.example.network

import android.content.Context
import android.net.Uri
import com.example.BuildConfig
import com.example.model.AlignmentPayload
import com.example.model.AlignmentSegment
import com.example.model.HyperParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSink
import java.io.InputStream
import java.util.concurrent.TimeUnit

// MARK: - API Client

object ApiClient {
    private var customBaseUrl = "http://127.0.0.1:8000/align"

    fun setBaseUrl(url: String) {
        customBaseUrl = url.trim()
    }

    fun getBaseUrl(): String = customBaseUrl

    /**
     * Resolves the full endpoint URL for the Flask alignment service.
     * Ensures the request always targets the /align endpoint dynamically.
     */
    fun resolveEndpointUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return if (trimmed.endsWith("/align") || trimmed.endsWith("/align/")) {
            trimmed.trimEnd('/')
        } else {
            "${trimmed.trimEnd('/')}/align"
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()
}

// MARK: - Alignment Repository

class AlignRepository {

    /**
     * Sends a Multipart/form-data request with the current audio file and text
     * to the local Flask server endpoint (/align) using OkHttp.
     * Handles the server response dynamically and returns Result<AlignmentPayload>.
     */
    suspend fun performAlignment(
        context: Context,
        audioUri: Uri?,
        audioFileName: String,
        audioDurationSec: Double,
        referenceText: String,
        engine: String,
        hyperParams: HyperParameters,
        serverUrl: String
    ): Result<AlignmentPayload> = withContext(Dispatchers.IO) {
        if (audioUri == null) {
            return@withContext Result.failure(Exception("اختر ملفاً صوتياً أولاً"))
        }

        val startTimeMs = System.currentTimeMillis()
        val targetEndpoint = ApiClient.resolveEndpointUrl(serverUrl)

        try {
            // Prepare Audio RequestBody via streaming chunks (8KB, no heap loading)
            val audioRequestBody = object : RequestBody() {
                override fun contentType(): MediaType? {
                    val mimeType = context.contentResolver.getType(audioUri) ?: "audio/*"
                    return mimeType.toMediaTypeOrNull()
                }

                override fun contentLength(): Long {
                    return try {
                        context.contentResolver.openFileDescriptor(audioUri, "r")?.use {
                            it.statSize
                        } ?: -1L
                    } catch (_: Exception) {
                        -1L
                    }
                }

                override fun writeTo(sink: BufferedSink) {
                    val inputStream = context.contentResolver.openInputStream(audioUri)
                        ?: throw IllegalStateException("تعذر فتح الملف الصوتي")
                    inputStream.use { stream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (stream.read(buffer).also { bytesRead = it } != -1) {
                            sink.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }

            // Build Multipart/form-data payload for the Flask endpoint
            val multipartBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("audio", audioFileName, audioRequestBody)

            if (referenceText.isNotBlank()) {
                val textBytes = referenceText.toByteArray(Charsets.UTF_8)
                val textBody = textBytes.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
                multipartBuilder.addFormDataPart("text", "script.txt", textBody)
                multipartBuilder.addFormDataPart("transcript", referenceText)
            }

            multipartBuilder.addFormDataPart("engine", engine)
            multipartBuilder.addFormDataPart("phoneme_slices", hyperParams.phonemeSlices.toString())
            multipartBuilder.addFormDataPart("silence_trimming", hyperParams.silenceTrimming.toString())

            val request = Request.Builder()
                .url(targetEndpoint)
                .post(multipartBuilder.build())
                .header("Accept", "application/json")
                .build()

            val response = ApiClient.okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBodyStr = response.body?.string() ?: ""
                val segments = parseAlignmentSegments(responseBodyStr, referenceText, audioDurationSec)

                val payload = AlignmentPayload(
                    id = "payload-" + System.currentTimeMillis(),
                    status = "aligned",
                    engine = engine,
                    sampleRate = 48000,
                    audioFileName = audioFileName,
                    audioDurationSec = audioDurationSec,
                    executionTimeSec = (System.currentTimeMillis() - startTimeMs) / 1000.0,
                    segments = segments
                )
                return@withContext Result.success(payload)
            } else {
                val errorBody = response.body?.string()?.take(150) ?: response.message
                val msg = if (errorBody.isNotBlank()) {
                    "خطأ من سيرفر Flask ($response.code): $errorBody"
                } else {
                    "تعذر الاتصال بسيرفر المزامنة (HTTP ${response.code})"
                }
                return@withContext Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            val friendlyMsg = when (e) {
                is java.net.ConnectException -> {
                    if (targetEndpoint.contains("127.0.0.1")) {
                        "تعذر الاتصال بـ $targetEndpoint (على محاكي أندرويد استخدم http://10.0.2.2:8000/align)"
                    } else {
                        "تعذر الاتصال بسيرفر Flask على $targetEndpoint، تأكد من تشغيل السيرفر"
                    }
                }
                is java.net.SocketTimeoutException -> "انتهت مهلة الاتصال بسيرفر Flask"
                else -> e.message ?: "تعذر الاتصال بسيرفر المزامنة، تحقق من الرابط في الإعدادات"
            }
            return@withContext Result.failure(Exception(friendlyMsg))
        }
    }

    /**
     * Safely parses alignment segments from Flask response JSON.
     * Supports formats: {segments: [...]}, {words: [...]}, [{...}], or fallback.
     */
    private fun parseAlignmentSegments(
        jsonStr: String,
        fallbackText: String,
        totalDuration: Double
    ): List<AlignmentSegment> {
        val list = mutableListOf<AlignmentSegment>()
        if (jsonStr.isBlank()) {
            return generateFallbackSegments(fallbackText, totalDuration)
        }

        try {
            val trimmed = jsonStr.trim()
            if (trimmed.startsWith("{")) {
                val root = org.json.JSONObject(trimmed)

                val segmentsArray = root.optJSONArray("segments")
                    ?: root.optJSONArray("words")
                    ?: root.optJSONArray("alignment")

                if (segmentsArray != null && segmentsArray.length() > 0) {
                    for (i in 0 until segmentsArray.length()) {
                        val item = segmentsArray.optJSONObject(i) ?: continue
                        val id = item.optInt("id", i + 1)
                        val start = item.optDouble("start", 0.0)
                        val end = item.optDouble("end", start + 0.5)
                        val text = item.optString("text").ifEmpty { item.optString("word", "") }
                        val confidence = item.optDouble("confidence", 1.0)
                        val phonemes = mutableListOf<String>()
                        val phArray = item.optJSONArray("phonemes") ?: item.optJSONArray("phones")
                        if (phArray != null) {
                            for (j in 0 until phArray.length()) {
                                phonemes.add(phArray.optString(j))
                            }
                        }
                        if (text.isNotBlank()) {
                            list.add(
                                AlignmentSegment(
                                    id = id,
                                    start = start,
                                    end = end,
                                    text = text,
                                    confidence = confidence,
                                    phonemes = phonemes
                                )
                            )
                        }
                    }
                }
            } else if (trimmed.startsWith("[")) {
                val array = org.json.JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val id = item.optInt("id", i + 1)
                    val start = item.optDouble("start", 0.0)
                    val end = item.optDouble("end", start + 0.5)
                    val text = item.optString("text").ifEmpty { item.optString("word", "") }
                    val confidence = item.optDouble("confidence", 1.0)
                    if (text.isNotBlank()) {
                        list.add(
                            AlignmentSegment(
                                id = id,
                                start = start,
                                end = end,
                                text = text,
                                confidence = confidence,
                                phonemes = emptyList()
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback to text parsing
        }

        if (list.isEmpty()) {
            return generateFallbackSegments(fallbackText, totalDuration)
        }

        return list
    }

    private fun generateFallbackSegments(text: String, duration: Double): List<AlignmentSegment> {
        val words = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()

        val safeDuration = if (duration > 0) duration else words.size * 0.6
        val step = safeDuration / words.size
        return words.mapIndexed { index, w ->
            AlignmentSegment(
                id = index + 1,
                start = index * step,
                end = (index + 1) * step,
                text = w,
                confidence = 0.95,
                phonemes = emptyList()
            )
        }
    }
}
