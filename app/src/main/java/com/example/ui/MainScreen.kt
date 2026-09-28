package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.DiscoveryTerminalScreen
import com.example.ui.screens.DynamicLauncherScreen
import com.example.ui.screens.EditorPreviewScreen
import com.example.ui.screens.MasterizerHubScreen
import com.example.ui.screens.SkillsCatalogScreen

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allSkills by viewModel.allSkills.collectAsStateWithLifecycle()
    val allCommands by viewModel.allCommands.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val editorCode by viewModel.editorCode.collectAsStateWithLifecycle()
    val currentAudit by viewModel.currentAudit.collectAsStateWithLifecycle()
    val auditHistory by viewModel.auditHistory.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val launcherConfig by viewModel.launcherConfig.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val watcherStatus by viewModel.watcherStatus.collectAsStateWithLifecycle()
    val watcherEvents by viewModel.watcherEvents.collectAsStateWithLifecycle()
    val apkWrappers by viewModel.apkWrappers.collectAsStateWithLifecycle()
    val symlinkTree by viewModel.symlinkTree.collectAsStateWithLifecycle()
    val lastHookResult by viewModel.lastHookResult.collectAsStateWithLifecycle()

    // Handle back button to return to home screen
    if (currentScreen != AppScreen.SKILLS_CATALOG) {
        BackHandler {
            viewModel.navigateTo(AppScreen.SKILLS_CATALOG)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isWide = maxWidth >= 600.dp

        if (isWide) {
            // Adaptive Tablet / Desktop Layout with Navigation Rail
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.SKILLS_CATALOG,
                        onClick = { viewModel.navigateTo(AppScreen.SKILLS_CATALOG) },
                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Skills") },
                        label = { Text("Skills", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.DYNAMIC_LAUNCHER,
                        onClick = { viewModel.navigateTo(AppScreen.DYNAMIC_LAUNCHER) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dynamic Launcher Canvas") },
                        label = { Text("Launcher", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.STANDALONE_EDITOR_PREVIEW,
                        onClick = { viewModel.navigateTo(AppScreen.STANDALONE_EDITOR_PREVIEW) },
                        icon = { Icon(Icons.Default.Code, contentDescription = "Editor & Preview") },
                        label = { Text("Preview", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.DISCOVERY_TERMINAL,
                        onClick = { viewModel.navigateTo(AppScreen.DISCOVERY_TERMINAL) },
                        icon = { Icon(Icons.Default.Terminal, contentDescription = "Discovery & SSOT") },
                        label = { Text("Discovery", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.MASTERIZER_HUB,
                        onClick = { viewModel.navigateTo(AppScreen.MASTERIZER_HUB) },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Masterizer") },
                        label = { Text("Masterizer", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.AI_CHATBOT,
                        onClick = { viewModel.navigateTo(AppScreen.AI_CHATBOT) },
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Chat") },
                        label = { Text("AI Assistant", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    ScreenContent(
                        currentScreen = currentScreen,
                        viewModel = viewModel,
                        allSkills = allSkills,
                        allCommands = allCommands,
                        selectedCategory = selectedCategory,
                        editorCode = editorCode,
                        currentAudit = currentAudit,
                        auditHistory = auditHistory,
                        chatMessages = chatMessages,
                        isGenerating = isGenerating,
                        selectedModel = selectedModel,
                        launcherConfig = launcherConfig,
                        installedApps = installedApps,
                        watcherStatus = watcherStatus,
                        watcherEvents = watcherEvents,
                        apkWrappers = apkWrappers,
                        symlinkTree = symlinkTree,
                        lastHookResult = lastHookResult
                    )
                }
            }
        } else {
            // Mobile Compact Layout with Bottom NavigationBar
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.SKILLS_CATALOG,
                            onClick = { viewModel.navigateTo(AppScreen.SKILLS_CATALOG) },
                            icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Skills") },
                            label = { Text("Skills") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.DYNAMIC_LAUNCHER,
                            onClick = { viewModel.navigateTo(AppScreen.DYNAMIC_LAUNCHER) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Launcher") },
                            label = { Text("Launcher") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.STANDALONE_EDITOR_PREVIEW,
                            onClick = { viewModel.navigateTo(AppScreen.STANDALONE_EDITOR_PREVIEW) },
                            icon = { Icon(Icons.Default.Code, contentDescription = "Editor & Preview") },
                            label = { Text("Preview") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.DISCOVERY_TERMINAL,
                            onClick = { viewModel.navigateTo(AppScreen.DISCOVERY_TERMINAL) },
                            icon = { Icon(Icons.Default.Terminal, contentDescription = "Discovery") },
                            label = { Text("Discovery") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.MASTERIZER_HUB,
                            onClick = { viewModel.navigateTo(AppScreen.MASTERIZER_HUB) },
                            icon = { Icon(Icons.Default.Security, contentDescription = "Masterizer") },
                            label = { Text("Masterizer") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.AI_CHATBOT,
                            onClick = { viewModel.navigateTo(AppScreen.AI_CHATBOT) },
                            icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Chat") },
                            label = { Text("AI Assistant") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                },
                contentWindowInsets = WindowInsets.safeDrawing,
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ScreenContent(
                        currentScreen = currentScreen,
                        viewModel = viewModel,
                        allSkills = allSkills,
                        allCommands = allCommands,
                        selectedCategory = selectedCategory,
                        editorCode = editorCode,
                        currentAudit = currentAudit,
                        auditHistory = auditHistory,
                        chatMessages = chatMessages,
                        isGenerating = isGenerating,
                        selectedModel = selectedModel,
                        launcherConfig = launcherConfig,
                        installedApps = installedApps,
                        watcherStatus = watcherStatus,
                        watcherEvents = watcherEvents,
                        apkWrappers = apkWrappers,
                        symlinkTree = symlinkTree,
                        lastHookResult = lastHookResult
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    currentScreen: AppScreen,
    viewModel: MainViewModel,
    allSkills: List<com.example.data.local.SkillEntity>,
    allCommands: List<com.example.data.local.SystemCommandEntity>,
    selectedCategory: String,
    editorCode: String,
    currentAudit: com.example.engine.SkillAuditReport,
    auditHistory: List<com.example.data.local.AuditResultEntity>,
    chatMessages: List<com.example.data.local.ChatMessageEntity>,
    isGenerating: Boolean,
    selectedModel: String,
    launcherConfig: com.example.data.launcher.LauncherConfig,
    installedApps: List<com.example.data.launcher.InstalledApp>,
    watcherStatus: com.example.service.WatcherStatus,
    watcherEvents: List<com.example.service.FileWatchEvent>,
    apkWrappers: List<com.example.data.wrapper.ApkWrapperConfig>,
    symlinkTree: List<com.example.data.wrapper.DirectorySymlinkNode>,
    lastHookResult: com.example.data.wrapper.HookExecutionOutput?
) {
    when (currentScreen) {
        AppScreen.SKILLS_CATALOG -> {
            SkillsCatalogScreen(
                skills = allSkills,
                selectedCategory = selectedCategory,
                onSelectCategory = { viewModel.selectCategory(it) },
                onSendToEditor = { code ->
                    viewModel.injectCodeToEditorAndSwitch(code)
                },
                onAskAi = { prompt ->
                    viewModel.sendMessage(prompt)
                    viewModel.navigateTo(AppScreen.AI_CHATBOT)
                }
            )
        }
        AppScreen.DYNAMIC_LAUNCHER -> {
            DynamicLauncherScreen(
                config = launcherConfig,
                installedApps = installedApps,
                watcherStatus = watcherStatus,
                watcherEvents = watcherEvents,
                apkWrappers = apkWrappers,
                symlinkTree = symlinkTree,
                lastHookResult = lastHookResult,
                onApplySkillMarkdown = { md -> viewModel.applyLauncherMarkdown(md) },
                onPresetSelected = { preset -> viewModel.applyLauncherPreset(preset) },
                onSimulateWatcherWrite = { fileName, content, asSymlink ->
                    viewModel.simulateWatcherExternalWrite(fileName, content, asSymlink)
                },
                onExecuteHook = { pkgOrId, hookType ->
                    viewModel.executeWrapperHook(pkgOrId, hookType)
                },
                onCreateWrapper = { pkg, apkPath, title, type ->
                    viewModel.createCustomWrapper(pkg, apkPath, title, type)
                },
                onRefreshSymlinkTree = {
                    viewModel.refreshSymlinkTree()
                },
                onOpenWebCanvas = {
                    viewModel.navigateTo(AppScreen.STANDALONE_EDITOR_PREVIEW)
                },
                onConsultAi = { prompt ->
                    viewModel.sendMessage(prompt)
                    viewModel.navigateTo(AppScreen.AI_CHATBOT)
                }
            )
        }
        AppScreen.STANDALONE_EDITOR_PREVIEW -> {
            EditorPreviewScreen(
                currentCode = editorCode,
                onCodeChanged = { viewModel.updateEditorCode(it) },
                onRunAudit = { code ->
                    viewModel.runAudit(code)
                    viewModel.navigateTo(AppScreen.MASTERIZER_HUB)
                },
                onAskAi = { prompt ->
                    viewModel.sendMessage(prompt)
                    viewModel.navigateTo(AppScreen.AI_CHATBOT)
                }
            )
        }
        AppScreen.DISCOVERY_TERMINAL -> {
            DiscoveryTerminalScreen(
                commands = allCommands,
                onSendToEditor = { code ->
                    viewModel.injectCodeToEditorAndSwitch(code)
                },
                onConsultAi = { prompt ->
                    viewModel.sendMessage(prompt)
                    viewModel.navigateTo(AppScreen.AI_CHATBOT)
                }
            )
        }
        AppScreen.MASTERIZER_HUB -> {
            MasterizerHubScreen(
                report = currentAudit,
                auditHistory = auditHistory,
                onTriggerAudit = { viewModel.runAudit() },
                onOpenPreview = { viewModel.navigateTo(AppScreen.STANDALONE_EDITOR_PREVIEW) },
                onConsultAi = { prompt ->
                    viewModel.sendMessage(prompt)
                    viewModel.navigateTo(AppScreen.AI_CHATBOT)
                }
            )
        }
        AppScreen.AI_CHATBOT -> {
            AiChatScreen(
                messages = chatMessages,
                isGenerating = isGenerating,
                selectedModel = selectedModel,
                onSelectModel = { viewModel.selectModel(it) },
                onSendMessage = { viewModel.sendMessage(it) },
                onClearChat = { viewModel.clearChatHistory() },
                onSendCodeToPreview = { code ->
                    viewModel.injectCodeToEditorAndSwitch(code)
                }
            )
        }
    }
}
