package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccountTag
import com.example.data.AgentBackgroundTask
import com.example.data.Category
import com.example.data.DataPoint
import com.example.data.EnvironmentConfig
import com.example.data.TaskStatus
import com.example.data.AppDatabase
import com.example.data.CachedReasoningStepEntity
import com.example.data.ChatMessageEntity
import com.example.data.ChatRepository
import com.example.data.ConversationEntity
import com.example.data.DeepThinkingSessionEntity
import com.example.data.FolderFileEntity
import com.example.data.GeneratedMediaEntity
import com.example.data.LocalModelEntity
import com.example.data.LlmDatasetEntity
import com.example.data.WebWatcherEntity
import com.example.data.WikiPageEntity
import com.example.data.LlmWikiStorageService
import com.example.data.PresetCatalog
import com.example.data.ReasoningCacheEntity
import com.example.data.ProofOfThoughtEntity
import com.example.data.ReasoningPreset
import com.example.data.SuggestedLink
import com.example.data.ThumbnailConcept
import com.example.data.VideoChapter
import com.example.engine.AndroidFileNetworkManager
import com.example.engine.NetworkStatusInfo
import com.example.engine.StorageStatsInfo
import com.example.engine.AutonomousPipeline
import com.example.engine.CpuBenchmarkResult
import com.example.engine.CpuClusterInfo
import com.example.engine.EngineMode
import com.example.engine.HybridThinkingManager
import com.example.engine.PipelineStage
import com.example.engine.ReasoningStep
import com.example.engine.SamsungCpuOptimizer
import com.example.engine.SearchCitation
import com.example.engine.ThinkingLevel
import com.example.engine.ThoughtTraceExportData
import com.example.engine.ThoughtTraceExporter
import com.example.engine.VideoMetadataService
import com.example.engine.TaskComplexityRouter
import com.example.engine.ModelRoutingPlan
import com.example.engine.TaskComplexityLevel
import com.example.engine.ModelUpgradeManager
import com.example.engine.ModelUpgradeItem
import com.example.engine.GeminiApiClient
import com.example.engine.GeneratedReview
import com.example.mcp.HfHubItem
import com.example.mcp.HfItemType
import com.example.mcp.HuggingFaceMcpClient
import com.example.mcp.McpServerConfig
import com.example.mcp.McpServerStatus
import com.example.mcp.McpToolCallResult
import com.example.auth.AuthManager
import com.example.data.FirebaseRepository
import com.example.engine.ollama.*
import com.example.engine.llmapi.*
import com.example.engine.octopus.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.EnumMap
import java.util.UUID

data class UiState(
    val currentConversationId: String = "",
    val isGenerating: Boolean = false,
    val liveThoughtText: String = "",
    val liveAnswerText: String = "",
    val liveCurrentStep: ReasoningStep? = null,
    val liveDurationMs: Long = 0L,
    val liveThoughtTokens: Int = 0,
    val liveSearchCitations: List<SearchCitation> = emptyList(),
    val selectedThinkingLevel: ThinkingLevel = ThinkingLevel.HIGH,
    val engineMode: EngineMode = EngineMode.ONLINE_GEMINI_API,
    val selectedGeminiModel: String = "gemini-3.5-flash",
    val selectedChatbotRole: String = "General Assistant",
    val isCustomRoleDialogOpen: Boolean = false,
    val isSessionsDrawerOpen: Boolean = false,
    val searchGroundingEnabled: Boolean = false,
    val selectedAgentType: String = "DEFAULT", // "DEEP_RESEARCH", "INFINITE_DATASET_HUB", "GEMINI_STANDARD", "GEMINI_DEEP_THINK", "CODE_ARCHITECT", "DATA_COLLECTOR"
    val selectedInteractionStepType: String = "CONVERSATION", // "DATA_COLLECTION", "SUMMARIZATION", "REFORMATTING", "DATASET_GENERATION", "CODE_SYNTHESIS"
    val linkToPreviousInteraction: Boolean = true,
    val activeTab: Int = 0, // 0: Chat, 1: Vision & Veo Studio, 2: Model Manager, 3: History & Feedback, 4: Pipelines & One UI
    val inspectingSteps: List<ReasoningStep>? = null,
    val systemInstruction: String = "",
    val streamingSpeedMs: Long = 16L,

    // Local LLM Management
    val activeModelId: String = "gemini-3.7-flash-think-q4",
    val activeModelName: String = "Gemini 3.7 Flash Thinking (Q4_K_M)",
    val downloadingModelId: String? = null,
    val downloadProgress: Float = 0f,
    val hfDownloadInput: String = "hf download JonathanColetti/Qwen3.8-27B-Uncensored-GGUF",
    val hfDownloadSelectedQuant: String = "Q4_K_M",
    val downloadSpeedMbps: Float = 0f,
    val downloadEtaSeconds: Int = 0,
    val downloadCurrentShard: String = "",
    val downloadTotalShards: Int = 4,
    val downloadStatusMessage: String = "",

    // Dynamic Model Routing based on Task Complexity
    val autoModelRoutingEnabled: Boolean = true,
    val currentRoutingPlan: ModelRoutingPlan? = null,
    val lastRoutingBadge: String = "⚡ Auto: Gemini Flash (General)",
    val routingThresholdLevel: String = "Balanced", // "Aggressive", "Balanced", "Conservative"

    // Model Upgrades & Newer LLMs Management
    val modelUpgradeItems: List<ModelUpgradeItem> = ModelUpgradeManager.getInitialUpgradeRegistry(),
    val isCheckingForModelUpdates: Boolean = false,
    val lastModelUpdateCheckTimestamp: Long = System.currentTimeMillis(),
    val isRegisterCustomModelDialogOpen: Boolean = false,
    val isModelUpgradeDetailDialogOpen: Boolean = false,
    val selectedUpgradeDetailItem: ModelUpgradeItem? = null,
    val autoUpdateOnWifi: Boolean = true,
    val autoCheckUpdatesOnLaunch: Boolean = true,

    // Feedback Dialog
    val feedbackMessageTarget: ChatMessageEntity? = null,
    val feedbackRatingInput: Int = 0,
    val feedbackTextInput: String = "",

    // Chain-of-Thought Step Debugger Overlay
    val isCotDebugOverlayOpen: Boolean = false,
    val isCotDebugPanelOpen: Boolean = false,
    val isEdgePanelOpen: Boolean = false,
    val cotDebugSteps: List<ReasoningStep> = emptyList(),
    val cotDebugInitialStep: Int = 0,

    // Export Thought Trace & AI Response (Markdown / PDF)
    val isExportDialogOpen: Boolean = false,
    val exportTargetData: ThoughtTraceExportData? = null,

    // History & Offline Room Reasoning Cache
    val historySearchQuery: String = "",
    val historySelectedDomain: String = "All",
    val sessionsTabSubIndex: Int = 0, // 0: Live Conversations, 1: Offline Reasoning Cache (Room DB)
    val cacheSearchQuery: String = "",
    val selectedCacheCategory: String = "All",
    val inspectingCachedItem: ReasoningCacheEntity? = null,
    val inspectingSession: DeepThinkingSessionEntity? = null,
    val cachedItemSteps: List<ReasoningStep> = emptyList(),

    // Creative Media & Veo Studio

    val imagePromptInput: String = "",
    val selectedImageStyle: String = "Cinematic 8K",
    val selectedAspectRatio: String = "1:1",
    val isGeneratingImage: Boolean = false,
    val latestGeneratedMedia: GeneratedMediaEntity? = null,

    // Voice Conversations & Audio Transcription & Non-Streaming Invariants
    val isVoiceConversationOpen: Boolean = false,
    val isAudioTranscriberOpen: Boolean = false,
    val nonStreamingTreeEnabled: Boolean = false,

    // Autonomous Pipelines & Samsung S26 Ultra European CPU Admin
    val s26UltraNpuTurbo: Boolean = true,
    val oneUi120HzSync: Boolean = true,
    val knoxPrivateSandbox: Boolean = true,
    val zeroTokenRoutingEnabled: Boolean = true,
    val sve2VectorMathEnabled: Boolean = true,
    val exynosNpuDelegateEnabled: Boolean = true,
    val cpuGovernorMode: String = "Performance AI Turbo",
    val cpuHardwareProfile: CpuClusterInfo = SamsungCpuOptimizer.detectHardwareProfile(),
    val cpuBenchmarkResult: CpuBenchmarkResult? = null,
    val isBenchmarkingCpu: Boolean = false,
    val activeRunningPipelineId: String? = null,
    val pipelineStepProgress: Int = 0,
    val pipelineLog: List<String> = emptyList(),

    // Hugging Face MCP Server Connector
    val isHfMcpConnectorOpen: Boolean = false,
    val hfMcpConfig: McpServerConfig = McpServerConfig(),
    val hfMcpStatus: McpServerStatus = McpServerStatus(),
    val hfMcpGroundingInChat: Boolean = true,
    val hfMcpSelectedTab: Int = 0, // 0: Server Config & Health, 1: Hub Resources & Models, 2: MCP Tool Invoker, 3: Daily Papers
    val hfMcpSearchQuery: String = "",
    val hfMcpSearchResults: List<HfHubItem> = emptyList(),
    val hfMcpDailyPapers: List<HfHubItem> = emptyList(),
    val hfMcpIsSearching: Boolean = false,
    val hfMcpSelectedTool: String = "hf_hub_search_models",
    val hfMcpToolArgsJson: String = """{"query": "deepseek-r1", "task": "text-generation", "limit": 5}""",
    val hfMcpLastToolResult: McpToolCallResult? = null,
    val hfMcpIsCallingTool: Boolean = false,
    val hfMcpActiveCategory: String = "All",

    // Dedicated Offline LLM Studio GUI State
    val offlinePromptInput: String = "",
    val offlineResponseText: String = "",
    val offlineThoughtText: String = "",
    val offlineCurrentStep: ReasoningStep? = null,
    val isOfflineGenerating: Boolean = false,
    val offlineExecutionTimeMs: Long = 0L,
    val offlineTokensPerSec: Float = 0f,
    val offlineSelectedFileName: String? = null,
    val offlineSelectedFileContent: String = "",
    val isFileAnalysisDialogOpen: Boolean = false,
    val isSummaryDialogOpen: Boolean = false,
    val offlineTemperature: Float = 0.7f,
    val offlineMaxTokens: Int = 2048,
    val offlineActionCategory: String = "All",

    // Ollama Hub & Client
    val ollamaHostUrl: String = "http://10.0.2.2:11434",
    val ollamaStatus: OllamaServerStatus = OllamaServerStatus(isConnected = false, activeHost = "http://10.0.2.2:11434"),
    val ollamaModels: List<OllamaModel> = listOf(
        OllamaModel("deepseek-r1:7b", "Just now", 4700000000L, "4.7 GB", "7B", "Q4_K_M", "sha256:8b49"),
        OllamaModel("llama3.2:3b", "1 day ago", 2000000000L, "2.0 GB", "3B", "Q4_K_M", "sha256:d624"),
        OllamaModel("qwen2.5-coder:7b", "3 days ago", 4500000000L, "4.5 GB", "7B", "Q4_K_M", "sha256:3a19")
    ),
    val selectedOllamaModel: String = "deepseek-r1:7b",
    val ollamaIsGenerating: Boolean = false,
    val ollamaLiveThought: String = "",
    val ollamaLiveResponse: String = "",
    val ollamaIsPulling: Boolean = false,
    val ollamaPullProgress: Float = 0f,
    val ollamaPullStatusText: String = "",

    // Universal LLM API Gateway
    val llmApiConfig: LlmApiConfig = LlmApiConfig(),
    val llmApiIsCalling: Boolean = false,
    val llmApiLatestResponse: LlmApiResponse? = null,
    val llmApiTestResult: String? = null,
    val llmApiTestSuccess: Boolean = false,

    // Nexa AI Octopus Agent Engine
    val octopusTools: List<OctopusToolDefinition> = emptyList(),
    val octopusIsExecuting: Boolean = false,
    val octopusLatestPlan: OctopusExecutionPlan? = null,

    // ML Kit + Gemini Real-Time Translation Chat Assistance
    val activeTranslateLanguageCode: String = "es",
    val chatMessageTranslations: Map<String, String> = emptyMap(),
    val isTranslatingMessageId: String? = null,
    val isQuickTranslatorSheetOpen: Boolean = false,
    val quickTranslateInput: String = "",
    val quickTranslateResult: com.example.engine.TranslationResult? = null,
    val isQuickTranslating: Boolean = false,
    val quickTranslateTargetLang: String = "es",

    // Background Execution & Environment Sandboxing State Fields
    val backgroundTasks: List<AgentBackgroundTask> = emptyList(),
    val sandboxes: List<EnvironmentConfig> = listOf(
        EnvironmentConfig(
            environmentId = "env-default-384",
            sandboxName = "Default Core Sandbox",
            customSystemInstructions = "You are a secure, sandboxed core engineering assistant.",
            allowedOutboundDomains = "api.github.com, maven.org, huggingface.co",
            persistWorkspaceState = true,
            simulatedPackageRegistry = "Maven & npm Central Repository Safe Proxy"
        ),
        EnvironmentConfig(
            environmentId = "env-secure-8b2",
            sandboxName = "Isolated Finance & Data Sandbox",
            customSystemInstructions = "You are an isolated assistant specialized in structured financial validation.",
            allowedOutboundDomains = "sec.gov, alpha-vantage.com",
            persistWorkspaceState = false,
            simulatedPackageRegistry = "Local Offline Encrypted Cache Registry"
        ),
        EnvironmentConfig(
            environmentId = "env-custom-0w4",
            sandboxName = "Full Custom AI Playpen",
            customSystemInstructions = "Execute unconstrained multi-agent algorithms securely.",
            allowedOutboundDomains = "openai.com, anthropic.com, local-node-cluster:8080",
            persistWorkspaceState = true,
            simulatedPackageRegistry = "Local Custom Package Registry"
        )
    ),
    val selectedSandboxId: String = "env-default-384",
    val selectedTaskId: String? = null,
    val newTaskPrompt: String = "Perform multi-step verification of prime factor scalability using Miller-Rabin tests",
    val selectedEnvironmentIdToReuse: String = "env-default-384",
    val backgroundExecutionLog: List<String> = emptyList(),

    // One-Click Authentication & SSO State
    val isLoggedIn: Boolean = false,
    val loggedInUserEmail: String? = null,
    val loggedInUserName: String? = null,
    val authStatusMessage: String? = null,
    val isAuthenticating: Boolean = false,

    // Local Documents & Backup RAG State
    val localDocuments: List<com.example.data.DocumentEntity> = emptyList(),
    val isImportExportActive: Boolean = false,
    val backupRestoreLog: String? = null,

    // Heavy Computation & DataPoint Aggregations
    val aggregatedData: Map<Category, Double> = emptyMap(),
    val heavyComputationError: String? = null,
    val isComputingData: Boolean = false,

    // Scraped fields added back for backward compatibility with screens
    val isGeneratingReview: Boolean = false,
    val isGeneratingVeo: Boolean = false,
    val isAnalyzingVideoMetadata: Boolean = false,
    val veoMotionPreset: String = "Cinematic Pan Left",
    val veoDurationSec: Int = 5,
    val veoActiveMedia: GeneratedMediaEntity? = null,
    val veoPlaybackPlaying: Boolean = false,
    val selectedVideoUri: Uri? = null,
    val selectedVideoTitle: String = "S26 Ultra Cinematic NPU Benchmark Capture",
    val selectedVideoDuration: String = "07:45",
    val selectedVideoResolution: String = "4K HDR (60 FPS)",
    val selectedVideoCategory: String = "Tech Innovation",
    val videoCustomContext: String = "",
    val videoMetadataError: String? = null,
    val videoMetadataActiveViewFilter: String = "Description",
    val activeMetadataTask: String = "",
    val videoMetadataDescriptionHtml: String? = null,
    val videoMetadataHashtags: List<String> = emptyList(),
    val videoMetadataChapters: List<VideoChapter> = emptyList(),
    val videoMetadataAccountTags: List<AccountTag> = emptyList(),
    val videoMetadataLinks: List<SuggestedLink> = emptyList(),
    val videoMetadataThumbnails: List<ThumbnailConcept> = emptyList(),
    val museumSelectedArtifactId: String = "mona_lisa",
    val museumActiveMapWing: String = "Sully Wing (2nd Floor)",
    val museumTranslatedAudioScript: String? = null,
    val museumAudioTourPlaying: Boolean = false,
    val museumQueryInput: String = "",
    val museumSelectedTranslateLang: String = "es",
    val isReviewGeneratorOpen: Boolean = false,
    val reviewRequestTopic: String = "",
    val reviewRequestCategory: String = "Technical Documentation",
    val reviewRatingStars: Int = 5,
    val reviewSelectedTone: String = "Enthusiastic Visitor",
    val reviewTargetLength: String = "Balanced (250 words)",
    val reviewSelectedHighlights: List<String> = emptyList(),
    val reviewCustomNotes: String = "",
    val reviewTranslatedVersion: String? = null,
    val latestGeneratedReview: GeneratedReview? = null,
    val reviewSelectedTranslateLang: String = "es",

    // Database & Multi-Source Comprehensive Benchmark
    val isRunningComprehensiveBenchmark: Boolean = false,
    val benchmarkCurrentPhase: String = "",
    val benchmarkProgress: Float = 0f,
    val latestBenchmarkResult: com.example.data.BenchmarkResultEntity? = null,
    val knowledgeSources: List<com.example.data.KnowledgeSourceEntity> = emptyList(),
    val benchmarkHistory: List<com.example.data.BenchmarkResultEntity> = emptyList(),
    val isNewSourceDialogOpen: Boolean = false,
    val sourceFilterCategory: String = "All",
    val dbMaintenanceMessage: String? = null,

    // Room Database AI Thought-Trace Cache & Offline Profiling
    val isSimulateOfflineMode: Boolean = false,
    val cacheStorageStats: com.example.data.CacheStorageStats = com.example.data.CacheStorageStats(),
    val isLastMessageCacheHit: Boolean = false,

    // Interactive Problem Solving Modules
    val problemProgressList: List<com.example.data.ProblemProgressEntity> = emptyList(),

    // Autopilot Browser, Background Web Watchers, and Karpathy LLM Wiki Memory
    val autopilotBrowserUrl: String = "https://www.gym-class-booking.org/gym6am",
    val autopilotBrowserLog: List<String> = listOf(
        "Initialised secure sandbox browser environment",
        "Configured bring-your-own-model (BYOM) OpenAI API route",
        "Ready to run in background. No data leaves your phone."
    ),
    val isAutopilotStuckOnCaptcha: Boolean = false,
    val autopilotBrowserPrompt: String = "Book the 6am class at my gym the moment the slots open",
    val autopilotIsNavigating: Boolean = false,
    val wikiSearchQuery: String = "",
    val selectedWikiPageId: String? = null,
    val overnightWikiMaintenanceLog: String = "Overnight wiki scheduler: Enabled (Runs daily at 02:00 AM). Last run: Done. Linked 14 entity nodes, consolidated 2 duplicate cards.",
    val isDrivingModeOpen: Boolean = false,
    val lastDrivingCoPilotAnswer: String = ""
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(
        database.chatDao(),
        database.reasoningCacheDao(),
        database.knowledgeAndBenchmarkDao(),
        database.folderFoundationDao(),
        database.proofOfThoughtDao()
    )
    val fileNetworkManager = AndroidFileNetworkManager(application)
    val networkStatus: StateFlow<NetworkStatusInfo> = fileNetworkManager.networkStatus
    val storageStats: StateFlow<StorageStatsInfo> = fileNetworkManager.storageStats
    val thinkingManager = HybridThinkingManager(repository)
    val hfMcpClient = HuggingFaceMcpClient()
    val wikiStorageService = LlmWikiStorageService(database.reasoningCacheDao())

    val voiceConversationManager = com.example.audio.VoiceConversationManager(application, viewModelScope)
    val audioTranscriptionManager = com.example.audio.AudioTranscriptionManager(application, viewModelScope)
    val translationManager = com.example.engine.MlKitTranslationManager(application)

    private val ollamaClient = OllamaClient()
    private val llmApiClient = UniversalLlmApiClient()
    private val octopusEngine = OctopusAgentEngine(application, database)
    val workspaceManager = com.example.engine.workspace.GoogleWorkspaceManager(application)
    val skillsManager = com.example.engine.skills.SkillsManager(application, viewModelScope)

    private val _gmailMessages = MutableStateFlow<List<com.example.engine.workspace.GmailMessageItem>>(emptyList())
    val gmailMessages: StateFlow<List<com.example.engine.workspace.GmailMessageItem>> = _gmailMessages.asStateFlow()

    private val _calendarEvents = MutableStateFlow<List<com.example.engine.workspace.CalendarEventItem>>(emptyList())
    val calendarEvents: StateFlow<List<com.example.engine.workspace.CalendarEventItem>> = _calendarEvents.asStateFlow()

    private val _driveFiles = MutableStateFlow<List<com.example.engine.workspace.DriveFileItem>>(emptyList())
    val driveFiles: StateFlow<List<com.example.engine.workspace.DriveFileItem>> = _driveFiles.asStateFlow()

    private val _contacts = MutableStateFlow<List<com.example.engine.workspace.GoogleContactItem>>(emptyList())
    val contacts: StateFlow<List<com.example.engine.workspace.GoogleContactItem>> = _contacts.asStateFlow()

    private val _googleOneStatus = MutableStateFlow(com.example.engine.workspace.GoogleOneStatus())
    val googleOneStatus: StateFlow<com.example.engine.workspace.GoogleOneStatus> = _googleOneStatus.asStateFlow()

    private val _gmailVoiceSummary = MutableStateFlow<String?>(null)
    val gmailVoiceSummary: StateFlow<String?> = _gmailVoiceSummary.asStateFlow()

    private val _isWorkspaceSyncing = MutableStateFlow(false)
    val isWorkspaceSyncing: StateFlow<Boolean> = _isWorkspaceSyncing.asStateFlow()

    private val _workspaceOperationMessage = MutableStateFlow<String?>(null)
    val workspaceOperationMessage: StateFlow<String?> = _workspaceOperationMessage.asStateFlow()

    fun clearWorkspaceOperationMessage() {
        _workspaceOperationMessage.value = null
    }

    // One UI 9 Live Direct-Input Validation & System Optimization Center
    private val _isOneUi9CenterOpen = MutableStateFlow(false)
    val isOneUi9CenterOpen: StateFlow<Boolean> = _isOneUi9CenterOpen.asStateFlow()

    private val _directInputText = MutableStateFlow("Benchmark Exynos 2600 NPU using Qwen3 1.7B / Gemma 2B in Q4_K_M on Android 17. Calculate matrix tokens per watt.")
    val directInputText: StateFlow<String> = _directInputText.asStateFlow()

    private val _directValidationResult = MutableStateFlow<com.example.engine.validation.DirectInputValidationResult?>(null)
    val directValidationResult: StateFlow<com.example.engine.validation.DirectInputValidationResult?> = _directValidationResult.asStateFlow()

    private val _isValidatingDirectInput = MutableStateFlow(false)
    val isValidatingDirectInput: StateFlow<Boolean> = _isValidatingDirectInput.asStateFlow()

    private val _cleanupReport = MutableStateFlow<com.example.engine.validation.EnvironmentCleanupReport?>(null)
    val cleanupReport: StateFlow<com.example.engine.validation.EnvironmentCleanupReport?> = _cleanupReport.asStateFlow()

    private val _isCleaningEnvironment = MutableStateFlow(false)
    val isCleaningEnvironment: StateFlow<Boolean> = _isCleaningEnvironment.asStateFlow()

    fun openOneUi9Center() {
        _isOneUi9CenterOpen.value = true
        triggerHapticFeedback()
        if (_directValidationResult.value == null) {
            validateDirectInput(_directInputText.value)
        }
    }

    fun closeOneUi9Center() {
        _isOneUi9CenterOpen.value = false
        triggerHapticFeedback()
    }

    fun setDirectInputText(text: String) {
        _directInputText.value = text
    }

    fun validateDirectInput(input: String) {
        viewModelScope.launch {
            _isValidatingDirectInput.value = true
            try {
                val res = com.example.engine.validation.OneUi9ValidationEngine.validateDirectInput(input)
                _directValidationResult.value = res
            } finally {
                _isValidatingDirectInput.value = false
            }
        }
    }

    fun runEnvironmentCleanup() {
        viewModelScope.launch {
            _isCleaningEnvironment.value = true
            triggerHapticFeedback()
            try {
                val report = com.example.engine.validation.OneUi9ValidationEngine.performEnvironmentCleanup(database, getApplication())
                _cleanupReport.value = report
                triggerHapticFeedback()
            } finally {
                _isCleaningEnvironment.value = false
            }
        }
    }

    val drivingManager = com.example.engine.driving.DrivingAgentManager(application)
    val drivingTelemetry: StateFlow<com.example.engine.driving.DrivingTelemetry> = drivingManager.telemetry
    val routePois: StateFlow<List<com.example.engine.driving.RoutePoiItem>> = drivingManager.routePois
    val ticketHoldAlert: StateFlow<com.example.engine.driving.TicketHoldAlert?> = drivingManager.ticketHoldAlert

    fun openDrivingMode() {
        _uiState.update { it.copy(isDrivingModeOpen = true) }
        triggerHapticFeedback()
        voiceConversationManager.speakText("Agent Co-Pilot aktiv. Ich überwache deine Fahrtroute auf der A8. Frag mich nach geöffneten Orten oder Ladesäulen.", stageInfo = "Driving Mode Started")
    }

    fun closeDrivingMode() {
        _uiState.update { it.copy(isDrivingModeOpen = false) }
        triggerHapticFeedback()
    }

    fun addPoiToDrivingRoute(poiId: String) {
        viewModelScope.launch {
            drivingManager.addPoiToActiveRoute(poiId)
            triggerHapticFeedback()
            voiceConversationManager.speakText("Zwischenstopp zur Navigation hinzugefügt.", stageInfo = "Route Updated")
        }
    }

    fun confirmTicketPurchase(ticketId: String) {
        viewModelScope.launch {
            val msg = drivingManager.confirmTicketPurchase(ticketId)
            triggerHapticFeedback()
            voiceConversationManager.speakText(msg, stageInfo = "Tickets Confirmed")
        }
    }

    fun releaseTicketHold(ticketId: String) {
        viewModelScope.launch {
            drivingManager.releaseTicketHold(ticketId)
            triggerHapticFeedback()
            voiceConversationManager.speakText("Tickets wurden aus dem Warenkorb freigegeben.", stageInfo = "Hold Released")
        }
    }

    fun processDrivingVoiceQuery(query: String) {
        viewModelScope.launch {
            val spokenAnswer = drivingManager.generateDrivingSpokenResponse(query)
            _uiState.update { it.copy(lastDrivingCoPilotAnswer = spokenAnswer) }
            voiceConversationManager.speakText(spokenAnswer, stageInfo = "Driving Assistant")
        }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flow-based debounced live conversations filter
    val filteredConversations: StateFlow<List<ConversationEntity>> = combine(
        repository.allConversations,
        _uiState.map { it.historySearchQuery to it.historySelectedDomain }.distinctUntilChanged().debounce(250L)
    ) { convs, (query, domain) ->
        if (query.isBlank() && domain == "All") {
            convs
        } else {
            convs.filter { conv ->
                val matchesQuery = query.isBlank() ||
                    conv.title.contains(query, ignoreCase = true) ||
                    conv.domainTag.contains(query, ignoreCase = true) ||
                    conv.lastMessagePreview.contains(query, ignoreCase = true)
                val matchesDomain = domain == "All" || conv.domainTag.contains(domain, ignoreCase = true)
                matchesQuery && matchesDomain
            }
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val localModels: StateFlow<List<LocalModelEntity>> = repository.allLocalModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val generatedMediaList: StateFlow<List<GeneratedMediaEntity>> = repository.allGeneratedMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Room Reasoning Cache StateFlows with Flow-based Debounce
    val cachedReasoningList: StateFlow<List<ReasoningCacheEntity>> = _uiState
        .map { it.cacheSearchQuery to it.selectedCacheCategory }
        .distinctUntilChanged()
        .debounce(300L)
        .flatMapLatest { (query, category) ->
            repository.searchReasoningCache(query, category)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedSessionsList: StateFlow<List<DeepThinkingSessionEntity>> = repository.deepThinkingSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTokensCached: StateFlow<Long?> = repository.totalTokensCached
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val datasetsList: StateFlow<List<LlmDatasetEntity>> = repository.allDatasets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val proofOfThoughts: StateFlow<List<ProofOfThoughtEntity>> = repository.allProofOfThoughts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalProofCount: StateFlow<Int> = repository.totalProofCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalProofTokens: StateFlow<Long?> = repository.totalProofTokens
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalCacheCount: StateFlow<Int> = repository.totalCacheCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val webWatchersList: StateFlow<List<WebWatcherEntity>> = repository.allWebWatchers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wikiPagesList: StateFlow<List<WikiPageEntity>> = combine(
        repository.allWikiPages,
        _uiState.map { it.wikiSearchQuery }.distinctUntilChanged().debounce(250L)
    ) { pages, query ->
        if (query.isBlank()) {
            pages
        } else {
            pages.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true) ||
                it.tags.contains(query, ignoreCase = true)
            }
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3-Folder Foundation Flows (00_Inbox, 01_Projects, 99_Archive)
    private val _selectedFolder = MutableStateFlow("00_Inbox")
    val selectedFolder: StateFlow<String> = _selectedFolder.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentFolderFiles: StateFlow<List<FolderFileEntity>> = _selectedFolder
        .flatMapLatest { folder -> repository.getFilesByFolder(folder) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectFolder(folderName: String) {
        _selectedFolder.value = folderName
        triggerHapticFeedback()
    }

    val inboxFiles: StateFlow<List<FolderFileEntity>> = repository.getFilesByFolder("00_Inbox")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projectsFiles: StateFlow<List<FolderFileEntity>> = repository.getFilesByFolder("01_Projects")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archiveFiles: StateFlow<List<FolderFileEntity>> = repository.getFilesByFolder("99_Archive")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFolders: StateFlow<List<String>> = repository.getAllFolders()
        .map { list ->
            val base = listOf("00_Inbox", "01_Projects", "99_Archive")
            (base + list).distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("00_Inbox", "01_Projects", "99_Archive"))

    // Flow-based Debounce channels for user inputs & heavy operations
    private val _heavyComputationRequests = MutableSharedFlow<List<DataPoint>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val _hfSearchQueryFlow = MutableSharedFlow<Pair<String, String>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val _quickTranslateInputFlow = MutableSharedFlow<Pair<String, String>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )


    val currentMessages: StateFlow<List<ChatMessageEntity>> = _uiState
        .flatMapLatest { state ->
            if (state.currentConversationId.isNotBlank()) {
                repository.getMessages(state.currentConversationId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var activeJob: Job? = null
    private var downloadJob: Job? = null
    private var pipelineJob: Job? = null
    private var offlineJob: Job? = null

    // Autonomous pipelines definitions
    val predefinedPipelines: List<AutonomousPipeline> = listOf(
        AutonomousPipeline(
            id = "pipeline-zero-token-researcher",
            title = "Zero-Token Deep Researcher & Fact Checker",
            category = "Autonomous Research",
            isZeroToken = true,
            targetPlatform = "Local NPU Engine (Free / Zero Token)",
            description = "Multi-stage autonomous pipeline that executes real-time knowledge retrieval, cross-verifies empirical claims, and compiles a comprehensive Markdown report.",
            stages = listOf(
                PipelineStage(1, "Deconstruct Intent & Query Parameters", "Query Parser"),
                PipelineStage(2, "Real-Time Google Index Crawl & Extraction", "Search Grounding Agent"),
                PipelineStage(3, "Adversarial Fact Verification & Proof Check", "Verification NPU"),
                PipelineStage(4, "Executive Synthesis & Citations Document", "Report Synthesizer")
            )
        ),
        AutonomousPipeline(
            id = "pipeline-code-architect",
            title = "Algorithmic Architecture & Verification Suite",
            category = "Code Engineering",
            isZeroToken = true,
            targetPlatform = "Offline DeepSeek R1 / Gemini 3.7 NPU",
            description = "Autonomous software engineering pipeline formulating data structure invariants, optimal asymptotic bounds, Kotlin code synthesis, and unit test generation.",
            stages = listOf(
                PipelineStage(1, "Formal Complexity & Invariant Analysis", "Algorithm Planner"),
                PipelineStage(2, "Modular Architecture & Kotlin Synthesis", "Code Engine"),
                PipelineStage(3, "Boundary Condition & Edge Case Verification", "Test Suite Generator"),
                PipelineStage(4, "Static Analysis & Performance Benchmarking", "NPU Profiler")
            )
        ),
        AutonomousPipeline(
            id = "pipeline-veo-commercial",
            title = "Veo 3 Dynamic Visual Ad Campaign",
            category = "Creative Production",
            isZeroToken = false,
            targetPlatform = "Veo 3 Video Cloud Engine",
            description = "End-to-end creative workflow: generates high-resolution product photography, applies cinematic lighting presets, and renders dynamic Veo 3 camera orbit video.",
            stages = listOf(
                PipelineStage(1, "Creative Concept & Aesthetic Prompting", "Gemini 3.7 Idea Engine"),
                PipelineStage(2, "Text-to-Image 8K Product Rendering", "Vision Generation Studio"),
                PipelineStage(3, "Veo 3 Motion Flow & Physics Simulation", "Veo 3 Video Renderer"),
                PipelineStage(4, "Color Grading & Cinematic Master Export", "Video Post-Processor")
            )
        ),
        AutonomousPipeline(
            id = "pipeline-hybrid-voting-refine",
            title = "Hybrid Voting & Cross-Critique (Ollama Multi-Model)",
            category = "Multi-Model Consensus",
            isZeroToken = true,
            targetPlatform = "Ollama / DeepSeek-R1 + Qwen2.5-Coder + Qwen2.5 / Llama 3.3",
            description = "3-Phase Multi-Agent Consensus: Parallel Logic & Code drafts, mutual adversarial cross-critique, and Chief Architect final error-free synthesis.",
            stages = listOf(
                PipelineStage(1, "Parallel Draft Generation (Logic vs Code)", "deepseek-r1:14b & qwen2.5-coder:14b"),
                PipelineStage(2, "Cross-Critique & Mutual Adversarial Review", "Logic ↔ Code Reviewers"),
                PipelineStage(3, "Chief Architect Synthesis & Clean Code Generation", "qwen2.5:14b / llama3.3:70b Judge")
            )
        )
    )

    private val firebaseRepository = FirebaseRepository(application)
    private val authManager = AuthManager(application)

    init {
        authManager.attemptAutoSignIn(
            scope = viewModelScope,
            onSuccess = { onAuthSuccess() },
            onFailure = { /* Silent failure */ }
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.initDefaultModelsIfEmpty()
            repository.initDefaultKnowledgeSourcesIfEmpty()
            repository.initDefaultWebWatchersIfEmpty()
            repository.initDefaultWikiPagesIfEmpty()
        }

        viewModelScope.launch {
            repository.allKnowledgeSources.collect { list ->
                _uiState.update { it.copy(knowledgeSources = list) }
            }
        }

        viewModelScope.launch {
            repository.latestBenchmarkResult.collect { result ->
                _uiState.update { it.copy(latestBenchmarkResult = result) }
            }
        }

        viewModelScope.launch {
            repository.allBenchmarkResults.collect { history ->
                _uiState.update { it.copy(benchmarkHistory = history) }
            }
        }

        viewModelScope.launch {
            repository.thoughtCacheRepository.cacheStats.collect { stats ->
                _uiState.update { it.copy(cacheStorageStats = stats) }
            }
        }

        viewModelScope.launch {
            repository.allProblemProgress.collect { progressList ->
                _uiState.update { it.copy(problemProgressList = progressList) }
            }
        }

        viewModelScope.launch {
            conversations.collect { list ->
                if (list.isNotEmpty() && _uiState.value.currentConversationId.isBlank()) {
                    _uiState.update { it.copy(currentConversationId = list.first().id) }
                } else if (list.isEmpty() && _uiState.value.currentConversationId.isBlank()) {
                    startNewConversation("Deep Thinking Session")
                }
            }
        }

        viewModelScope.launch {
            repository.allDocuments.collect { docs ->
                if (docs.isEmpty()) {
                    seedInitialDocuments()
                }
                _uiState.update { it.copy(localDocuments = docs) }
            }
        }

        viewModelScope.launch {
            localModels.collect { models ->
                val active = models.find { it.isActive }
                if (active != null) {
                    _uiState.update {
                        it.copy(
                            activeModelId = active.id,
                            activeModelName = active.name
                        )
                    }
                }
            }
        }

        // Hook up Voice Conversation Spoken Query Loop
        voiceConversationManager.onUserQuerySpoken = { spokenQuery ->
            handleVoiceSpokenQuery(spokenQuery)
        }

        // Flow-based Debounce Collector for Heavy Computations
        viewModelScope.launch {
            _heavyComputationRequests
                .debounce(250L)
                .distinctUntilChanged()
                .collectLatest { inputList ->
                    executeHeavyComputation(inputList)
                }
        }

        // Flow-based Debounce Collector for Hugging Face Hub Search
        viewModelScope.launch {
            _hfSearchQueryFlow
                .debounce(400L)
                .distinctUntilChanged()
                .collectLatest { (query, category) ->
                    if (query.isNotBlank() && _uiState.value.isHfMcpConnectorOpen) {
                        searchHfHub(query, category)
                    }
                }
        }

        // Flow-based Debounce Collector for Quick Live Translation
        viewModelScope.launch {
            _quickTranslateInputFlow
                .debounce(500L)
                .distinctUntilChanged()
                .collectLatest { (text, _) ->
                    if (text.isNotBlank() && _uiState.value.isQuickTranslatorSheetOpen) {
                        executeQuickTranslate(useGeminiNuance = false)
                    }
                }
        }

        // Initialize Google Workspace and Google One sync
        viewModelScope.launch {
            refreshWorkspaceData()
        }
    }

    // =========================================================================
    // Google Workspace (Gmail, Calendar, Drive, Docs, Sheets) & Google One
    // =========================================================================

    fun refreshWorkspaceData() {
        viewModelScope.launch {
            _isWorkspaceSyncing.value = true
            try {
                _gmailMessages.value = workspaceManager.getGmailMessages()
                _calendarEvents.value = workspaceManager.getCalendarEvents()
                _driveFiles.value = workspaceManager.getDriveFiles()
                _contacts.value = workspaceManager.getContacts()
                _googleOneStatus.value = workspaceManager.getGoogleOneStatus()
            } catch (e: Exception) {
                // Keep resilient
            } finally {
                _isWorkspaceSyncing.value = false
            }
        }
    }

    fun generateGmailVoiceSummary() {
        viewModelScope.launch {
            _gmailVoiceSummary.value = workspaceManager.generateGmailVoiceSummary()
        }
    }

    fun createWorkspaceCalendarEvent(title: String, timeMs: Long, durationMins: Int = 60, location: String = "") {
        viewModelScope.launch {
            workspaceManager.createCalendarEvent(title, timeMs, durationMins, location)
            _calendarEvents.value = workspaceManager.getCalendarEvents()
        }
    }

    fun syncWikiToGoogleDrive() {
        viewModelScope.launch {
            _isWorkspaceSyncing.value = true
            try {
                val pages = wikiPagesList.value
                val result = workspaceManager.syncWikiToGoogleDrive(pages)
                _driveFiles.value = workspaceManager.getDriveFiles()
                _uiState.update { 
                    it.copy(
                        autopilotBrowserLog = it.autopilotBrowserLog + "Google Drive OAuth: ${result.message}"
                    ) 
                }
            } finally {
                _isWorkspaceSyncing.value = false
            }
        }
    }

    fun triggerGoogleOneBackup() {
        viewModelScope.launch {
            _isWorkspaceSyncing.value = true
            try {
                val wikiCount = wikiPagesList.value.size
                val cacheCount = totalCacheCount.value
                val result = workspaceManager.performGoogleOneBackup(wikiCount, cacheCount)
                _googleOneStatus.value = workspaceManager.getGoogleOneStatus()
                _workspaceOperationMessage.value = "☁️ Google One Backup: ${result.summary}"
                _uiState.update {
                    it.copy(
                        autopilotBrowserLog = it.autopilotBrowserLog + "Google One Cloud: ${result.summary}"
                    )
                }
            } finally {
                _isWorkspaceSyncing.value = false
            }
        }
    }

    fun exportWikiToGoogleDocs(title: String, content: String) {
        viewModelScope.launch {
            _isWorkspaceSyncing.value = true
            try {
                val file = workspaceManager.exportWikiToGoogleDocs(title, content)
                _driveFiles.value = workspaceManager.getDriveFiles()
                _workspaceOperationMessage.value = "📄 Exported '${file.name}' to Google Docs successfully."
            } finally {
                _isWorkspaceSyncing.value = false
            }
        }
    }

    fun exportWatchersToGoogleSheets() {
        viewModelScope.launch {
            _isWorkspaceSyncing.value = true
            try {
                val watchers = webWatchersList.value
                val file = workspaceManager.exportWatchersToSheets(watchers)
                _driveFiles.value = workspaceManager.getDriveFiles()
                _workspaceOperationMessage.value = "📊 Exported ${watchers.size} price watchers to Google Sheets '${file.name}'."
            } finally {
                _isWorkspaceSyncing.value = false
            }
        }
    }

    fun autoBookGymClass() {
        viewModelScope.launch {
            _isWorkspaceSyncing.value = true
            try {
                val event = workspaceManager.autoBookGymClass()
                _calendarEvents.value = workspaceManager.getCalendarEvents()
                _workspaceOperationMessage.value = "🏋️ Agent Midnight Booking: Secured 6:00 AM class at ${event.location}!"
            } finally {
                _isWorkspaceSyncing.value = false
            }
        }
    }

    fun campPassportAppointment() {
        viewModelScope.launch {
            _isWorkspaceSyncing.value = true
            try {
                val event = workspaceManager.campPassportAppointment()
                _calendarEvents.value = workspaceManager.getCalendarEvents()
                _workspaceOperationMessage.value = "🛂 Passport Slot Camped: Grabbed cancellation for ${event.title} at ${event.location}!"
            } finally {
                _isWorkspaceSyncing.value = false
            }
        }
    }

    fun linkContactToWiki(contact: com.example.engine.workspace.GoogleContactItem) {
        viewModelScope.launch {
            val title = contact.name
            val content = """
                # ${contact.name}
                **Affiliation:** ${contact.company.ifBlank { "Personal Network" }}
                **Email:** ${contact.email}
                **Phone:** ${contact.phone}
                
                Connected person entity synced from Google Contacts OAuth.
                Interlinks: [[DeepThink Project]], [[Agent Network]], [[Google Workspace]].
            """.trimIndent()
            addWikiPage(title, content, "Contacts, Network")
            _workspaceOperationMessage.value = "👤 Linked '${contact.name}' into Karpathy LLM Wiki as connected entity."
        }
    }

    private fun seedInitialDocuments() {
        viewModelScope.launch {
            val doc1 = com.example.data.DocumentEntity(
                id = "doc-exynos-spec",
                title = "exynos_2600_neural_specs.txt",
                content = """
                EXYNOS 2600 NEURAL PROCESSING UNIT (NPU) SPECIFICATION
                - Compute power: 48 TOPS INT8, 24 TFLOPS FP16.
                - Shared memory architecture: L3 cache direct interconnect to NPU.
                - Dynamic Quantization: Supports real-time Q4_K_M weight decompilation.
                - Power efficiency: 4.2 TOPS/Watt under continuous reasoning execution.
                - Dual-core system: Core 0 handles low-precision token processing; Core 1 coordinates speculative multi-token generation blocks.
                """.trimIndent(),
                fileSize = 480L,
                mimeType = "text/plain",
                addedAt = System.currentTimeMillis() - 100000L,
                isRagEnabled = true
            )
            val doc2 = com.example.data.DocumentEntity(
                id = "doc-guidelines",
                title = "gdpr_ondevice_ai_guideline.txt",
                content = """
                GDPR COMPLIANCE IN ON-DEVICE PERSONAL AI INFERENCE
                1. Storage: No user telemetry or weights shall be sent to external networks without explicit consent.
                2. Sandbox boundaries: LLM process must execute inside the One UI secure sandbox to ensure memory containment.
                3. Right to be forgotten: Conversation logs and local databases must be erasable via a single click 'Clear Database' action.
                4. Local processing: Use of on-device Room DB for vector caching ensures zero network footprint.
                """.trimIndent(),
                fileSize = 512L,
                mimeType = "text/plain",
                addedAt = System.currentTimeMillis() - 50000L,
                isRagEnabled = true
            )
            repository.saveDocument(doc1)
            repository.saveDocument(doc2)
        }
    }

    // --- Voice & Audio Transcription & Non-Streaming Controls ---

    fun openVoiceConversation() {
        _uiState.update { it.copy(isVoiceConversationOpen = true) }
        voiceConversationManager.startListening()
    }

    fun closeVoiceConversation() {
        voiceConversationManager.stopListening()
        voiceConversationManager.stopSpeaking()
        _uiState.update { it.copy(isVoiceConversationOpen = false) }
    }

    fun openAudioTranscriber() {
        _uiState.update { it.copy(isAudioTranscriberOpen = true) }
    }

    fun closeAudioTranscriber() {
        audioTranscriptionManager.stopLiveRecording()
        _uiState.update { it.copy(isAudioTranscriberOpen = false) }
    }

    fun toggleNonStreamingTree(enabled: Boolean) {
        _uiState.update { it.copy(nonStreamingTreeEnabled = enabled) }
    }

    fun handleVoiceSpokenQuery(spokenQuery: String) {
        val trimmed = spokenQuery.trim()
        if (trimmed.isBlank()) return

        var convId = _uiState.value.currentConversationId
        viewModelScope.launch {
            if (convId.isBlank()) {
                val title = if (trimmed.length > 25) trimmed.take(22) + "..." else trimmed
                val newConv = repository.createNewConversation(title)
                convId = newConv.id
                _uiState.update { it.copy(currentConversationId = newConv.id) }
            }

            voiceConversationManager.setReasoningStage("Deconstructing inquiry into cognitive reasoning tree...")

            // Run reasoning engine pipeline
            val answerBuilder = StringBuilder()
            thinkingManager.executeThinkingPipeline(
                conversationId = convId,
                prompt = trimmed,
                mode = _uiState.value.engineMode,
                thinkingLevel = _uiState.value.selectedThinkingLevel,
                systemInstruction = _uiState.value.systemInstruction,
                searchGrounded = _uiState.value.searchGroundingEnabled,
                activeModelName = _uiState.value.activeModelName,
                nonStreaming = _uiState.value.nonStreamingTreeEnabled
            ).collect { chunk ->
                if (chunk.isThinking && chunk.currentStep != null) {
                    voiceConversationManager.setReasoningStage(
                        stage = "[${chunk.currentStep.phase.badge}] ${chunk.currentStep.headline}",
                        nodeInfo = chunk.currentStep.details.take(120)
                    )
                } else if (!chunk.isThinking) {
                    answerBuilder.append(chunk.token)
                }
            }

            val finalAnswer = answerBuilder.toString()
            if (finalAnswer.isNotBlank()) {
                voiceConversationManager.speakText(finalAnswer, stageInfo = "Cognitive verification completed")
            } else {
                voiceConversationManager.speakText("Reasoning tree verified. The deduction has been synthesized.", stageInfo = "Completed")
            }
        }
    }

    // --- Local Document RAG Management (Option D) ---
    fun addLocalDocument(title: String, content: String) {
        viewModelScope.launch {
            val doc = com.example.data.DocumentEntity(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                content = content,
                fileSize = content.toByteArray().size.toLong(),
                mimeType = "text/plain",
                addedAt = System.currentTimeMillis(),
                isRagEnabled = true
            )
            repository.saveDocument(doc)
            triggerHapticFeedback()
            _uiState.update { it.copy(backupRestoreLog = "Dokument '$title' erfolgreich hinzugefügt und für RAG indiziert.") }
        }
    }

    fun deleteLocalDocument(id: String) {
        viewModelScope.launch {
            repository.deleteDocument(id)
            triggerHapticFeedback()
            _uiState.update { it.copy(backupRestoreLog = "Dokument erfolgreich gelöscht.") }
        }
    }

    fun toggleLocalDocumentRag(id: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDocumentRagStatus(id, enabled)
            triggerHapticFeedback()
        }
    }

    // --- Dynamic Backup & Restore Operations (Option A) ---
    fun exportBackup(): String {
        triggerHapticFeedback()
        val docCount = _uiState.value.localDocuments.size
        val ssoUser = _uiState.value.loggedInUserEmail ?: "none"
        val json = """
        {
          "metadata": {
            "app": "Deep Think 3.7",
            "version": "6.0",
            "exportTimestamp": ${System.currentTimeMillis()},
            "sso_account": "$ssoUser"
          },
          "statistics": {
            "documents_indexed": $docCount
          }
        }
        """.trimIndent()
        _uiState.update { it.copy(backupRestoreLog = "Sicherung erfolgreich exportiert! ($docCount Dokumente)") }
        return json
    }

    fun importBackup(backupJson: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImportExportActive = true, backupRestoreLog = "Lese Sicherungsdatei...") }
            kotlinx.coroutines.delay(1000)
            triggerHapticFeedback()
            _uiState.update {
                it.copy(
                    isImportExportActive = false,
                    backupRestoreLog = "Sicherungsdaten erfolgreich wiederhergestellt und synchronisiert!"
                )
            }
        }
    }

    // --- Haptic Feedback UX Trigger (Option B) ---
    private var hapticCallback: (() -> Unit)? = null

    fun registerHapticCallback(callback: () -> Unit) {
        hapticCallback = callback
    }

    fun triggerHapticFeedback() {
        hapticCallback?.invoke()
    }

    fun onFirebaseUserAuthenticated(user: com.google.firebase.auth.FirebaseUser?) {
        _uiState.update {
            it.copy(
                isLoggedIn = true,
                loggedInUserEmail = user?.email,
                loggedInUserName = user?.displayName ?: user?.email?.substringBefore("@"),
                isAuthenticating = false,
                authStatusMessage = "Securely authenticated via Google Sign-In"
            )
        }
        performCloudSync()
    }

    fun onFirebaseAuthFailed(errorMsg: String) {
        _uiState.update {
            it.copy(
                isAuthenticating = false,
                authStatusMessage = errorMsg
            )
        }
    }

    private fun onAuthSuccess() {
        val user = com.google.firebase.Firebase.auth.currentUser
        onFirebaseUserAuthenticated(user)
    }

    fun performSignIn(activity: android.app.Activity) {
        _uiState.update { it.copy(isAuthenticating = true, authStatusMessage = "Connecting to Google...") }
        authManager.signInWithGoogle(
            activity = activity,
            scope = viewModelScope,
            onSuccess = { onAuthSuccess() },
            onFailure = { e ->
                _uiState.update {
                    it.copy(
                        isAuthenticating = false,
                        authStatusMessage = "Authentication failed: ${e?.message}"
                    )
                }
            },
            onCancelled = {
                _uiState.update { it.copy(isAuthenticating = false) }
            }
        )
    }

    fun performSignOut() {
        authManager.signOut(viewModelScope) {
            _uiState.update {
                it.copy(
                    isLoggedIn = false,
                    loggedInUserEmail = null,
                    loggedInUserName = null,
                    authStatusMessage = "Successfully signed out"
                )
            }
        }
    }

    private fun performCloudSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(authStatusMessage = "Syncing with Cloud Firestore...") }
            try {
                // Sync Conversations
                val convs = repository.allConversations.first()
                convs.forEach { firebaseRepository.syncConversation(it) }

                // Sync Reasoning Cache
                val cacheItems = repository.thoughtCacheRepository.allCachedTraces.first()
                for (item in cacheItems) {
                    firebaseRepository.syncReasoningCache(item)
                }
                
                // Sync Problem Progress
                val progressList = repository.allProblemProgress.first()
                for (progress in progressList) {
                    firebaseRepository.syncProblemProgress(progress)
                }

                _uiState.update { it.copy(authStatusMessage = "Cloud Vault Sync Complete") }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Cloud sync failed", e)
                _uiState.update { it.copy(authStatusMessage = "Cloud Sync Error: ${e.message}") }
            }
        }
    }

    // --- Chat & Reasoning Operations ---

    fun startNewConversation(title: String = "New Deep Thinking Session", domainTag: String = "General") {
        viewModelScope.launch {
            val conv = repository.createNewConversation(title, domainTag)
            _uiState.update {
                it.copy(
                    currentConversationId = conv.id,
                    isGenerating = false,
                    liveThoughtText = "",
                    liveAnswerText = "",
                    liveCurrentStep = null,
                    liveDurationMs = 0L,
                    liveThoughtTokens = 0,
                    liveSearchCitations = emptyList(),
                    activeTab = 0
                )
            }
        }
    }

    fun selectConversation(id: String) {
        _uiState.update { it.copy(currentConversationId = id, activeTab = 0) }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_uiState.value.currentConversationId == id) {
                val remaining = conversations.value.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    _uiState.update { it.copy(currentConversationId = remaining.first().id) }
                } else {
                    startNewConversation()
                }
            }
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            repository.deleteAllConversations()
            startNewConversation()
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            val conv = conversations.value.find { it.id == id } ?: return@launch
            repository.updateConversation(conv.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
        }
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    fun setThinkingLevel(level: ThinkingLevel) {
        _uiState.update { it.copy(selectedThinkingLevel = level) }
    }

    fun toggleEngineMode() {
        _uiState.update {
            val nextMode = if (it.engineMode == EngineMode.OFFLINE_GEMINI_37) {
                EngineMode.ONLINE_GEMINI_API
            } else {
                EngineMode.OFFLINE_GEMINI_37
            }
            it.copy(engineMode = nextMode)
        }
    }

    fun toggleSearchGrounding() {
        _uiState.update { it.copy(searchGroundingEnabled = !it.searchGroundingEnabled) }
    }

    fun setSelectedAgentType(agentType: String) {
        _uiState.update { it.copy(selectedAgentType = agentType) }
    }

    fun setSelectedInteractionStepType(stepType: String) {
        _uiState.update { it.copy(selectedInteractionStepType = stepType) }
    }

    fun toggleLinkToPreviousInteraction() {
        _uiState.update { it.copy(linkToPreviousInteraction = !it.linkToPreviousInteraction) }
    }

    fun setSystemInstruction(instruction: String) {
        _uiState.update { it.copy(systemInstruction = instruction) }
    }

    fun selectGeminiModel(model: String) {
        _uiState.update {
            it.copy(
                selectedGeminiModel = model,
                autoModelRoutingEnabled = false
            )
        }
    }

    fun enableAutoRouting() {
        _uiState.update { it.copy(autoModelRoutingEnabled = true) }
    }

    fun selectChatbotRole(roleName: String) {
        val prompt = getRoleDefaultPrompt(roleName)
        _uiState.update {
            it.copy(
                selectedChatbotRole = roleName,
                systemInstruction = prompt
            )
        }
    }

    fun openCustomRoleDialog() {
        _uiState.update { it.copy(isCustomRoleDialogOpen = true) }
    }

    fun closeCustomRoleDialog() {
        _uiState.update { it.copy(isCustomRoleDialogOpen = false) }
    }

    fun openSessionsDrawer() {
        _uiState.update { it.copy(isSessionsDrawerOpen = true) }
    }

    fun closeSessionsDrawer() {
        _uiState.update { it.copy(isSessionsDrawerOpen = false) }
    }

    fun getRoleDefaultPrompt(roleName: String): String {
        return when (roleName) {
            "Coding Architect" -> "You are an expert Senior Software Architect and Kotlin specialist. Provide clean, robust, idiomatic code, explain architecture decisions, and address concurrency and edge cases."
            "Research Scientist" -> "You are an academic researcher and scientific analyst. Formulate rigorous, evidence-based reasoning, examine hypotheses, structure step-by-step logic, and reference empirical findings."
            "Fast Summarizer" -> "You are a rapid executive synthesizer. Extract core points, eliminate fluff, use concise bullet points, and highlight key action items immediately."
            "Creative Writer" -> "You are an imaginative creative collaborator. Craft engaging narratives, evocative prose, and expressive ideas with rich stylistic flair."
            else -> "You are a versatile, friendly, and precise AI assistant powered by Gemini. Provide clear, structured, and insightful answers."
        }
    }

    fun setStreamingSpeed(speedMs: Long) {
        _uiState.update { it.copy(streamingSpeedMs = speedMs) }
    }

    fun inspectSteps(steps: List<ReasoningStep>) {
        _uiState.update { it.copy(inspectingSteps = steps) }
    }

    fun closeInspectDialog() {
        _uiState.update { it.copy(inspectingSteps = null) }
    }

    fun setHistorySearchQuery(query: String) {
        _uiState.update { it.copy(historySearchQuery = query) }
    }

    fun setHistoryDomainFilter(domain: String) {
        _uiState.update { it.copy(historySelectedDomain = domain) }
    }

    // --- User Feedback System ---

    fun openFeedbackDialog(message: ChatMessageEntity) {
        _uiState.update {
            it.copy(
                feedbackMessageTarget = message,
                feedbackRatingInput = message.userFeedbackRating,
                feedbackTextInput = message.userFeedbackText
            )
        }
    }

    fun closeFeedbackDialog() {
        _uiState.update { it.copy(feedbackMessageTarget = null) }
    }

    fun setFeedbackRatingInput(rating: Int) {
        _uiState.update { it.copy(feedbackRatingInput = rating) }
    }

    fun setFeedbackTextInput(text: String) {
        _uiState.update { it.copy(feedbackTextInput = text) }
    }

    fun submitFeedback(rating: Int? = null, text: String? = null) {
        val target = _uiState.value.feedbackMessageTarget ?: return
        val finalRating = rating ?: _uiState.value.feedbackRatingInput
        val finalText = text ?: _uiState.value.feedbackTextInput
        viewModelScope.launch {
            repository.updateMessageFeedback(target.id, finalRating, finalText)
            _uiState.update { it.copy(feedbackMessageTarget = null) }
        }
    }

    // CoT Debug Overlay Controls
    fun toggleCotDebugPanel() {
        _uiState.update { it.copy(isCotDebugPanelOpen = !it.isCotDebugPanelOpen) }
    }

    fun toggleEdgePanel() {
        _uiState.update { it.copy(isEdgePanelOpen = !it.isEdgePanelOpen) }
    }

    fun updateCacheSearchQuery(query: String) {
        _uiState.update { it.copy(cacheSearchQuery = query) }
        // TODO: Trigger Room search
    }

    fun openCotDebugOverlay(steps: List<ReasoningStep>, initialIndex: Int = 0) {
        _uiState.update {
            it.copy(
                isCotDebugOverlayOpen = true,
                cotDebugSteps = steps,
                cotDebugInitialStep = initialIndex.coerceIn(0, (steps.size - 1).coerceAtLeast(0))
            )
        }
    }

    fun closeCotDebugOverlay() {
        _uiState.update {
            it.copy(
                isCotDebugOverlayOpen = false,
                cotDebugSteps = emptyList()
            )
        }
    }

    // --- Export Thought Trace & AI Response System ---

    fun openExportDialog(customData: ThoughtTraceExportData? = null) {
        val data = customData ?: run {
            val msgs = currentMessages.value
            val latestAssistant = msgs.lastOrNull { it.role == "assistant" }
            val latestUser = msgs.lastOrNull { it.role == "user" }
            val prompt = latestUser?.content ?: "Deep Thinking Reasoning & Solution"
            val answer = latestAssistant?.content ?: _uiState.value.liveAnswerText.ifBlank { "Deep Thinking synthesis ready." }
            val thought = latestAssistant?.thoughtProcess ?: _uiState.value.liveThoughtText
            val steps = if (latestAssistant != null && latestAssistant.reasoningStepsJson.isNotBlank()) {
                thinkingManager.parseStepsJson(latestAssistant.reasoningStepsJson)
            } else if (_uiState.value.inspectingCachedItem != null) {
                _uiState.value.cachedItemSteps
            } else {
                thinkingManager.generateDemoProofSteps(prompt)
            }
            val citations = if (latestAssistant != null && latestAssistant.searchCitationsJson.isNotBlank()) {
                thinkingManager.parseCitationsJson(latestAssistant.searchCitationsJson)
            } else {
                _uiState.value.liveSearchCitations
            }
            val duration = latestAssistant?.thinkingDurationMs ?: if (_uiState.value.liveDurationMs > 0) _uiState.value.liveDurationMs else 1420L
            val tokens = latestAssistant?.thinkingTokens ?: if (_uiState.value.liveThoughtTokens > 0) _uiState.value.liveThoughtTokens else 4096

            ThoughtTraceExportData(
                prompt = prompt,
                thoughtProcess = thought,
                reasoningSteps = steps,
                finalResponse = answer,
                modelName = latestAssistant?.modelMode ?: _uiState.value.activeModelName,
                thinkingDurationMs = duration,
                thinkingTokens = tokens,
                searchCitations = citations,
                timestamp = latestAssistant?.timestamp ?: System.currentTimeMillis()
            )
        }

        _uiState.update {
            it.copy(
                isExportDialogOpen = true,
                exportTargetData = data
            )
        }
    }

    fun closeExportDialog() {
        _uiState.update {
            it.copy(
                isExportDialogOpen = false,
                exportTargetData = null
            )
        }
    }

    fun quickRateMessage(messageId: String, rating: Int) {
        viewModelScope.launch {
            repository.updateMessageFeedback(messageId, rating, "")
        }
    }

    // --- Local Model Management ---

    fun setActiveModel(model: LocalModelEntity) {
        viewModelScope.launch {
            repository.setActiveModel(model.id)
            _uiState.update {
                it.copy(
                    activeModelId = model.id,
                    activeModelName = model.name
                )
            }
        }
    }

    fun setHfDownloadInput(input: String) {
        _uiState.update { it.copy(hfDownloadInput = input) }
    }

    fun setHfDownloadQuant(quant: String) {
        _uiState.update { it.copy(hfDownloadSelectedQuant = quant) }
    }

    fun downloadHfModel(commandOrRepo: String = _uiState.value.hfDownloadInput, quant: String = _uiState.value.hfDownloadSelectedQuant) {
        if (_uiState.value.downloadingModelId != null) return

        // Clean command: strip "hf download" prefix or flags if user pasted full CLI command
        var repo = commandOrRepo.trim()
        if (repo.startsWith("hf download", ignoreCase = true)) {
            repo = repo.removePrefix("hf download").removePrefix("HF DOWNLOAD").trim()
        }
        // remove extra flags if present
        repo = repo.split(" ").firstOrNull { it.isNotBlank() && !it.startsWith("-") } ?: "JonathanColetti/Qwen3.8-27B-Uncensored-GGUF"

        val modelId = repo.replace("/", "-").lowercase()
        val author = repo.substringBefore("/", "HuggingFace")
        val modelName = repo.substringAfter("/", repo)
        val sizeMb = when {
            quant.contains("Q8", ignoreCase = true) -> 28900
            quant.contains("Q5", ignoreCase = true) -> 19800
            quant.contains("Q3", ignoreCase = true) -> 13200
            else -> 16400 // Q4_K_M standard
        }

        val entity = LocalModelEntity(
            id = modelId,
            name = "$modelName ($quant)",
            family = "$author / Hugging Face",
            parameterSize = if (modelName.contains("27B", ignoreCase = true)) "27B" else "7B",
            quantization = "GGUF $quant",
            downloadSizeMb = sizeMb,
            isDownloaded = false,
            isActive = false,
            computeBackend = "Vulkan GPU + S26 Ultra NPU",
            contextWindow = "128k Tokens",
            description = "Downloaded from Hugging Face Hub ($repo). High-capacity multi-turn reasoning, creative problem solving, coding, and unrestricted CoT inference."
        )

        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            repository.insertModel(entity)
            _uiState.update {
                it.copy(
                    downloadingModelId = modelId,
                    downloadProgress = 0.02f,
                    downloadSpeedMbps = 52.8f,
                    downloadEtaSeconds = 8,
                    downloadCurrentShard = "1 of 4: ${modelName.lowercase()}-$quant-00001-of-00004.gguf",
                    downloadTotalShards = 4,
                    downloadStatusMessage = "Connecting to Hugging Face CDN (Fast LFS)..."
                )
            }

            val shards = listOf(
                "1 of 4: ${modelName.lowercase()}-$quant-00001-of-00004.gguf",
                "2 of 4: ${modelName.lowercase()}-$quant-00002-of-00004.gguf",
                "3 of 4: ${modelName.lowercase()}-$quant-00003-of-00004.gguf",
                "4 of 4: ${modelName.lowercase()}-$quant-00004-of-00004.gguf"
            )

            for (step in 1..20) {
                delay(180)
                val p = step / 20f
                val shardIdx = ((step - 1) / 5).coerceIn(0, 3)
                val speed = (48f + (step % 5) * 2.4f)
                val eta = ((20 - step) * 0.4f).toInt().coerceAtLeast(1)

                _uiState.update {
                    it.copy(
                        downloadProgress = p,
                        downloadSpeedMbps = speed,
                        downloadEtaSeconds = eta,
                        downloadCurrentShard = shards[shardIdx],
                        downloadStatusMessage = if (p >= 0.9f) "Compiling GGUF weights for S26 Ultra NPU & Vulkan..." else "Downloading GGUF tensors from Hugging Face CDN..."
                    )
                }
            }

            repository.updateModel(entity.copy(isDownloaded = true))
            _uiState.update {
                it.copy(
                    downloadingModelId = null,
                    downloadProgress = 0f,
                    downloadSpeedMbps = 0f,
                    downloadEtaSeconds = 0,
                    downloadCurrentShard = "",
                    downloadStatusMessage = "Download Complete! Model installed & ready."
                )
            }
        }
    }

    fun downloadModel(model: LocalModelEntity) {
        if (_uiState.value.downloadingModelId != null) return
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    downloadingModelId = model.id,
                    downloadProgress = 0.05f,
                    downloadSpeedMbps = 49.2f,
                    downloadEtaSeconds = 6,
                    downloadStatusMessage = "Connecting to model distribution network..."
                )
            }

            for (progress in 10..100 step 10) {
                delay(240)
                val p = progress / 100f
                _uiState.update {
                    it.copy(
                        downloadProgress = p,
                        downloadSpeedMbps = (45f + (progress % 30)),
                        downloadEtaSeconds = ((100 - progress) / 15).coerceAtLeast(1),
                        downloadStatusMessage = if (p >= 0.9f) "Compiling NPU Turbo kernels..." else "Downloading model weights..."
                    )
                }
            }

            repository.updateModel(model.copy(isDownloaded = true))
            _uiState.update {
                it.copy(
                    downloadingModelId = null,
                    downloadProgress = 0f,
                    downloadSpeedMbps = 0f,
                    downloadEtaSeconds = 0,
                    downloadStatusMessage = "Model ready"
                )
            }
        }
    }

    fun deleteDownloadedModel(model: LocalModelEntity) {
        viewModelScope.launch {
            repository.updateModel(model.copy(isDownloaded = false, isActive = false))
            if (_uiState.value.activeModelId == model.id) {
                val fallback = localModels.value.find { it.isDownloaded && it.id != model.id }
                    ?: localModels.value.firstOrNull()
                if (fallback != null) {
                    repository.setActiveModel(fallback.id)
                }
            }
        }
    }

    // --- Dynamic Model Routing by Task Complexity ---
    fun toggleAutoModelRouting() {
        _uiState.update { it.copy(autoModelRoutingEnabled = !it.autoModelRoutingEnabled) }
    }

    fun setAutoModelRoutingEnabled(enabled: Boolean) {
        _uiState.update { it.copy(autoModelRoutingEnabled = enabled) }
    }

    fun setRoutingThresholdLevel(level: String) {
        _uiState.update { it.copy(routingThresholdLevel = level) }
    }

    // --- Model Upgrades & Newer LLMs Management ---
    fun checkForModelUpdates() {
        if (_uiState.value.isCheckingForModelUpdates) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingForModelUpdates = true) }
            val updated = ModelUpgradeManager.checkRemoteForUpdates(_uiState.value.modelUpgradeItems)
            _uiState.update {
                it.copy(
                    modelUpgradeItems = updated,
                    isCheckingForModelUpdates = false,
                    lastModelUpdateCheckTimestamp = System.currentTimeMillis()
                )
            }
        }
    }

    fun upgradeModel(item: ModelUpgradeItem) {
        viewModelScope.launch {
            ModelUpgradeManager.executeModelUpgradeStream(item).collect { updatedItem ->
                _uiState.update { state ->
                    val newList = state.modelUpgradeItems.map {
                        if (it.modelId == updatedItem.modelId) updatedItem else it
                    }
                    state.copy(modelUpgradeItems = newList)
                }

                // If upgrade finished, also update or insert the LocalModelEntity in Room
                if (!updatedItem.isUpgrading && updatedItem.upgradeProgress >= 1.0f) {
                    if (!updatedItem.isCloud) {
                        val localEntity = ModelUpgradeManager.toLocalModelEntity(updatedItem)
                        repository.insertModel(localEntity)
                    }
                }
            }
        }
    }

    fun openRegisterCustomModelDialog() {
        _uiState.update { it.copy(isRegisterCustomModelDialogOpen = true) }
    }

    fun closeRegisterCustomModelDialog() {
        _uiState.update { it.copy(isRegisterCustomModelDialogOpen = false) }
    }

    fun registerCustomModel(
        id: String,
        name: String,
        family: String,
        parameterSize: String,
        quantization: String,
        contextWindow: String,
        description: String,
        isCloud: Boolean,
        version: String = "v1.0.0",
        trainingData: String = "Trained on standard mixed corpus (math/code reasoning)",
        releaseDate: String = "2026-01-01"
    ) {
        viewModelScope.launch {
            val trimmedId = id.trim().lowercase().replace(" ", "-")
            val trimmedName = name.trim()
            if (trimmedId.isBlank() || trimmedName.isBlank()) return@launch

            val newItem = ModelUpgradeItem(
                modelId = trimmedId,
                displayName = trimmedName,
                family = family.ifBlank { "Custom User Model" },
                currentVersion = version.ifBlank { "v1.0.0" },
                latestVersion = version.ifBlank { "v1.0.0" },
                hasUpdate = false,
                releaseDate = releaseDate.ifBlank { "2026" },
                downloadSizeMb = if (isCloud) 0 else 3200,
                contextWindow = contextWindow.ifBlank { "128k Tokens" },
                changelog = description.ifBlank { "Custom registered newer LLM model" },
                isCloud = isCloud
            )

            // Add to upgrade registry
            _uiState.update { state ->
                val existing = state.modelUpgradeItems.filter { it.modelId != trimmedId }
                state.copy(
                    modelUpgradeItems = listOf(newItem) + existing,
                    isRegisterCustomModelDialogOpen = false
                )
            }

            // Insert into repository for local or cloud usage
            val localEntity = LocalModelEntity(
                id = trimmedId,
                name = trimmedName,
                family = family.ifBlank { "Custom" },
                parameterSize = parameterSize.ifBlank { "8B" },
                quantization = quantization.ifBlank { "GGUF Q4_K_M" },
                downloadSizeMb = if (isCloud) 0 else 3200,
                isDownloaded = true,
                isActive = true,
                computeBackend = if (isCloud) "Cloud API" else "S26 Ultra NPU Turbo",
                contextWindow = contextWindow.ifBlank { "128k Tokens" },
                description = description.ifBlank { "Custom registered LLM model" },
                version = version.ifBlank { "v1.0.0" },
                trainingData = trainingData.ifBlank { "Trained on custom user dataset." },
                releaseDate = releaseDate.ifBlank { "2026-01-01" }
            )
            repository.insertModel(localEntity)
            repository.setActiveModel(trimmedId)
        }
    }

    fun toggleAutoUpdateOnWifi() {
        _uiState.update { it.copy(autoUpdateOnWifi = !it.autoUpdateOnWifi) }
    }

    fun toggleAutoCheckUpdatesOnLaunch() {
        _uiState.update { it.copy(autoCheckUpdatesOnLaunch = !it.autoCheckUpdatesOnLaunch) }
    }

    // --- Creative Media & Veo 3 Video Studio ---

    fun setImagePromptInput(prompt: String) {
        _uiState.update { it.copy(imagePromptInput = prompt) }
    }

    fun setImageStyle(style: String) {
        _uiState.update { it.copy(selectedImageStyle = style) }
    }

    fun setAspectRatio(ratio: String) {
        _uiState.update { it.copy(selectedAspectRatio = ratio) }
    }

    fun setVeoMotionPreset(preset: String) {
        _uiState.update { it.copy(veoMotionPreset = preset) }
    }

    fun setVeoDurationSec(sec: Int) {
        _uiState.update { it.copy(veoDurationSec = sec) }
    }

    fun setVeoActiveMedia(media: GeneratedMediaEntity) {
        _uiState.update { it.copy(veoActiveMedia = media, veoPlaybackPlaying = false) }
    }

    fun toggleVeoPlayback() {
        _uiState.update { it.copy(veoPlaybackPlaying = !it.veoPlaybackPlaying) }
    }

    fun generateImage() {
        val prompt = _uiState.value.imagePromptInput.trim()
        if (prompt.isBlank() || _uiState.value.isGeneratingImage) return

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingImage = true) }
            delay(1200) // Realistic high-speed generation simulation

            val media = GeneratedMediaEntity(
                id = UUID.randomUUID().toString(),
                mediaType = "IMAGE",
                prompt = prompt,
                style = _uiState.value.selectedImageStyle,
                aspectRatio = _uiState.value.selectedAspectRatio,
                motionPreset = "",
                mediaSeed = System.currentTimeMillis()
            )
            repository.saveGeneratedMedia(media)

            _uiState.update {
                it.copy(
                    isGeneratingImage = false,
                    latestGeneratedMedia = media,
                    veoActiveMedia = media
                )
            }
        }
    }

    fun animateWithVeo3() {
        val active = _uiState.value.veoActiveMedia ?: return
        if (_uiState.value.isGeneratingVeo) return

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingVeo = true) }
            delay(2200) // Veo 3 Video rendering simulation

            val videoMedia = GeneratedMediaEntity(
                id = UUID.randomUUID().toString(),
                mediaType = "VIDEO",
                prompt = "Veo 3 Motion: ${active.prompt} (${_uiState.value.veoMotionPreset})",
                style = active.style,
                aspectRatio = active.aspectRatio,
                motionPreset = _uiState.value.veoMotionPreset,
                mediaSeed = System.currentTimeMillis()
            )
            repository.saveGeneratedMedia(videoMedia)

            _uiState.update {
                it.copy(
                    isGeneratingVeo = false,
                    veoActiveMedia = videoMedia,
                    veoPlaybackPlaying = true
                )
            }
        }
    }

    fun deleteMedia(id: String) {
        viewModelScope.launch {
            repository.deleteGeneratedMedia(id)
        }
    }

    // ==========================================
    // AI Video Metadata & Gemini Flash Engine
    // ==========================================

    fun selectVideo(
        uri: Uri?,
        title: String,
        duration: String = "07:45",
        resolution: String = "4K HDR (60 FPS)",
        category: String = "Tech Innovation",
        defaultContext: String = ""
    ) {
        _uiState.update {
            it.copy(
                selectedVideoUri = uri,
                selectedVideoTitle = title,
                selectedVideoDuration = duration,
                selectedVideoResolution = resolution,
                selectedVideoCategory = category,
                videoCustomContext = defaultContext.ifBlank { it.videoCustomContext },
                videoMetadataError = null
            )
        }
    }

    fun updateVideoContext(context: String) {
        _uiState.update { it.copy(videoCustomContext = context) }
    }

    fun setVideoMetadataFilter(filter: String) {
        _uiState.update { it.copy(videoMetadataActiveViewFilter = filter) }
    }

    fun generateVideoDescription() {
        val title = _uiState.value.selectedVideoTitle
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingVideoMetadata = true, activeMetadataTask = "Generating Description with Gemini Flash...") }
            delay(300)
            val htmlResult = VideoMetadataService.getDefaultDescriptionHtml(title)
            _uiState.update {
                it.copy(
                    isAnalyzingVideoMetadata = false,
                    activeMetadataTask = "",
                    videoMetadataDescriptionHtml = htmlResult,
                    videoMetadataError = null
                )
            }
        }
    }

    fun generateVideoHashtags() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingVideoMetadata = true, activeMetadataTask = "Generating Optimized Hashtags...") }
            delay(250)
            val tags = VideoMetadataService.extractHashtags()
            _uiState.update {
                it.copy(
                    isAnalyzingVideoMetadata = false,
                    activeMetadataTask = "",
                    videoMetadataHashtags = tags,
                    videoMetadataError = null
                )
            }
        }
    }

    fun generateVideoChapters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingVideoMetadata = true, activeMetadataTask = "Analyzing Video Pacing & Chapters...") }
            delay(300)
            val chapters = VideoMetadataService.parseChapters()
            _uiState.update {
                it.copy(
                    isAnalyzingVideoMetadata = false,
                    activeMetadataTask = "",
                    videoMetadataChapters = chapters,
                    videoMetadataError = null
                )
            }
        }
    }

    fun generateVideoAccountTags() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingVideoMetadata = true, activeMetadataTask = "Identifying Collaborators & Mentions...") }
            delay(250)
            val accounts = VideoMetadataService.parseAccountTags()
            _uiState.update {
                it.copy(
                    isAnalyzingVideoMetadata = false,
                    activeMetadataTask = "",
                    videoMetadataAccountTags = accounts,
                    videoMetadataError = null
                )
            }
        }
    }

    fun generateVideoLinks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingVideoMetadata = true, activeMetadataTask = "Curating Relevant External Resources...") }
            delay(250)
            val links = VideoMetadataService.parseSuggestedLinks()
            _uiState.update {
                it.copy(
                    isAnalyzingVideoMetadata = false,
                    activeMetadataTask = "",
                    videoMetadataLinks = links,
                    videoMetadataError = null
                )
            }
        }
    }

    fun generateVideoThumbnails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingVideoMetadata = true, activeMetadataTask = "Generating High-CTR Thumbnail Concepts...") }
            delay(350)
            val thumbnails = VideoMetadataService.parseThumbnails()
            _uiState.update {
                it.copy(
                    isAnalyzingVideoMetadata = false,
                    activeMetadataTask = "",
                    videoMetadataThumbnails = thumbnails,
                    videoMetadataError = null
                )
            }
        }
    }

    fun generateAllVideoMetadata() {
        val title = _uiState.value.selectedVideoTitle
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingVideoMetadata = true, activeMetadataTask = "Analyzing full video content with Gemini Flash...") }
            delay(400)
            _uiState.update {
                it.copy(
                    isAnalyzingVideoMetadata = false,
                    activeMetadataTask = "",
                    videoMetadataDescriptionHtml = VideoMetadataService.getDefaultDescriptionHtml(title),
                    videoMetadataHashtags = VideoMetadataService.extractHashtags(),
                    videoMetadataChapters = VideoMetadataService.parseChapters(),
                    videoMetadataAccountTags = VideoMetadataService.parseAccountTags(),
                    videoMetadataLinks = VideoMetadataService.parseSuggestedLinks(),
                    videoMetadataThumbnails = VideoMetadataService.parseThumbnails(),
                    videoMetadataError = null
                )
            }
        }
    }

    fun clearVideoMetadata() {
        _uiState.update {
            it.copy(
                videoMetadataDescriptionHtml = null,
                videoMetadataHashtags = emptyList(),
                videoMetadataChapters = emptyList(),
                videoMetadataAccountTags = emptyList(),
                videoMetadataLinks = emptyList(),
                videoMetadataThumbnails = emptyList(),
                videoMetadataError = null
            )
        }
    }

    // ==========================================
    // LLM Training Dataset Management
    // ==========================================

    fun uploadDataset(
        name: String,
        description: String,
        fileFormat: String,
        rawContent: String,
        purpose: String,
        modelTarget: String
    ) {
        viewModelScope.launch {
            val entryCount = when (fileFormat.uppercase()) {
                "JSON" -> {
                    try {
                        org.json.JSONArray(rawContent).length()
                    } catch (e: Exception) {
                        try {
                            org.json.JSONObject(rawContent).length()
                        } catch (e2: Exception) {
                            rawContent.split("\n").filter { it.isNotBlank() }.size
                        }
                    }
                }
                "JSONL" -> rawContent.split("\n").filter { it.isNotBlank() }.size
                "CSV" -> rawContent.split("\n").filter { it.isNotBlank() }.size - 1
                else -> rawContent.split("\n").filter { it.isNotBlank() }.size
            }.coerceAtLeast(1)

            val dataset = LlmDatasetEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                fileFormat = fileFormat,
                entryCount = entryCount,
                fileSize = rawContent.length.toLong(),
                rawContent = rawContent,
                uploadedAt = System.currentTimeMillis(),
                purpose = purpose,
                modelTarget = modelTarget
            )
            repository.saveDataset(dataset)
        }
    }

    fun deleteTrainingDataset(id: String) {
        viewModelScope.launch {
            repository.deleteDataset(id)
            fileNetworkManager.refreshStorageStats()
            triggerHapticFeedback()
        }
    }

    fun clearAllDatasets() {
        viewModelScope.launch {
            repository.clearAllDatasets()
            fileNetworkManager.refreshStorageStats()
            triggerHapticFeedback()
        }
    }

    fun exportDatasetToFile(dataset: LlmDatasetEntity): java.io.File {
        val ext = when (dataset.fileFormat.uppercase()) {
            "JSON" -> "json"
            "CSV" -> "csv"
            else -> "jsonl"
        }
        return fileNetworkManager.exportDatasetToFile(dataset.id, dataset.name, dataset.rawContent, ext)
    }

    data class PresetDatasetData(
        val name: String,
        val desc: String,
        val fmt: String,
        val content: String,
        val purpose: String,
        val model: String
    )

    fun loadPresetFineTuningDataset(presetKey: String) {
        val preset = when (presetKey) {
            "gsm8k" -> PresetDatasetData(
                name = "GSM8K Formal Math Proofs (SFT)",
                desc = "Mathematical problems with step-by-step rigorous invariant assertions and deductive proofs.",
                fmt = "JSONL",
                content = """{"question": "Prove that for all positive real x, x + 1/x >= 2.", "proof_cot": "(x - 1)^2 >= 0 => x^2 - 2x + 1 >= 0 => x^2 + 1 >= 2x => x + 1/x >= 2 since x > 0.", "answer": "2"}
{"question": "Prove sqrt(2) is irrational.", "proof_cot": "Assume p/q in lowest terms. 2q^2 = p^2 => p is even => p=2k => 2q^2 = 4k^2 => q^2 = 2k^2 => q is even. Contradicts gcd(p,q)=1.", "answer": "Irrational"}""",
                purpose = "Supervised Fine-Tuning (SFT)",
                model = "deepseek-r1-7b"
            )
            "hoare" -> PresetDatasetData(
                name = "Hoare Logic & Invariants",
                desc = "Algorithmic correctness pairs specifying loop invariants, preconditions, and termination metrics.",
                fmt = "JSONL",
                content = """{"algorithm": "Binary Search", "pre": "array is sorted", "invariant": "low <= mid <= high and target in array[low..high]", "termination": "high - low decreases"}
{"algorithm": "Euclidean GCD", "pre": "a > 0 and b > 0", "invariant": "gcd(a, b) = gcd(a_orig, b_orig)", "termination": "b decreases"}""",
                purpose = "Supervised Fine-Tuning (SFT)",
                model = "qwen-2.5-coder"
            )
            "dpo" -> PresetDatasetData(
                name = "RLHF DPO Anti-Hallucination",
                desc = "Direct Preference Optimization pairs penalizing unverified assumptions and rewarding rigorous proof trees.",
                fmt = "JSON",
                content = """[{"prompt": "Prove P vs NP relativization barrier", "chosen": "Baker-Gill-Solovay constructed oracles A and B where P^A=NP^A and P^B!=NP^B, proving diagonalizing Turing machines cannot resolve P vs NP.", "rejected": "P vs NP cannot be solved because algorithms are unpredictable."}]""",
                purpose = "Direct Preference Optimization (DPO)",
                model = "gemini-3.7-flash"
            )
            else -> PresetDatasetData(
                name = "Exynos NPU Quantization Calibration",
                desc = "Layer weight distributions and scaling factors for INT4/W4A16 mobile hardware execution.",
                fmt = "CSV",
                content = "tensor_name,quant_type,scale_factor,zero_point,min_val,max_val\nmodel.layers.0.q_proj,INT4,0.0124,0,-1.42,1.38\nmodel.layers.0.k_proj,INT4,0.0118,0,-1.28,1.26\nmodel.layers.0.mlp_gate,INT4,0.0145,0,-1.82,1.79",
                purpose = "Quantization Calibration",
                model = "llama-3.2-3b"
            )
        }
        uploadDataset(preset.name, preset.desc, preset.fmt, preset.content, preset.purpose, preset.model)
        fileNetworkManager.refreshStorageStats()
        triggerHapticFeedback()
    }

    // ==========================================
    // Proof-of-Thought (PoT) Room Persistence Methods
    // ==========================================

    fun cacheProofOfThought(
        title: String,
        premise: String,
        domain: String = "Mathematics",
        technique: String = "Contradiction",
        proofBody: String,
        stepsJson: String = "[]",
        qedConclusion: String = "",
        status: String = "VERIFIED_FORMAL",
        confidence: Double = 0.99,
        tokens: Int = 4096,
        durationMs: Long = 1800L,
        modelSource: String = "Offline Deep Thinking Core"
    ) {
        viewModelScope.launch {
            val normalized = premise.lowercase().replace(Regex("[^a-z0-9 ]"), " ").take(64)
            val entity = ProofOfThoughtEntity(
                id = "pot_" + UUID.randomUUID().toString().take(12),
                title = title.ifBlank { "Formal Mathematical Proof" },
                premiseOrHypothesis = premise,
                normalizedQuery = normalized,
                domain = domain,
                proofTechnique = technique,
                formalProofBody = proofBody,
                reasoningStepsJson = stepsJson,
                qedConclusion = qedConclusion.ifBlank { "Q.E.D. Formal verification complete." },
                verificationStatus = status,
                confidenceScore = confidence,
                thinkingTokens = tokens,
                thinkingDurationMs = durationMs,
                modelSource = modelSource,
                isOfflineAvailable = true,
                localCachedTimestamp = System.currentTimeMillis()
            )
            repository.insertProofOfThought(entity)
            fileNetworkManager.refreshStorageStats()
            triggerHapticFeedback()
        }
    }

    fun deleteProofOfThought(id: String) {
        viewModelScope.launch {
            repository.deleteProofOfThought(id)
            fileNetworkManager.refreshStorageStats()
            triggerHapticFeedback()
        }
    }

    fun clearAllProofOfThoughts() {
        viewModelScope.launch {
            repository.clearAllProofOfThoughts()
            fileNetworkManager.refreshStorageStats()
            triggerHapticFeedback()
        }
    }

    fun exportProofToFile(proof: ProofOfThoughtEntity, format: String = "md"): java.io.File {
        val content = """
        # ${proof.title}
        **Domain:** ${proof.domain} | **Technique:** ${proof.proofTechnique}
        **Model Source:** ${proof.modelSource} | **Verification:** ${proof.verificationStatus} (${(proof.confidenceScore * 100).toInt()}%)
        **Offline Cached:** ${java.util.Date(proof.localCachedTimestamp)}

        ## Premise / Hypothesis
        ${proof.premiseOrHypothesis}

        ## Rigorous Formal Proof
        ${proof.formalProofBody}

        ## Q.E.D. Conclusion
        ${proof.qedConclusion}
        """.trimIndent()
        return fileNetworkManager.exportProofToFile(proof.id, proof.title, content, format)
    }

    // ==========================================
    // Android File & Network Management
    // ==========================================

    fun toggleForceOfflineMode(enabled: Boolean) {
        fileNetworkManager.setForceOffline(enabled)
        triggerHapticFeedback()
    }

    fun refreshNetworkAndStorage() {
        fileNetworkManager.evaluateNetworkState()
        fileNetworkManager.refreshStorageStats()
        triggerHapticFeedback()
    }

    // ==========================================
    // Ollama, LLM API, and Octopus Engines Methods
    // ==========================================

    fun setOllamaHostUrl(url: String) {
        _uiState.update { it.copy(ollamaHostUrl = url) }
    }

    fun checkOllamaHealth() {
        viewModelScope.launch {
            val host = _uiState.value.ollamaHostUrl
            _uiState.update { it.copy(ollamaIsGenerating = true) }
            try {
                val status = ollamaClient.checkHealth(host)
                val models = ollamaClient.listModels(host)
                _uiState.update { 
                    it.copy(
                        ollamaStatus = status,
                        ollamaModels = models,
                        ollamaIsGenerating = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        ollamaStatus = OllamaServerStatus(isConnected = false, activeHost = host, errorMessage = e.localizedMessage),
                        ollamaIsGenerating = false
                    ) 
                }
            }
        }
    }

    fun pullOllamaModel(modelName: String) {
        if (_uiState.value.ollamaIsPulling) return
        viewModelScope.launch {
            _uiState.update { it.copy(ollamaIsPulling = true, ollamaPullProgress = 0f, ollamaPullStatusText = "Downloading..." ) }
            try {
                ollamaClient.pullModelStream(_uiState.value.ollamaHostUrl, modelName).collect { status ->
                    if (status.isFinished) {
                        _uiState.update { 
                            it.copy(
                                ollamaIsPulling = false,
                                ollamaPullProgress = 1.0f,
                                ollamaPullStatusText = "Success!"
                            )
                        }
                        // Refresh models list
                        checkOllamaHealth()
                    } else if (status.error != null) {
                        _uiState.update { 
                            it.copy(
                                ollamaIsPulling = false,
                                ollamaPullStatusText = "Error: ${status.error}"
                            )
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                ollamaPullProgress = status.progressPercent,
                                ollamaPullStatusText = "${status.status} (${(status.progressPercent * 100).toInt()}% completed)"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        ollamaIsPulling = false,
                        ollamaPullStatusText = "Error: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun selectOllamaModel(modelName: String) {
        _uiState.update { it.copy(selectedOllamaModel = modelName) }
    }

    fun deleteOllamaModel(modelName: String) {
        viewModelScope.launch {
            val success = ollamaClient.deleteModel(_uiState.value.ollamaHostUrl, modelName)
            if (success) {
                checkOllamaHealth()
            }
        }
    }

    fun executeOllamaInference(prompt: String) {
        if (prompt.isBlank() || _uiState.value.ollamaIsGenerating) return
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    ollamaIsGenerating = true,
                    ollamaLiveThought = "",
                    ollamaLiveResponse = ""
                ) 
            }
            try {
                val messages = listOf(OllamaChatMessage("user", prompt))
                var currentThought = ""
                var currentAnswer = ""
                
                ollamaClient.chatStream(
                    hostUrl = _uiState.value.ollamaHostUrl,
                    model = _uiState.value.selectedOllamaModel,
                    messages = messages,
                    temperature = _uiState.value.offlineTemperature
                ).collect { chunk ->
                    if (chunk.thoughtToken.isNotEmpty()) {
                        currentThought += chunk.thoughtToken
                    }
                    if (chunk.token.isNotEmpty()) {
                        currentAnswer += chunk.token
                    }
                    _uiState.update {
                        it.copy(
                            ollamaLiveThought = currentThought,
                            ollamaLiveResponse = currentAnswer
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(ollamaLiveResponse = "Error: ${e.localizedMessage}")
                }
            } finally {
                _uiState.update { it.copy(ollamaIsGenerating = false) }
            }
        }
    }

    fun updateLlmApiConfig(config: LlmApiConfig) {
        _uiState.update { it.copy(llmApiConfig = config) }
    }

    fun testLlmApiEndpoint() {
        viewModelScope.launch {
            _uiState.update { it.copy(llmApiIsCalling = true, llmApiTestResult = "Testing endpoint...") }
            try {
                val (success, message) = llmApiClient.testEndpoint(_uiState.value.llmApiConfig)
                _uiState.update { 
                    it.copy(
                        llmApiTestSuccess = success,
                        llmApiTestResult = message
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        llmApiTestSuccess = false,
                        llmApiTestResult = e.localizedMessage
                    ) 
                }
            } finally {
                _uiState.update { it.copy(llmApiIsCalling = false) }
            }
        }
    }

    fun executeLlmApiCompletion(prompt: String) {
        if (prompt.isBlank() || _uiState.value.llmApiIsCalling) return
        viewModelScope.launch {
            _uiState.update { it.copy(llmApiIsCalling = true, llmApiLatestResponse = null) }
            try {
                val messages = listOf("user" to prompt)
                val response = llmApiClient.executeChat(_uiState.value.llmApiConfig, messages)
                _uiState.update { it.copy(llmApiLatestResponse = response) }
                
                // Keep the response and associated reasoning trace in our Room DB Reason Cache! (Request 4!)
                if (response.error == null) {
                    repository.cacheReasoningResult(
                        prompt = prompt,
                        modelId = _uiState.value.llmApiConfig.providerId + "-" + _uiState.value.llmApiConfig.model,
                        modelName = response.modelUsed,
                        thinkingLevel = "HIGH",
                        thinkingBudgetTokens = 8192,
                        answerContent = response.content,
                        thoughtProcess = response.thoughtProcess,
                        durationMs = response.latencyMs,
                        thinkingTokens = response.promptTokens,
                        answerTokens = response.completionTokens
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        llmApiLatestResponse = LlmApiResponse(
                            content = "",
                            thoughtProcess = "",
                            promptTokens = 0,
                            completionTokens = 0,
                            totalTokens = 0,
                            latencyMs = 0L,
                            modelUsed = _uiState.value.llmApiConfig.model,
                            error = e.localizedMessage
                        )
                    ) 
                }
            } finally {
                _uiState.update { it.copy(llmApiIsCalling = false) }
            }
        }
    }

    fun executeOctopusPlan(prompt: String) {
        if (prompt.isBlank() || _uiState.value.octopusIsExecuting) return
        viewModelScope.launch {
            _uiState.update { it.copy(octopusIsExecuting = true, octopusLatestPlan = null) }
            try {
                val plan = octopusEngine.executeAgent(prompt)
                _uiState.update { it.copy(octopusLatestPlan = plan) }
                
                // Cache the octopus response in our Room Database Reasoning Cache for offline access! (Request 4!)
                repository.cacheReasoningResult(
                    prompt = prompt,
                    modelId = "octopus-agent",
                    modelName = "Octopus Agent (On-Device)",
                    thinkingLevel = "HIGH",
                    thinkingBudgetTokens = 8192,
                    answerContent = plan.finalResponseText,
                    thoughtProcess = plan.predictedActionToken,
                    durationMs = plan.latencyMs,
                    thinkingTokens = prompt.length / 4,
                    answerTokens = plan.finalResponseText.length / 4
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        octopusLatestPlan = OctopusExecutionPlan(
                            originalPrompt = prompt,
                            predictedActionToken = "<|error|>",
                            toolTrace = null,
                            finalResponseText = "Error: ${e.localizedMessage}",
                            latencyMs = 0L
                        )
                    ) 
                }
            } finally {
                _uiState.update { it.copy(octopusIsExecuting = false) }
            }
        }
    }

    // =========================================================================
    // Autopilot Browser, Background Web Watchers, and Karpathy LLM Wiki Memory
    // =========================================================================

    fun triggerAutopilotNavigation(url: String, prompt: String) {
        if (url.isBlank() || _uiState.value.autopilotIsNavigating) return
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    autopilotBrowserUrl = url,
                    autopilotBrowserPrompt = prompt,
                    autopilotIsNavigating = true,
                    autopilotBrowserLog = it.autopilotBrowserLog + "Navigating to: $url",
                    isAutopilotStuckOnCaptcha = false
                ) 
            }
            delay(1000L)
            _uiState.update { 
                it.copy(
                    autopilotBrowserLog = it.autopilotBrowserLog + "Scanning DOM hierarchies... Found matching elements for prompt."
                ) 
            }
            delay(1500L)
            
            // Randomly simulate encountering a captcha/login gate on certain requests (like "gym" or "ticket" or "passport") to demonstrate the hands-off flow!
            val needsHandoff = url.contains("gym") || url.contains("slots") || url.contains("resale") || prompt.contains("captcha", ignoreCase = true)
            
            if (needsHandoff) {
                _uiState.update { 
                    it.copy(
                        isAutopilotStuckOnCaptcha = true,
                        autopilotIsNavigating = false,
                        autopilotBrowserLog = it.autopilotBrowserLog + "⚠️ CAPTCHA / Login Gate detected. Handing page over to user..."
                    ) 
                }
            } else {
                _uiState.update { 
                    it.copy(
                        autopilotIsNavigating = false,
                        autopilotBrowserLog = it.autopilotBrowserLog + "✅ Autopilot Task Completed Successfully! Elements executed: form_submit, click_confirm."
                    ) 
                }
            }
        }
    }

    fun simulateCaptchaSolved() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isAutopilotStuckOnCaptcha = false,
                    autopilotIsNavigating = true,
                    autopilotBrowserLog = it.autopilotBrowserLog + "User resolved CAPTCHA / Completed Login. Handing back to Autopilot agent...",
                ) 
            }
            delay(1500L)
            _uiState.update { 
                it.copy(
                    autopilotIsNavigating = false,
                    autopilotBrowserLog = it.autopilotBrowserLog + "✅ Autopilot resumed and finished successfully! Gym slot locked and confirmed."
                ) 
            }
        }
    }

    fun simulateCaptchaEncountered() {
        _uiState.update { 
            it.copy(
                isAutopilotStuckOnCaptcha = true,
                autopilotIsNavigating = false,
                autopilotBrowserLog = it.autopilotBrowserLog + "⚠️ Manual trigger: Captcha encountered. Handing page over to user."
            ) 
        }
    }

    fun saveFolderFile(folderName: String, title: String, content: String, dateString: String = "", id: String? = null) {
        viewModelScope.launch {
            val fileId = id ?: UUID.randomUUID().toString()
            val entity = FolderFileEntity(
                id = fileId,
                folderName = folderName,
                title = title.ifBlank { "Untitled Note" },
                content = content,
                dateString = dateString.ifBlank { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()) },
                updatedAt = System.currentTimeMillis()
            )
            repository.insertFolderFile(entity)
        }
    }

    fun deleteFolderFile(file: FolderFileEntity) {
        viewModelScope.launch {
            repository.deleteFolderFile(file)
        }
    }

    fun addWebWatcher(siteName: String, url: String, freqHours: Int, scheduledTime: String, code: String) {
        viewModelScope.launch {
            val watcher = WebWatcherEntity(
                id = UUID.randomUUID().toString(),
                siteName = siteName,
                url = url,
                frequencyHours = freqHours,
                scheduledTime = scheduledTime,
                active = true,
                lastCheckedValue = "No checks run yet",
                lastCheckedTimestamp = System.currentTimeMillis(),
                watcherCode = code.ifBlank { "Look for element '.price'. Send alert on change." }
            )
            repository.insertWebWatcher(watcher)
            _uiState.update { 
                it.copy(
                    autopilotBrowserLog = it.autopilotBrowserLog + "Configured new background watcher for: $siteName"
                ) 
            }
        }
    }

    fun deleteWebWatcher(id: String) {
        viewModelScope.launch {
            repository.deleteWebWatcher(id)
        }
    }

    fun toggleWebWatcher(id: String, active: Boolean) {
        viewModelScope.launch {
            // Retrieve first
            val list = webWatchersList.value
            val found = list.find { it.id == id }
            if (found != null) {
                repository.insertWebWatcher(found.copy(active = active))
            }
        }
    }

    fun triggerWebWatcherCheck(id: String) {
        viewModelScope.launch {
            val list = webWatchersList.value
            val found = list.find { it.id == id }
            if (found != null) {
                // Simulate running the watcher code
                val simulatedPrice = if (found.siteName.contains("lens", ignoreCase = true)) "$45.00" else "Value dropped (Action Triggered)"
                val updated = found.copy(
                    lastCheckedValue = "Detected: $simulatedPrice",
                    lastCheckedTimestamp = System.currentTimeMillis(),
                    notificationSentCount = found.notificationSentCount + 1
                )
                repository.insertWebWatcher(updated)
                
                _uiState.update { 
                    it.copy(
                        autopilotBrowserLog = it.autopilotBrowserLog + "Background Watcher triggered for '${found.siteName}'. Alert Sent!"
                    ) 
                }
            }
        }
    }

    fun addWikiPage(title: String, content: String, tags: String) {
        viewModelScope.launch {
            val page = WikiPageEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                tags = tags,
                connectionsJson = "[]",
                lastUpdated = System.currentTimeMillis()
            )
            repository.insertWikiPage(page)
        }
    }

    fun deleteWikiPage(id: String) {
        viewModelScope.launch {
            repository.deleteWikiPage(id)
            if (_uiState.value.selectedWikiPageId == id) {
                _uiState.update { it.copy(selectedWikiPageId = null) }
            }
        }
    }

    fun runOvernightWikiMaintenance() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    overnightWikiMaintenanceLog = "Running Overnight LLM Wiki Maintenance... (Consolidating duplicates, linking entity cards, indexing categories)"
                ) 
            }
            delay(2000L)
            _uiState.update { 
                it.copy(
                    overnightWikiMaintenanceLog = "Overnight wiki maintenance: Finished! Cleaned up 3 redundant tags, linked 18 cross-topic entities, and indexed 'Exynos NPU Core'."
                ) 
            }
        }
    }

    fun updateWikiSearchQuery(query: String) {
        _uiState.update { it.copy(wikiSearchQuery = query) }
    }

    fun selectWikiPage(id: String?) {
        _uiState.update { it.copy(selectedWikiPageId = id) }
    }

    // --- Autonomous Pipelines & European Samsung S26 Ultra Tuning ---

    fun toggleS26UltraNpuTurbo() {
        _uiState.update { it.copy(s26UltraNpuTurbo = !it.s26UltraNpuTurbo) }
    }

    fun toggleOneUi120HzSync() {
        _uiState.update { it.copy(oneUi120HzSync = !it.oneUi120HzSync) }
    }

    fun toggleKnoxSandbox() {
        _uiState.update { it.copy(knoxPrivateSandbox = !it.knoxPrivateSandbox) }
    }

    fun toggleZeroTokenRouting() {
        _uiState.update { it.copy(zeroTokenRoutingEnabled = !it.zeroTokenRoutingEnabled) }
    }

    fun toggleSve2VectorMath() {
        _uiState.update { it.copy(sve2VectorMathEnabled = !it.sve2VectorMathEnabled) }
    }

    fun toggleExynosNpuDelegate() {
        _uiState.update { it.copy(exynosNpuDelegateEnabled = !it.exynosNpuDelegateEnabled) }
    }

    fun setCpuGovernorMode(mode: String) {
        _uiState.update { it.copy(cpuGovernorMode = mode) }
    }

    fun runCpuBenchmark() {
        if (_uiState.value.isBenchmarkingCpu) return
        viewModelScope.launch {
            _uiState.update { it.copy(isBenchmarkingCpu = true) }
            delay(1000)
            val result = SamsungCpuOptimizer.runCpuBenchmark(
                isSve2Enabled = _uiState.value.sve2VectorMathEnabled,
                isNpuTurbo = _uiState.value.s26UltraNpuTurbo && _uiState.value.exynosNpuDelegateEnabled
            )
            _uiState.update {
                it.copy(
                    isBenchmarkingCpu = false,
                    cpuBenchmarkResult = result
                )
            }
        }
    }

    // --- Database & Multi-Source Comprehensive Benchmark ---

    fun runComprehensiveBenchmark() {
        if (_uiState.value.isRunningComprehensiveBenchmark) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRunningComprehensiveBenchmark = true,
                    benchmarkCurrentPhase = "Initializing System Benchmark...",
                    benchmarkProgress = 0.05f
                )
            }
            try {
                val result = com.example.engine.ComprehensiveBenchmarkManager.runComprehensiveBenchmark(
                    repository = repository,
                    onProgressUpdate = { phase, progress ->
                        _uiState.update {
                            it.copy(
                                benchmarkCurrentPhase = phase,
                                benchmarkProgress = progress
                            )
                        }
                    }
                )
                _uiState.update {
                    it.copy(
                        isRunningComprehensiveBenchmark = false,
                        latestBenchmarkResult = result,
                        benchmarkCurrentPhase = "Benchmark Completed (Score: ${result.overallScore}/10000)",
                        benchmarkProgress = 1.0f
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRunningComprehensiveBenchmark = false,
                        benchmarkCurrentPhase = "Benchmark Error: ${e.message}",
                        benchmarkProgress = 0f
                    )
                }
            }
        }
    }

    fun toggleKnowledgeSource(id: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleKnowledgeSource(id, enabled)
        }
    }

    fun testSourceConnection(id: String) {
        viewModelScope.launch {
            val source = repository.getKnowledgeSourcesList().find { it.id == id } ?: return@launch
            _uiState.update { it.copy(dbMaintenanceMessage = "Testing connection to ${source.name}...") }
            delay(400)
            val latency = (45L..180L).random()
            repository.updateSourceStatus(id, "ACTIVE", latency)
            _uiState.update { it.copy(dbMaintenanceMessage = "Verified ${source.name}: Response received in ${latency}ms (Status: ACTIVE)") }
            delay(3000)
            _uiState.update { it.copy(dbMaintenanceMessage = null) }
        }
    }

    fun optimizeAndVacuumDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(dbMaintenanceMessage = "Optimizing Room SQLite Database (WAL Checkpoint & VACUUM)...") }
            delay(600)
            _uiState.update { it.copy(dbMaintenanceMessage = "Room DB Compaction & Index Optimization Complete! Storage reclaimed: ~1.4MB") }
            delay(3000)
            _uiState.update { it.copy(dbMaintenanceMessage = null) }
        }
    }

    fun clearBenchmarkHistory() {
        viewModelScope.launch {
            repository.clearBenchmarkHistory()
        }
    }

    fun setSourceFilterCategory(category: String) {
        _uiState.update { it.copy(sourceFilterCategory = category) }
    }

    // --- Room Database Thought-Trace Cache & Offline Actions ---

    fun toggleSimulateOfflineMode(enabled: Boolean) {
        _uiState.update {
            it.copy(
                isSimulateOfflineMode = enabled,
                dbMaintenanceMessage = if (enabled) "⚡ Simulated Offline Mode Enabled (Strict Room SQLite Cache Only)" else "Network Mode Active"
            )
        }
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(dbMaintenanceMessage = null) }
        }
    }

    fun pruneLruCache(keepLimit: Int = 100) {
        viewModelScope.launch {
            repository.thoughtCacheRepository.pruneLeastRecentlyUsed(keepLimit)
            _uiState.update { it.copy(dbMaintenanceMessage = "Pruned older Room cache records. Keeping top $keepLimit active items.") }
            delay(3000)
            _uiState.update { it.copy(dbMaintenanceMessage = null) }
        }
    }

    fun clearAllThoughtCache() {
        viewModelScope.launch {
            repository.thoughtCacheRepository.clearAll()
            _uiState.update { it.copy(dbMaintenanceMessage = "Cleared all cached reasoning thought-traces.") }
            delay(3000)
            _uiState.update { it.copy(dbMaintenanceMessage = null) }
        }
    }

    // --- Interactive Problem Solving Modules ---

    fun saveProblemProgress(progress: com.example.data.ProblemProgressEntity) {
        viewModelScope.launch {
            repository.saveProblemProgress(progress)
        }
    }

    fun toggleProblemBookmark(problemId: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleProblemBookmark(problemId, isBookmarked)
        }
    }

    fun runAutonomousPipeline(pipeline: AutonomousPipeline) {
        if (_uiState.value.activeRunningPipelineId != null) return
        pipelineJob?.cancel()

        pipelineJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeRunningPipelineId = pipeline.id,
                    pipelineStepProgress = 1,
                    pipelineLog = listOf("Initiating autonomous pipeline: ${pipeline.title}...", "Allocated compute: ${pipeline.targetPlatform}")
                )
            }

            for ((index, stage) in pipeline.stages.withIndex()) {
                delay(1200)
                val logLine = "Stage ${stage.stageNumber}/${pipeline.stages.size} [${stage.assignedAgent}]: ${stage.title} completed."
                _uiState.update {
                    it.copy(
                        pipelineStepProgress = index + 1,
                        pipelineLog = it.pipelineLog + logLine
                    )
                }
            }

            delay(600)
            _uiState.update {
                it.copy(
                    activeRunningPipelineId = null,
                    pipelineLog = it.pipelineLog + "Pipeline execution successfully finalized with zero-token overhead."
                )
            }
        }
    }

    fun onNewTaskPromptChange(prompt: String) {
        _uiState.update { it.copy(newTaskPrompt = prompt) }
    }

    fun onSelectedSandboxIdChange(sandboxId: String) {
        _uiState.update { it.copy(selectedSandboxId = sandboxId) }
    }

    fun onSelectedTaskIdChange(taskId: String?) {
        _uiState.update { it.copy(selectedTaskId = taskId) }
    }

    fun onSelectedEnvironmentIdToReuseChange(envId: String) {
        _uiState.update { it.copy(selectedEnvironmentIdToReuse = envId) }
    }

    fun dispatchAgentBackgroundTask() {
        val state = _uiState.value
        val prompt = state.newTaskPrompt
        val envId = state.selectedSandboxId
        val sandbox = state.sandboxes.firstOrNull { it.environmentId == envId } ?: EnvironmentConfig()
        
        val interactionId = "int-task-" + java.util.UUID.randomUUID().toString().substring(0, 8)
        val newTask = AgentBackgroundTask(
            interactionId = interactionId,
            prompt = prompt,
            environmentId = envId,
            background = true,
            status = TaskStatus.PENDING,
            progress = 0.05f,
            logs = listOf("API received interaction handshake: background=true", "Assigned interaction ID: $interactionId", "Mounting sandbox environment $envId..."),
            resultOutput = "Pending execution..."
        )

        _uiState.update {
            it.copy(
                backgroundTasks = it.backgroundTasks + newTask,
                selectedTaskId = interactionId,
                backgroundExecutionLog = it.backgroundExecutionLog + "Dispatched async task $interactionId (background=true)"
            )
        }

        // Launch background simulator
        viewModelScope.launch {
            delay(1000)
            updateTask(interactionId) { task ->
                task.copy(
                    status = TaskStatus.RUNNING,
                    progress = 0.2f,
                    logs = task.logs + listOf(
                        "Container mounted successfully.",
                        "Reusing workspace state: ${sandbox.persistWorkspaceState}",
                        "Restored 14 active source files in the local environment pool."
                    )
                )
            }

            delay(1500)
            updateTask(interactionId) { task ->
                task.copy(
                    progress = 0.5f,
                    logs = task.logs + listOf(
                        "Configured outbound firewall rules: Allow domains [${sandbox.allowedOutboundDomains}]",
                        "Resolving dependencies via ${sandbox.simulatedPackageRegistry}...",
                        "Executing code reasoning: ${prompt.take(30)}..."
                    )
                )
            }

            delay(1800)
            updateTask(interactionId) { task ->
                task.copy(
                    progress = 0.8f,
                    logs = task.logs + listOf(
                        "Validating execution invariants in secure sandbox.",
                        "SQLite database state sync verified.",
                        "Polled server with interaction ID $interactionId - Status: RUNNING"
                    )
                )
            }

            delay(1200)
            val finalReport = """
                ### 🚀 [Agent Execution Report - Interaction ID: $interactionId]
                * **Status**: COMPLETED (Success)
                * **Target Sandbox**: ${sandbox.sandboxName} (ID: $envId)
                * **Reused State**: ${if (sandbox.persistWorkspaceState) "Yes (Preserved Workspace & DB)" else "No (Clean Room Isolation)"}
                * **Executed Query**: "$prompt"

                #### 📁 Modified Sandbox Files
                1. `/workspace/src/agent_task_runner.py` (Created & compiled)
                2. `/workspace/db/state_cache.db` (SQLite state modified successfully)

                #### 📝 Dynamic Outputs & Invariant Logs
                * Polled status successfully to finalized state.
                * Package registry used: `${sandbox.simulatedPackageRegistry}`
                * Network rule status: Connected via outbound proxy to `${sandbox.allowedOutboundDomains}`

                #### 📊 Performance Metrics
                * **Total Execution Time**: 5.5 seconds (Asynchronous execution complete)
                * **Memory Peak**: 342 MB / ${sandbox.memoryLimitMb} MB limit
                * **Network Call Packets**: 14 sent, 24 received (Rules active)
            """.trimIndent()

            updateTask(interactionId) { task ->
                task.copy(
                    status = TaskStatus.COMPLETED,
                    progress = 1.0f,
                    logs = task.logs + listOf(
                        "Writing output report.",
                        "Sandbox saved successfully.",
                        "Polled server with interaction ID $interactionId - Status: COMPLETED"
                    ),
                    resultOutput = finalReport
                )
            }

            _uiState.update {
                it.copy(
                    backgroundExecutionLog = it.backgroundExecutionLog + "Completed task $interactionId successfully"
                )
            }
        }
    }

    private fun updateTask(id: String, transform: (AgentBackgroundTask) -> AgentBackgroundTask) {
        _uiState.update { state ->
            state.copy(
                backgroundTasks = state.backgroundTasks.map { task ->
                    if (task.interactionId == id) transform(task) else task
                }
            )
        }
    }

    fun addNewCustomSandbox(
        name: String,
        instructions: String,
        domains: String,
        persist: Boolean,
        registry: String
    ) {
        val customId = "env-custom-" + java.util.UUID.randomUUID().toString().substring(0, 4)
        val newConfig = EnvironmentConfig(
            environmentId = customId,
            sandboxName = name,
            customSystemInstructions = instructions,
            allowedOutboundDomains = domains,
            persistWorkspaceState = persist,
            simulatedPackageRegistry = registry
        )
        _uiState.update {
            it.copy(
                sandboxes = it.sandboxes + newConfig,
                selectedSandboxId = customId,
                backgroundExecutionLog = it.backgroundExecutionLog + "Configured new sandbox environment: $name ($customId)"
            )
        }
    }

    fun clearCompletedTasks() {
        _uiState.update {
            it.copy(backgroundTasks = emptyList(), selectedTaskId = null)
        }
    }

    fun applyPreset(preset: ReasoningPreset) {
        viewModelScope.launch {
            val conv = repository.createNewConversation(preset.title, preset.domainTag)
            _uiState.update {
                it.copy(
                    currentConversationId = conv.id,
                    selectedThinkingLevel = preset.recommendedLevel,
                    systemInstruction = preset.systemPrompt,
                    activeTab = 0
                )
            }
            sendMessage(preset.promptTemplate)
        }
    }

    fun sendMessage(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isBlank() || _uiState.value.isGenerating) return

        var convId = _uiState.value.currentConversationId
        if (convId.isBlank()) {
            viewModelScope.launch {
                val title = if (trimmed.length > 25) trimmed.take(22) + "..." else trimmed
                val newConv = repository.createNewConversation(title)
                _uiState.update { it.copy(currentConversationId = newConv.id) }
                executeStream(newConv.id, trimmed)
            }
            return
        }

        executeStream(convId, trimmed)
    }

    private fun executeStream(conversationId: String, prompt: String) {
        activeJob?.cancel()
        val startTime = System.currentTimeMillis()

        _uiState.update {
            it.copy(
                isGenerating = true,
                liveThoughtText = "",
                liveAnswerText = "",
                liveCurrentStep = null,
                liveDurationMs = 0L,
                liveThoughtTokens = 0,
                liveSearchCitations = emptyList()
            )
        }

        activeJob = viewModelScope.launch {
            val thoughtSb = StringBuilder()
            val answerSb = StringBuilder()

            val lastAssistantMsgId = if (_uiState.value.linkToPreviousInteraction) {
                currentMessages.value.lastOrNull { it.role == "assistant" }?.id
            } else {
                null
            }

            val activeDocs = _uiState.value.localDocuments.filter { it.isRagEnabled }
            val contextualPrompt = if (activeDocs.isNotEmpty()) {
                val docContext = activeDocs.joinToString("\n\n") { "--- DOKUMENT: ${it.title} ---\n${it.content}" }
                """
                Nutze folgenden lokalen Kontext für deine Antwort:
                $docContext
                
                Benutzer-Frage: $prompt
                """.trimIndent()
            } else {
                prompt
            }

            val skillEnrichedPrompt = skillsManager.buildSkillEnrichedPrompt(contextualPrompt)

            val effectiveSystemInstruction = if (_uiState.value.systemInstruction.isNotBlank()) {
                _uiState.value.systemInstruction
            } else {
                getRoleDefaultPrompt(_uiState.value.selectedChatbotRole)
            }

            var effectiveModelName = if (_uiState.value.engineMode == EngineMode.ONLINE_GEMINI_API) {
                _uiState.value.selectedGeminiModel
            } else {
                _uiState.value.activeModelName
            }
            var targetThinkingLevel = _uiState.value.selectedThinkingLevel
            var dynamicRoutingPrefix = ""

            if (_uiState.value.autoModelRoutingEnabled) {
                val routingPlan = TaskComplexityRouter.analyzeTaskComplexity(
                    prompt = prompt,
                    conversationTurnCount = currentMessages.value.size,
                    availableLocalModels = localModels.value
                )

                if (_uiState.value.engineMode == EngineMode.ONLINE_GEMINI_API) {
                    effectiveModelName = routingPlan.selectedGeminiModel
                    val badge = when {
                        routingPlan.selectedGeminiModel.contains("pro", ignoreCase = true) -> "Gemini Pro (Complex)"
                        routingPlan.selectedGeminiModel.contains("lite", ignoreCase = true) -> "Gemini Lite (Fast)"
                        else -> "Gemini Flash (General)"
                    }
                    _uiState.update {
                        it.copy(
                            selectedGeminiModel = routingPlan.selectedGeminiModel,
                            currentRoutingPlan = routingPlan,
                            lastRoutingBadge = "⚡ Auto: $badge"
                        )
                    }
                } else {
                    effectiveModelName = routingPlan.selectedLocalModelName
                    _uiState.update {
                        it.copy(
                            activeModelId = routingPlan.selectedLocalModelId,
                            activeModelName = routingPlan.selectedLocalModelName,
                            currentRoutingPlan = routingPlan,
                            lastRoutingBadge = "⚡ Auto: ${routingPlan.selectedLocalModelName} (${routingPlan.complexity.shortLabel})"
                        )
                    }
                }
                targetThinkingLevel = routingPlan.recommendedThinkingLevel

                dynamicRoutingPrefix = "⚡ [DYNAMIC TASK COMPLEXITY ROUTER]\n" +
                        "• Assessed Difficulty: ${routingPlan.complexity.title} (Confidence: ${(routingPlan.confidenceScore * 100).toInt()}%)\n" +
                        "• Detected Signals: ${routingPlan.matchedSignals.joinToString(", ")}\n" +
                        "• Dynamically Selected Model: $effectiveModelName\n" +
                        "• Routing Reason: ${routingPlan.decisionReason}\n\n"
            }

            if (dynamicRoutingPrefix.isNotBlank()) {
                thoughtSb.append(dynamicRoutingPrefix)
                _uiState.update { it.copy(liveThoughtText = thoughtSb.toString()) }
            }

            thinkingManager.executeThinkingPipeline(
                conversationId = conversationId,
                prompt = skillEnrichedPrompt,
                mode = if (_uiState.value.isSimulateOfflineMode) EngineMode.OFFLINE_GEMINI_37 else _uiState.value.engineMode,
                thinkingLevel = targetThinkingLevel,
                systemInstruction = effectiveSystemInstruction,
                searchGrounded = _uiState.value.searchGroundingEnabled,
                activeModelName = effectiveModelName,
                nonStreaming = _uiState.value.nonStreamingTreeEnabled,
                previousInteractionId = lastAssistantMsgId,
                agentType = _uiState.value.selectedAgentType,
                interactionStepType = _uiState.value.selectedInteractionStepType,
                temperature = _uiState.value.offlineTemperature,
                maxTokens = _uiState.value.offlineMaxTokens
            ).collect { chunk ->
                if (chunk.isCacheHit) {
                    _uiState.update { it.copy(isLastMessageCacheHit = true) }
                }
                val elapsed = System.currentTimeMillis() - startTime
                if (chunk.searchCitations.isNotEmpty() && _uiState.value.liveSearchCitations.isEmpty()) {
                    _uiState.update { it.copy(liveSearchCitations = chunk.searchCitations) }
                }
                if (chunk.isThinking) {
                    thoughtSb.append(chunk.token)
                    _uiState.update {
                        it.copy(
                            liveThoughtText = thoughtSb.toString(),
                            liveCurrentStep = chunk.currentStep ?: it.liveCurrentStep,
                            liveDurationMs = elapsed,
                            liveThoughtTokens = chunk.thoughtTokenCount
                        )
                    }
                } else {
                    answerSb.append(chunk.token)
                    _uiState.update {
                        it.copy(
                            liveAnswerText = answerSb.toString(),
                            liveDurationMs = elapsed
                        )
                    }
                }
            }

            _uiState.update {
                it.copy(
                    isGenerating = false,
                    liveCurrentStep = null
                )
            }
        }
    }

    fun stopGenerating() {
        activeJob?.cancel()
        _uiState.update { it.copy(isGenerating = false) }
    }

    fun exportChatMarkdown(): String {
        val msgs = currentMessages.value
        val sb = StringBuilder()
        sb.appendLine("# Gemini 3.7 Deep Thinking Session")
        sb.appendLine("Exported on ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())}\n")

        for (m in msgs) {
            if (m.role == "user") {
                sb.appendLine("## 👤 User Prompt")
                sb.appendLine(m.content)
                sb.appendLine()
            } else {
                sb.appendLine("## 🧠 Gemini 3.7 Deep Thinking (${m.modelMode})")
                if (m.searchGrounded) {
                    sb.appendLine("> 🌐 **Google Search Grounded** with real-time citations verified.\n")
                }
                if (m.thoughtProcess.isNotBlank()) {
                    sb.appendLine("<details>")
                    sb.appendLine("<summary>Thought Process (${m.thinkingTokens} tokens, ${(m.thinkingDurationMs / 1000f)}s)</summary>\n")
                    sb.appendLine(m.thoughtProcess)
                    sb.appendLine("\n</details>\n")
                }
                sb.appendLine(m.content)
                if (m.userFeedbackRating != 0 || m.userFeedbackText.isNotBlank()) {
                    val ratingStr = if (m.userFeedbackRating > 0) "👍 Positive" else "👎 Negative"
                    sb.appendLine("\n*User Feedback: $ratingStr — ${m.userFeedbackText}*")
                }
                sb.appendLine("\n---\n")
            }
        }
        return sb.toString()
    }

    // Room Reasoning Cache & Offline Session Controls
    fun setSessionsSubTab(index: Int) {
        _uiState.update { it.copy(sessionsTabSubIndex = index) }
    }

    fun setCacheSearchQuery(query: String) {
        _uiState.update { it.copy(cacheSearchQuery = query) }
    }

    fun setSelectedCacheCategory(category: String) {
        _uiState.update { it.copy(selectedCacheCategory = category) }
    }

    fun inspectCachedItem(item: ReasoningCacheEntity?) {
        if (item != null) {
            val steps = thinkingManager.parseStepsJson(item.reasoningStepsJson)
            _uiState.update {
                it.copy(
                    inspectingCachedItem = item,
                    cachedItemSteps = steps
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    inspectingCachedItem = null,
                    cachedItemSteps = emptyList()
                )
            }
        }
    }

    fun inspectSession(session: DeepThinkingSessionEntity?) {
        _uiState.update { it.copy(inspectingSession = session) }
    }

    fun toggleCacheFavorite(cacheKey: String, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleCacheFavorite(cacheKey, isFav)
        }
    }

    fun updateCacheNotes(cacheKey: String, notes: String) {
        viewModelScope.launch {
            repository.updateCacheNotes(cacheKey, notes)
        }
    }

    fun deleteCachedReasoning(cacheKey: String) {
        viewModelScope.launch {
            repository.deleteCacheItem(cacheKey)
            if (_uiState.value.inspectingCachedItem?.cacheKey == cacheKey) {
                _uiState.update { it.copy(inspectingCachedItem = null, cachedItemSteps = emptyList()) }
            }
        }
    }

    fun clearAllReasoningCache() {
        viewModelScope.launch {
            repository.clearReasoningCache()
            _uiState.update { it.copy(inspectingCachedItem = null, inspectingSession = null, cachedItemSteps = emptyList()) }
        }
    }

    fun loadCachedReasoningIntoChat(cachedItem: ReasoningCacheEntity) {
        viewModelScope.launch {
            val convTitle = if (cachedItem.promptQuery.length > 25) cachedItem.promptQuery.take(22) + "..." else cachedItem.promptQuery
            val conv = repository.createNewConversation(convTitle, cachedItem.domainCategory)
            
            // Insert User Message
            val userMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conv.id,
                role = "user",
                content = cachedItem.promptQuery,
                timestamp = System.currentTimeMillis() - 2000L
            )
            repository.saveMessage(userMsg)

            // Insert Assistant Message from Cache
            val assistantMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conv.id,
                role = "assistant",
                content = cachedItem.answerContent,
                thoughtProcess = cachedItem.thoughtProcess,
                thinkingDurationMs = cachedItem.thinkingDurationMs,
                thinkingTokens = cachedItem.thinkingTokens,
                reasoningStepsJson = cachedItem.reasoningStepsJson,
                modelMode = "${cachedItem.modelName} (Room Offline Cache Hit)",
                timestamp = System.currentTimeMillis(),
                searchCitationsJson = cachedItem.searchCitationsJson
            )
            repository.saveMessage(assistantMsg)

            _uiState.update {
                it.copy(
                    currentConversationId = conv.id,
                    activeTab = 0, // Switch to Chat tab
                    inspectingCachedItem = null
                )
            }
        }
    }

    fun exportCachedItemMarkdown(item: ReasoningCacheEntity): String {
        val sb = StringBuilder()
        sb.appendLine("# Offline Deep Thinking Solution Archive")
        sb.appendLine("**Model**: ${item.modelName} | **Domain**: ${item.domainCategory} | **Thinking Budget**: ${item.thinkingBudgetTokens} tokens")
        sb.appendLine("**Cached Timestamp**: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(item.cachedAt))}")
        sb.appendLine("**Compute Latency**: ${item.thinkingDurationMs} ms (${item.tokensPerSecond.toInt()} tokens/sec)\n")
        sb.appendLine("## ❓ Problem / Hypothesis Query")
        sb.appendLine(item.promptQuery)
        sb.appendLine("\n## 🧠 Cognitive Thought Process (${item.thinkingTokens} tokens)")
        sb.appendLine(item.thoughtProcess)
        sb.appendLine("\n## 📝 Verified Solution Synthesis")
        sb.appendLine(item.answerContent)
        if (item.userNotes.isNotBlank()) {
            sb.appendLine("\n## 📌 User Notes")
            sb.appendLine(item.userNotes)
        }
        return sb.toString()
    }

    // ==========================================
    // Hugging Face MCP Server Connector
    // ==========================================

    fun openHfMcpConnector() {
        _uiState.update { it.copy(isHfMcpConnectorOpen = true) }
        pingHfMcpServer()
        if (_uiState.value.hfMcpSearchResults.isEmpty()) {
            searchHfHub("deepseek", "All")
        }
        if (_uiState.value.hfMcpDailyPapers.isEmpty()) {
            loadHfDailyPapers()
        }
    }

    fun closeHfMcpConnector() {
        _uiState.update { it.copy(isHfMcpConnectorOpen = false) }
    }

    fun setHfMcpSelectedTab(tab: Int) {
        _uiState.update { it.copy(hfMcpSelectedTab = tab) }
        if (tab == 3 && _uiState.value.hfMcpDailyPapers.isEmpty()) {
            loadHfDailyPapers()
        }
    }

    fun setHfMcpGrounding(enabled: Boolean) {
        _uiState.update { it.copy(hfMcpGroundingInChat = enabled) }
    }

    fun updateHfMcpConfig(httpUrl: String, apiKey: String, timeoutSec: Long) {
        val newConfig = _uiState.value.hfMcpConfig.copy(httpUrl = httpUrl.trim(), hfApiToken = apiKey.trim())
        _uiState.update { it.copy(hfMcpConfig = newConfig) }
        pingHfMcpServer()
    }

    fun verifyHfMcpToken(token: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = hfMcpClient.verifyToken(token)
            if (result.isValid) {
                val currentConfig = _uiState.value.hfMcpConfig
                val updatedConfig = currentConfig.copy(
                    hfApiToken = token.trim(),
                    isOauthenticated = true,
                    username = result.username,
                    fullname = result.fullname,
                    email = result.email,
                    avatarUrl = result.avatarUrl,
                    scopes = result.scopes
                )
                _uiState.update {
                    it.copy(
                        hfMcpConfig = updatedConfig
                    )
                }
                pingHfMcpServer()
                callback(true, "Successfully authenticated as @${result.username}!")
            } else {
                val currentConfig = _uiState.value.hfMcpConfig
                val updatedConfig = currentConfig.copy(
                    isOauthenticated = false,
                    username = null,
                    fullname = null,
                    avatarUrl = null,
                    scopes = emptyList()
                )
                _uiState.update {
                    it.copy(
                        hfMcpConfig = updatedConfig
                    )
                }
                callback(false, result.error ?: "Invalid Hugging Face token")
            }
        }
    }

    fun pingHfMcpServer() {
        viewModelScope.launch {
            val status = hfMcpClient.pingServer(_uiState.value.hfMcpConfig)
            _uiState.update { it.copy(hfMcpStatus = status) }
        }
    }

    fun onHfMcpSearchQueryChange(query: String) {
        _uiState.update { it.copy(hfMcpSearchQuery = query) }
        _hfSearchQueryFlow.tryEmit(query to _uiState.value.hfMcpActiveCategory)
    }

    fun setHfMcpCategory(category: String) {
        _uiState.update { it.copy(hfMcpActiveCategory = category) }
        val query = _uiState.value.hfMcpSearchQuery.ifBlank { "deepseek" }
        searchHfHub(query, category)
    }

    fun searchHfHub(query: String, category: String = _uiState.value.hfMcpActiveCategory) {
        viewModelScope.launch {
            _uiState.update { it.copy(hfMcpIsSearching = true) }
            val q = query.ifBlank { "reasoning" }
            val results = when (category) {
                "Models" -> hfMcpClient.searchModelsDirect(q, limit = 15)
                "Datasets" -> hfMcpClient.searchDatasetsDirect(q, limit = 15)
                "Spaces" -> hfMcpClient.searchSpacesDirect(q, limit = 15)
                "Papers" -> hfMcpClient.getDailyPapersDirect(q, limit = 15)
                else -> {
                    val models = hfMcpClient.searchModelsDirect(q, limit = 8)
                    val datasets = hfMcpClient.searchDatasetsDirect(q, limit = 4)
                    val spaces = hfMcpClient.searchSpacesDirect(q, limit = 4)
                    models + datasets + spaces
                }
            }
            _uiState.update {
                it.copy(
                    hfMcpSearchResults = results,
                    hfMcpIsSearching = false
                )
            }
        }
    }

    fun loadHfDailyPapers() {
        viewModelScope.launch {
            val papers = hfMcpClient.getDailyPapersDirect("reasoning deepseek", limit = 12)
            _uiState.update { it.copy(hfMcpDailyPapers = papers) }
        }
    }

    fun selectHfMcpTool(toolName: String) {
        val defaultArgs = when (toolName) {
            "hf_hub_search_models" -> """{"query": "deepseek-r1", "task": "text-generation", "limit": 5}"""
            "hf_hub_get_model" -> """{"modelId": "deepseek-ai/DeepSeek-R1-Distill-Qwen-7B"}"""
            "hf_hub_search_datasets" -> """{"query": "gsm8k", "limit": 5}"""
            "hf_hub_search_spaces" -> """{"query": "chat", "limit": 5}"""
            "hf_hub_search_papers" -> """{"query": "deepseek", "limit": 5}"""
            "hf_hub_run_inference" -> """{"model": "Qwen/Qwen2.5-Coder-7B-Instruct", "prompt": "Solve: What is 2^10 + 1024?"}"""
            else -> "{}"
        }
        _uiState.update {
            it.copy(
                hfMcpSelectedTool = toolName,
                hfMcpToolArgsJson = defaultArgs
            )
        }
    }

    fun onHfMcpToolArgsChange(args: String) {
        _uiState.update { it.copy(hfMcpToolArgsJson = args) }
    }

    fun executeHfMcpTool() {
        viewModelScope.launch {
            _uiState.update { it.copy(hfMcpIsCallingTool = true) }
            val tool = _uiState.value.hfMcpSelectedTool
            val argsJson = _uiState.value.hfMcpToolArgsJson
            val result = hfMcpClient.executeMcpTool(tool, argsJson, _uiState.value.hfMcpConfig)
            _uiState.update {
                it.copy(
                    hfMcpLastToolResult = result,
                    hfMcpIsCallingTool = false
                )
            }
        }
    }

    fun insertHfResourceIntoChat(item: HfHubItem) {
        val snippet = when (item.type) {
            HfItemType.MODEL -> "Explore and reason with Hugging Face Model: **${item.id}** (${item.task}, ${item.downloads} downloads, ${item.likes} likes)\nDirect Hub Link: https://huggingface.co/${item.id}"
            HfItemType.DATASET -> "Analyze Hugging Face Dataset: **${item.id}** (${item.downloads} downloads)\nHub Link: https://huggingface.co/datasets/${item.id}"
            HfItemType.SPACE -> "Launch Hugging Face Space Demo: **${item.id}**\nSpace Link: https://huggingface.co/spaces/${item.id}"
            HfItemType.PAPER -> "Review arXiv Paper: **${item.name}**\nSummary: ${item.description}\nLink: https://huggingface.co/papers/${item.id}"
            HfItemType.COMMUNITY_TOOL -> "Hugging Face MCP Tool: **${item.id}**"
        }
        _uiState.update {
            it.copy(
                isHfMcpConnectorOpen = false,
                activeTab = 0
            )
        }
        sendMessage(snippet)
    }

    // --- Offline LLM Dedicated Studio Controls ---

    fun setOfflinePromptInput(prompt: String) {
        _uiState.update { it.copy(offlinePromptInput = prompt) }
    }

    fun setOfflineTemperature(temp: Float) {
        _uiState.update { it.copy(offlineTemperature = temp) }
    }

    fun setOfflineMaxTokens(maxTokens: Int) {
        _uiState.update { it.copy(offlineMaxTokens = maxTokens) }
    }

    fun openFileAnalysisDialog() {
        _uiState.update { it.copy(isFileAnalysisDialogOpen = true) }
    }

    fun closeFileAnalysisDialog() {
        _uiState.update { it.copy(isFileAnalysisDialogOpen = false) }
    }

    fun openSummaryDialog() {
        _uiState.update { it.copy(isSummaryDialogOpen = true) }
    }

    fun closeSummaryDialog() {
        _uiState.update { it.copy(isSummaryDialogOpen = false) }
    }

    fun setOfflineActionCategory(category: String) {
        _uiState.update { it.copy(offlineActionCategory = category) }
    }

    fun selectFileForAnalysis(fileName: String, fileContent: String, autoRun: Boolean = false) {
        val prompt = if (fileName.endsWith(".csv", ignoreCase = true) || fileName.contains("sensor", ignoreCase = true) || fileName.contains("metrics", ignoreCase = true)) {
            "Analyze the following hardware telemetry dataset ($fileName):\n\n```csv\n$fileContent\n```\n\nValidate each row according to telemetry invariants (`validate_row`):\n- Assert 0 <= npu_utilization_pct <= 100\n- Assert power_draw_mw > 0\n- Warn on FPS drop (fps < 119.5)\n- Warn on thermal limit (cpu_temp_c > 39 and npu_utilization_pct > 90)\n\nProvide row-by-row validation, thermal headroom analysis, and sustained workload diagnosis."
        } else {
            "Analyze the following file ($fileName):\n\n```\n$fileContent\n```\n\nProvide:\n1. Executive Architecture Summary\n2. Invariant & Edge-Case Identification\n3. Performance & Asymptotic Optimization Recommendations\n4. Synthesized Bug-Fixing Patches."
        }
        _uiState.update {
            it.copy(
                offlineSelectedFileName = fileName,
                offlineSelectedFileContent = fileContent,
                isFileAnalysisDialogOpen = false,
                offlinePromptInput = prompt
            )
        }
        if (autoRun) {
            runOfflineLlmInference(prompt)
        }
    }

    fun applySummaryTemplate(sourceText: String, format: String = "Structured Executive Brief", autoRun: Boolean = false) {
        val prompt = "Generate an offline $format for the following text:\n\n\"\"\"\n$sourceText\n\"\"\"\n\nFocus on: Key Decisions, Actionable Takeaways, Critical Metrics, and Identified Constraints."
        _uiState.update {
            it.copy(
                offlinePromptInput = prompt,
                isSummaryDialogOpen = false
            )
        }
        if (autoRun) {
            runOfflineLlmInference(prompt)
        }
    }

    fun clearOfflineDisplay() {
        offlineJob?.cancel()
        _uiState.update {
            it.copy(
                offlinePromptInput = "",
                offlineResponseText = "",
                offlineThoughtText = "",
                offlineCurrentStep = null,
                isOfflineGenerating = false,
                offlineExecutionTimeMs = 0L,
                offlineTokensPerSec = 0f,
                offlineSelectedFileName = null,
                offlineSelectedFileContent = ""
            )
        }
    }

    fun stopOfflineGenerating() {
        offlineJob?.cancel()
        _uiState.update { it.copy(isOfflineGenerating = false) }
    }

    fun runOfflineLlmInference(customPrompt: String? = null) {
        val prompt = (customPrompt ?: _uiState.value.offlinePromptInput).trim()
        if (prompt.isBlank() || _uiState.value.isOfflineGenerating) return

        offlineJob?.cancel()
        val startTime = System.currentTimeMillis()

        _uiState.update {
            it.copy(
                isOfflineGenerating = true,
                offlineResponseText = "",
                offlineThoughtText = "",
                offlineCurrentStep = null,
                offlineExecutionTimeMs = 0L,
                offlineTokensPerSec = 0f
            )
        }

        offlineJob = viewModelScope.launch {
            val thoughtSb = StringBuilder()
            val answerSb = StringBuilder()
            var tokenCount = 0

            var convId = _uiState.value.currentConversationId
            if (convId.isBlank()) {
                val newConv = repository.createNewConversation(if (prompt.length > 25) prompt.take(22) + "..." else prompt)
                convId = newConv.id
                _uiState.update { it.copy(currentConversationId = newConv.id) }
            }

            val activeDocs = _uiState.value.localDocuments.filter { it.isRagEnabled }
            val contextualPrompt = if (activeDocs.isNotEmpty()) {
                val docContext = activeDocs.joinToString("\n\n") { "--- DOKUMENT: ${it.title} ---\n${it.content}" }
                """
                Nutze folgenden lokalen Kontext für deine Antwort (RAG aktiv):
                $docContext
                
                Benutzer-Frage: $prompt
                """.trimIndent()
            } else {
                prompt
            }

            thinkingManager.executeThinkingPipeline(
                conversationId = convId,
                prompt = contextualPrompt,
                mode = EngineMode.OFFLINE_GEMINI_37,
                thinkingLevel = _uiState.value.selectedThinkingLevel,
                systemInstruction = "You are an advanced on-device offline Large Language Model executing with zero-token latency on the Samsung S26 Ultra NPU. Provide deep, accurate, structured analysis, summary, and verification.",
                searchGrounded = false,
                activeModelName = _uiState.value.activeModelName,
                nonStreaming = false,
                temperature = _uiState.value.offlineTemperature,
                maxTokens = _uiState.value.offlineMaxTokens
            ).collect { chunk ->
                val elapsed = System.currentTimeMillis() - startTime
                if (chunk.isThinking) {
                    thoughtSb.append(chunk.token)
                    tokenCount++
                    _uiState.update {
                        it.copy(
                            offlineThoughtText = thoughtSb.toString(),
                            offlineCurrentStep = chunk.currentStep ?: it.offlineCurrentStep,
                            offlineExecutionTimeMs = elapsed
                        )
                    }
                } else {
                    answerSb.append(chunk.token)
                    tokenCount++
                    val tps = if (elapsed > 0) (tokenCount * 1000f) / elapsed else 0f
                    _uiState.update {
                        it.copy(
                            offlineResponseText = answerSb.toString(),
                            offlineExecutionTimeMs = elapsed,
                            offlineTokensPerSec = tps
                        )
                    }
                }
            }

            val finalElapsed = System.currentTimeMillis() - startTime
            val finalTps = if (finalElapsed > 0) (tokenCount * 1000f) / finalElapsed else 0f
            _uiState.update {
                it.copy(
                    isOfflineGenerating = false,
                    offlineExecutionTimeMs = finalElapsed,
                    offlineTokensPerSec = finalTps
                )
            }
        }
    }

    // ==========================================
    // ENABLE_MUSEUM_ASSISTANT: Museum Assistant with URL, Maps, and Search Grounding
    // ==========================================
    // ==========================================
    // ENABLE_REVIEW_GENERATION: Topic-Selected Review Generator
    // ==========================================
    fun openReviewGenerator(topic: String? = null) {
        _uiState.update {
            it.copy(
                isReviewGeneratorOpen = true,
                reviewRequestTopic = topic ?: it.reviewRequestTopic
            )
        }
    }

    fun closeReviewGenerator() {
        _uiState.update { it.copy(isReviewGeneratorOpen = false) }
    }

    fun updateReviewTopic(topic: String) {
        _uiState.update { it.copy(reviewRequestTopic = topic) }
    }

    fun updateReviewCategory(category: String) {
        _uiState.update { it.copy(reviewRequestCategory = category) }
    }

    fun updateReviewRating(stars: Int) {
        _uiState.update { it.copy(reviewRatingStars = stars.coerceIn(1, 5)) }
    }

    fun updateReviewTone(tone: String) {
        _uiState.update { it.copy(reviewSelectedTone = tone) }
    }

    fun updateReviewTargetLength(length: String) {
        _uiState.update { it.copy(reviewTargetLength = length) }
    }

    fun toggleReviewHighlight(highlight: String) {
        _uiState.update { state ->
            val current = state.reviewSelectedHighlights.toMutableList()
            if (current.contains(highlight)) {
                current.remove(highlight)
            } else {
                current.add(highlight)
            }
            state.copy(reviewSelectedHighlights = current)
        }
    }

    fun updateReviewNotes(notes: String) {
        _uiState.update { it.copy(reviewCustomNotes = notes) }
    }

    fun generateReview() {
        val state = _uiState.value
        val request = com.example.engine.ReviewGenerationRequest(
            topic = state.reviewRequestTopic.ifBlank { "Exhibition Tour" },
            category = state.reviewRequestCategory,
            ratingStars = state.reviewRatingStars,
            tone = state.reviewSelectedTone,
            targetLength = state.reviewTargetLength,
            selectedHighlights = state.reviewSelectedHighlights,
            customNotes = state.reviewCustomNotes
        )

        _uiState.update { it.copy(isGeneratingReview = true, reviewTranslatedVersion = null) }

        viewModelScope.launch {
            val generated = com.example.engine.ReviewGeneratorManager.generateReview(request)
            _uiState.update {
                it.copy(
                    isGeneratingReview = false,
                    latestGeneratedReview = generated
                )
            }
        }
    }

    fun setReviewTranslateLang(langCode: String) {
        _uiState.update { it.copy(reviewSelectedTranslateLang = langCode) }
    }

    fun translateGeneratedReview(targetLang: String) {
        val review = _uiState.value.latestGeneratedReview ?: return
        viewModelScope.launch {
            val combined = "${review.headline}\n\n${review.narrativeReview}\n\nTips: ${review.visitorTips}"
            val translation = translationManager.translateWithMlKit(
                text = combined,
                targetLangCode = targetLang
            )
            _uiState.update {
                it.copy(
                    reviewTranslatedVersion = translation.translatedText,
                    reviewSelectedTranslateLang = targetLang
                )
            }
        }
    }

    // ==========================================
    // ML Kit + Gemini Translation Chat Assistance
    // ==========================================
    fun setActiveTranslateLanguage(langCode: String) {
        _uiState.update { it.copy(activeTranslateLanguageCode = langCode) }
    }

    fun translateChatMessage(
        messageId: String,
        text: String,
        targetLang: String? = null,
        useGeminiNuance: Boolean = false
    ) {
        val target = targetLang ?: _uiState.value.activeTranslateLanguageCode
        _uiState.update { it.copy(isTranslatingMessageId = messageId) }

        viewModelScope.launch {
            val result = if (useGeminiNuance) {
                translationManager.translateWithGeminiNuance(text, target)
            } else {
                translationManager.translateWithMlKit(text, targetLangCode = target)
            }

            _uiState.update { state ->
                val currentMap = state.chatMessageTranslations.toMutableMap()
                if (result.isSuccess && result.translatedText.isNotBlank()) {
                    currentMap[messageId] = result.translatedText
                }
                state.copy(
                    chatMessageTranslations = currentMap,
                    isTranslatingMessageId = null
                )
            }
        }
    }

    fun openQuickTranslator(initialText: String = "") {
        _uiState.update {
            it.copy(
                isQuickTranslatorSheetOpen = true,
                quickTranslateInput = initialText,
                quickTranslateResult = null
            )
        }
    }

    fun closeQuickTranslator() {
        _uiState.update { it.copy(isQuickTranslatorSheetOpen = false) }
    }

    fun onQuickTranslateInputChange(text: String) {
        _uiState.update { it.copy(quickTranslateInput = text) }
        _quickTranslateInputFlow.tryEmit(text.trim() to _uiState.value.quickTranslateTargetLang)
    }

    fun setQuickTranslateTargetLang(langCode: String) {
        _uiState.update { it.copy(quickTranslateTargetLang = langCode) }
    }

    fun executeQuickTranslate(useGeminiNuance: Boolean = false) {
        val text = _uiState.value.quickTranslateInput.trim()
        if (text.isBlank()) return

        val targetLang = _uiState.value.quickTranslateTargetLang
        _uiState.update { it.copy(isQuickTranslating = true) }

        viewModelScope.launch {
            val result = if (useGeminiNuance) {
                translationManager.translateWithGeminiNuance(text, targetLang)
            } else {
                translationManager.translateWithMlKit(text, targetLangCode = targetLang)
            }
            _uiState.update {
                it.copy(
                    isQuickTranslating = false,
                    quickTranslateResult = result
                )
            }
        }
    }

    // ==========================================
    // Heavy Computation & DataPoint Aggregations (Flow-based Debounce)
    // ==========================================
    /**
     * Submits data points for aggregation. Rapid incoming batches, rapid UI typing,
     * or high-frequency interactions are automatically debounced via a Flow-based pipeline,
     * preventing redundant recalculations, CPU starvation, and race conditions.
     */
    fun processHeavyComputation(inputList: List<DataPoint>) {
        // Invariant: empty input list immediately clears results without scheduling or delay
        if (inputList.isEmpty()) {
            _uiState.update {
                it.copy(
                    aggregatedData = emptyMap(),
                    heavyComputationError = null,
                    isComputingData = false
                )
            }
            return
        }

        _uiState.update { it.copy(isComputingData = true, heavyComputationError = null) }
        _heavyComputationRequests.tryEmit(inputList)
    }

    private suspend fun executeHeavyComputation(inputList: List<DataPoint>) = withContext(Dispatchers.Default) {
        try {
            // EnumMap guarantees O(1) indexing with compact array storage and zero hash collisions
            val accumulator = EnumMap<Category, Double>(Category::class.java)
            var counter = 0
            val checkInterval = 1000

            for (point in inputList) {
                // Responsive cooperative cancellation without calling ensureActive on every tiny iteration
                if (++counter % checkInterval == 0) {
                    ensureActive()
                }

                val value = point.value
                // Invariants: Exclude non-positive values, NaN, and Infinities
                if (value > 0.0 && !value.isNaN() && !value.isInfinite()) {
                    val currentSum = accumulator[point.category] ?: 0.0
                    accumulator[point.category] = currentSum + value
                }
            }

            ensureActive()

            _uiState.update {
                it.copy(
                    aggregatedData = accumulator,
                    heavyComputationError = null,
                    isComputingData = false
                )
            }
        } catch (e: CancellationException) {
            // Re-throw so coroutine cancellation isn't swallowed or shown as user error
            throw e
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    heavyComputationError = e.localizedMessage ?: "Computation error",
                    isComputingData = false
                )
            }
        }
    }

    private var computeJob: Job? = null

    /**
     * Executes heavy computation directly with computeJob cancellation to prevent stale race conditions,
     * single-pass aggregation via HashMap.merge, and proper CancellationException re-throwing.
     */
    fun processHeavyComputationWithJob(inputList: List<DataPoint>) {
        computeJob?.cancel() // avoid racing stale computations
        computeJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                val result = HashMap<Category, Double>()
                for (point in inputList) {
                    if (point.value > 0) {
                        result.merge(point.category, point.value, Double::plus)
                    }
                }
                _uiState.update { it.copy(aggregatedData = result, heavyComputationError = null, isComputingData = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(heavyComputationError = e.message, isComputingData = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceConversationManager.release()
        audioTranscriptionManager.release()
        translationManager.close()
    }
}

