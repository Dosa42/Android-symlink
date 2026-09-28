package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SkillsData
import com.example.engine.MasterizerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SkillMaster Lab", appName)
    }

    @Test
    fun `verify embedded android skills are populated`() {
        val skills = SkillsData.ALL_SKILLS
        assertTrue(skills.isNotEmpty())
        assertTrue(skills.any { it.id == "gemini-api" })
        assertTrue(skills.any { it.id == "room-database-integration" })
        assertTrue(skills.any { it.id == "app-icon-generation" })
    }

    @Test
    fun `verify masterizer engine flags insecure keys`() {
        val badCode = "val apiKey = \"AIzaSyFakeKey12345\""
        val report = MasterizerEngine.auditCode(badCode)
        assertTrue(report.securityScore < 100)
        assertTrue(report.warnings.any { it.contains("Hardcoded API key") })
    }

    @Test
    fun `verify omarchy dynamic discovery engine commands`() {
        val compgenResult = com.example.engine.DiscoveryEngine.executeDiscovery("compgen -c | grep -E '^android-' | sort -u")
        assertTrue(compgenResult.stdout.contains("android-theme-set"))
        assertTrue(compgenResult.stdout.contains("android-room-entity"))

        val catResult = com.example.engine.DiscoveryEngine.executeDiscovery("cat $(which android-theme-set)")
        assertTrue(catResult.stdout.contains("#!/usr/bin/env bash"))
        assertTrue(catResult.stdout.contains("Single Source of Truth"))

        val execResult = com.example.engine.DiscoveryEngine.executeDiscovery("android-theme-set --primary #00E5FF")
        assertTrue(execResult.stdout.contains("\"status\": \"success\""))
    }

    @Test
    fun `verify dynamic custom launcher skill and ssot parser`() {
        val skills = SkillsData.ALL_SKILLS
        assertTrue(skills.any { it.id == "custom-android-launchers" })

        val sampleMd = """
---
title: Minimalist Terminal Canvas
mode: minimal_terminal
themeColor: #00E5FF
showTerminalWidget: true
maxPinnedApps: 3
pinnedApps:
  - Terminal
  - Phone
  - Browser
---
        """.trimIndent()
        val config = com.example.data.launcher.LauncherSSoTManager.parseMarkdown(sampleMd)
        assertEquals("Minimalist Terminal Canvas", config.title)
        assertEquals("minimal_terminal", config.mode)
        assertEquals(3, config.pinnedApps.size)
        assertTrue(config.pinnedApps.contains("Terminal"))
        assertTrue(config.showTerminalWidget)
    }

    @Test
    fun `verify ssot filesystem watcher service and symlink simulation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.service.SSoTWatcherEngine.start(context)
        val status = com.example.service.SSoTWatcherEngine.status.value
        assertTrue(status.isWatching)
        assertTrue(status.watchedDirectoryPath.isNotEmpty())

        val testMd = """
---
title: Inotify Live Test
mode: cyber_hud
themeColor: #00FF66
showTerminalWidget: true
maxPinnedApps: 3
pinnedApps:
  - Terminal
  - Browser
  - Phone
---
        """.trimIndent()

        // Simulate write via SSoT FileWatcher
        val file = com.example.service.SSoTWatcherEngine.simulateExternalWrite(
            context = context,
            fileName = "phone-interface.md",
            content = testMd,
            asSymlink = false
        )
        assertTrue(file.exists())

        // Verify that Launcher config was updated live
        val currentLauncherConfig = com.example.data.launcher.LauncherSSoTManager.currentConfig.value
        assertEquals("Inotify Live Test", currentLauncherConfig.title)
        assertEquals("cyber_hud", currentLauncherConfig.mode)

        // Verify events were logged
        val events = com.example.service.SSoTWatcherEngine.events.value
        assertTrue(events.isNotEmpty())
        assertTrue(events.any { it.fileName == "phone-interface.md" })
    }

    @Test
    fun `verify system default prior skill and apk thin wrapper engine`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Verify System Default Prior Skill in catalog
        val skills = SkillsData.ALL_SKILLS
        assertTrue(skills.any { it.id == "system-default-prior-ssot-wrapper" })

        // 2. Initialize ApkWrapperManager
        com.example.data.wrapper.ApkWrapperManager.init(context)

        // 3. Verify directory symlink tree
        val tree = com.example.data.wrapper.ApkWrapperManager.symlinkTree.value
        assertTrue(tree.isNotEmpty())
        assertTrue(tree.any { it.sourcePath == "/system/priv-app" })
        assertTrue(tree.any { it.sourcePath == "/system/priv-app/TouchWizHome_2017" })
        assertTrue(tree.any { it.sourcePath == "~/.agents/skills" })

        // 4. Verify baseline wrappers
        val wrappers = com.example.data.wrapper.ApkWrapperManager.wrappers.value
        assertTrue(wrappers.isNotEmpty())
        assertTrue(wrappers.any { it.id == "TouchWizHome_2017" })
        assertTrue(wrappers.any { it.id == "SecSettings" })
        assertTrue(wrappers.any { it.id == "SecMyFiles2020" })

        // 5. Test wrapping a custom APK
        val custom = com.example.data.wrapper.ApkWrapperManager.wrapCustomApk(
            context = context,
            packageName = "com.samsung.android.calendar",
            originalApkPath = "/system/priv-app/SamsungCalendarProvider/SamsungCalendarProvider.apk",
            title = "Samsung Calendar Provider SSoT Wrapper",
            wrapperType = "INTENT_INTERCEPTOR"
        )
        assertEquals("com_samsung_android_calendar", custom.id)
        assertEquals("INTENT_INTERCEPTOR", custom.wrapperType)

        // 6. Test executing lifecycle hooks
        val hookResult = com.example.data.wrapper.ApkWrapperManager.executeHook(
            context = context,
            pkgOrId = "TouchWizHome_2017",
            hookType = "intent_intercept"
        )
        assertEquals("SUCCESS", hookResult.status)
        assertEquals(0, hookResult.exitCode)
        assertTrue(hookResult.returnPayloadJson.contains("SUCCESS"))
        assertTrue(hookResult.logs.any { it.contains("Intent intercepted successfully") })

        // 7. Test discovery commands for wrappers
        val wrapCmd = com.example.engine.DiscoveryEngine.executeDiscovery("android-apk-wrap --pkg com.test.app --apk /system/priv-app/Test/Test.apk")
        assertTrue(wrapCmd.stdout.contains("wrapped"))

        val treeCmd = com.example.engine.DiscoveryEngine.executeDiscovery("android-symlink-tree-build")
        assertTrue(treeCmd.stdout.contains("tree_built"))

        val hookCmd = com.example.engine.DiscoveryEngine.executeDiscovery("android-apk-hook-exec --wrapper TouchWizHome_2017 --hook intent_intercept")
        assertTrue(hookCmd.stdout.contains("hook_executed") || hookCmd.inspectedCommand?.returnContractJson?.contains("hook_executed") == true)
    }
}
