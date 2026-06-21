package com.example.network

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Sends a question to Gemini based on the content of a crawled website.
     */
    suspend fun analyzeWebPage(webTitle: String, webContent: String, userQuestion: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "שגיאה: מפתח ה-API של Gemini אינו מוגדר. אנא הגדר אותו בפאנל Secrets של AI Studio."
        }

        // Limit the site content to avoid token overflow
        val truncatedContent = if (webContent.length > 6000) {
            webContent.substring(0, 6000) + "... [תוכן קוצר לצורכי חיסכון במקום]"
        } else {
            webContent
        }

        val systemInstruction = "אתה עוזר AI מקצועי באפליקציית WebVault. תפקידך לענות על שאלות המשתמש לגבי התוכן של האתר הבא שנסרק ונשמר באופליין. ענה תמיד בעברית ברורה וידידותית למשתמש."
        val prompt = """
            שם האתר: $webTitle
            תוכן האתר:
            ---
            $truncatedContent
            ---
            שאלת המשתמש: $userQuestion
        """.trimIndent()

        try {
            val jsonRequest = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
            }

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Request failed: ${response.code} - $responseBody")
                return@withContext "שגיאה בגישה ל-Gemini (קוד: ${response.code}). אנא וודא שמפתח ה-API תקין."
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")

            return@withContext text.trim()
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API failure", e)
            return@withContext "שגיאה בתקשורת עם הבינה המלאכותית: ${e.localizedMessage ?: "שגיאה לא ידועה"}"
        }
    }
}
