package com.example.engine

import com.example.data.SystemCommandsData
import com.example.data.local.SystemCommandEntity

data class DiscoveryOutput(
    val executedCommand: String,
    val stdout: String = "",
    val stderr: String = "",
    val exitCode: Int = 0,
    val discoveredCommands: List<SystemCommandEntity> = emptyList(),
    val inspectedCommand: SystemCommandEntity? = null
)

object DiscoveryEngine {
    fun executeDiscovery(rawInput: String): DiscoveryOutput {
        val trimmed = rawInput.trim()

        return when {
            // compgen discovery
            trimmed.contains("compgen") || trimmed.contains("grep") && trimmed.contains("android-") -> {
                val commands = SystemCommandsData.ALL_SYSTEM_COMMANDS
                val names = commands.map { it.commandName }.sorted()
                DiscoveryOutput(
                    executedCommand = rawInput,
                    stdout = names.joinToString("\n"),
                    discoveredCommands = commands
                )
            }
            // cat $(which <cmd>) or cat /path/<cmd>
            trimmed.startsWith("cat ") || trimmed.startsWith("cat") -> {
                val cmdName = extractCommandName(trimmed)
                val found = SystemCommandsData.ALL_SYSTEM_COMMANDS.find { it.commandName == cmdName }
                if (found != null) {
                    DiscoveryOutput(
                        executedCommand = rawInput,
                        stdout = found.bashSource,
                        inspectedCommand = found
                    )
                } else {
                    DiscoveryOutput(
                        executedCommand = rawInput,
                        stderr = "cat: cannot find command or file for: $cmdName",
                        exitCode = 1
                    )
                }
            }
            // which <cmd>
            trimmed.startsWith("which ") -> {
                val cmdName = trimmed.removePrefix("which ").trim()
                val found = SystemCommandsData.ALL_SYSTEM_COMMANDS.find { it.commandName == cmdName }
                if (found != null) {
                    DiscoveryOutput(
                        executedCommand = rawInput,
                        stdout = found.executablePath,
                        inspectedCommand = found
                    )
                } else {
                    DiscoveryOutput(
                        executedCommand = rawInput,
                        stderr = "which: no $cmdName in (/usr/local/bin:/usr/bin:/bin)",
                        exitCode = 1
                    )
                }
            }
            // Direct command execution (e.g. android-theme-set ...)
            trimmed.startsWith("android-") -> {
                val parts = trimmed.split("\\s+".toRegex())
                val cmdName = parts.firstOrNull() ?: ""
                val found = SystemCommandsData.ALL_SYSTEM_COMMANDS.find { it.commandName == cmdName }
                if (found != null) {
                    if (trimmed.contains("--help") || trimmed.contains("-h")) {
                        DiscoveryOutput(
                            executedCommand = rawInput,
                            stdout = "${found.commandName}: ${found.summary}\nUsage: ${found.usageSyntax}\nExecutable: ${found.executablePath}",
                            inspectedCommand = found
                        )
                    } else if (cmdName == "android-apk-hook-exec") {
                        val wrapperArg = if (parts.contains("--wrapper")) parts.getOrNull(parts.indexOf("--wrapper") + 1) ?: "TouchWizHome_2017" else "TouchWizHome_2017"
                        val hookArg = if (parts.contains("--hook")) parts.getOrNull(parts.indexOf("--hook") + 1) ?: "intent_intercept" else "intent_intercept"
                        val outputJson = """
                            {
                              "status": "hook_executed",
                              "wrapper": "$wrapperArg",
                              "hookType": "$hookArg",
                              "intentIntercepted": true,
                              "executionLatencyMs": 1,
                              "action": "Diverted to Omarchy Dynamic SSoT Canvas shell",
                              "timestamp": "${System.currentTimeMillis()}"
                            }
                        """.trimIndent()
                        DiscoveryOutput(
                            executedCommand = rawInput,
                            stdout = outputJson,
                            inspectedCommand = found
                        )
                    } else {
                        // Return structured JSON contract
                        DiscoveryOutput(
                            executedCommand = rawInput,
                            stdout = found.returnContractJson,
                            inspectedCommand = found
                        )
                    }
                } else {
                    DiscoveryOutput(
                        executedCommand = rawInput,
                        stderr = "bash: $cmdName: command not found",
                        exitCode = 127
                    )
                }
            }
            // help
            trimmed == "help" || trimmed == "?" -> {
                DiscoveryOutput(
                    executedCommand = rawInput,
                    stdout = """
                        Omarchy Discovery Engine:
                        • compgen -c | grep -E '^android-' | sort -u   -> Discover all system commands dynamically
                        • cat $(which <command>)                      -> Inspect command source (Single Source of Truth)
                        • which <command>                             -> Find executable location
                        • <command> --help                            -> View command arguments and options
                        • <command> [args]                            -> Execute command and receive structured contract
                    """.trimIndent()
                )
            }
            else -> {
                DiscoveryOutput(
                    executedCommand = rawInput,
                    stderr = "bash: command not recognized. Type 'help' or 'compgen -c | grep -E \"^android-\"' to discover system skills.",
                    exitCode = 1
                )
            }
        }
    }

    private fun extractCommandName(input: String): String {
        val clean = input.replace("cat", "")
            .replace("$(which", "")
            .replace("`which", "")
            .replace("`", "")
            .replace(")", "")
            .replace("/usr/local/bin/", "")
            .trim()
        return clean.split(" ").firstOrNull() ?: clean
    }
}
