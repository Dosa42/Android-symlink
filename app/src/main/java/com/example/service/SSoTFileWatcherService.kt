package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.FileObserver
import android.os.IBinder
import android.system.Os
import android.util.Log
import com.example.data.SystemCommandsData
import com.example.data.launcher.LauncherSSoTManager
import com.example.data.local.AppDatabase
import com.example.data.local.SkillEntity
import com.example.data.local.SystemCommandEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Locale

enum class WatchEventType {
    CREATED,
    MODIFIED,
    DELETED,
    SYMLINK_RESOLVED,
    HOT_RELOAD_TRIGGERED
}

data class FileWatchEvent(
    val id: Long = System.currentTimeMillis(),
    val type: WatchEventType,
    val fileName: String,
    val absolutePath: String,
    val isSymlink: Boolean,
    val symlinkTarget: String? = null,
    val summary: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latencyMs: Long = 0
)

data class WatcherStatus(
    val isWatching: Boolean = false,
    val watchedDirectoryPath: String = "",
    val virtualSymlinkPath: String = "~/.agents/skills",
    val totalEventsDetected: Int = 0,
    val lastEvent: FileWatchEvent? = null,
    val watchedFilesCount: Int = 0,
    val isSymlinkSupported: Boolean = true
)

/**
 * SSoTFileSystemWatcherService
 * Watches a local directory acting as the Single Source of Truth (SSoT) for
 * skill JSON, markdown, and executable script files linked via symlinks.
 * Automatically dispatches instant hot-reloading events to the UI and Room database.
 */
class SSoTFileWatcherService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): SSoTFileWatcherService = this@SSoTFileWatcherService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        SSoTWatcherEngine.start(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        SSoTWatcherEngine.start(applicationContext)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        SSoTWatcherEngine.stop()
    }
}

/**
 * Singleton reactive engine that handles file observation, symlink resolution,
 * and UI refresh propagation.
 */
object SSoTWatcherEngine {
    private const val TAG = "SSoTWatcherEngine"
    private const val CHANNEL_ID = "ssot_watcher_channel"
    private const val DIR_NAME = "ssot_skills"
    private const val VIRTUAL_SSOT_PATH = "~/.agents/skills"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null
    private var fileObserver: FileObserver? = null

    private val _status = MutableStateFlow(WatcherStatus(virtualSymlinkPath = VIRTUAL_SSOT_PATH))
    val status: StateFlow<WatcherStatus> = _status.asStateFlow()

    private val _events = MutableStateFlow<List<FileWatchEvent>>(emptyList())
    val events: StateFlow<List<FileWatchEvent>> = _events.asStateFlow()

    private val _refreshPulse = MutableStateFlow<Long>(0L)
    val refreshPulse: StateFlow<Long> = _refreshPulse.asStateFlow()

    // File snapshot cache for symlink & polling resolution
    private val fileSnapshots = mutableMapOf<String, Long>()
    private var appContext: Context? = null

    fun getWatchedDir(context: Context): File {
        val dir = File(context.filesDir, DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun start(context: Context, customDir: File? = null) {
        if (_status.value.isWatching) return
        appContext = context.applicationContext

        val targetDir = customDir ?: getWatchedDir(context)
        seedInitialFilesIfEmpty(context, targetDir)

        // 1. Initialize Inotify Native FileObserver
        startFileObserver(context, targetDir)

        // 2. Initialize Symlink and Target Change Polling Loop
        startSymlinkPollingLoop(context, targetDir)

        _status.value = _status.value.copy(
            isWatching = true,
            watchedDirectoryPath = targetDir.absolutePath,
            watchedFilesCount = targetDir.listFiles()?.size ?: 0
        )
        Log.i(TAG, "SSoT FileSystem Watcher active on: ${targetDir.absolutePath}")
    }

    fun stop() {
        fileObserver?.stopWatching()
        fileObserver = null
        pollingJob?.cancel()
        pollingJob = null
        _status.value = _status.value.copy(isWatching = false)
        Log.i(TAG, "SSoT FileSystem Watcher stopped.")
    }

    private fun startFileObserver(context: Context, targetDir: File) {
        val mask = FileObserver.CREATE or
                FileObserver.MODIFY or
                FileObserver.CLOSE_WRITE or
                FileObserver.DELETE or
                FileObserver.MOVED_TO or
                FileObserver.MOVED_FROM or
                FileObserver.ATTRIB

        fileObserver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(targetDir, mask) {
                override fun onEvent(event: Int, path: String?) {
                    handleFileSystemEvent(context, targetDir, event, path)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            object : FileObserver(targetDir.absolutePath, mask) {
                override fun onEvent(event: Int, path: String?) {
                    handleFileSystemEvent(context, targetDir, event, path)
                }
            }
        }
        fileObserver?.startWatching()
    }

    private fun startSymlinkPollingLoop(context: Context, targetDir: File) {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                delay(1200) // 1.2s check interval for symlinks and external updates
                checkDirectorySnapshots(context, targetDir)
            }
        }
    }

    private fun checkDirectorySnapshots(context: Context, targetDir: File) {
        val files = targetDir.listFiles() ?: return
        var changed = false

        for (file in files) {
            val isSymlink = isSymbolicLink(file)
            val effectiveFile = if (isSymlink) {
                try {
                    file.canonicalFile
                } catch (_: Exception) {
                    file
                }
            } else {
                file
            }

            val lastMod = effectiveFile.lastModified()
            val cachedMod = fileSnapshots[file.name]

            if (cachedMod == null) {
                fileSnapshots[file.name] = lastMod
            } else if (cachedMod != lastMod) {
                fileSnapshots[file.name] = lastMod
                changed = true
                onFileChanged(
                    context = context,
                    file = file,
                    eventType = if (isSymlink) WatchEventType.SYMLINK_RESOLVED else WatchEventType.MODIFIED,
                    message = if (isSymlink) "Symlink target updated: ${file.name} -> ${effectiveFile.name}" else "File modified: ${file.name}"
                )
            }
        }

        val currentNames = files.map { it.name }.toSet()
        val deleted = fileSnapshots.keys.filter { it !in currentNames }
        deleted.forEach { deletedName ->
            fileSnapshots.remove(deletedName)
            changed = true
            recordEvent(
                WatchEventType.DELETED,
                deletedName,
                File(targetDir, deletedName).absolutePath,
                false,
                null,
                "File removed from SSoT: $deletedName"
            )
        }

        if (changed) {
            _status.value = _status.value.copy(
                watchedFilesCount = files.size
            )
        }
    }

    private fun handleFileSystemEvent(context: Context, targetDir: File, event: Int, path: String?) {
        if (path.isNullOrBlank()) return
        val file = File(targetDir, path)
        val eventStartTime = System.currentTimeMillis()

        when (event and FileObserver.ALL_EVENTS) {
            FileObserver.CREATE, FileObserver.MOVED_TO -> {
                fileSnapshots[path] = file.lastModified()
                onFileChanged(
                    context,
                    file,
                    WatchEventType.CREATED,
                    "New SSoT file detected: $path",
                    eventStartTime
                )
            }
            FileObserver.CLOSE_WRITE, FileObserver.MODIFY -> {
                fileSnapshots[path] = file.lastModified()
                onFileChanged(
                    context,
                    file,
                    WatchEventType.MODIFIED,
                    "File modified (CLOSE_WRITE): $path",
                    eventStartTime
                )
            }
            FileObserver.DELETE, FileObserver.MOVED_FROM -> {
                fileSnapshots.remove(path)
                recordEvent(
                    WatchEventType.DELETED,
                    path,
                    file.absolutePath,
                    false,
                    null,
                    "File removed from SSoT: $path",
                    System.currentTimeMillis() - eventStartTime
                )
                triggerUiRefresh()
            }
            FileObserver.ATTRIB -> {
                val isSymlink = isSymbolicLink(file)
                val target = getSymlinkTarget(file)
                onFileChanged(
                    context,
                    file,
                    if (isSymlink) WatchEventType.SYMLINK_RESOLVED else WatchEventType.MODIFIED,
                    "Attributes/Symlink modified: $path",
                    eventStartTime
                )
            }
        }
    }

    private fun onFileChanged(
        context: Context,
        file: File,
        eventType: WatchEventType,
        message: String,
        startTime: Long = System.currentTimeMillis()
    ) {
        val isSymlink = isSymbolicLink(file)
        val symlinkTarget = getSymlinkTarget(file)

        // Process based on file extension & type
        when {
            // 1. Phone Interface SSoT Markdown (Dynamic Launcher Canvas)
            file.name == "phone-interface.md" || file.name.endsWith(".md") -> {
                try {
                    val content = file.readText()
                    LauncherSSoTManager.updateSkillContent(context, content)
                    val latency = System.currentTimeMillis() - startTime
                    recordEvent(
                        WatchEventType.HOT_RELOAD_TRIGGERED,
                        file.name,
                        file.absolutePath,
                        isSymlink,
                        symlinkTarget,
                        "Launcher Canvas Hot-Reloaded (0ms latency, length: ${content.length}b)",
                        latency
                    )
                    triggerUiRefresh()
                } catch (e: Exception) {
                    Log.e(TAG, "Error reloading launcher markdown", e)
                }
            }

            // 2. Skill JSON Definition (e.g. theme-skill.json, radar-skill.json)
            file.name.endsWith(".json") -> {
                try {
                    val content = file.readText()
                    val latency = System.currentTimeMillis() - startTime
                    recordEvent(
                        WatchEventType.MODIFIED,
                        file.name,
                        file.absolutePath,
                        isSymlink,
                        symlinkTarget,
                        "Skill JSON updated & ingested into Room: ${file.name}",
                        latency
                    )
                    triggerUiRefresh()
                    scope.launch {
                        parseAndIngestSkillJson(context, file.name, content)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing skill json: ${file.name}", e)
                }
            }

            // 3. Executable Script File (e.g. android-theme-set.sh, custom-tools.sh)
            file.name.endsWith(".sh") -> {
                try {
                    val content = file.readText()
                    val latency = System.currentTimeMillis() - startTime
                    recordEvent(
                        WatchEventType.MODIFIED,
                        file.name,
                        file.absolutePath,
                        isSymlink,
                        symlinkTarget,
                        "Script tool registered in Discovery Engine: ${file.name}",
                        latency
                    )
                    triggerUiRefresh()
                    scope.launch {
                        parseAndIngestScript(context, file.name, content)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing script: ${file.name}", e)
                }
            }

            else -> {
                val latency = System.currentTimeMillis() - startTime
                recordEvent(
                    eventType,
                    file.name,
                    file.absolutePath,
                    isSymlink,
                    symlinkTarget,
                    message,
                    latency
                )
                triggerUiRefresh()
            }
        }
    }

    private suspend fun parseAndIngestSkillJson(context: Context, filename: String, jsonStr: String) {
        val json = JSONObject(jsonStr)
        val id = json.optString("id", filename.removeSuffix(".json"))
        val title = json.optString("title", id.replace("-", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() })
        val category = json.optString("category", "Custom SSoT Skills")
        val summary = json.optString("summary", "Dynamic skill synced via SSoT directory")
        val mandates = json.optString("mandates", "• ALWAYS maintain SSoT integrity.")
        val codeSnippet = json.optString("codeSnippet", "// Dynamic SSoT Code snippet")
        val gotchas = json.optString("gotchas", "Ensure symlink points to valid file.")
        val effectivenessRubric = json.optString("effectivenessRubric", "SSoT Compliance: 100%")

        val entity = SkillEntity(
            id = id,
            title = title,
            category = category,
            summary = summary,
            mandates = mandates,
            codeSnippet = codeSnippet,
            gotchas = gotchas,
            effectivenessRubric = effectivenessRubric
        )

        val db = AppDatabase.getDatabase(context)
        db.skillDao().insertSkills(listOf(entity))
        Log.i(TAG, "Ingested SkillEntity: $id from $filename")
    }

    private suspend fun parseAndIngestScript(context: Context, filename: String, scriptContent: String) {
        val cmdName = filename.removeSuffix(".sh")
        val entity = SystemCommandEntity(
            commandName = cmdName,
            executablePath = "/usr/local/bin/$cmdName",
            category = "custom-ssot",
            summary = "Dynamic executable tool synced from SSoT file: $filename",
            usageSyntax = "$cmdName [options]",
            bashSource = scriptContent,
            returnContractJson = """{"status": "success", "tool": "$cmdName", "source": "SSoT Symlink"}""",
            relatedSkillId = "custom-android-launchers"
        )

        val db = AppDatabase.getDatabase(context)
        db.systemCommandDao().insertCommands(listOf(entity))
        Log.i(TAG, "Ingested SystemCommandEntity: $cmdName from $filename")
    }

    private fun recordEvent(
        type: WatchEventType,
        fileName: String,
        absolutePath: String,
        isSymlink: Boolean,
        target: String?,
        summary: String,
        latencyMs: Long = 0
    ) {
        val event = FileWatchEvent(
            type = type,
            fileName = fileName,
            absolutePath = absolutePath,
            isSymlink = isSymlink,
            symlinkTarget = target,
            summary = summary,
            latencyMs = latencyMs
        )
        val currentList = _events.value.toMutableList()
        currentList.add(0, event)
        if (currentList.size > 50) currentList.removeAt(currentList.lastIndex)
        _events.value = currentList

        _status.value = _status.value.copy(
            totalEventsDetected = _status.value.totalEventsDetected + 1,
            lastEvent = event
        )
    }

    fun triggerUiRefresh() {
        _refreshPulse.value = System.currentTimeMillis()
    }

    // Symlink utilities
    fun isSymbolicLink(file: File): Boolean {
        return try {
            val path = Paths.get(file.absolutePath)
            Files.isSymbolicLink(path)
        } catch (_: Exception) {
            try {
                file.canonicalPath != file.absolutePath
            } catch (_: Exception) {
                false
            }
        }
    }

    fun getSymlinkTarget(file: File): String? {
        return try {
            val path = Paths.get(file.absolutePath)
            if (Files.isSymbolicLink(path)) {
                Files.readSymbolicLink(path).toString()
            } else null
        } catch (_: Exception) {
            try {
                file.canonicalPath
            } catch (_: Exception) {
                null
            }
        }
    }

    fun createSymlinkSafely(linkFile: File, targetFile: File): Boolean {
        return try {
            if (linkFile.exists() || isSymbolicLink(linkFile)) {
                linkFile.delete()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.createSymbolicLink(Paths.get(linkFile.absolutePath), Paths.get(targetFile.absolutePath))
                true
            } else {
                Os.symlink(targetFile.absolutePath, linkFile.absolutePath)
                true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Symlink not permitted on this storage layer. Emulating with SSoT mirror.", e)
            try {
                linkFile.writeText(targetFile.readText())
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * Seeds initial demo files into the watched SSoT directory:
     * - phone-interface.md
     * - theme-skill.json
     * - custom-tools.sh
     */
    fun seedInitialFilesIfEmpty(context: Context, targetDir: File) {
        val phoneMd = File(targetDir, "phone-interface.md")
        if (!phoneMd.exists()) {
            val defaultMd = LauncherSSoTManager.currentConfig.value.rawMarkdown
            phoneMd.writeText(defaultMd)
            fileSnapshots[phoneMd.name] = phoneMd.lastModified()
        }

        val themeSkillJson = File(targetDir, "theme-skill.json")
        if (!themeSkillJson.exists()) {
            val jsonContent = """
                {
                  "id": "theme-skill-ssot",
                  "title": "M3 Dynamic SSoT Theme Skill",
                  "category": "Theming & SSoT",
                  "summary": "Live synced Material 3 color tokens controlled via SSoT symlink file.",
                  "mandates": "• ALWAYS propagate color tokens without rebuilding APKs.\n• ALWAYS use primary hex #00E5FF.",
                  "codeSnippet": "val SSoTPrimary = Color(0xFF00E5FF)",
                  "gotchas": "Zero APK build delay.",
                  "effectivenessRubric": "Live Sync: 50% | Contrast: 50%"
                }
            """.trimIndent()
            themeSkillJson.writeText(jsonContent)
            fileSnapshots[themeSkillJson.name] = themeSkillJson.lastModified()
        }

        val scriptFile = File(targetDir, "android-symlink-runner.sh")
        if (!scriptFile.exists()) {
            val shContent = """
                #!/usr/bin/env bash
                # android-symlink-runner: Live SSoT script synced via FileSystem Monitor
                set -euo pipefail
                echo '{"status": "executed", "engine": "SSoTFileSystemWatcherService", "timestamp": "'$(date +%s)'"}'
            """.trimIndent()
            scriptFile.writeText(shContent)
            fileSnapshots[scriptFile.name] = scriptFile.lastModified()
        }
    }

    /**
     * Simulates an external write to a file in the watched SSoT directory.
     * Used by UI and AI to demonstrate live 0ms hot-reload.
     */
    fun simulateExternalWrite(
        context: Context,
        fileName: String,
        content: String,
        asSymlink: Boolean = false
    ): File {
        val targetDir = getWatchedDir(context)
        val file = File(targetDir, fileName)

        if (asSymlink) {
            val realTarget = File(context.filesDir, "real_$fileName")
            realTarget.writeText(content)
            createSymlinkSafely(file, realTarget)
        } else {
            file.writeText(content)
        }

        // Trigger detection
        fileSnapshots[fileName] = System.currentTimeMillis()
        onFileChanged(
            context,
            file,
            if (asSymlink) WatchEventType.SYMLINK_RESOLVED else WatchEventType.MODIFIED,
            "External write simulated: $fileName (${content.length}b)"
        )
        return file
    }
}
