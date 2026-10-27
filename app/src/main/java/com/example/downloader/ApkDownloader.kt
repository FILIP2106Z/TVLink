package com.example.downloader

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ApkDownloader(private val context: Context) {

    private val TAG = "ApkDownloader"

    suspend fun downloadApk(
        urlString: String,
        onProgress: (Int) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        var currentUrl = urlString.trim()
        val destFile = File(context.cacheDir, "remote_sideload.apk")

        try {
            var redirects = 0
            var connection: HttpURLConnection

            while (true) {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = false
                connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; AdbCommander) AppleWebKit/537.36"
                )
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.connect()

                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                    code == HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == HttpURLConnection.HTTP_SEE_OTHER ||
                    code == 307 || code == 308
                ) {
                    val location = connection.getHeaderField("Location")
                        ?: return@withContext Result.failure(Exception("Redirect with missing Location header"))
                    currentUrl = if (location.startsWith("http")) {
                        location
                    } else {
                        URL(url, location).toString()
                    }
                    redirects++
                    if (redirects > 8) {
                        return@withContext Result.failure(Exception("Too many HTTP redirects"))
                    }
                    connection.disconnect()
                } else if (code in 200..299) {
                    break
                } else {
                    return@withContext Result.failure(Exception("HTTP error $code: ${connection.responseMessage}"))
                }
            }

            val totalBytes = connection.contentLengthLong
            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(destFile)

            var downloadedBytes = 0L
            val buffer = ByteArray(8192)
            var read: Int

            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
                downloadedBytes += read
                if (totalBytes > 0) {
                    val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                    onProgress(progress)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            connection.disconnect()

            if (destFile.length() < 1000) {
                return@withContext Result.failure(Exception("Downloaded file is too small or corrupt"))
            }

            Log.d(TAG, "Downloaded APK successfully: ${destFile.length()} bytes")
            Result.success(destFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading APK from $urlString", e)
            Result.failure(e)
        }
    }
}
