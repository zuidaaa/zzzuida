package com.example.security.recaptcha

/**
 * Google reCAPTCHA Enterprise Assessment Request and Response Models
 * Target endpoint: https://recaptchaenterprise.googleapis.com/v1/projects/{projectId}/assessments?key={apiKey}
 */

data class RecaptchaEvent(
    val token: String,
    val expectedAction: String,
    val siteKey: String
)

data class RecaptchaAssessmentRequest(
    val event: RecaptchaEvent
)

data class RiskAnalysis(
    val score: Float = 1.0f,
    val reasons: List<String> = emptyList(),
    val extendedVerdictReasons: List<String> = emptyList()
)

data class TokenProperties(
    val valid: Boolean = true,
    val invalidReason: String? = null,
    val hostname: String? = null,
    val action: String? = null,
    val createTime: String? = null
)

data class RecaptchaAssessmentResponse(
    val name: String? = null,
    val event: RecaptchaEvent? = null,
    val riskAnalysis: RiskAnalysis? = null,
    val tokenProperties: TokenProperties? = null
)

data class RecaptchaSettings(
    val projectId: String = "gen-lang-client-0819348356",
    val siteKey: String = "6Lfr9vQtAAAAAOPatUeVnYsops31L9VKdftJQq8i",
    val apiKeyOverride: String = "",
    val isEnabled: Boolean = true,
    val minScoreThreshold: Float = 0.5f,
    val isSimulationMode: Boolean = false
)

data class RecaptchaTelemetryEntry(
    val id: String,
    val timestamp: Long,
    val action: String,
    val score: Float,
    val isValid: Boolean,
    val reasons: List<String>,
    val statusMessage: String,
    val isSuspicious: Boolean,
    val assessmentEndpoint: String,
    val latencyMs: Long
)
