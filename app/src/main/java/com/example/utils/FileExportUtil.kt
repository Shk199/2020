package com.example.utils

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

object FileExportUtil {

    /**
     * Saves alignment output directly to the phone's public Downloads directory.
     * Fully compatible with modern Android (10, 11, 12, 13, 14, 15) and legacy versions.
     * On Android 9 (API 28) and below, requests WRITE_EXTERNAL_STORAGE at runtime or falls back to sharing if denied.
     */
    fun saveToDownloads(
        context: Context,
        fileName: String,
        content: String,
        mimeType: String
    ): Result<String> {
        return try {
            val bytes = content.toByteArray(Charsets.UTF_8)
            var savedPath = "Downloads/SliceVoice/$fileName"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SliceVoice")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { os ->
                        os.write(bytes)
                        os.flush()
                    }

                    // Clear pending status so other apps can access the file immediately
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                } else {
                    // Fallback to standard app download directory
                    val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    val targetFile = File(fallbackDir, fileName)
                    FileOutputStream(targetFile).use { it.write(bytes) }
                    savedPath = targetFile.absolutePath
                }
            } else {
                // Android 9 (API 28) and below: check and request runtime permission
                val isGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

                if (!isGranted) {
                    val activity = findActivity(context)
                    if (activity != null) {
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            1001
                        )
                    }
                    // Fallback to sharing when permission is not granted
                    shareContent(context, fileName, content, mimeType)
                    return Result.failure(SecurityException("تم رفض إذن التخزين، تم تحويل الملف إلى المشاركة"))
                }

                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "SliceVoice")
                if (!targetDir.exists()) targetDir.mkdirs()

                val targetFile = File(targetDir, fileName)
                FileOutputStream(targetFile).use { fos ->
                    fos.write(bytes)
                    fos.flush()
                }
                savedPath = targetFile.absolutePath
            }

            Result.success(savedPath)
        } catch (e: Exception) {
            shareContent(context, fileName, content, mimeType)
            Result.failure(e)
        }
    }

    private fun findActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) return currentContext
            currentContext = currentContext.baseContext
        }
        return null
    }

    /**
     * Shares generated alignment content with other apps (Telegram, WhatsApp, Drive, CapCut, etc.)
     */
    fun shareContent(
        context: Context,
        fileName: String,
        content: String,
        mimeType: String
    ) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, fileName)
                putExtra(Intent.EXTRA_TEXT, content)
                type = mimeType
            }
            val shareIntent = Intent.createChooser(sendIntent, "مشاركة ملف المزامنة ($fileName)")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (_: Exception) {
            // graceful ignore
        }
    }
}
