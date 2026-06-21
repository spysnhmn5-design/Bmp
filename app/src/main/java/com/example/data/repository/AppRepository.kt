package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.CommunityMessage
import com.example.data.model.LocalMedia
import com.example.data.model.WebProject
import com.example.network.CommunityService
import com.example.network.WebCrawler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ProjectRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val projectDao = db.projectDao()
    private val mediaDao = db.mediaDao()
    private val communityDao = db.communityDao()

    // Web projects
    val allProjects: Flow<List<WebProject>> = projectDao.getAllProjects()

    suspend fun getProjectById(id: Int): WebProject? = withContext(Dispatchers.IO) {
        projectDao.getProjectById(id)
    }

    suspend fun deleteProject(project: WebProject) = withContext(Dispatchers.IO) {
        projectDao.deleteProject(project)
        mediaDao.deleteMediaForProject(project.id)
    }

    suspend fun updateProject(project: WebProject) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project)
    }

    // Crawl a URL and save it in SQLite and internal files
    suspend fun crawlAndSave(url: String, onProgress: (String) -> Unit): WebProject = withContext(Dispatchers.IO) {
        val (project, mediaList) = WebCrawler.crawl(context, url, onProgress)
        
        // Insert empty/initial project first to get a real ID increment
        val generatedId = projectDao.insertProject(project).toInt()
        
        // Update all local media to map back to this generated ID
        onProgress("Saving offline media links...")
        val updatedMediaList = mediaList.map { it.copy(projectId = generatedId) }
        updatedMediaList.forEach {
            mediaDao.insertMedia(it)
        }
        
        // Return matching project
        val finalProject = project.copy(id = generatedId)
        projectDao.updateProject(finalProject)
        
        onProgress("Scan complete! Page saved successfully.")
        finalProject
    }

    // Media
    fun getMediaForProject(projectId: Int): Flow<List<LocalMedia>> {
        return mediaDao.getMediaForProject(projectId)
    }

    // Community Message Board Cache & Synchronizer
    val cachedMessages: Flow<List<CommunityMessage>> = communityDao.getAllMessages()

    suspend fun syncCommunityMessages() = withContext(Dispatchers.IO) {
        try {
            val netMessages = CommunityService.fetchMessages()
            if (netMessages.isNotEmpty()) {
                communityDao.clearAll()
                communityDao.insertMessages(netMessages)
            }
        } catch (e: Exception) {
            // If offline, default fallback to seeds if database is empty
            val current = communityDao.getAllMessages().first()
            if (current.isEmpty()) {
                communityDao.insertMessages(CommunityService.getSeedMessages())
            }
        }
    }

    suspend fun submitCommunityMessage(author: String, content: String): Boolean = withContext(Dispatchers.IO) {
        val success = CommunityService.postMessage(author, content)
        if (success) {
            // Refresh
            syncCommunityMessages()
        } else {
            // Local fallback post
            communityDao.insertMessage(
                CommunityMessage(
                    authorName = author,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    isFromMe = true
                )
            )
        }
        success
    }
}
