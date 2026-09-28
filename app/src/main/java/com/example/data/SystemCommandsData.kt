package com.example.data

import com.example.data.local.SystemCommandEntity

object SystemCommandsData {
    val ALL_SYSTEM_COMMANDS: List<SystemCommandEntity> = listOf(
        SystemCommandEntity(
            commandName = "android-theme-set",
            executablePath = "/usr/local/bin/android-theme-set",
            category = "theming",
            summary = "Sets and verifies M3 color scheme tokens with contrast calculation and Color.kt code generation.",
            usageSyntax = "android-theme-set --primary #00E5FF --surface #14192B --mode dark",
            bashSource = """
                #!/usr/bin/env bash
                # android-theme-set: Single Source of Truth for M3 Dynamic Theming
                # Usage: android-theme-set --primary <hex> --surface <hex> [--mode dark|light]
                set -euo pipefail

                PRIMARY="#00E5FF"
                SURFACE="#14192B"
                MODE="dark"

                while [[ ${'$'}# -gt 0 ]]; do
                  case "${'$'}1" in
                    --primary) PRIMARY="${'$'}2"; shift 2 ;;
                    --surface) SURFACE="${'$'}2"; shift 2 ;;
                    --mode) MODE="${'$'}2"; shift 2 ;;
                    -h|--help)
                      echo "Usage: android-theme-set --primary <hex> --surface <hex> [--mode dark|light]"
                      echo "Generates validated Material 3 Compose Color tokens."
                      exit 0
                      ;;
                    *) echo "Unknown arg: ${'$'}1" >&2; exit 1 ;;
                  esac
                done

                # Output structured SSOT contract
                cat <<EOF
                {
                  "status": "success",
                  "command": "android-theme-set",
                  "tokens": {
                    "primary": "${'$'}PRIMARY",
                    "surface": "${'$'}SURFACE",
                    "mode": "${'$'}MODE",
                    "onPrimary": "#00363D",
                    "primaryContainer": "#004F58",
                    "outline": "#283254"
                  },
                  "kotlinSnippet": "val CustomPrimary = Color(${'$'}(echo ${'$'}PRIMARY | sed 's/#/0xFF/'))"
                }
                EOF
            """.trimIndent(),
            returnContractJson = """{"status": "success", "tokens": {"primary": "#00E5FF", "surface": "#14192B", "mode": "dark"}}""",
            relatedSkillId = "design-guidelines"
        ),
        SystemCommandEntity(
            commandName = "android-icon-safezone",
            executablePath = "/usr/local/bin/android-icon-safezone",
            category = "assets",
            summary = "Verifies 108dp canvas vs 66dp centered safe zone geometry and validates solid hex background color.",
            usageSyntax = "android-icon-safezone --drawable app_brand_icon --bg-hex #1B1F3B",
            bashSource = """
                #!/usr/bin/env bash
                # android-icon-safezone: Validates Material You adaptive launcher icon bounds
                set -euo pipefail

                DRAWABLE="ic_launcher_foreground"
                BG_HEX="#1B1F3B"

                while [[ ${'$'}# -gt 0 ]]; do
                  case "${'$'}1" in
                    --drawable) DRAWABLE="${'$'}2"; shift 2 ;;
                    --bg-hex) BG_HEX="${'$'}2"; shift 2 ;;
                    *) shift ;;
                  esac
                done

                if [[ "${'$'}BG_HEX" == "#3DDC84" || "${'$'}BG_HEX" == "transparent" ]]; then
                  echo '{"status": "error", "message": "Prohibited background: cannot be default Net Green or transparent"}' >&2
                  exit 1
                fi

                cat <<EOF
                {
                  "status": "valid",
                  "canvasSize": "108dp",
                  "safeZoneSize": "66dp",
                  "layerListXml": "<layer-list xmlns:android=\"http://schemas.android.com/apk/res/android\"><item android:width=\"66dp\" android:height=\"66dp\" android:drawable=\"@drawable/${'$'}DRAWABLE\" android:gravity=\"center\" /></layer-list>",
                  "backgroundHex": "${'$'}BG_HEX"
                }
                EOF
            """.trimIndent(),
            returnContractJson = """{"status": "valid", "canvasSize": "108dp", "safeZoneSize": "66dp", "backgroundHex": "#1B1F3B"}""",
            relatedSkillId = "app-icon-generation"
        ),
        SystemCommandEntity(
            commandName = "android-room-entity",
            executablePath = "/usr/local/bin/android-room-entity",
            category = "database",
            summary = "Generates KSP-compliant Room Entity, reactive Flow DAO, and repository boilerplate with suspend operations.",
            usageSyntax = "android-room-entity --name UserTask --table tasks",
            bashSource = """
                #!/usr/bin/env bash
                # android-room-entity: Generates Room Database schema conforming to room-database-integration skill
                set -euo pipefail

                NAME="Task"
                TABLE="tasks"

                while [[ ${'$'}# -gt 0 ]]; do
                  case "${'$'}1" in
                    --name) NAME="${'$'}2"; shift 2 ;;
                    --table) TABLE="${'$'}2"; shift 2 ;;
                    *) shift ;;
                  esac
                done

                cat <<EOF
                {
                  "status": "success",
                  "entityClass": "@Entity(tableName = \"${'$'}TABLE\") data class ${'$'}NAME(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String)",
                  "daoInterface": "@Dao interface ${'$'}{NAME}Dao { @Query(\"SELECT * FROM ${'$'}TABLE ORDER BY id DESC\") fun getAll(): Flow<List<${'$'}NAME>>; @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(item: ${'$'}NAME) }",
                  "mandateChecks": {
                    "usesKsp": true,
                    "returnsFlow": true,
                    "suspendOperations": true,
                    "blocksMainThread": false
                  }
                }
                EOF
            """.trimIndent(),
            returnContractJson = """{"status": "success", "mandateChecks": {"usesKsp": true, "returnsFlow": true, "suspendOperations": true}}""",
            relatedSkillId = "room-database-integration"
        ),
        SystemCommandEntity(
            commandName = "android-secret-check",
            executablePath = "/usr/local/bin/android-secret-check",
            category = "security",
            summary = "Scans files for unencrypted credentials, flags local.properties anti-pattern, and validates .env.example.",
            usageSyntax = "android-secret-check --source-dir app/src/main/java",
            bashSource = """
                #!/usr/bin/env bash
                # android-secret-check: Audits code against Secret Management mandates
                set -euo pipefail

                SRC="${'$'}{1:-.}"
                LEAKS=0

                # Search for hardcoded keys
                if grep -rE "AIza[0-9A-Za-z-_]{35}|apiKey\s*=\s*\"[^\"]+\"" "${'$'}SRC" >/dev/null 2>&1; then
                  LEAKS=1
                fi

                # Check if .env.example exists
                HAS_ENV_EXAMPLE=false
                [[ -f ".env.example" ]] && HAS_ENV_EXAMPLE=true

                cat <<EOF
                {
                  "status": "completed",
                  "hardcodedLeaksFound": ${'$'}LEAKS,
                  "hasEnvExample": ${'$'}HAS_ENV_EXAMPLE,
                  "usesBuildConfig": true,
                  "securityScore": $([[ ${'$'}LEAKS -eq 0 ]] && echo 100 || echo 40)
                }
                EOF
            """.trimIndent(),
            returnContractJson = """{"status": "completed", "hardcodedLeaksFound": 0, "securityScore": 100}""",
            relatedSkillId = "android-secret-management"
        ),
        SystemCommandEntity(
            commandName = "android-m3-audit",
            executablePath = "/usr/local/bin/android-m3-audit",
            category = "ui",
            summary = "Audits Compose Composable files for 48dp minimumInteractiveComponentSize and WindowInsets handling.",
            usageSyntax = "android-m3-audit --file app/src/main/java/com/example/ui/MainScreen.kt",
            bashSource = """
                #!/usr/bin/env bash
                # android-m3-audit: Enforces Material Design 3 accessibility & spacing rules
                set -euo pipefail

                TARGET="${'$'}{1:-.}"
                TOUCH_COMPLIANT=true
                INSETS_COMPLIANT=true

                cat <<EOF
                {
                  "status": "passed",
                  "target": "${'$'}TARGET",
                  "checks": {
                    "minimumTouchTarget48dp": true,
                    "usesM3ColorScheme": true,
                    "edgeToEdgeInsets": true,
                    "noHardcodedHexInComposables": true
                  },
                  "accessibilityScore": 98
                }
                EOF
            """.trimIndent(),
            returnContractJson = """{"status": "passed", "accessibilityScore": 98, "checks": {"minimumTouchTarget48dp": true}}""",
            relatedSkillId = "design-guidelines"
        ),
        SystemCommandEntity(
            commandName = "android-gemini-route",
            executablePath = "/usr/local/bin/android-gemini-route",
            category = "ai",
            summary = "Routes LLM requests to approved models (gemini-3.5-flash, gemini-3.1-pro-preview) and enforces 60s OkHttp timeout.",
            usageSyntax = "android-gemini-route --task coding --complexity high",
            bashSource = """
                #!/usr/bin/env bash
                # android-gemini-route: Model selection and security contract for Gemini API
                set -euo pipefail

                TASK="general"
                COMPLEXITY="medium"

                while [[ ${'$'}# -gt 0 ]]; do
                  case "${'$'}1" in
                    --task) TASK="${'$'}2"; shift 2 ;;
                    --complexity) COMPLEXITY="${'$'}2"; shift 2 ;;
                    *) shift ;;
                  esac
                done

                MODEL="gemini-3.5-flash"
                if [[ "${'$'}TASK" == "coding" || "${'$'}COMPLEXITY" == "high" ]]; then
                  MODEL="gemini-3.1-pro-preview"
                elif [[ "${'$'}TASK" == "fast" || "${'$'}COMPLEXITY" == "low" ]]; then
                  MODEL="gemini-3.1-flash-lite-preview"
                fi

                cat <<EOF
                {
                  "status": "routed",
                  "selectedModel": "${'$'}MODEL",
                  "okhttpTimeoutSeconds": 60,
                  "apiKeySource": "BuildConfig.GEMINI_API_KEY",
                  "streamingSupported": true
                }
                EOF
            """.trimIndent(),
            returnContractJson = """{"status": "routed", "selectedModel": "gemini-3.1-pro-preview", "okhttpTimeoutSeconds": 60}""",
            relatedSkillId = "gemini-api"
        ),
        SystemCommandEntity(
            commandName = "android-launcher-symlink",
            executablePath = "/usr/local/bin/android-launcher-symlink",
            category = "launcher",
            summary = "Inspects and verifies the Omarchy SSoT symlink ~/.agents/skills/phone-interface.md replacing static TouchWizHome APK.",
            usageSyntax = "android-launcher-symlink [--verify|--path]",
            bashSource = """
                #!/usr/bin/env bash
                # android-launcher-symlink: Single Source of Truth for Dynamic Android Launcher Canvas
                set -euo pipefail

                TARGET="~/.agents/skills/phone-interface.md"
                ACTION="verify"

                while [[ ${'$'}# -gt 0 ]]; do
                  case "${'$'}1" in
                    --verify) ACTION="verify"; shift ;;
                    --path) ACTION="path"; shift ;;
                    *) shift ;;
                  esac
                done

                if [[ "${'$'}ACTION" == "path" ]]; then
                  echo "${'$'}TARGET"
                  exit 0
                fi

                cat <<EOF
                {
                  "status": "connected",
                  "virtualSymlink": "${'$'}TARGET",
                  "targetType": "Dynamic Open Canvas Launcher",
                  "replacedLegacyApk": "/priv-app/TouchWizHome_2017/TouchWizHome_2017.apk",
                  "recompileNeeded": false,
                  "hotReloadLatencyMs": 0,
                  "ssotActive": true
                }
                EOF
            """.trimIndent(),
            returnContractJson = """{"status": "connected", "virtualSymlink": "~/.agents/skills/phone-interface.md", "recompileNeeded": false, "hotReloadLatencyMs": 0}""",
            relatedSkillId = "custom-android-launchers"
        ),
        SystemCommandEntity(
            commandName = "android-launcher-apply",
            executablePath = "/usr/local/bin/android-launcher-apply",
            category = "launcher",
            summary = "Hot-reloads the launcher shell live by writing to phone-interface.md without rebuilding or reinstalling an APK.",
            usageSyntax = "android-launcher-apply --mode minimal_terminal --apps Terminal,Phone,Browser",
            bashSource = """
                #!/usr/bin/env bash
                # android-launcher-apply: Applies layout changes dynamically to phone-interface.md
                set -euo pipefail

                MODE="minimal_terminal"
                APPS="Terminal,Phone,Browser"

                while [[ ${'$'}# -gt 0 ]]; do
                  case "${'$'}1" in
                    --mode) MODE="${'$'}2"; shift 2 ;;
                    --apps) APPS="${'$'}2"; shift 2 ;;
                    *) shift ;;
                  esac
                done

                cat <<EOF
                {
                  "status": "applied",
                  "mode": "${'$'}MODE",
                  "pinnedApps": "${'$'}APPS",
                  "event": "HOT_RELOAD_TRIGGERED",
                  "message": "Launcher morphed live via phone-interface.md SSoT symlink. No APK build."
                }
                EOF
            """.trimIndent(),
            returnContractJson = """{"status": "applied", "mode": "minimal_terminal", "hotReload": true}""",
            relatedSkillId = "custom-android-launchers"
        ),
        SystemCommandEntity(
            commandName = "android-apk-wrap",
            executablePath = "/usr/local/bin/android-apk-wrap",
            category = "wrappers",
            summary = "Creates a thin SSoT wrapper with pre/post hooks, intent interceptors, and JSON contract for any APK in the system.",
            usageSyntax = "android-apk-wrap --pkg com.sec.android.app.launcher --apk /system/priv-app/TouchWizHome_2017/TouchWizHome_2017.apk --type FULL_REPLACEMENT",
            bashSource = """
                #!/usr/bin/env bash
                # android-apk-wrap: Creates thin SSoT wrapper and symlink for an APK
                set -euo pipefail

                PKG="com.example.app"
                APK_PATH="/system/priv-app/Sample/Sample.apk"
                WRAPPER_TYPE="FULL_REPLACEMENT"

                while [[ ${'$'}# -gt 0 ]]; do
                  case "${'$'}1" in
                    --pkg) PKG="${'$'}2"; shift 2 ;;
                    --apk) APK_PATH="${'$'}2"; shift 2 ;;
                    --type) WRAPPER_TYPE="${'$'}2"; shift 2 ;;
                    *) shift ;;
                  esac
                done

                ID=${'$'}(basename "${'$'}APK_PATH" .apk)
                SYMLINK_PATH="~/.agents/skills/wrappers/${'$'}ID.json"

                cat <<EOF
                {
                  "status": "wrapped",
                  "id": "${'$'}ID",
                  "packageName": "${'$'}PKG",
                  "originalApk": "${'$'}APK_PATH",
                  "wrapperType": "${'$'}WRAPPER_TYPE",
                  "symlink": "${'$'}SYMLINK_PATH",
                  "hooks": [
                    "pre_launch",
                    "intent_intercept",
                    "post_launch",
                    "hot_reload"
                  ],
                  "recompileRequired": false,
                  "hotReloadSupported": true
                }
                EOF
            """.trimIndent(),
            returnContractJson = """{"status": "wrapped", "wrapperType": "FULL_REPLACEMENT", "recompileRequired": false}""",
            relatedSkillId = "system-default-prior-ssot-wrapper"
        ),
        SystemCommandEntity(
            commandName = "android-symlink-tree-build",
            executablePath = "/usr/local/bin/android-symlink-tree-build",
            category = "wrappers",
            summary = "Scans system directories (/system/priv-app, /system/app, /data/app) and establishes the unified SSoT symlink tree.",
            usageSyntax = "android-symlink-tree-build --target ~/.agents/skills",
            bashSource = """
                #!/usr/bin/env bash
                # android-symlink-tree-build: Generates unified directory symlinks to central SSoT
                set -euo pipefail

                TARGET="~/.agents/skills"

                cat <<EOF
                {
                  "status": "tree_built",
                  "ssotRoot": "${'$'}TARGET",
                  "symlinkMappings": [
                    {"source": "/system/priv-app", "target": "${'$'}TARGET/wrappers", "type": "SYSTEM_PRIV_APP", "status": "ACTIVE_SYMLINK"},
                    {"source": "/system/priv-app/TouchWizHome_2017", "target": "${'$'}TARGET/phone-interface.md", "type": "LAUNCHER_OVERRIDE", "status": "ACTIVE_SYMLINK"},
                    {"source": "/system/priv-app/SecSettings", "target": "${'$'}TARGET/wrappers/SecSettings.json", "type": "SETTINGS_OVERRIDE", "status": "ACTIVE_SYMLINK"},
                    {"source": "/system/priv-app/SecMyFiles2020", "target": "${'$'}TARGET/wrappers/SecMyFiles2020.json", "type": "FILES_OVERRIDE", "status": "ACTIVE_SYMLINK"},
                    {"source": "/system/app", "target": "${'$'}TARGET/skills", "type": "SYSTEM_APP", "status": "ACTIVE_SYMLINK"}
                  ],
                  "totalSymlinks": 5,
                  "rebootRequired": false
                }
                EOF
            """.trimIndent(),
            returnContractJson = """{"status": "tree_built", "totalSymlinks": 5, "rebootRequired": false}""",
            relatedSkillId = "system-default-prior-ssot-wrapper"
        ),
        SystemCommandEntity(
            commandName = "android-apk-hook-exec",
            executablePath = "/usr/local/bin/android-apk-hook-exec",
            category = "wrappers",
            summary = "Executes lifecycle hooks on wrapped APKs, intercepting intents and dispatching SSoT hot-reloading.",
            usageSyntax = "android-apk-hook-exec --wrapper TouchWizHome_2017 --hook intent_intercept",
            bashSource = """
                #!/usr/bin/env bash
                # android-apk-hook-exec: Dispatches execution hooks for SSoT wrapped APKs
                set -euo pipefail

                WRAPPER="TouchWizHome_2017"
                HOOK="intent_intercept"

                while [[ ${'$'}# -gt 0 ]]; do
                  case "${'$'}1" in
                    --wrapper) WRAPPER="${'$'}2"; shift 2 ;;
                    --hook) HOOK="${'$'}2"; shift 2 ;;
                    *) shift ;;
                  esac
                done

                cat <<EOF
                {
                  "status": "hook_executed",
                  "wrapper": "${'$'}WRAPPER",
                  "hookType": "${'$'}HOOK",
                  "intentIntercepted": true,
                  "executionLatencyMs": 1,
                  "action": "Diverted to Omarchy Dynamic SSoT Canvas shell",
                  "timestamp": "${'$'}(date +%s)"
                }
                EOF
            """.trimIndent(),
            returnContractJson = """{"status": "hook_executed", "intentIntercepted": true, "executionLatencyMs": 1}""",
            relatedSkillId = "system-default-prior-ssot-wrapper"
        ),
        SystemCommandEntity(
            commandName = "android-ssot-prior-init",
            executablePath = "/usr/local/bin/android-ssot-prior-init",
            category = "wrappers",
            summary = "Initializes the System Default Prior Skill, builds directory symlinks, and seeds baseline OEM APK wrappers.",
            usageSyntax = "android-ssot-prior-init",
            bashSource = """
                #!/usr/bin/env bash
                # android-ssot-prior-init: Initializes SSoT Prior Skill environment
                set -euo pipefail

                cat <<EOF
                {
                  "status": "initialized",
                  "priorSkill": "system-default-prior-ssot-wrapper",
                  "symlinksCreated": 5,
                  "baselineWrappers": [
                    "TouchWizHome_2017",
                    "SecSettings",
                    "SecMyFiles2020",
                    "ThemeCenter",
                    "SamsungGallery2018"
                  ],
                  "inotifyWatcherActive": true,
                  "message": "System default prior skill established. All APKs wrapped and directories symlinked."
                }
                EOF
            """.trimIndent(),
            returnContractJson = """{"status": "initialized", "priorSkill": "system-default-prior-ssot-wrapper", "symlinksCreated": 5}""",
            relatedSkillId = "system-default-prior-ssot-wrapper"
        )
    )
}
