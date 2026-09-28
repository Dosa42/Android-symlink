package com.example.data.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class LauncherConfig(
    val title: String = "Omarchy Dynamic Canvas",
    val mode: String = "minimal_terminal", // minimal_terminal, cyber_hud, fluid_m3, focus_zen
    val themeColorHex: String = "#00E5FF",
    val backgroundColorHex: String = "#0D111E",
    val showTerminalWidget: Boolean = true,
    val terminalHeader: String = "agent@omarchy:~$ ls -l /skills",
    val maxPinnedApps: Int = 3,
    val pinnedApps: List<String> = listOf("Phone", "Terminal", "Browser"),
    val customWidgetTitle: String = "Active Skill: phone-interface.md",
    val customWidgetBody: String = "Symlinked to ~/.agents/skills/phone-interface.md (Live SSoT)",
    val rawMarkdown: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

data class InstalledApp(
    val label: String,
    val packageName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false
)

object LauncherSSoTManager {
    private const val FILE_NAME = "phone-interface.md"
    private const val SYMLINK_VIRTUAL_PATH = "~/.agents/skills/phone-interface.md"

    private val _currentConfig = MutableStateFlow(createDefaultMinimalConfig())
    val currentConfig: StateFlow<LauncherConfig> = _currentConfig.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    fun getVirtualPath(): String = SYMLINK_VIRTUAL_PATH

    fun init(context: Context) {
        loadInstalledApps(context)
        loadFromDiskOrSeed(context)
        com.example.service.SSoTWatcherEngine.start(context)
    }

    fun loadInstalledApps(context: Context) {
        try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val list = resolveInfos.mapNotNull { ri ->
                val label = ri.loadLabel(pm).toString()
                val pkg = ri.activityInfo.packageName
                if (pkg != context.packageName) {
                    val icon = ri.loadIcon(pm)
                    InstalledApp(label = label, packageName = pkg, icon = icon)
                } else null
            }.sortedBy { it.label }

            if (list.isNotEmpty()) {
                _installedApps.value = list
            } else {
                _installedApps.value = getFallbackApps()
            }
        } catch (_: Exception) {
            _installedApps.value = getFallbackApps()
        }
    }

    private fun getFallbackApps(): List<InstalledApp> {
        return listOf(
            InstalledApp("Terminal", "com.android.terminal"),
            InstalledApp("Phone", "com.android.dialer"),
            InstalledApp("Browser", "org.chromium.chrome"),
            InstalledApp("Camera", "com.android.camera2"),
            InstalledApp("Settings", "com.android.settings"),
            InstalledApp("Files", "com.android.documentsui"),
            InstalledApp("Gallery", "com.android.gallery3d"),
            InstalledApp("Clock", "com.android.deskclock")
        )
    }

    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun loadFromDiskOrSeed(context: Context) {
        val ssotDir = com.example.service.SSoTWatcherEngine.getWatchedDir(context)
        val ssotFile = File(ssotDir, FILE_NAME)
        val legacyFile = File(context.filesDir, FILE_NAME)

        val targetFile = if (ssotFile.exists()) ssotFile else legacyFile
        if (targetFile.exists()) {
            val content = targetFile.readText()
            _currentConfig.value = parseMarkdown(content)
            if (!ssotFile.exists()) {
                ssotFile.writeText(content)
            }
        } else {
            val defaultConfig = createDefaultMinimalConfig()
            val md = generateMarkdown(defaultConfig)
            ssotFile.writeText(md)
            legacyFile.writeText(md)
            _currentConfig.value = defaultConfig.copy(rawMarkdown = md)
        }
    }

    fun reloadFromDisk(context: Context) {
        val ssotDir = com.example.service.SSoTWatcherEngine.getWatchedDir(context)
        val ssotFile = File(ssotDir, FILE_NAME)
        val file = if (ssotFile.exists()) ssotFile else File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            try {
                val content = file.readText()
                val parsed = parseMarkdown(content)
                _currentConfig.value = parsed.copy(
                    rawMarkdown = content,
                    lastUpdated = System.currentTimeMillis()
                )
            } catch (_: Exception) { }
        }
    }

    fun updateSkillContent(context: Context, newMarkdown: String) {
        try {
            val ssotDir = com.example.service.SSoTWatcherEngine.getWatchedDir(context)
            val ssotFile = File(ssotDir, FILE_NAME)
            val legacyFile = File(context.filesDir, FILE_NAME)
            ssotFile.writeText(newMarkdown)
            legacyFile.writeText(newMarkdown)

            val parsed = parseMarkdown(newMarkdown)
            _currentConfig.value = parsed.copy(
                rawMarkdown = newMarkdown,
                lastUpdated = System.currentTimeMillis()
            )
        } catch (_: Exception) {
            // keep current on error
        }
    }

    fun applyPreset(context: Context, presetType: String) {
        val config = when (presetType) {
            "minimal" -> createDefaultMinimalConfig()
            "cyber" -> LauncherConfig(
                title = "Cyber Matrix HUD",
                mode = "cyber_hud",
                themeColorHex = "#00FF66",
                backgroundColorHex = "#050B08",
                showTerminalWidget = true,
                terminalHeader = "root@cyberdeck:~$ neofetch --ascii_distro omarchy",
                maxPinnedApps = 4,
                pinnedApps = listOf("Terminal", "Browser", "Files", "Settings"),
                customWidgetTitle = "SYSTEM CORE: 100% SSoT SYMLINKED",
                customWidgetBody = "Active nodes: 8 skills | Host: Omarchy Canvas | Zero hardcoded APK lag"
            )
            "m3" -> LauncherConfig(
                title = "Material You Dynamic Fluid",
                mode = "fluid_m3",
                themeColorHex = "#6750A4",
                backgroundColorHex = "#141218",
                showTerminalWidget = false,
                terminalHeader = "Google AI Search & Skills",
                maxPinnedApps = 5,
                pinnedApps = listOf("Phone", "Browser", "Camera", "Files", "Settings"),
                customWidgetTitle = "At a Glance • Live SSoT",
                customWidgetBody = "Upcoming: Review Gate 2 Architecture • 24°C Sunny"
            )
            "zen" -> LauncherConfig(
                title = "Focus Zen Canvas",
                mode = "focus_zen",
                themeColorHex = "#A8C7FA",
                backgroundColorHex = "#0E141B",
                showTerminalWidget = false,
                terminalHeader = "Breathe & Focus",
                maxPinnedApps = 3,
                pinnedApps = listOf("Phone", "Terminal", "Settings"),
                customWidgetTitle = "Focus Mode Active",
                customWidgetBody = "Distraction-free environment. 0 notifications permitted."
            )
            else -> createDefaultMinimalConfig()
        }

        val md = generateMarkdown(config)
        updateSkillContent(context, md)
    }

    private fun createDefaultMinimalConfig(): LauncherConfig {
        return LauncherConfig(
            title = "Minimalist Terminal Canvas",
            mode = "minimal_terminal",
            themeColorHex = "#00E5FF",
            backgroundColorHex = "#0D111E",
            showTerminalWidget = true,
            terminalHeader = "agent@omarchy:~$ ls -l ~/.agents/skills/",
            maxPinnedApps = 3,
            pinnedApps = listOf("Terminal", "Phone", "Browser"),
            customWidgetTitle = "Live SSoT Interface Skill",
            customWidgetBody = "Symlink: ~/.agents/skills/phone-interface.md -> Live UI. No APK build required.",
            rawMarkdown = """
# Phone Interface Skill (phone-interface.md)
<!-- Omarchy SSoT Dynamic Launcher Configuration -->
---
title: Minimalist Terminal Canvas
mode: minimal_terminal
themeColor: #00E5FF
backgroundColor: #0D111E
showTerminalWidget: true
terminalHeader: agent@omarchy:~$ ls -l ~/.agents/skills/
maxPinnedApps: 3
pinnedApps:
  - Terminal
  - Phone
  - Browser
customWidgetTitle: Live SSoT Interface Skill
customWidgetBody: Symlink: ~/.agents/skills/phone-interface.md -> Live UI. No APK build required.
---

## Description
This interface is not a static TouchWizHome_2017.apk. It is rendered in real-time by the Omarchy dynamic host shell reading this file via symlink.
            """.trimIndent()
        )
    }

    fun parseMarkdown(md: String): LauncherConfig {
        var title = "Omarchy Dynamic Canvas"
        var mode = "minimal_terminal"
        var themeColorHex = "#00E5FF"
        var backgroundColorHex = "#0D111E"
        var showTerminalWidget = true
        var terminalHeader = "agent@omarchy:~$"
        var maxPinnedApps = 3
        val pinned = mutableListOf<String>()
        var customWidgetTitle = "Live SSoT Interface Skill"
        var customWidgetBody = "Symlinked to ~/.agents/skills/phone-interface.md"

        val lines = md.lines()
        var insideYaml = false
        var readingPinned = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed == "---") {
                insideYaml = !insideYaml
                if (!insideYaml) readingPinned = false
                continue
            }
            if (insideYaml) {
                when {
                    trimmed.startsWith("title:") -> title = trimmed.removePrefix("title:").trim()
                    trimmed.startsWith("mode:") -> mode = trimmed.removePrefix("mode:").trim()
                    trimmed.startsWith("themeColor:") -> themeColorHex = trimmed.removePrefix("themeColor:").trim()
                    trimmed.startsWith("backgroundColor:") -> backgroundColorHex = trimmed.removePrefix("backgroundColor:").trim()
                    trimmed.startsWith("showTerminalWidget:") -> showTerminalWidget = trimmed.removePrefix("showTerminalWidget:").trim().toBoolean()
                    trimmed.startsWith("terminalHeader:") -> terminalHeader = trimmed.removePrefix("terminalHeader:").trim()
                    trimmed.startsWith("maxPinnedApps:") -> maxPinnedApps = trimmed.removePrefix("maxPinnedApps:").trim().toIntOrNull() ?: 3
                    trimmed.startsWith("pinnedApps:") -> readingPinned = true
                    trimmed.startsWith("customWidgetTitle:") -> customWidgetTitle = trimmed.removePrefix("customWidgetTitle:").trim()
                    trimmed.startsWith("customWidgetBody:") -> customWidgetBody = trimmed.removePrefix("customWidgetBody:").trim()
                    readingPinned && trimmed.startsWith("- ") -> {
                        pinned.add(trimmed.removePrefix("- ").trim())
                    }
                    else -> if (!trimmed.startsWith("-")) readingPinned = false
                }
            }
        }

        val finalPinned = if (pinned.isNotEmpty()) pinned else listOf("Terminal", "Phone", "Browser")

        return LauncherConfig(
            title = title,
            mode = mode,
            themeColorHex = if (themeColorHex.startsWith("#")) themeColorHex else "#00E5FF",
            backgroundColorHex = if (backgroundColorHex.startsWith("#")) backgroundColorHex else "#0D111E",
            showTerminalWidget = showTerminalWidget,
            terminalHeader = terminalHeader,
            maxPinnedApps = maxPinnedApps,
            pinnedApps = finalPinned,
            customWidgetTitle = customWidgetTitle,
            customWidgetBody = customWidgetBody,
            rawMarkdown = md,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun generateMarkdown(config: LauncherConfig): String {
        return """
# Phone Interface Skill (phone-interface.md)
<!-- Omarchy SSoT Dynamic Launcher Configuration -->
---
title: ${config.title}
mode: ${config.mode}
themeColor: ${config.themeColorHex}
backgroundColor: ${config.backgroundColorHex}
showTerminalWidget: ${config.showTerminalWidget}
terminalHeader: ${config.terminalHeader}
maxPinnedApps: ${config.maxPinnedApps}
pinnedApps:
${config.pinnedApps.joinToString("\n") { "  - $it" }}
customWidgetTitle: ${config.customWidgetTitle}
customWidgetBody: ${config.customWidgetBody}
---

## SSoT Manifest
This launcher skin is rendered dynamically via symlink to ~/.agents/skills/phone-interface.md.
Changes take effect immediately without APK recompilation.
        """.trimIndent()
    }
}
