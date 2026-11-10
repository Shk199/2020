package com.example.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerEngine
import com.example.model.AlignerTab
import com.example.model.AlignmentPayload
import com.example.model.AlignmentSegment
import com.example.model.AudioPreset
import com.example.model.HyperParameters
import com.example.model.OutputFormat
import com.example.model.PresetsData
import com.example.network.ApiClient
import com.example.network.AlignRepository
import com.example.utils.FileExportUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

/**
 * Main UI State representing the state of SliceVoice studio
 */
data class SliceVoiceUiState(
    // App Navigation
    val selectedTab: AlignerTab = AlignerTab.STUDIO,

    // Audio Input State
    val audioUri: Uri? = null,
    val audioFileName: String = PresetsData.defaultQuranPreset.fileName,
    val audioFormatTag: String = PresetsData.defaultQuranPreset.formatTag,
    val audioSizeMb: Double = PresetsData.defaultQuranPreset.sizeMb,
    val audioDurationSec: Double = PresetsData.defaultQuranPreset.durationSec,

    // Text Input State
    val textUri: Uri? = null,
    val textFileName: String = "script_quran.txt",
    val textSizeKb: Double = 4.2,
    val referenceText: String = PresetsData.defaultQuranPreset.referenceText,

    // Hyperparameters
    val hyperParameters: HyperParameters = HyperParameters(),

    // Backend Connection & Engine
    val serverUrl: String = ApiClient.getBaseUrl(),
    val selectedEngine: String = "Whisper-X + CTC v2.4 (عصبي)",
    val latencyMs: Int = 24,

    // Processing State
    val isAligning: Boolean = false,
    val alignmentProgress: Float = 0f,
    val statusMessage: String = "جاهز للمعالجة",
    val isSuccess: Boolean = false,
    val hasResult: Boolean = false,
    val errorMessage: String? = null,

    // Output State
    val alignmentPayload: AlignmentPayload = PresetsData.initialPayload,
    val selectedFormat: OutputFormat = OutputFormat.JSON,
    val historyList: List<AlignmentPayload> = emptyList(),

    // Active playing word
    val activeSegmentId: Int? = null,

    // UI Sheets & Feedback
    val showTextEditSheet: Boolean = false,
    val showPresetPickerSheet: Boolean = false,
    val toastMessage: String? = null
)

/**
 * MainViewModel managing business logic, network requests, audio playback and file exports
 */
class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SliceVoiceUiState())
    val uiState: StateFlow<SliceVoiceUiState> = _uiState.asStateFlow()

    private val repository = AlignRepository()
    val audioEngine = AudioPlayerEngine(viewModelScope)

    init {
        audioEngine.setDuration(_uiState.value.audioDurationSec)
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }

    // MARK: - Navigation
    fun selectTab(tab: AlignerTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun setFormat(format: OutputFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }

    // MARK: - Smart File Pickers
    /**
     * Handles audio file selected from device storage (MP3, WAV, M4A)
     */
    fun handleAudioPicked(context: Context, uri: Uri) {
        var displayName = "custom_audio.wav"
        var sizeBytes: Long = 0

        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) displayName = it.getString(nameIndex) ?: displayName

                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) sizeBytes = it.getLong(sizeIndex)
                }
            }
        } catch (_: Exception) {
            displayName = uri.lastPathSegment ?: displayName
        }

        var durationSec = 0.0
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationMsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationMsStr?.toLongOrNull() ?: 0L
            if (durationMs > 0) {
                durationSec = durationMs / 1000.0
            }
            retriever.release()
        } catch (_: Exception) {
        }

        val finalDurationSec = if (durationSec > 0.0) durationSec else 180.0
        val sizeMb = if (sizeBytes > 0) sizeBytes / (1024.0 * 1024.0) else 5.4
        val ext = displayName.substringAfterLast('.', "WAV").uppercase()

        _uiState.value = _uiState.value.copy(
            audioUri = uri,
            audioFileName = displayName,
            audioFormatTag = "$ext 48kHz",
            audioSizeMb = sizeMb,
            audioDurationSec = finalDurationSec,
            toastMessage = "تم اختيار الملف الصوتي: $displayName بنجاح ✓"
        )

        audioEngine.setDuration(finalDurationSec)
        audioEngine.seekTo(0.0)
    }

    /**
     * Handles reference text file (.txt) selected from device storage
     */
    fun handleTextFilePicked(context: Context, uri: Uri) {
        var displayName = "reference_script.txt"
        var fileContent = ""

        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) displayName = it.getString(nameIndex) ?: displayName
                }
            }

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    fileContent = reader.readText()
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(toastMessage = "فشل قراءة الملف النصي: ${e.message}")
            return
        }

        if (fileContent.isNotBlank()) {
            val sizeKb = fileContent.toByteArray(Charsets.UTF_8).size / 1024.0
            _uiState.value = _uiState.value.copy(
                textUri = uri,
                textFileName = displayName,
                textSizeKb = sizeKb,
                referenceText = fileContent,
                toastMessage = "تم تحميل المستند النصي: $displayName بنجاح ✓"
            )
        }
    }

    fun updateReferenceText(newText: String) {
        val sizeKb = newText.toByteArray(Charsets.UTF_8).size / 1024.0
        _uiState.value = _uiState.value.copy(
            referenceText = newText,
            textSizeKb = sizeKb,
            showTextEditSheet = false
        )
    }

    fun selectPreset(preset: AudioPreset) {
        _uiState.value = _uiState.value.copy(
            audioUri = null,
            audioFileName = preset.fileName,
            audioFormatTag = preset.formatTag,
            audioSizeMb = preset.sizeMb,
            audioDurationSec = preset.durationSec,
            referenceText = preset.referenceText,
            showPresetPickerSheet = false
        )
        audioEngine.setDuration(preset.durationSec)
        audioEngine.seekTo(0.0)
    }

    // MARK: - Hyperparameters Toggles
    fun togglePhonemeSlices() {
        val curr = _uiState.value.hyperParameters
        _uiState.value = _uiState.value.copy(hyperParameters = curr.copy(phonemeSlices = !curr.phonemeSlices))
    }

    fun toggleHighConfidence() {
        val curr = _uiState.value.hyperParameters
        _uiState.value = _uiState.value.copy(hyperParameters = curr.copy(highConfidenceThreshold = !curr.highConfidenceThreshold))
    }

    fun toggleSilenceTrimming() {
        val curr = _uiState.value.hyperParameters
        _uiState.value = _uiState.value.copy(hyperParameters = curr.copy(silenceTrimming = !curr.silenceTrimming))
    }

    fun toggleVadSmoothing() {
        val curr = _uiState.value.hyperParameters
        _uiState.value = _uiState.value.copy(hyperParameters = curr.copy(vadSmoothing = !curr.vadSmoothing))
    }

    fun setEngine(engineName: String) {
        val latency = when (engineName) {
            "Whisper-X + CTC v2.4 (عصبي)" -> 24
            "Montreal Forced Aligner (MFA v2.2)" -> 68
            "Wav2Vec2 Arabic CTC (مُسرّع)" -> 18
            else -> 30
        }
        _uiState.value = _uiState.value.copy(selectedEngine = engineName, latencyMs = latency)
    }

    fun setServerUrl(url: String) {
        ApiClient.setBaseUrl(url)
        _uiState.value = _uiState.value.copy(serverUrl = ApiClient.getBaseUrl())
    }

    // MARK: - Backend Alignment Execution
    /**
     * Executes the main forced alignment workflow with realistic live progress indicators
     * and Retrofit multipart network transmission.
     */
    fun generateTimestamps(context: Context) {
        if (_uiState.value.isAligning) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAligning = true,
                alignmentProgress = 0.15f,
                statusMessage = "جاري إرسال الملف الصوتي للسيرفر (POST /align)...",
                errorMessage = null
            )

            delay(350)
            _uiState.value = _uiState.value.copy(
                alignmentProgress = 0.45f,
                statusMessage = "جاري مطابقة النص مع الصوت عبر مصفوفة CTC..."
            )

            delay(400)
            _uiState.value = _uiState.value.copy(
                alignmentProgress = 0.75f,
                statusMessage = "يتم استخراج التوقيتات وحساب الفونيمات (±2ms)..."
            )

            val result = repository.performAlignment(
                context = context,
                audioUri = _uiState.value.audioUri,
                audioFileName = _uiState.value.audioFileName,
                audioDurationSec = _uiState.value.audioDurationSec,
                referenceText = _uiState.value.referenceText,
                engine = _uiState.value.selectedEngine,
                hyperParams = _uiState.value.hyperParameters,
                serverUrl = _uiState.value.serverUrl
            )

            result.onSuccess { payload ->
                val updatedHistory = listOf(payload) + _uiState.value.historyList
                _uiState.value = _uiState.value.copy(
                    isAligning = false,
                    alignmentProgress = 1.0f,
                    statusMessage = "اكتملت المزامنة بنجاح من السيرفر ✓",
                    alignmentPayload = payload,
                    historyList = updatedHistory,
                    isSuccess = true,
                    hasResult = true,
                    toastMessage = "تم استلام الاستجابة وتوليد ملف التقطيع بنجاح في ${String.format(Locale.US, "%.2f", payload.executionTimeSec)} ثانية ✓"
                )
            }.onFailure { err ->
                val errorMsg = err.message ?: "تعذر الاتصال بسيرفر المزامنة، تحقق من الرابط في الإعدادات"
                _uiState.value = _uiState.value.copy(
                    isAligning = false,
                    alignmentProgress = 0f,
                    statusMessage = errorMsg,
                    errorMessage = errorMsg,
                    isSuccess = false,
                    hasResult = false,
                    toastMessage = errorMsg
                )
            }
        }
    }

    // MARK: - Audio Playback
    fun playSegment(segment: AlignmentSegment) {
        _uiState.value = _uiState.value.copy(activeSegmentId = segment.id)
        audioEngine.playSegment(segment.start, segment.end)
    }

    // MARK: - File Export & Download
    /**
     * Downloads and saves the generated alignment file directly into the device's Downloads directory.
     */
    fun downloadFileToStorage(context: Context) {
        val payload = _uiState.value.alignmentPayload
        val format = _uiState.value.selectedFormat
        val fileName = "SliceVoice_${System.currentTimeMillis()}.${format.extension}"

        val content = when (format) {
            OutputFormat.JSON -> payload.toJsonString()
            OutputFormat.SRT -> payload.toSrtString()
            OutputFormat.VTT -> payload.toVttString()
            OutputFormat.CSV -> payload.toCsvString()
        }

        val saveResult = FileExportUtil.saveToDownloads(context, fileName, content, format.mimeType)
        saveResult.onSuccess { path ->
            _uiState.value = _uiState.value.copy(
                toastMessage = "تم حفظ الملف بنجاح في مجلد التنزيلات ($path) ✓"
            )
        }.onFailure { e ->
            // Fallback to system share
            FileExportUtil.shareContent(context, fileName, content, format.mimeType)
            _uiState.value = _uiState.value.copy(
                toastMessage = "تم فتح خيارات حفظ وتصدير الملف: ${e.message}"
            )
        }
    }

    fun exportAndShare(context: Context) {
        val payload = _uiState.value.alignmentPayload
        val format = _uiState.value.selectedFormat
        val fileName = "SliceVoice_alignment.${format.extension}"

        val content = when (format) {
            OutputFormat.JSON -> payload.toJsonString()
            OutputFormat.SRT -> payload.toSrtString()
            OutputFormat.VTT -> payload.toVttString()
            OutputFormat.CSV -> payload.toCsvString()
        }

        FileExportUtil.shareContent(context, fileName, content, format.mimeType)
    }

    fun exportHistoryItem(context: Context, item: AlignmentPayload) {
        val format = _uiState.value.selectedFormat
        val fileName = "SliceVoice_${item.audioFileName.substringBeforeLast('.')}.${format.extension}"

        val content = when (format) {
            OutputFormat.JSON -> item.toJsonString()
            OutputFormat.SRT -> item.toSrtString()
            OutputFormat.VTT -> item.toVttString()
            OutputFormat.CSV -> item.toCsvString()
        }

        FileExportUtil.shareContent(context, fileName, content, format.mimeType)
    }

    fun copyToClipboard(context: Context) {
        val payload = _uiState.value.alignmentPayload
        val content = when (_uiState.value.selectedFormat) {
            OutputFormat.JSON -> payload.toJsonString()
            OutputFormat.SRT -> payload.toSrtString()
            OutputFormat.VTT -> payload.toVttString()
            OutputFormat.CSV -> payload.toCsvString()
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("SliceVoice_${_uiState.value.selectedFormat.extension}", content)
        clipboard.setPrimaryClip(clip)

        _uiState.value = _uiState.value.copy(toastMessage = "تم نسخ كود التوقيتات للحافظة بنجاح! ✓")
    }

    // MARK: - Modals & Helpers
    fun showTextEditDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTextEditSheet = show)
    }

    fun showPresetPickerDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPresetPickerSheet = show)
    }

    fun dismissToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun deleteHistoryItem(item: AlignmentPayload) {
        _uiState.value = _uiState.value.copy(
            historyList = _uiState.value.historyList.filter { it.id != item.id }
        )
    }

    fun loadFromHistory(item: AlignmentPayload) {
        _uiState.value = _uiState.value.copy(
            alignmentPayload = item,
            audioUri = null,
            audioFileName = item.audioFileName,
            audioDurationSec = item.audioDurationSec,
            hasResult = true,
            selectedTab = AlignerTab.STUDIO,
            toastMessage = "تنبيه: الملف الصوتي الأصلي غير محفوظ، يرجى اختيار ملف صوتي جديد للمزامنة"
        )
        audioEngine.setDuration(item.audioDurationSec)
    }
}
