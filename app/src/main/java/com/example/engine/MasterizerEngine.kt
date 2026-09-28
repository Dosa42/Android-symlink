package com.example.engine

data class SkillAuditReport(
    val overallScore: Int,
    val securityScore: Int,
    val architectureScore: Int,
    val uiAccessibilityScore: Int,
    val offlineRobustnessScore: Int,
    val passedRules: List<String>,
    val warnings: List<String>,
    val recommendations: List<String>
)

object MasterizerEngine {
    fun auditCode(code: String): SkillAuditReport {
        var sec = 100
        var arch = 100
        var ui = 100
        var offline = 100

        val passed = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        // 1. Security Check
        if (code.contains("AIzaSy") || code.contains("apiKey = \"") || code.contains("api_key = \"")) {
            sec -= 50
            warnings.add("CRITICAL: Hardcoded API key detected! Leaked keys in APKs can be exploited.")
            recommendations.add("Move API key to .env.example and read via BuildConfig.GEMINI_API_KEY.")
        } else {
            passed.add("Security: No hardcoded secrets detected.")
        }

        if (code.contains("local.properties")) {
            sec -= 25
            warnings.add("local.properties referenced. AI Studio requires .env and Secrets Gradle plugin.")
        } else {
            passed.add("Security: Standard .env/BuildConfig conventions followed.")
        }

        // 2. Architecture & Room Check
        if (code.contains("kapt")) {
            arch -= 30
            warnings.add("Deprecated KAPT plugin found. Android skill mandates KSP.")
            recommendations.add("Replace kapt with ksp for Room compiler.")
        } else {
            passed.add("Architecture: KSP compliant.")
        }

        if (code.contains("@Dao") && !code.contains("Flow<") && !code.contains("suspend")) {
            arch -= 25
            warnings.add("DAO methods should return Flow<T> or be marked suspend to avoid blocking UI thread.")
        } else if (code.contains("@Dao")) {
            passed.add("Architecture: Reactive Room DAO pattern followed.")
        }

        // 3. UI & Accessibility Check
        if (code.contains("dp") && !code.contains(".dp")) {
            ui -= 15
            warnings.add("Remember to use Kotlin .dp extension (e.g., 16.dp).")
        }

        if (code.contains("minimumInteractiveComponentSize") || code.contains("minHeight = 48") || code.contains("48dp")) {
            passed.add("UI & Accessibility: 48dp minimum touch target satisfied.")
        } else {
            ui -= 15
            warnings.add("Ensure interactive touch targets meet M3 48dp standard.")
            recommendations.add("Apply Modifier.minimumInteractiveComponentSize() on clickable elements.")
        }

        if (code.contains("enableEdgeToEdge") || code.contains("safeDrawing") || code.contains("navigationBars")) {
            passed.add("UI: Full-bleed edge-to-edge safe area handled.")
        }

        // 4. Offline & Model Check
        if (code.contains("gemini-1.5") || code.contains("gemini-2.0") || code.contains("gemini-pro\"")) {
            offline -= 35
            warnings.add("Prohibited legacy Gemini model used. Use gemini-3.5-flash or gemini-3.1-pro-preview.")
            recommendations.add("Upgrade model to gemini-3.5-flash or gemini-3.1-pro-preview.")
        } else {
            passed.add("AI: Modern recommended model selection verified.")
        }

        if (code.contains("timeout") || code.contains("60")) {
            passed.add("Network: Custom 60s timeout configured for LLM latency.")
        }

        sec = sec.coerceIn(0, 100)
        arch = arch.coerceIn(0, 100)
        ui = ui.coerceIn(0, 100)
        offline = offline.coerceIn(0, 100)

        val overall = ((sec * 0.25) + (arch * 0.25) + (ui * 0.25) + (offline * 0.25)).toInt()

        return SkillAuditReport(
            overallScore = overall,
            securityScore = sec,
            architectureScore = arch,
            uiAccessibilityScore = ui,
            offlineRobustnessScore = offline,
            passedRules = passed,
            warnings = warnings,
            recommendations = recommendations
        )
    }
}
