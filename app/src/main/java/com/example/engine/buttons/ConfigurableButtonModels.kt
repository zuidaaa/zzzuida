package com.example.engine.buttons

enum class DigitalButtonKey(val id: String, val displayName: String, val defaultLabel: String) {
    VOLUME_UP("vol_up", "Volume Up Key", "Vol+"),
    VOLUME_DOWN("vol_down", "Volume Down Key", "Vol-"),
    SIDE_KEY_BIXBY("side_key", "Side / Bixby Key", "Power/Side"),
    SOFT_BUTTON_ALPHA("soft_alpha", "Digital Soft Button Alpha", "Quick Action A"),
    SOFT_BUTTON_BETA("soft_beta", "Digital Soft Button Beta", "Quick Action B"),
    SOFT_BUTTON_GAMMA("soft_gamma", "Digital Soft Button Gamma", "Quick Action C"),
    SOFT_BUTTON_DELTA("soft_delta", "Digital Soft Button Delta", "Quick Action D"),
    EDGE_SWIPE_TRIGGER("edge_swipe", "Edge Panel Gesture Trigger", "Edge Swipe"),
    KEYBOARD_ACCESSORY_F1("kbd_f1", "Keyboard Accessory F1 Key", "Kbd F1")
}

enum class TriggerType(val displayName: String) {
    SINGLE_CLICK("Single Tap"),
    DOUBLE_CLICK("Double Tap"),
    LONG_PRESS("Long Press (500ms+)")
}

enum class MappedAction(val id: String, val displayName: String, val description: String, val category: String) {
    TRIGGER_VOICE_INPUT("voice_input", "Toggle Voice Transcription", "Starts recording microphone audio for Gemini transcription", "Audio & Voice"),
    EXECUTE_QUICK_LLM_PROMPT("quick_prompt", "Execute Quick LLM Prompt", "Sends pre-configured quick prompt to Gemini Flash model", "AI Reasoning"),
    START_HARDWARE_BENCHMARK("start_benchmark", "Run 90s Hardware Benchmark", "Triggers 5-stage sustained stress benchmark", "System Telemetry"),
    CLEAR_REASONING_CACHE("clear_cache", "Flush Reasoning Cache", "Clears Room SQLite proof-of-thought cache database", "Database & Memory"),
    TOGGLE_TURBO_MODE("toggle_turbo", "Toggle NPU Turbo Mode", "Enables or disables 85 TOPS NPU acceleration", "Hardware Performance"),
    OPEN_OCTOPUS_AGENT("open_octopus", "Launch Octopus Agent Mesh", "Opens autonomous multi-step reasoning agent", "Autonomous Agents"),
    EXPORT_THOUGHT_TRACE("export_trace", "Export Thought Trace JSON", "Exports thought traces to local storage", "Developer Tools"),
    EMERGENCY_CANCEL_LLM("cancel_llm", "Emergency Cancel Inference", "Immediately halts all running LLM network generation", "Safety & System"),
    CUSTOM_MACRO_SCRIPT("custom_macro", "Trigger Custom Macro Script", "Runs user-defined multi-step automation workflow", "Automation")
}

data class ButtonActionMapping(
    val key: DigitalButtonKey,
    val triggerType: TriggerType,
    val mappedAction: MappedAction,
    val customPayload: String = "",
    val isEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

data class ButtonExecutionEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val key: DigitalButtonKey,
    val triggerType: TriggerType,
    val mappedAction: MappedAction,
    val pressDurationMs: Long,
    val executionLatencyMs: Long,
    val isSuccess: Boolean,
    val statusMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)
