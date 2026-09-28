package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SkillRepository
import com.example.data.SkillsData
import com.example.data.local.AppDatabase
import com.example.data.local.AuditResultEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.RecipeEntity
import com.example.data.local.SkillEntity
import com.example.data.local.SystemCommandEntity
import com.example.engine.MasterizerEngine
import com.example.engine.SkillAuditReport
import com.example.network.ContentItem
import com.example.network.GeminiApiClient
import com.example.network.PartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    SKILLS_CATALOG,
    DYNAMIC_LAUNCHER,
    STANDALONE_EDITOR_PREVIEW,
    DISCOVERY_TERMINAL,
    MASTERIZER_HUB,
    AI_CHATBOT
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = SkillRepository(database)
    private val geminiClient = GeminiApiClient()

    // Dynamic Launcher SSoT Manager
    val launcherConfig: StateFlow<com.example.data.launcher.LauncherConfig> =
        com.example.data.launcher.LauncherSSoTManager.currentConfig
    val installedApps: StateFlow<List<com.example.data.launcher.InstalledApp>> =
        com.example.data.launcher.LauncherSSoTManager.installedApps

    // SSoT FileSystem Watcher Service flows
    val watcherStatus: StateFlow<com.example.service.WatcherStatus> =
        com.example.service.SSoTWatcherEngine.status
    val watcherEvents: StateFlow<List<com.example.service.FileWatchEvent>> =
        com.example.service.SSoTWatcherEngine.events

    // APK Thin Wrappers & SSoT Symlink Tree
    val apkWrappers: StateFlow<List<com.example.data.wrapper.ApkWrapperConfig>> =
        com.example.data.wrapper.ApkWrapperManager.wrappers
    val symlinkTree: StateFlow<List<com.example.data.wrapper.DirectorySymlinkNode>> =
        com.example.data.wrapper.ApkWrapperManager.symlinkTree
    val lastHookResult: StateFlow<com.example.data.wrapper.HookExecutionOutput?> =
        com.example.data.wrapper.ApkWrapperManager.lastHookResult

    init {
        com.example.data.launcher.LauncherSSoTManager.init(application)
        com.example.service.SSoTWatcherEngine.start(application)
        com.example.data.wrapper.ApkWrapperManager.init(application)
    }

    fun executeWrapperHook(pkgOrId: String, hookType: String) {
        com.example.data.wrapper.ApkWrapperManager.executeHook(getApplication(), pkgOrId, hookType)
    }

    fun createCustomWrapper(pkg: String, apkPath: String, title: String, type: String) {
        com.example.data.wrapper.ApkWrapperManager.wrapCustomApk(
            context = getApplication(),
            packageName = pkg,
            originalApkPath = apkPath,
            title = title,
            wrapperType = type
        )
    }

    fun refreshSymlinkTree() {
        com.example.data.wrapper.ApkWrapperManager.refreshSymlinkTree(getApplication())
    }

    fun applyLauncherMarkdown(md: String) {
        com.example.data.launcher.LauncherSSoTManager.updateSkillContent(getApplication(), md)
    }

    fun applyLauncherPreset(preset: String) {
        com.example.data.launcher.LauncherSSoTManager.applyPreset(getApplication(), preset)
    }

    fun simulateWatcherExternalWrite(fileName: String, content: String, asSymlink: Boolean = false) {
        com.example.service.SSoTWatcherEngine.simulateExternalWrite(
            context = getApplication(),
            fileName = fileName,
            content = content,
            asSymlink = asSymlink
        )
    }

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.SKILLS_CATALOG)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Skills
    val allSkills: StateFlow<List<SkillEntity>> = repository.allSkills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Discovered System Commands (Single Source of Truth)
    val allCommands: StateFlow<List<SystemCommandEntity>> = repository.allCommands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSkill = MutableStateFlow<SkillEntity?>(null)
    val selectedSkill: StateFlow<SkillEntity?> = _selectedSkill.asStateFlow()

    fun selectSkill(skill: SkillEntity?) {
        _selectedSkill.value = skill
    }

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun selectCategory(cat: String) {
        _selectedCategory.value = cat
    }

    // Editor & Standalone Preview Code
    private val _editorCode = MutableStateFlow(SkillsData.INITIAL_RECIPES.first().code)
    val editorCode: StateFlow<String> = _editorCode.asStateFlow()

    fun updateEditorCode(code: String) {
        _editorCode.value = code
    }

    // Masterizer Audit State
    private val _currentAudit = MutableStateFlow(MasterizerEngine.auditCode(_editorCode.value))
    val currentAudit: StateFlow<SkillAuditReport> = _currentAudit.asStateFlow()

    val auditHistory: StateFlow<List<AuditResultEntity>> = repository.auditHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun runAudit(codeToAudit: String = _editorCode.value) {
        val report = MasterizerEngine.auditCode(codeToAudit)
        _currentAudit.value = report

        viewModelScope.launch {
            repository.recordAudit(
                AuditResultEntity(
                    codeSnippet = codeToAudit.take(500),
                    overallScore = report.overallScore,
                    securityScore = report.securityScore,
                    architectureScore = report.architectureScore,
                    uiAccessibilityScore = report.uiAccessibilityScore,
                    offlineRobustnessScore = report.offlineRobustnessScore,
                    suggestions = report.recommendations.joinToString("; ")
                )
            )
        }
    }

    // AI Chatbox
    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    fun selectModel(model: String) {
        _selectedModel.value = model
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        val currentModel = _selectedModel.value

        viewModelScope.launch {
            // Save user message to Room DB
            repository.addChatMessage(
                role = "user",
                content = userText,
                model = currentModel
            )

            _isGenerating.value = true

            // Build conversation history for multi-turn
            val history = chatMessages.value.takeLast(6).map {
                ContentItem(
                    role = if (it.role == "user") "user" else "model",
                    parts = listOf(PartItem(text = it.content))
                )
            }

            val result = geminiClient.generateContent(
                prompt = userText,
                history = history,
                model = currentModel,
                customSystemInstruction = SkillsData.buildSystemInstruction()
            )

            _isGenerating.value = false

            val replyText = result.getOrElse { "Error generating response. Offline knowledge base active." }

            // Extract code snippet if present
            val extractedCode = extractCodeSnippet(replyText)

            // Save assistant message to Room DB
            repository.addChatMessage(
                role = "model",
                content = replyText,
                model = currentModel,
                extractedCode = extractedCode
            )
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun injectCodeToEditorAndSwitch(code: String) {
        _editorCode.value = code
        _currentScreen.value = AppScreen.STANDALONE_EDITOR_PREVIEW
    }

    private fun extractCodeSnippet(text: String): String? {
        val pattern = Regex("```(?:kotlin|xml|json|javascript|jsx)?\\s*([\\s\\S]*?)```")
        val match = pattern.find(text)
        return match?.groups?.get(1)?.value?.trim()
    }
}
