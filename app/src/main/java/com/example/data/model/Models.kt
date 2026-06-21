package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "web_projects")
data class WebProject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val title: String,
    val scannedAt: Long,
    val textContent: String = "",
    val htmlContent: String = "",
    val passcode: String? = null // Stored plain or hash; used for lock checks
)

@Entity(tableName = "local_media")
data class LocalMedia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val originalUrl: String,
    val localFileName: String, // Saved inside app filesDir
    val mimeType: String
)

@Entity(tableName = "community_messages")
data class CommunityMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorName: String,
    val content: String,
    val timestamp: Long,
    val isFromMe: Boolean = false
)
