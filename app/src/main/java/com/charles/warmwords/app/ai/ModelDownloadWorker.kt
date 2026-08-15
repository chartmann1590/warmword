package com.charles.warmwords.app.ai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.charles.warmwords.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "ModelDownloadWorker"
private const val CHANNEL_ID = "model_download_channel_foreground"
private const val TMP_FILE_EXT = ".gallerytmp"

data class DownloadProgress(
    val receivedBytes: Long,
    val totalBytes: Long,
    val progressPercent: Int,
    val downloadRateBytesPerSecond: Long,
    val remainingMs: Long
)

object DownloadWorkerInput {
    const val KEY_MODEL_URL = "model_url"
    const val KEY_MODEL_NAME = "model_name"
    const val KEY_MODEL_VERSION = "model_version"
    const val KEY_MODEL_FILE_NAME = "model_file_name"
    const val KEY_MODEL_TOTAL_BYTES = "model_total_bytes"
    const val KEY_MODEL_SHA256 = "model_sha256"
    const val KEY_MODEL_DIR = "model_dir"
}

object DownloadWorkerOutput {
    const val KEY_MODEL_PATH = "model_path"
    const val KEY_DOWNLOAD_ERROR = "download_error"
}

class ModelDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val externalFilesDir = context.getExternalFilesDir(null)

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val notificationId: Int = params.id.hashCode()

    init {
        if (!channelCreated) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.model_download_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.model_download_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
            channelCreated = true
        }
    }

    override suspend fun doWork(): Result {
        val modelUrl = inputData.getString(DownloadWorkerInput.KEY_MODEL_URL)
        val modelName = inputData.getString(DownloadWorkerInput.KEY_MODEL_NAME) ?: "Model"
        val modelVersion = inputData.getString(DownloadWorkerInput.KEY_MODEL_VERSION) ?: "v1"
        val fileName = inputData.getString(DownloadWorkerInput.KEY_MODEL_FILE_NAME)
        val totalBytes = inputData.getLong(DownloadWorkerInput.KEY_MODEL_TOTAL_BYTES, 0L)
        val expectedSha256 = inputData.getString(DownloadWorkerInput.KEY_MODEL_SHA256)
        val modelDir = inputData.getString(DownloadWorkerInput.KEY_MODEL_DIR) ?: "models"

        return withContext(Dispatchers.IO) {
            if (modelUrl == null || fileName == null) {
                Result.failure()
            } else {
                try {
                    setForeground(
                        createForegroundInfo(progress = 0, modelName = modelName)
                    )

                    val outputDir = File(
                        externalFilesDir,
                        listOf(modelDir, modelVersion).joinToString(separator = File.separator)
                    )
                    if (!outputDir.exists()) {
                        outputDir.mkdirs()
                    }

                    val tmpFile = File(outputDir, "$fileName$TMP_FILE_EXT")
                    val finalFile = File(outputDir, fileName)

                     if (finalFile.exists() && finalFile.length() == totalBytes) {
                        Log.d(TAG, "Model already fully downloaded at ${finalFile.absolutePath}")
                        if (expectedSha256 != null) {
                            val actualSha256 = computeSha256(finalFile)
                            if (actualSha256 != expectedSha256) {
                                Log.e(TAG, "SHA-256 mismatch on existing file: expected=$expectedSha256, actual=$actualSha256")
                                throw IOException("SHA-256 checksum verification failed for existing file")
                            }
                            Log.d(TAG, "SHA-256 verification passed for existing file")
                        }
                        return@withContext Result.success(
                            Data.Builder()
                                .putString(DownloadWorkerOutput.KEY_MODEL_PATH, finalFile.absolutePath)
                                .build()
                        )
                     }

                    val downloadedBytes = downloadWithResume(
                        url = modelUrl,
                        outputFile = tmpFile,
                        existingBytes = tmpFile.length(),
                        totalBytes = totalBytes
                    )

                    if (downloadedBytes < 0) {
                        throw IOException("Download failed")
                    }

                    tmpFile.renameTo(finalFile)
                    Log.d(TAG, "Download complete: ${finalFile.absolutePath}")

                    if (expectedSha256 != null) {
                        val actualSha256 = computeSha256(finalFile)
                        if (actualSha256 != expectedSha256) {
                            Log.e(TAG, "SHA-256 mismatch: expected=$expectedSha256, actual=$actualSha256")
                            throw IOException("SHA-256 checksum verification failed")
                        }
                        Log.d(TAG, "SHA-256 verification passed")
                    }

                    Result.success(
                        Data.Builder()
                            .putString(DownloadWorkerOutput.KEY_MODEL_PATH, finalFile.absolutePath)
                            .build()
                    )
                } catch (e: IOException) {
                    Log.e(TAG, e.message, e)
                    Result.failure(
                        Data.Builder()
                            .putString(DownloadWorkerOutput.KEY_DOWNLOAD_ERROR, e.message)
                            .build()
                    )
                }
            }
        }
    }

    private suspend fun downloadWithResume(
        url: String,
        outputFile: File,
        existingBytes: Long,
        totalBytes: Long
    ): Long {
        val urlObj = URL(url)
        val connection = urlObj.openConnection() as HttpURLConnection
        connection.setRequestProperty("Accept-Encoding", "identity")
        connection.connectTimeout = 30000
        connection.readTimeout = 60000

        if (existingBytes > 0) {
            Log.d(TAG, "Resuming download from byte $existingBytes")
            connection.setRequestProperty("Range", "bytes=$existingBytes-")
        }

        connection.connect()

        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK &&
            responseCode != HttpURLConnection.HTTP_PARTIAL
        ) {
            throw IOException("HTTP error code: $responseCode")
        }

         val contentRange = connection.getHeaderField("Content-Range")
         var serverTotal = connection.contentLengthLong

        if (contentRange != null) {
            val rangeParts = contentRange.substringAfter("bytes ").split("/")
            val totalPart = rangeParts.getOrNull(1)
            if (totalPart != null && totalPart != "*") {
                serverTotal = totalPart.toLong()
            }
        }

        if (serverTotal <= 0) {
            serverTotal = totalBytes
            Log.d(TAG, "Falling back to declared totalBytes=$totalBytes")
        }

        val inputStream: InputStream = connection.inputStream
        val outputStream = FileOutputStream(outputFile, true)

        val buffer = ByteArray(8192)
        var bytesRead: Int
        var totalDownloaded = existingBytes
        var lastProgressUpdate = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
              outputStream.write(buffer, 0, bytesRead)
              totalDownloaded += bytesRead

              val curTs = System.currentTimeMillis()
              if (curTs - lastProgressUpdate > 1000 || totalDownloaded >= serverTotal) {
                val progressPercent = if (serverTotal > 0) {
                    (totalDownloaded * 100 / serverTotal).toInt()
                    .coerceIn(0, 100)
                } else 0

                setProgress(
                    Data.Builder()
                        .putLong("received_bytes", totalDownloaded)
                        .putLong("total_bytes", serverTotal)
                        .putInt("progress_percent", progressPercent)
                        .build()
                )
                setForeground(
                    createForegroundInfo(
                        progress = progressPercent,
                        modelName = inputData.getString(DownloadWorkerInput.KEY_MODEL_NAME)
                            ?: "Model"
                    )
                )
                lastProgressUpdate = curTs
            }
        }

        outputStream.close()
        inputStream.close()
        connection.disconnect()

        return totalDownloaded
    }

    private suspend fun computeSha256(file: File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val inputStream = file.inputStream()
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
        inputStream.close()
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(0)
    }

    private fun createForegroundInfo(
        progress: Int,
        modelName: String? = null
    ): ForegroundInfo {
        var title = applicationContext.getString(R.string.downloading_model)
        if (modelName != null) {
            title = applicationContext.getString(R.string.downloading_model_named, modelName)
        }
        val content = applicationContext.getString(
            R.string.download_progress,
            progress
        )

        val intent = Intent(applicationContext, Class.forName("com.charles.warmwords.app.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(com.charles.warmwords.app.R.drawable.ic_notification)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .setContentIntent(pendingIntent)
            .build()

        return ForegroundInfo(
            notificationId,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    companion object {
        private var channelCreated = false
    }
}
