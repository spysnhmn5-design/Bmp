package com.example.network

import android.content.Context
import android.util.Log
import com.example.data.model.LocalMedia
import com.example.data.model.WebProject
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.File
import java.util.concurrent.TimeUnit

object WebCrawler {
    private const val TAG = "WebCrawler"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun crawl(context: Context, targetUrl: String, onProgress: (String) -> Unit): Pair<WebProject, List<LocalMedia>> {
        var formattedUrl = targetUrl.trim()
        if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            formattedUrl = "https://$formattedUrl"
        }

        onProgress("Connecting to $formattedUrl...")
        Log.d(TAG, "Starting crawl for: $formattedUrl")

        val request = Request.Builder()
            .url(formattedUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Failed to load page. HTTP Code: ${response.code}")
        }

        val rawHtml = response.body?.string() ?: throw Exception("Empty response body")
        onProgress("Parsing web content...")

        val doc = Jsoup.parse(rawHtml, formattedUrl)
        val title = doc.title().ifBlank { "Scanned Site: ${doc.location()}" }
        val plainText = doc.body()?.text() ?: ""
        
        // Generate a random temporary project ID for folder creation (or use timestamp)
        val tempId = System.currentTimeMillis().toInt()
        val projectDir = File(context.filesDir, "project_$tempId")
        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }

        val images = doc.select("img")
        val localMediaList = mutableListOf<LocalMedia>()
        
        onProgress("Found ${images.size} images. Saving locally for offline support...")
        
        var downloadedCount = 0
        images.forEachIndexed { index, imgElement ->
            val imgUrl = imgElement.absUrl("src")
            if (imgUrl.isNotBlank() && (imgUrl.startsWith("http://") || imgUrl.startsWith("https://"))) {
                val extension = getExtension(imgUrl)
                val fileName = "img_${index}_${System.currentTimeMillis()}.$extension"
                val destFile = File(projectDir, fileName)
                
                try {
                    onProgress("Downloading image ${index + 1}/${images.size}...")
                    val success = downloadFile(imgUrl, destFile)
                    if (success) {
                        downloadedCount++
                        // Update the img src inside Jsoup document to point to local file!
                        imgElement.attr("src", "file://${destFile.absolutePath}")
                        
                        localMediaList.add(
                            LocalMedia(
                                projectId = 0, // Will be updated when project is inserted in room with final ID
                                originalUrl = imgUrl,
                                localFileName = fileName,
                                mimeType = "image/$extension"
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download image: $imgUrl", e)
                }
            }
        }

        onProgress("Analyzing media and video elements...")
        // Scrape for video/iframe elements as well to store their links
        val videos = doc.select("video source, iframe")
        videos.forEachIndexed { index, videoElement ->
            var url = videoElement.absUrl("src")
            if (url.isBlank()) {
                url = videoElement.absUrl("data-src")
            }
            if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                localMediaList.add(
                    LocalMedia(
                        projectId = 0,
                        originalUrl = url,
                        localFileName = "video_$index", // Placeholder reference since full streaming video files can be huge
                        mimeType = if (url.contains("youtube") || url.contains("youtu.be")) "video/youtube" else "video/mp4"
                    )
                )
            }
        }

        onProgress("Finalizing web project package...")
        val finalizedHtml = doc.html()
        
        val project = WebProject(
            url = formattedUrl,
            title = title,
            scannedAt = System.currentTimeMillis(),
            textContent = plainText,
            htmlContent = finalizedHtml,
            passcode = null // User can protect it later
        )

        return Pair(project, localMediaList)
    }

    private fun downloadFile(url: String, destFile: File): Boolean {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return false
            response.body?.byteStream()?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return false
        }
        return true
    }

    private fun getExtension(url: String): String {
        val path = url.substringBefore('?').substringBefore('#')
        val ext = path.substringAfterLast('.', "jpg")
        return if (ext.length in 2..4) ext else "jpg"
    }
}
