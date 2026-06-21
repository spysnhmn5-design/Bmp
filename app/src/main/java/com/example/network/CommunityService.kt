package com.example.network

import android.util.Log
import com.example.data.model.CommunityMessage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object CommunityService {
    private const val TAG = "CommunityService"
    // Open public, keyless document JSONBin on npoint.io
    private const val BIN_URL = "https://api.npoint.io/abf845d0f622cd4f7ca8"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchMessages(): List<CommunityMessage> {
        val request = Request.Builder()
            .url(BIN_URL)
            .get()
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful || responseBody.isBlank()) {
                Log.e(TAG, "Failed to fetch from community bin: ${response.code}")
                return getSeedMessages()
            }

            val array = JSONArray(responseBody)
            val list = mutableListOf<CommunityMessage>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CommunityMessage(
                        authorName = obj.optString("authorName", "עוזר סורק"),
                        content = obj.optString("content", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isFromMe = false
                    )
                )
            }
            list.sortedBy { it.timestamp }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching social messages, reverting to local seeds", e)
            getSeedMessages()
        }
    }

    suspend fun postMessage(authorName: String, content: String): Boolean {
        return try {
            // First get existing
            val existing = fetchMessages().toMutableList()
            
            // Append new message
            val newItem = CommunityMessage(
                authorName = authorName,
                content = content,
                timestamp = System.currentTimeMillis(),
                isFromMe = true
            )
            existing.add(newItem)

            // Limit list size to last 50 messages to keep bin light and fast
            val limitedList = if (existing.size > 50) existing.takeLast(50) else existing

            // Format as json array
            val jsonArray = JSONArray()
            for (msg in limitedList) {
                val obj = JSONObject().apply {
                    put("authorName", msg.authorName)
                    put("content", msg.content)
                    put("timestamp", msg.timestamp)
                }
                jsonArray.put(obj)
            }

            // Put back
            val requestBody = jsonArray.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(BIN_URL)
                .put(requestBody)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit post to community bin", e)
            false
        }
    }

    fun getSeedMessages(): List<CommunityMessage> {
        val now = System.currentTimeMillis()
        return listOf(
            CommunityMessage(
                authorName = "אלירן כהן",
                content = "שלום לכולם! הכלי הזה פשוט מעולה לסריקת אתרי לימוד ומדריכים שלמים לקריאה באופליין בטיסות. שמירתי ספריית למידה שלמה!",
                timestamp = now - 3 * 3600 * 1000 // 3 hours ago
            ),
            CommunityMessage(
                authorName = "נועה מזרחי",
                content = "מישהו ניסה להפעיל את עוזר ה-AI על אתרים ארוכים? הוא עושה עבודה מדהימה בסיכום וניתוח התוכן! חוסך לי מלא זמן קריאה.",
                timestamp = now - 2 * 3600 * 1000 // 2 hours ago
            ),
            CommunityMessage(
                authorName = "דוד לוי",
                content = "שימו לב שאפשר לקבוע קוד גישה (PIN) נפרד לכל פרויקט שנסרק, זה קריטי לשמירה על הפרטיות כאשר משאילים את הטלפון לחברים.",
                timestamp = now - 1 * 3600 * 1000 // 1 hour ago
            ),
            CommunityMessage(
                authorName = "יוסי ברק (מפתח הכלי)",
                content = "ברוכים הבאים לקהילת הפיתוח שלנו! דברו חופשי, הציעו רעיונות תחת מסך 'תכונות ניסיוניות' ואני כבר אשקול אותם לגרסה הבאה. 😊",
                timestamp = now - 1200 * 1000 // 20 minutes ago
            )
        )
    }
}
