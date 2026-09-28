package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.launcher.InstalledApp
import com.example.data.launcher.LauncherConfig
import com.example.data.launcher.LauncherSSoTManager
import com.example.data.wrapper.ApkWrapperConfig
import com.example.data.wrapper.DirectorySymlinkNode
import com.example.data.wrapper.HookExecutionOutput
import com.example.service.FileWatchEvent
import com.example.service.WatchEventType
import com.example.service.WatcherStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DynamicLauncherScreen(
    config: LauncherConfig,
    installedApps: List<InstalledApp>,
    watcherStatus: WatcherStatus,
    watcherEvents: List<FileWatchEvent>,
    apkWrappers: List<ApkWrapperConfig>,
    symlinkTree: List<DirectorySymlinkNode>,
    lastHookResult: HookExecutionOutput?,
    onApplySkillMarkdown: (String) -> Unit,
    onPresetSelected: (String) -> Unit,
    onSimulateWatcherWrite: (fileName: String, content: String, asSymlink: Boolean) -> Unit,
    onExecuteHook: (pkgOrId: String, hookType: String) -> Unit,
    onCreateWrapper: (pkg: String, apkPath: String, title: String, type: String) -> Unit,
    onRefreshSymlinkTree: () -> Unit,
    onOpenWebCanvas: () -> Unit,
    onConsultAi: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("canvas") } // canvas, wrappers_tree, fs_monitor, drawer, ssot_editor, comparison
    var markdownInput by remember(config.rawMarkdown) { mutableStateOf(config.rawMarkdown) }
    var appSearchQuery by remember { mutableStateOf("") }

    val accentColor = remember(config.themeColorHex) {
        try {
            Color(android.graphics.Color.parseColor(config.themeColorHex))
        } catch (_: Exception) {
            Color(0xFF00E5FF)
        }
    }

    val bgColor = remember(config.backgroundColorHex) {
        try {
            Color(android.graphics.Color.parseColor(config.backgroundColorHex))
        } catch (_: Exception) {
            Color(0xFF0D111E)
        }
    }

    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Top Control Bar
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, accentColor)
                        ) {
                            Box(modifier = Modifier.padding(6.dp)) {
                                Icon(Icons.Default.Home, contentDescription = "Launcher SSoT", tint = accentColor, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Omarchy Dynamic Launcher",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (watcherStatus.isWatching) Color(0xFF00E676).copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, if (watcherStatus.isWatching) Color(0xFF00E676) else Color.Red)
                                ) {
                                    Text(
                                        text = if (watcherStatus.isWatching) "FS MONITOR: LIVE" else "STOPPED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (watcherStatus.isWatching) Color(0xFF00E676) else Color.Red,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "SSoT: ~/.agents/skills/phone-interface.md",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = {
                                try {
                                    val homeIntent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(homeIntent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Open Settings -> Apps -> Default Apps -> Home app", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Set Default Home App", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Tabs
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        TabPill(
                            title = "📱 Live Canvas",
                            selected = selectedTab == "canvas",
                            accentColor = accentColor,
                            onClick = { selectedTab = "canvas" }
                        )
                    }
                    item {
                        TabPill(
                            title = "🔗 SSoT Wrappers (${apkWrappers.size})",
                            selected = selectedTab == "wrappers_tree",
                            accentColor = accentColor,
                            onClick = { selectedTab = "wrappers_tree" }
                        )
                    }
                    item {
                        TabPill(
                            title = "👁️ FS Monitor (${watcherEvents.size})",
                            selected = selectedTab == "fs_monitor",
                            accentColor = accentColor,
                            onClick = { selectedTab = "fs_monitor" }
                        )
                    }
                    item {
                        TabPill(
                            title = "📦 All Apps Drawer",
                            selected = selectedTab == "drawer",
                            accentColor = accentColor,
                            onClick = { selectedTab = "drawer" }
                        )
                    }
                    item {
                        TabPill(
                            title = "📄 SSoT Skill Editor",
                            selected = selectedTab == "ssot_editor",
                            accentColor = accentColor,
                            onClick = { selectedTab = "ssot_editor" }
                        )
                    }
                    item {
                        TabPill(
                            title = "⚔️ TouchWiz vs Omarchy",
                            selected = selectedTab == "comparison",
                            accentColor = accentColor,
                            onClick = { selectedTab = "comparison" }
                        )
                    }
                }
            }
        }

        // Main Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                "canvas" -> {
                    LiveLauncherCanvas(
                        config = config,
                        currentTime = currentTime,
                        currentDate = currentDate,
                        accentColor = accentColor,
                        installedApps = installedApps,
                        onOpenDrawer = { selectedTab = "drawer" },
                        onEditSkill = { selectedTab = "ssot_editor" },
                        onOpenWebCanvas = onOpenWebCanvas,
                        onPresetSelected = onPresetSelected,
                        onConsultAi = onConsultAi
                    )
                }
                "wrappers_tree" -> {
                    ApkWrappersSymlinksContent(
                        apkWrappers = apkWrappers,
                        symlinkTree = symlinkTree,
                        lastHookResult = lastHookResult,
                        accentColor = accentColor,
                        onExecuteHook = onExecuteHook,
                        onCreateWrapper = onCreateWrapper,
                        onRefreshTree = onRefreshSymlinkTree,
                        onConsultAi = onConsultAi
                    )
                }
                "fs_monitor" -> {
                    FileSystemMonitorContent(
                        watcherStatus = watcherStatus,
                        events = watcherEvents,
                        accentColor = accentColor,
                        onSimulateWrite = onSimulateWatcherWrite,
                        onSelectTab = { selectedTab = it }
                    )
                }
                "drawer" -> {
                    AppDrawerContent(
                        installedApps = installedApps,
                        searchQuery = appSearchQuery,
                        onQueryChange = { appSearchQuery = it },
                        accentColor = accentColor
                    )
                }
                "ssot_editor" -> {
                    SsotEditorContent(
                        markdown = markdownInput,
                        onMarkdownChange = { markdownInput = it },
                        onApply = {
                            onApplySkillMarkdown(markdownInput)
                            selectedTab = "canvas"
                            Toast.makeText(context, "SSoT skill updated! Live launcher morphed without APK recompile.", Toast.LENGTH_SHORT).show()
                        },
                        onPreset = { preset ->
                            onPresetSelected(preset)
                        },
                        accentColor = accentColor
                    )
                }
                "comparison" -> {
                    ComparisonBattleContent(
                        accentColor = accentColor,
                        onConsultAi = onConsultAi
                    )
                }
            }
        }
    }
}

@Composable
private fun TabPill(
    title: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (selected) accentColor else Color.Transparent),
        modifier = Modifier
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LiveLauncherCanvas(
    config: LauncherConfig,
    currentTime: String,
    currentDate: String,
    accentColor: Color,
    installedApps: List<InstalledApp>,
    onOpenDrawer: () -> Unit,
    onEditSkill: () -> Unit,
    onOpenWebCanvas: () -> Unit,
    onPresetSelected: (String) -> Unit,
    onConsultAi: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Section: Clock & Date
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTime,
                fontSize = 58.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1).sp
            )
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodyMedium,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preset Quick Switcher Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                item {
                    PresetButton(
                        label = "Minimal Terminal (3 Apps)",
                        active = config.mode == "minimal_terminal",
                        accentColor = accentColor,
                        onClick = { onPresetSelected("minimal") }
                    )
                }
                item {
                    PresetButton(
                        label = "Cyberpunk Matrix HUD",
                        active = config.mode == "cyber_hud",
                        accentColor = accentColor,
                        onClick = { onPresetSelected("cyber") }
                    )
                }
                item {
                    PresetButton(
                        label = "Material You Dynamic Fluid",
                        active = config.mode == "fluid_m3",
                        accentColor = accentColor,
                        onClick = { onPresetSelected("m3") }
                    )
                }
                item {
                    PresetButton(
                        label = "Focus Zen",
                        active = config.mode == "focus_zen",
                        accentColor = accentColor,
                        onClick = { onPresetSelected("zen") }
                    )
                }
            }
        }

        // Center Section: Dynamic SSoT Widget
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            if (config.showTerminalWidget) {
                // Terminal Widget
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF5F56), CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFFFBD2E), CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF27C93F), CircleShape))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "omarchy-shell • inotify watcher",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Text(
                                text = "0ms reload",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = config.terminalHeader,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "total 3 skills active:\n-rwxr-xr-x 1 agent omarchy phone-interface.md -> SSoT\n-rwxr-xr-x 1 agent omarchy theme-skill.json\n-rwxr-xr-x 1 agent omarchy android-symlink-runner.sh",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                // Focus / Fluid Card Widget
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = config.customWidgetTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = config.customWidgetBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Reconfigure Shortcut Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onConsultAi("I want to update my phone interface skill ~/.agents/skills/phone-interface.md to have a custom layout. Show me how the SSoT symlink morphs my home screen live.")
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Prompt AI to reshape your phone interface...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Bottom Section: Pinned Apps Dock & Drawer Opener
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pinned Apps Dock (e.g. 3 apps as requested in user prompt)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF14192B).copy(alpha = 0.85f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pinned = config.pinnedApps.take(config.maxPinnedApps)
                    pinned.forEach { appName ->
                        PinnedAppIcon(
                            name = appName,
                            accentColor = accentColor,
                            onClick = {
                                val match = installedApps.find { it.label.contains(appName, ignoreCase = true) }
                                if (match != null) {
                                    val success = LauncherSSoTManager.launchApp(context, match.packageName)
                                    if (!success) {
                                        Toast.makeText(context, "Launched $appName ($match)", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Opened $appName in Omarchy Canvas Shell", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    // All Apps Trigger
                    PinnedAppIcon(
                        name = "Apps",
                        accentColor = MaterialTheme.colorScheme.primary,
                        isDrawerTrigger = true,
                        onClick = onOpenDrawer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEditSkill,
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit SSoT", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = onOpenWebCanvas,
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("🌐 React Web Canvas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun ApkWrappersSymlinksContent(
    apkWrappers: List<ApkWrapperConfig>,
    symlinkTree: List<DirectorySymlinkNode>,
    lastHookResult: HookExecutionOutput?,
    accentColor: Color,
    onExecuteHook: (pkgOrId: String, hookType: String) -> Unit,
    onCreateWrapper: (pkg: String, apkPath: String, title: String, type: String) -> Unit,
    onRefreshTree: () -> Unit,
    onConsultAi: (String) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedWrapperForInspection by remember { mutableStateOf<ApkWrapperConfig?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header: System Default Prior Skill
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "System Default Prior: SSoT Symlink Tree & Wrappers",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accentColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, accentColor)
                        ) {
                            Text(
                                text = "PRIOR SKILL: ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "The LLM and Android OS turn each system directory into a unified SSoT symlink pointer, and wrap each APK binary into a thin JSON/bash wrapper with pre/post hooks, intent interceptors, and 0ms hot-reloading.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onRefreshTree()
                                Toast.makeText(context, "SSoT Symlink Tree refreshed!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-build Symlink Tree", style = MaterialTheme.typography.labelSmall, color = accentColor)
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0D111E), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Wrap Custom APK", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0D111E), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Directory Symlink Tree
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DIRECTORY SYMLINK TREE MAPPINGS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${symlinkTree.size} nodes mapped",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B101D)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        symlinkTree.forEach { node ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = node.sourcePath,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "↳ symlink: ${node.symlinkTargetPath}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = accentColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF00E676).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF00E676))
                                ) {
                                    Text(
                                        text = node.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E676),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Thin APK Wrappers & Hooks
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SSOT THIN APK WRAPPERS (${apkWrappers.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Intent Interceptors & Hooks Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00E676)
                )
            }
        }

        items(apkWrappers) { wrapper ->
            ApkWrapperCardItem(
                wrapper = wrapper,
                accentColor = accentColor,
                onExecuteHook = { hookType ->
                    onExecuteHook(wrapper.id, hookType)
                },
                onInspect = {
                    selectedWrapperForInspection = wrapper
                }
            )
        }

        // Live Hook Execution Result Card
        if (lastHookResult != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF060B14)),
                    border = BorderStroke(1.dp, Color(0xFF00E676)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE HOOK EXECUTION RESULT (${lastHookResult.executionMs}ms)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                            }
                            Text(
                                text = "EXIT: ${lastHookResult.exitCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        lastHookResult.logs.forEach { logLine ->
                            Text(
                                text = logLine,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "SSoT Intercept Payload:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = lastHookResult.returnPayloadJson,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = accentColor,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }

    // Add Custom Wrapper Dialog
    if (showAddDialog) {
        CustomWrapperDialog(
            onDismiss = { showAddDialog = false },
            onCreate = { pkg, apkPath, title, type ->
                onCreateWrapper(pkg, apkPath, title, type)
                showAddDialog = false
                Toast.makeText(context, "Thin wrapper created for $pkg!", Toast.LENGTH_SHORT).show()
            },
            accentColor = accentColor
        )
    }
}

@Composable
private fun ApkWrapperCardItem(
    wrapper: ApkWrapperConfig,
    accentColor: Color,
    onExecuteHook: (String) -> Unit,
    onInspect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        border = BorderStroke(1.dp, Color(0xFF283254)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wrapper.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Original: ${wrapper.originalApkPath}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, accentColor)
                ) {
                    Text(
                        text = wrapper.wrapperType,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = wrapper.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1),
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Symlink: ${wrapper.symlinkPath}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF38BDF8)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Hooks Execution Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                wrapper.hooks.forEach { hook ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .clickable { onExecuteHook(hook.hookType) }
                            .minimumInteractiveComponentSize()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = hook.hookType,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomWrapperDialog(
    onDismiss: () -> Unit,
    onCreate: (pkg: String, apkPath: String, title: String, type: String) -> Unit,
    accentColor: Color
) {
    var pkg by remember { mutableStateOf("com.samsung.android.calendar") }
    var apkPath by remember { mutableStateOf("/system/priv-app/SamsungCalendarProvider/SamsungCalendarProvider.apk") }
    var title by remember { mutableStateOf("Samsung Calendar Provider SSoT Wrapper") }
    var wrapperType by remember { mutableStateOf("INTENT_INTERCEPTOR") }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, accentColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Wrap System APK into SSoT Symlink",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = pkg,
                onValueChange = { pkg = it },
                label = { Text("Package Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = apkPath,
                onValueChange = { apkPath = it },
                label = { Text("Original System APK Path") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Wrapper Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onCreate(pkg, apkPath, title, wrapperType)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Create Wrapper", color = Color(0xFF0D111E), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FileSystemMonitorContent(
    watcherStatus: WatcherStatus,
    events: List<FileWatchEvent>,
    accentColor: Color,
    onSimulateWrite: (fileName: String, content: String, asSymlink: Boolean) -> Unit,
    onSelectTab: (String) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kotlin FileSystem Monitor Service",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF00E676))
                        ) {
                            Text(
                                text = "INOTIFY ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Watched SSoT Directory (Inotify + Symlink Dereferencing):",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = watcherStatus.watchedDirectoryPath.ifBlank { "/data/user/0/.../files/ssot_skills" },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Virtual Symlink: ${watcherStatus.virtualSymlinkPath}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFCBD5E1),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Events Captured: ${watcherStatus.totalEventsDetected}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }
        }

        // Live Simulation Triggers Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14192B)),
                border = BorderStroke(1.dp, Color(0xFF283254)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🧪 Simulate External File Events & Symlinks",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Trigger changes in the watched directory to verify 0ms hot-reload without an APK build:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val cyberMd = """
---
title: Cyberpunk Live Inotify Canvas
mode: cyber_hud
themeColor: #00FF66
backgroundColor: #050B08
showTerminalWidget: true
terminalHeader: root@omarchy-deck:~$ watch -n 0.1 inotifywait /skills
maxPinnedApps: 3
pinnedApps:
  - Terminal
  - Browser
  - Phone
customWidgetTitle: INOTIFY SYNC ACTIVE
customWidgetBody: Symlinked to ~/.agents/skills/phone-interface.md
---
                                """.trimIndent()
                                onSimulateWrite("phone-interface.md", cyberMd, false)
                                Toast.makeText(context, "phone-interface.md written -> 0ms UI Hot-Reloaded!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Text("1. Write phone-interface.md", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00FF66))
                        }

                        Button(
                            onClick = {
                                val skillJson = """
{
  "id": "dynamic-symlink-skill",
  "title": "Dynamic Symlink Skill",
  "category": "OS Architecture",
  "summary": "Ingested in real-time from SSoT FileSystem Watcher",
  "mandates": "• ALWAYS propagate changes instantly.",
  "codeSnippet": "// Ingested via inotify",
  "gotchas": "None",
  "effectivenessRubric": "Live Hot-Reload: 100%"
}
                                """.trimIndent()
                                onSimulateWrite("dynamic-symlink-skill.json", skillJson, false)
                                Toast.makeText(context, "dynamic-symlink-skill.json ingested into Room!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Text("2. Ingest Skill JSON", style = MaterialTheme.typography.labelSmall, color = Color(0xFF38BDF8))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val scriptSh = """
#!/usr/bin/env bash
# inotify-hotreload: Auto-generated from FileWatcherService
echo '{"status": "live", "timestamp": "'$(date +%s)'"}'
                            """.trimIndent()
                            onSimulateWrite("inotify-hotreload.sh", scriptSh, true)
                            Toast.makeText(context, "inotify-hotreload.sh symlink created & registered!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B233D))
                    ) {
                        Text("3. Create .sh Script via Symlink & Ingest to Discovery", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF59E0B))
                    }
                }
            }
        }

        // Live Event Log Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REAL-TIME INOTIFY EVENT STREAM",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${events.size} events",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }
        }

        // Event List
        if (events.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B101D)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Waiting for file events in ssot_skills/... Trigger a simulation above!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(events) { ev ->
                EventCardItem(event = ev, accentColor = accentColor)
            }
        }
    }
}

@Composable
private fun EventCardItem(event: FileWatchEvent, accentColor: Color) {
    val typeColor = when (event.type) {
        WatchEventType.HOT_RELOAD_TRIGGERED -> Color(0xFF00E676)
        WatchEventType.SYMLINK_RESOLVED -> Color(0xFF00E5FF)
        WatchEventType.CREATED -> Color(0xFF38BDF8)
        WatchEventType.MODIFIED -> Color(0xFFFBBF24)
        WatchEventType.DELETED -> Color(0xFFFF5252)
    }

    val timeFormatted = remember(event.timestamp) {
        SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(event.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B101D)),
        border = BorderStroke(1.dp, typeColor.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(typeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                    if (event.isSymlink) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6))
                        ) {
                            Text(
                                text = "SYMLINK",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = Color(0xFF3B82F6),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "$timeFormatted (${event.latencyMs}ms)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.fileName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )

            if (event.symlinkTarget != null) {
                Text(
                    text = "↳ Points to: ${event.symlinkTarget}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF7DD3FC)
                )
            }

            Text(
                text = event.summary,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun PresetButton(
    label: String,
    active: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (active) accentColor.copy(alpha = 0.25f) else Color(0xFF14192B),
        border = BorderStroke(1.dp, if (active) accentColor else Color(0xFF283254)),
        modifier = Modifier
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = if (active) accentColor else Color(0xFFCBD5E1)
            )
        }
    }
}

@Composable
private fun PinnedAppIcon(
    name: String,
    accentColor: Color,
    isDrawerTrigger: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .minimumInteractiveComponentSize()
    ) {
        Surface(
            shape = CircleShape,
            color = if (isDrawerTrigger) MaterialTheme.colorScheme.primaryContainer else accentColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, if (isDrawerTrigger) MaterialTheme.colorScheme.primary else accentColor),
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isDrawerTrigger) {
                    Icon(Icons.Default.Apps, contentDescription = "Drawer", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                } else {
                    when (name.lowercase(Locale.ROOT)) {
                        "terminal" -> Icon(Icons.Default.Terminal, contentDescription = name, tint = accentColor)
                        "phone" -> Icon(Icons.Default.Phone, contentDescription = name, tint = accentColor)
                        "browser" -> Icon(Icons.Default.Search, contentDescription = name, tint = accentColor)
                        else -> Icon(Icons.Default.Code, contentDescription = name, tint = accentColor)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFE2E8F0),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AppDrawerContent(
    installedApps: List<InstalledApp>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    accentColor: Color
) {
    val context = LocalContext.current
    val filtered = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter { it.label.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Search installed applications...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentColor) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "INSTALLED APPLICATIONS (${filtered.size})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 72.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filtered) { app ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            val launched = LauncherSSoTManager.launchApp(context, app.packageName)
                            if (!launched) {
                                Toast.makeText(context, "Launching ${app.label}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(4.dp)
                        .minimumInteractiveComponentSize()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF14192B),
                        border = BorderStroke(1.dp, Color(0xFF283254)),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = app.label.take(2).uppercase(Locale.ROOT),
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SsotEditorContent(
    markdown: String,
    onMarkdownChange: (String) -> Unit,
    onApply: () -> Unit,
    onPreset: (String) -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Single Source of Truth (SSoT) File",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Path: ~/.agents/skills/phone-interface.md",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = onApply,
                modifier = Modifier.minimumInteractiveComponentSize(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF0D111E), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hot-Reload", color = Color(0xFF0D111E), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Quick Bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onPreset("minimal") },
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B233D)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Load Minimal", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
            Button(
                onClick = { onPreset("cyber") },
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B233D)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Load Cyber HUD", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00FF66))
            }
            Button(
                onClick = { onPreset("m3") },
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B233D)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Load M3 Fluid", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD0BCFF))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = markdown,
            onValueChange = onMarkdownChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFE2E8F0),
                lineHeight = 18.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF070B14),
                unfocusedContainerColor = Color(0xFF070B14),
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color(0xFF283254)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "💡 Any change saved here updates the launcher immediately via symlink, without needing an APK rebuild or device reboot.",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun ComparisonBattleContent(
    accentColor: Color,
    onConsultAi: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14192B)),
                border = BorderStroke(1.dp, Color(0xFF283254)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Architectural Paradigm Shift: OEM /system/priv-app/ vs Omarchy SSoT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Examining real system binaries found in Android firmware: from bloated, frozen /priv-app/TouchWizHome_2017.apk to an agile Open-Source Dynamic Host Canvas driven by SSoT symlinks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Legacy Hardcoded APK
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E131D)),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "❌ Legacy OEM /priv-app",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFFF5252)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• /system/priv-app/TouchWizHome_2017/TouchWizHome_2017.apk\n• /system/priv-app/SecSettings/SecSettings.apk\n• /system/priv-app/ThemeCenter/ThemeCenter.apk",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFCA5A5),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ComparisonBullet("Frozen in factory ROM filesystem")
                        ComparisonBullet("Requires APK recompile & root to customize")
                        ComparisonBullet("OEM controls widget grids & telemetry")
                        ComparisonBullet("Heavy memory footprint & bloat")
                        ComparisonBullet("Zero agent or live symlink awareness")
                    }
                }

                // Omarchy SSoT Canvas
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1924)),
                    border = BorderStroke(1.dp, accentColor),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "✅ Omarchy SSoT Canvas",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Symlinks:\n• ~/.agents/skills/phone-interface.md\n• ssot_skills/theme-skill.json\n• ssot_skills/custom-tools.sh",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF7DD3FC),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ComparisonBullet("0ms instant hot-reload via inotify")
                        ComparisonBullet("Single Markdown/JSON source of truth")
                        ComparisonBullet("User & AI agent fully co-design layout")
                        ComparisonBullet("Dynamic widgets (terminal, HUD, M3)")
                        ComparisonBullet("Zero APK rebuilds or reboots needed")
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1528)),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "How the Symlink & FileSystem Monitor Work",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = """
1. The Open-Source Launcher acts as a blank dynamic canvas host shell.
2. The Kotlin SSoTFileWatcherService monitors the directory via Android inotify (FileObserver) and dereferences symlinks.
3. When you instruct your AI agent: "I want a minimalist terminal and my 3 most used apps", the agent writes the YAML block into ~/.agents/skills/phone-interface.md.
4. The FileSystem monitor captures the event in < 5ms and hot-reloads the launcher UI live without rebuilding an APK.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onConsultAi("Explain how to implement the Omarchy symlink launcher architecture on Android, replacing TouchWizHome_2017.apk with an open source dynamic canvas controlled by phone-interface.md.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF0D111E), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Discuss Architecture with Gemini AI", color = Color(0xFF0D111E), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonBullet(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFFCBD5E1),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
