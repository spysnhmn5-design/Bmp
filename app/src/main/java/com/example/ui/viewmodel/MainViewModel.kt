package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CommunityMessage
import com.example.data.model.WebProject
import com.example.data.repository.ProjectRepository
import com.example.network.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    DASHBOARD,
    PROJECT_VIEWER,
    COMMUNITY_HUB,
    EXPERIMENTAL_LAB
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProjectRepository(application)

    // Current screen navigation
    val currentScreen = MutableStateFlow(Screen.DASHBOARD)

    // All crawled web projects
    val projects: StateFlow<List<WebProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All community messages
    val communityMessages: StateFlow<List<CommunityMessage>> = repository.cachedMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected project for reading/AI chat
    val selectedProject = MutableStateFlow<WebProject?>(null)

    // PIN lock workflow states
    val projectLockedForVerification = MutableStateFlow<WebProject?>(null)
    val pinInput = MutableStateFlow("")
    val pinError = MutableStateFlow(false)

    // Web scanner state
    val isCrawling = MutableStateFlow(false)
    val crawlProgress = MutableStateFlow("")
    val targetUrlInput = MutableStateFlow("")
    val showCrawlDialog = MutableStateFlow(false)

    // Community input state
    val nicknameInput = MutableStateFlow("סורק עצמאי #${(100..999).random()}")
    val messageInput = MutableStateFlow("")
    val isSubmittingMessage = MutableStateFlow(false)

    // AI Assistant state
    val aiPromptInput = MutableStateFlow("")
    val aiIsGenerating = MutableStateFlow(false)
    val projectAiChats = MutableStateFlow<Map<Int, List<Pair<String, String>>>>(emptyMap()) // map: projectId -> list of (userPrompt, aiResponse)

    // Experimental state features
    val enableJavascriptSandbox = MutableStateFlow(false)
    val autoCompressImages = MutableStateFlow(true)
    val deepCrawlingDepth = MutableStateFlow(false)
    val darkThemeInjected = MutableStateFlow(false)

    init {
        // Sync community posts on app start
        viewModelScope.launch {
            repository.syncCommunityMessages()
        }
    }

    // Crawl action
    fun startCrawl(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            try {
                isCrawling.value = true
                crawlProgress.value = "מאתחל סורק..."
                val project = repository.crawlAndSave(url) { progressStep ->
                    crawlProgress.value = progressStep
                }
                targetUrlInput.value = ""
                showCrawlDialog.value = false
                
                // Select and open the newly crawled project immediately
                selectProject(project)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Crawl failed", e)
                crawlProgress.value = "שגיאה: ${e.localizedMessage ?: "לא ניתן לגשת לאתר"}"
            } finally {
                isCrawling.value = false
            }
        }
    }

    // Handles project selection & lock mechanism
    fun selectProject(project: WebProject) {
        if (project.passcode != null) {
            // Check if password passcode is required
            projectLockedForVerification.value = project
            pinInput.value = ""
            pinError.value = false
        } else {
            selectedProject.value = project
            currentScreen.value = Screen.PROJECT_VIEWER
        }
    }

    fun submitPin(enteredPin: String) {
        val project = projectLockedForVerification.value ?: return
        if (project.passcode == enteredPin) {
            projectLockedForVerification.value = null
            selectedProject.value = project
            currentScreen.value = Screen.PROJECT_VIEWER
        } else {
            pinError.value = true
        }
    }

    fun cancelPinVerification() {
        projectLockedForVerification.value = null
        pinInput.value = ""
        pinError.value = false
    }

    // Modify PIN passcode for selected project
    fun updateProjectPasscode(project: WebProject, newPasscode: String?) {
        viewModelScope.launch {
            val updated = project.copy(passcode = if (newPasscode.isNullOrBlank()) null else newPasscode)
            repository.updateProject(updated)
            if (selectedProject.value?.id == project.id) {
                selectedProject.value = updated
            }
        }
    }

    fun deleteProject(project: WebProject) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (selectedProject.value?.id == project.id) {
                selectedProject.value = null
                currentScreen.value = Screen.DASHBOARD
            }
        }
    }

    // Send AI queries
    fun askAiAssistant(question: String) {
        val project = selectedProject.value ?: return
        if (question.isBlank()) return

        viewModelScope.launch {
            aiIsGenerating.value = true
            aiPromptInput.value = ""
            
            // Add user question first
            val currentMap = projectAiChats.value.toMutableMap()
            val currentChat = currentMap[project.id]?.toMutableList() ?: mutableListOf()
            currentChat.add(question to "חושב ומנתח את האתר...")
            currentMap[project.id] = currentChat
            projectAiChats.value = currentMap

            // Ask Gemini Web Query context
            val response = GeminiService.analyzeWebPage(project.title, project.textContent, question)
            
            // Replace loading response with exact answer
            val finalisedMap = projectAiChats.value.toMutableMap()
            val finalisedChat = finalisedMap[project.id]?.toMutableList() ?: mutableListOf()
            if (finalisedChat.isNotEmpty()) {
                finalisedChat[finalisedChat.size - 1] = question to response
            } else {
                finalisedChat.add(question to response)
            }
            finalisedMap[project.id] = finalisedChat
            projectAiChats.value = finalisedMap

            aiIsGenerating.value = false
        }
    }

    // Submit community post
    fun submitPost(content: String) {
        if (content.isBlank()) return
        val nick = nicknameInput.value.trim().ifEmpty { "משתמש אנונימי" }
        viewModelScope.launch {
            isSubmittingMessage.value = true
            val success = repository.submitCommunityMessage(nick, content)
            if (success) {
                messageInput.value = ""
            }
            isSubmittingMessage.value = false
        }
    }

    fun refreshCommunity() {
        viewModelScope.launch {
            repository.syncCommunityMessages()
        }
    }
}
