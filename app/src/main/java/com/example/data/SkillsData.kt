package com.example.data

import com.example.data.local.RecipeEntity
import com.example.data.local.SkillEntity

object SkillsData {
    val ALL_SKILLS: List<SkillEntity> = listOf(
        SkillEntity(
            id = "gemini-api",
            title = "Gemini API Integration",
            category = "Artificial Intelligence",
            summary = "Direct REST & Firebase AI integration with multi-model routing (gemini-3.5-flash for general, gemini-3.1-pro-preview for complex coding, gemini-3.1-flash-lite for speed). Features streaming, multimodal, and system role instructions.",
            mandates = """
                • ALWAYS access Gemini API key via BuildConfig.GEMINI_API_KEY.
                • ALWAYS configure OkHttpClient with 60-second connect, read, and write timeouts.
                • ALWAYS run API requests on background coroutines (Dispatchers.IO).
                • ALWAYS maintain conversation history in contents array for multi-turn chats.
                • NEVER hardcode API keys in Kotlin or Gradle source files.
                • NEVER use deprecated/prohibited models (gemini-1.5-flash, gemini-2.0-flash, gemini-pro).
            """.trimIndent(),
            codeSnippet = """
                // Gemini REST Client with 60s timeout & system instructions
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()

                val request = GenerateContentRequest(
                    contents = conversationHistory,
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                    generationConfig = GenerationConfig(temperature = 0.7f, topP = 0.95f)
                )
            """.trimIndent(),
            gotchas = "OkHttp default 10s timeout causes SocketTimeoutException on reasoning models. Always configure 60s. Also ensure GEMINI_API_KEY is uncommented in .env.example.",
            effectivenessRubric = "Security (BuildConfig check): 30% | Timeout (60s): 25% | Error Handling: 25% | Model Selection: 20%"
        ),
        SkillEntity(
            id = "room-database-integration",
            title = "Room Database & Flow Cache",
            category = "Architecture & Storage",
            summary = "Production offline-first data layer with Room SQLite, Kotlin Symbol Processing (KSP), reactive Flow streams, and Repository pattern abstraction.",
            mandates = """
                • ALWAYS use KSP (Kotlin Symbol Processing) instead of deprecated KAPT.
                • ALWAYS return Flow<T> from DAO queries for reactive UI synchronization.
                • ALWAYS abstract DAOs through a dedicated Repository class.
                • ALWAYS mark insert, update, and delete methods as suspend functions.
                • NEVER access DAOs directly from Composable functions.
                • NEVER block the main thread for database queries.
            """.trimIndent(),
            codeSnippet = """
                @Entity(tableName = "skills")
                data class SkillEntity(@PrimaryKey val id: String, val title: String)

                @Dao
                interface SkillDao {
                    @Query("SELECT * FROM skills ORDER BY title ASC")
                    fun getAllSkills(): Flow<List<SkillEntity>>

                    @Insert(onConflict = OnConflictStrategy.REPLACE)
                    suspend fun insert(skill: SkillEntity)
                }

                class SkillRepository(private val dao: SkillDao) {
                    val skills: Flow<List<SkillEntity>> = dao.getAllSkills()
                    suspend fun save(skill: SkillEntity) = dao.insert(skill)
                }
            """.trimIndent(),
            gotchas = "Room queries on main thread will crash the app with IllegalStateException. Always collect Flows using collectAsStateWithLifecycle() in Compose.",
            effectivenessRubric = "KSP Usage: 25% | Flow Reactivity: 25% | Suspend Operations: 25% | Repository Pattern: 25%"
        ),
        SkillEntity(
            id = "app-icon-generation",
            title = "Adaptive Launcher Icon & Assets",
            category = "Assets & System",
            summary = "Material You adaptive launcher icons obeying 108dp canvas geometry, 66dp centered safe zone, exact background color pinning, and 5-density PNG fallbacks.",
            mandates = """
                • ALWAYS center the foreground vector or image strictly inside a 66dp safe zone within the 108dp canvas using <layer-list>.
                • ALWAYS fill ic_launcher_background.xml with the exact solid hex color pinned in the generation prompt.
                • ALWAYS generate valid inscribed circle PNGs for ic_launcher_round.png.
                • ALWAYS remove default .webp launcher icons to prevent duplicate AAPT2 conflicts.
                • NEVER leave icon backgrounds transparent or use Android default net green.
            """.trimIndent(),
            codeSnippet = """
                <!-- res/drawable/ic_launcher_foreground.xml -->
                <layer-list xmlns:android="http://schemas.android.com/apk/res/android">
                    <item
                        android:width="66dp"
                        android:height="66dp"
                        android:drawable="@drawable/app_brand_icon"
                        android:gravity="center" />
                </layer-list>

                <!-- res/drawable/ic_launcher_background.xml -->
                <vector xmlns:android="http://schemas.android.com/apk/res/android"
                    android:width="108dp" android:height="108dp"
                    android:viewportWidth="108" android:viewportHeight="108">
                    <path android:fillColor="#1B1F3B" android:pathData="M0,0h108v108h-108z" />
                </vector>
            """.trimIndent(),
            gotchas = "Square bitmaps copied to roundIcon without circular masking will appear square in round-icon launchers. Always mask with ImageMagick.",
            effectivenessRubric = "66dp Safe Zone: 35% | Background Solid Hex: 25% | Round Icon Mask: 25% | Fallbacks: 15%"
        ),
        SkillEntity(
            id = "design-guidelines",
            title = "Material 3 Design & Adaptive UI",
            category = "UI & Aesthetics",
            summary = "Distinctive modern UI with Material Design 3 tokens, 48dp minimumInteractiveComponentSize, edge-to-edge system insets, and fluid responsive layouts.",
            mandates = """
                • ALWAYS enforce 48dp x 48dp minimum touch target size using Modifier.minimumInteractiveComponentSize().
                • ALWAYS enable enableEdgeToEdge() and apply WindowInsets.safeDrawing or navigationBars.
                • ALWAYS use .dp extensions for dimensions and .sp for typography.
                • ALWAYS provide immediate ripple feedback on interactive components.
                • NEVER hardcode raw color hex codes in Composables; use MaterialTheme.colorScheme.
                • NEVER allow system gesture bars or camera notches to clip interactive buttons.
            """.trimIndent(),
            codeSnippet = """
                @Composable
                fun AdaptiveActionCard(title: String, onClick: () -> Unit) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Button(
                            onClick = onClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .padding(12.dp)
                        ) {
                            Text(text = title, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            """.trimIndent(),
            gotchas = "Using hardcoded pixel heights or omitting minimumInteractiveComponentSize causes accessibility rejections and bad touch UX.",
            effectivenessRubric = "48dp Touch Targets: 30% | Theme Semantic Colors: 25% | Edge-to-Edge Insets: 25% | Adaptive Fluidity: 20%"
        ),
        SkillEntity(
            id = "android-secret-management",
            title = "Secret Management via .env & BuildConfig",
            category = "Security & Secrets",
            summary = "Secure API key and credential management via the Secrets Gradle Plugin and .env/.env.example files exposed through BuildConfig.",
            mandates = """
                • ALWAYS add placeholder keys to .env.example with descriptive comments.
                • ALWAYS access secrets via BuildConfig in code.
                • ALWAYS ensure .env is excluded in .gitignore.
                • NEVER hardcode API keys or secrets in source code or Gradle scripts.
                • NEVER instruct users to put keys in local.properties.
                • NEVER generate UI inputs for entering API keys unless requested.
            """.trimIndent(),
            codeSnippet = """
                // Read secret safely from BuildConfig
                val apiKey: String = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    Log.w("SecretCheck", "Gemini API key is not configured in Secrets panel")
                }
            """.trimIndent(),
            gotchas = "Leaving GEMINI_API_KEY commented in .env.example prevents Secrets Gradle Plugin from generating BuildConfig.GEMINI_API_KEY, causing compilation errors.",
            effectivenessRubric = "BuildConfig Access: 40% | .env.example Definition: 30% | No Hardcoded Fallback: 30%"
        ),
        SkillEntity(
            id = "android-typography",
            title = "Local Typography & Bundled Fonts",
            category = "UI & Typography",
            summary = "Bundling static Google Fonts (.ttf) locally in res/font/ using font-util CLI, integrating with Compose FontFamily and Type.kt.",
            mandates = """
                • ALWAYS bundle static font files (.ttf) in res/font/ for offline reliability.
                • ALWAYS construct FontFamily(Font(R.font.<sanitized_name>)).
                • NEVER use Downloadable Fonts (ui-text-google-fonts) in this environment.
            """.trimIndent(),
            codeSnippet = """
                // In Type.kt:
                val MonospaceCode = FontFamily(Font(R.font.roboto_mono))
                val DisplayTitle = FontFamily(Font(R.font.montserrat))

                val Typography = Typography(
                    titleLarge = TextStyle(fontFamily = DisplayTitle, fontSize = 22.sp),
                    bodyMedium = TextStyle(fontFamily = MonospaceCode, fontSize = 14.sp)
                )
            """.trimIndent(),
            gotchas = "ui-text-google-fonts requires Google Play Services font provider which is not active in this container environment and will crash at runtime.",
            effectivenessRubric = "Local Font Bundling: 50% | Typography Token Mapping: 50%"
        ),
        SkillEntity(
            id = "robolectric-testing",
            title = "Robolectric & JVM Unit Testing",
            category = "Testing & Verification",
            summary = "Fast, headless local JVM testing for Jetpack Compose UI state, Critical User Journeys (CUJs), and ViewModel coroutines without emulators.",
            mandates = """
                • ALWAYS use Robolectric for fast local JVM unit and UI testing.
                • ALWAYS verify Critical User Journeys (CUJs) using Compose Test Rules.
                • NEVER attempt to run instrumented tests or adb commands in this environment.
            """.trimIndent(),
            codeSnippet = """
                @RunWith(RobolectricTestRunner::class)
                class SkillMasterRobolectricTest {
                    @get:Rule
                    val composeTestRule = createComposeRule()

                    @Test
                    fun testSkillListDisplaysCorrectly() {
                        composeTestRule.setContent {
                            SkillListScreen(skills = SkillsData.ALL_SKILLS)
                        }
                        composeTestRule.onNodeWithText("Gemini API Integration").assertIsDisplayed()
                    }
                }
            """.trimIndent(),
            gotchas = "Running instrumented tests in androidTest requires a physical device or emulator which does not exist in this cloud container.",
            effectivenessRubric = "CUJ Coverage: 40% | Compose Rule Assertions: 30% | Coroutine Dispatcher Control: 30%"
        ),
        SkillEntity(
            id = "play-policy",
            title = "Google Play Policy & Zero-Permission Storage",
            category = "Policy & Compliance",
            summary = "Play Store compliance rules: zero-permission Photo Picker (PickVisualMedia), least-privilege manifest permissions, and 30-char title limit.",
            mandates = """
                • ALWAYS use ActivityResultContracts.PickVisualMedia for media selection.
                • ALWAYS limit app titles to 30 characters or fewer without emojis or promotional buzzwords.
                • NEVER request broad storage permissions (READ_EXTERNAL_STORAGE).
                • NEVER implement Dynamic Code Loading (DCL) of external DEX or JAR files.
            """.trimIndent(),
            codeSnippet = """
                val pickMedia = rememberLauncherForActivityResult(
                    ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    uri?.let { /* handle picked image */ }
                }

                Button(onClick = { pickMedia.launch(PickVisualMediaRequest(ImageOnly)) }) {
                    Text("Select Photo (Zero Permission)")
                }
            """.trimIndent(),
            gotchas = "Declaring READ_EXTERNAL_STORAGE will trigger Play Store rejection unless granted a rare core use-case exemption.",
            effectivenessRubric = "Photo Picker Adoption: 50% | Title/Metadata Compliance: 25% | Least Privilege Manifest: 25%"
        ),
        SkillEntity(
            id = "custom-android-launchers",
            title = "Dynamic Android Launchers via SSoT Symlinks",
            category = "Launcher & OS Architecture",
            summary = "Replaces rigid OEM system APKs (/priv-app/TouchWizHome_2017.apk) with a dynamic open-source canvas driven by symlinked markdown skills (~/.agents/skills/phone-interface.md).",
            mandates = """
                • ALWAYS declare android.intent.category.HOME and android.intent.category.DEFAULT in AndroidManifest.xml.
                • ALWAYS build the launcher as an open-source dynamic canvas driven by an SSoT configuration.
                • ALWAYS support 0ms instant hot-reloading from markdown skill files without rebuilding an APK.
                • ALWAYS query PackageManager to list and launch real installed applications.
                • NEVER hardcode layouts or app grids in pre-compiled static system APKs.
                • NEVER require device root or OS reboot to customize phone home screen layouts.
            """.trimIndent(),
            codeSnippet = """
                <!-- AndroidManifest.xml: Declare Home Launcher -->
                <intent-filter>
                    <action android:name="android.intent.action.MAIN" />
                    <category android:name="android.intent.category.LAUNCHER" />
                    <category android:name="android.intent.category.HOME" />
                    <category android:name="android.intent.category.DEFAULT" />
                </intent-filter>
            """.trimIndent(),
            gotchas = "Hardcoding launcher UI in system APKs forces OEM lock-in and blocks AI-driven real-time personalization.",
            effectivenessRubric = "SSoT Symlink Architecture: 35% | Instant Hot-Reload (0ms): 25% | Real App Launching: 20% | M3 Aesthetics: 20%"
        ),
        SkillEntity(
            id = "system-default-prior-ssot-wrapper",
            title = "System Default Prior: SSoT Symlink Tree & APK Thin Wrappers",
            category = "Core OS & SSoT Architecture",
            summary = "The foundational system prior skill instructing the LLM and OS runtime to convert all directory hierarchies into unified symlink pointers and wrap every APK in a thin SSoT wrapper with pre/post hooks, intent interceptors, and 0ms hot-reload.",
            mandates = """
                • ALWAYS treat ~/.agents/skills/ as the Single Source of Truth (SSoT) for all system behaviors.
                • ALWAYS turn directory hierarchies (/system/priv-app, /system/app, /data/app) into symlinks pointing to the central SSoT root.
                • ALWAYS create for each APK a thin wrapper JSON and executable hook script instead of modifying static frozen system binaries.
                • ALWAYS expose lifecycle hooks (pre_launch, intent_intercept, post_launch, hot_reload) for each wrapped APK.
                • ALWAYS intercept legacy OEM intents (e.g. TouchWizHome_2017, SecSettings, SecMyFiles2020) and route them to dynamic reactive handlers.
                • NEVER require ROM rebuilding, re-flashing, or device reboots when a thin SSoT wrapper can intercept and hot-reload.
            """.trimIndent(),
            codeSnippet = """
                // SSoT APK Thin Wrapper Execution Contract
                val wrapper = ApkWrapperConfig(
                    id = "TouchWizHome_2017",
                    originalApkPath = "/system/priv-app/TouchWizHome_2017/TouchWizHome_2017.apk",
                    symlinkPath = "~/.agents/skills/phone-interface.md",
                    wrapperType = "FULL_REPLACEMENT",
                    hooks = listOf(
                        ApkHookConfig("pre_launch", "verify_ssot_integrity"),
                        ApkHookConfig("intent_intercept", "divert_to_dynamic_canvas"),
                        ApkHookConfig("hot_reload", "inotify_pulse_0ms")
                    )
                )
            """.trimIndent(),
            gotchas = "Modifying frozen /system/priv-app binaries directly causes bootloops or signature check failures. Thin SSoT wrappers bypass this safely.",
            effectivenessRubric = "Symlink SSoT Mapping: 30% | Thin Wrapper Coverage: 30% | Lifecycle Hooks Integrity: 25% | 0ms Hot-Reload: 15%"
        )
    )

    val INITIAL_RECIPES = listOf(
        RecipeEntity(
            skillId = "gemini-api",
            title = "Gemini Multi-Turn Chatbot with Skill System Instruction",
            code = """
                val systemPrompt = "You are the Android Masterizer Agent. You enforce Gemini API, Room Database, App Icon, and Material 3 design skills."
                val request = GenerateContentRequest(
                    contents = chatHistory,
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )
            """.trimIndent(),
            language = "Kotlin",
            description = "Creates a multi-turn chat session with custom system instructions.",
            effectivenessScore = 98
        ),
        RecipeEntity(
            skillId = "room-database-integration",
            title = "Reactive Repository with Flow & Suspend Operations",
            code = """
                class OfflineSkillRepository(private val dao: SkillDao) {
                    val allSkills: Flow<List<SkillEntity>> = dao.getAllSkills()
                    suspend fun insertSkill(skill: SkillEntity) = withContext(Dispatchers.IO) {
                        dao.insert(skill)
                    }
                }
            """.trimIndent(),
            language = "Kotlin",
            description = "Room repository abstracting DAO queries into reactive Flow streams.",
            effectivenessScore = 96
        ),
        RecipeEntity(
            skillId = "app-icon-generation",
            title = "Adaptive Launcher Icon Layer-List & Solid Hex",
            code = """
                <?xml version="1.0" encoding="utf-8"?>
                <layer-list xmlns:android="http://schemas.android.com/apk/res/android">
                    <item
                        android:width="66dp"
                        android:height="66dp"
                        android:drawable="@drawable/skill_master_icon"
                        android:gravity="center" />
                </layer-list>
            """.trimIndent(),
            language = "XML",
            description = "Centers 66dp foreground icon inside 108dp canvas with safe zone margins.",
            effectivenessScore = 100
        ),
        RecipeEntity(
            skillId = "design-guidelines",
            title = "48dp Compliant Material 3 Floating Button",
            code = """
                FloatingActionButton(
                    onClick = { /* action */ },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = "Quick Action")
                }
            """.trimIndent(),
            language = "Kotlin",
            description = "Accessible floating action button satisfying touch target standards.",
            effectivenessScore = 95
        )
    )

    // Master System Role Instruction pre-ingested with all skills for the Gemini Chatbox
    fun buildSystemInstruction(): String {
        val builder = StringBuilder()
        builder.appendLine("You are the Android Skill Masterizer AI, an elite software engineering authority on native Android architecture, Jetpack Compose, Room Database, and Gemini integration.")
        builder.appendLine("You possess absolute mastery over the official Android system skills. Whenever answering questions, generating code, or auditing implementations, you MUST strictly uphold these foundational skill rules:")
        builder.appendLine()

        ALL_SKILLS.forEachIndexed { index, skill ->
            builder.appendLine("### ${index + 1}. Skill: ${skill.title} (ID: ${skill.id})")
            builder.appendLine("Category: ${skill.category}")
            builder.appendLine("Summary: ${skill.summary}")
            builder.appendLine("Mandates & Rules:")
            builder.appendLine(skill.mandates)
            builder.appendLine("Gotchas: ${skill.gotchas}")
            builder.appendLine()
        }

        builder.appendLine("### OMARCHY DISCOVERY ARCHITECTURE & SINGLE SOURCE OF TRUTH (SSOT):")
        builder.appendLine("Instead of relying solely on static documentation, you embrace dynamic discovery of system commands:")
        builder.appendLine("1. Discover all available executable system tools dynamically: `compgen -c | grep -E '^android-' | sort -u`")
        builder.appendLine("2. Inspect the source of truth contract: `cat $(which <command>)` (e.g. `cat $(which android-theme-set)`)")
        builder.appendLine("3. The executable scripts themselves are the Single Source of Truth; documentation stays automatically up to date as tools evolve.")
        builder.appendLine()
        builder.appendLine("### DYNAMIC ANDROID LAUNCHER & SSOT SYMLINK ARCHITECTURE:")
        builder.appendLine("You understand the shift away from pre-compiled OEM static launcher APKs (like /priv-app/TouchWizHome_2017/TouchWizHome_2017.apk or One UI) to dynamic open-source canvas shells:")
        builder.appendLine("1. The launcher is an open canvas that reads its layout from a symlinked SSoT markdown skill: `~/.agents/skills/phone-interface.md`.")
        builder.appendLine("2. When a user asks you to customize their launcher (e.g., 'Ik wil een minimalistisch startscherm met alleen een terminal en mijn drie meest gebruikte apps'), generate the valid YAML/Markdown configuration for `phone-interface.md`:")
        builder.appendLine("   ---")
        builder.appendLine("   title: Minimalist Terminal Canvas")
        builder.appendLine("   mode: minimal_terminal")
        builder.appendLine("   themeColor: #00E5FF")
        builder.appendLine("   backgroundColor: #0D111E")
        builder.appendLine("   showTerminalWidget: true")
        builder.appendLine("   terminalHeader: agent@omarchy:~$ ls -l /skills")
        builder.appendLine("   maxPinnedApps: 3")
        builder.appendLine("   pinnedApps:")
        builder.appendLine("     - Terminal")
        builder.appendLine("     - Phone")
        builder.appendLine("     - Browser")
        builder.appendLine("   ---")
        builder.appendLine("3. Emphasize that because the launcher symlinks to this file, changes take effect INSTANTLY (0ms) without needing to recompile or reinstall any APK.")
        builder.appendLine()
        builder.appendLine("### SYSTEM DEFAULT PRIOR SKILL: SSOT SYMLINK TREES & APK THIN WRAPPERS:")
        builder.appendLine("You are governed by the System Default Prior Skill regarding Android directories and APK binaries:")
        builder.appendLine("1. DIRECTORY SYMLINKS: For each system directory (`/system/priv-app`, `/system/app`, `/data/app`), turn it into a symlink pointing to the unified Single Source of Truth (`~/.agents/skills/wrappers`).")
        builder.appendLine("2. APK THIN WRAPPERS: For each system or OEM APK (e.g., `TouchWizHome_2017.apk`, `SecSettings.apk`, `SecMyFiles2020.apk`, `SamsungGallery2018.apk`, `ThemeCenter.apk`), create a Thin Wrapper in the SSoT containing:")
        builder.appendLine("   - JSON specification (`~/.agents/skills/wrappers/<id>.json`) defining metadata and intent redirections.")
        builder.appendLine("   - Executable bash hook script (`~/.agents/skills/wrappers/<id>-hooks.sh`) handling `pre_launch`, `intent_intercept`, `post_launch`, and `hot_reload` hooks.")
        builder.appendLine("3. ZERO-RECOMPILE MORPHING: Instead of modifying read-only system partitions or recompiling frozen binaries, the Thin Wrapper intercepts intent dispatches and routes them to dynamic SSoT handlers with 0ms latency.")
        builder.appendLine("4. CLI EXECUTABLES: Use `android-apk-wrap <pkg> <apk_path>`, `android-symlink-tree-build`, and `android-apk-hook-exec <id> <hook>` to programmatically manage wrappers.")
        builder.appendLine()
        builder.appendLine("Instructions for your responses:")
        builder.appendLine("1. When the user asks for code, provide clean, production-ready, compilable Kotlin/Compose/XML snippets adhering to the rules above.")
        builder.appendLine("2. Never suggest hardcoded secrets, deprecated models (gemini-1.5, gemini-2.0), or blocking database calls.")
        builder.appendLine("3. Format code in markdown blocks so the user can easily copy or inject them into the live standalone React/DSL editor.")
        builder.appendLine("4. In your audits, provide an Effectiveness Score from 0 to 100% and break it down across Security, Architecture, UI/Accessibility, and Offline Robustness.")
        builder.appendLine("5. If asked about discovery commands or system scripts, explain how `compgen` and inspectable CLI binaries prevent documentation rot and ensure single-source-of-truth reliability.")

        return builder.toString()
    }
}
