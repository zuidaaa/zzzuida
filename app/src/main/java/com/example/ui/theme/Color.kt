package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Unified LLM Application Interface Palette
// ==========================================

// Global Base Colors
val AbsoluteBlack = Color(0xFF000000)
val GlassCardPanel = Color(0xB3161B22) // rgba(22, 27, 34, 0.7)
val GlassOverlay = Color(0xD9000000)   // rgba(0, 0, 0, 0.85)

// Legacy compatibility aliases (CRITICAL for build)
val ThoughtAccent = Color(0xFF3B82F6)
val ThoughtBackground = Color(0xFF161B22)
val ThoughtBorder = Color(0xB330363D)
val CodeBlockBackground = Color(0xFF000000)

// Typography Colors
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA1A7B3)
val TextDisabled = Color(0xFF6E7681)
val TextTerminal = Color(0xFF00FFCC)

// Border Colors
val BorderSubtle = Color(0xB330363D)   // rgba(48, 54, 61, 0.7)
val BorderActive = Color(0xFF3B82F6)   // Luminous Blue

// Functional Accents
val LuminousBlue = Color(0xFF3B82F6)   // Active / Primary
val VibrantPurple = Color(0xFF8B5CF6)  // CoT / Reasoning
val VibrantTeal = Color(0xFF14B8A6)    // Engines / technical
val VibrantGreen = Color(0xFF22C55E)   // Success / Terminal
val LuminousYellow = Color(0xFFF59E0B) // Authentication / Warnings
val VibrantRed = Color(0xFFEF4444)     // Errors / Destructive

// Compatibility Aliases
val DeepThinkIndigo = LuminousBlue
val DeepThinkIndigoLight = LuminousBlue.copy(alpha = 0.7f)
val DeepThinkIndigoGlow = LuminousBlue.copy(alpha = 0.3f)
val DeepThinkCyan = VibrantTeal
val DeepThinkCyanLight = VibrantTeal.copy(alpha = 0.7f)
val DeepThinkViolet = VibrantPurple
val DeepThinkVioletLight = VibrantPurple.copy(alpha = 0.7f)
val DeepThinkEmerald = VibrantGreen
val DeepThinkEmeraldLight = VibrantGreen.copy(alpha = 0.7f)
val DeepThinkAmber = LuminousYellow
val DeepThinkRose = VibrantRed
val GeminiBlue = LuminousBlue
val GeminiBlueLight = DeepThinkIndigoLight
val GeminiBlueGlow = DeepThinkIndigoGlow
val GeminiCyan = VibrantTeal
val GeminiPurple = VibrantPurple
val GeminiPink = VibrantRed
val GeminiAmber = LuminousYellow
val GeminiEmerald = VibrantGreen
val VerificationGreen = VibrantGreen
val WarningOrange = LuminousYellow

val DarkBackground = AbsoluteBlack
val DarkSurface = GlassCardPanel
val DarkSurfaceVariant = Color(0xFF181F2E)
val DarkSurfaceElevated = Color(0xFF20293D)
val DarkSurfaceHigh = Color(0xFF20293D)
val DarkSurfaceHighlight = Color(0xFF28344D)
val DarkPrimary = LuminousBlue
val DarkOnPrimary = Color(0xFF000000)
val DarkSecondary = VibrantTeal
val DarkOnSecondary = Color(0xFF000000)
val DarkTertiary = VibrantPurple
val DarkOnTertiary = Color(0xFF000000)
val DarkOutline = BorderSubtle
val DarkOutlineVariant = BorderSubtle.copy(alpha = 0.5f)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkOnSurfaceVariant = Color(0xFFA1A7B3)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextMuted = Color(0xFFA1A7B3)
val DarkTextSubtle = Color(0xFF6E7681)

val GlowBlueAtmosphere = LuminousBlue.copy(alpha = 0.15f)
val GlowPurpleAtmosphere = VibrantPurple.copy(alpha = 0.15f)
val GlowCyanAtmosphere = VibrantTeal.copy(alpha = 0.15f)

// Light Scheme (Fallback/Legacy)
val LightBackground = Color(0xFFF4F7FB)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE9EFF7)
val LightSurfaceElevated = Color(0xFFDCE5F2)
val LightPrimary = Color(0xFF2557D6)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightSecondary = Color(0xFF0284C7)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightTertiary = Color(0xFF7E22CE)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightOutline = Color(0xFFC4D2E5)
val LightOutlineVariant = Color(0xFFDCE5F2)
val LightOnSurface = Color(0xFF0D1527)
val LightOnSurfaceVariant = Color(0xFF54647F)
