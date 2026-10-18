# Implementation Plan: Google reCAPTCHA Enterprise Risk Assessment Integration

Integrate Google reCAPTCHA Enterprise assessment capabilities to evaluate user and bot risk for critical workflows (Google Sign-In and Cloud Vault synchronization), record risk telemetry and reason codes into an in-app audit log without blocking user operations, and provide both BuildConfig secrets and in-app developer overrides.

---

## 1. Architectural Overview & Workflow Alignment

Based on user selections:
* **Target Actions:** Assessment triggers on `SIGN_IN` and `CLOUD_SYNC`.
* **Risk Handling:** Passive telemetry logging — suspicious risk scores (< 0.5) generate detailed audit entries with reason codes, but do not block legitimate users from continuing.
* **Credentials & Configuration:**
  * Project ID: `gen-lang-client-0819348356`
  * Default Site Key: `6Lfr9vQtAAAAAOPatUeVnYsops31L9VKdftJQq8i`
  * API Key: Sourced from `BuildConfig.RECAPTCHA_API_KEY` (with `.env.example` placeholder) and configurable via in-app developer override in the Settings screen.
* **Assessment Endpoint:**
  `POST https://recaptchaenterprise.googleapis.com/v1/projects/{projectId}/assessments?key={apiKey}`
  Request payload format:
  ```json
  {
    "event": {
      "token": "<TOKEN>",
      "expectedAction": "<USER_ACTION>",
      "siteKey": "<SITE_KEY>"
    }
  }
  ```

---

## 2. Proposed Changes

### Layer 1: Secret Management & BuildConfig
#### `.env.example`
* Add `RECAPTCHA_API_KEY=MY_RECAPTCHA_API_KEY` placeholder so AI Studio Secrets panel can securely inject the key at build time.

### Layer 2: reCAPTCHA Enterprise Models & Client
#### `app/src/main/java/com/example/security/recaptcha/RecaptchaModels.kt` (New File)
* Define data classes:
  * `RecaptchaEvent(token, expectedAction, siteKey)`
  * `RecaptchaAssessmentRequest(event)`
  * `RiskAnalysis(score, reasons, extendedVerdictReasons)`
  * `TokenProperties(valid, invalidReason, hostname, action, createTime)`
  * `RecaptchaAssessmentResponse(name, event, riskAnalysis, tokenProperties)`
  * `RecaptchaTelemetryEntry(id, timestamp, action, score, valid, reasons, statusMessage, isSuspicious)`
  * `RecaptchaSettings(projectId, siteKey, apiKeyOverride, isEnabled, minScoreThreshold)`

#### `app/src/main/java/com/example/security/recaptcha/RecaptchaEnterpriseClient.kt` (New File)
* Implement network client with coroutines:
  * Performs HTTP `POST` to the reCAPTCHA Enterprise Assessment REST API.
  * Formats the JSON payload matching the exact `event` schema.
  * Parses response risk analysis (`score`, `reasons`).
  * Handles offline resilience / fallback: if offline or API key is not configured, generates a local device assessment trace and logs telemetry seamlessly.

### Layer 3: MainViewModel Integration & Audit Logging
#### `app/src/main/java/com/example/ui/MainViewModel.kt`
* Add state fields:
  * `recaptchaSettings: RecaptchaSettings`
  * `recaptchaTelemetryLogs: List<RecaptchaTelemetryEntry>`
  * `isEvaluatingRecaptcha: Boolean`
  * `lastRecaptchaScore: Float?`
* Hook assessment triggers:
  * In `performSignIn(activity)`: invoke assessment with action `"SIGN_IN"` before initiating Google Credential Manager flow.
  * In `performCloudSync()`: invoke assessment with action `"CLOUD_SYNC"` before Firestore updates.
  * Append results to `recaptchaTelemetryLogs` with formatted timestamp, risk score badge, and reasons.
  * Expose helper functions: `updateRecaptchaSettings(...)`, `clearRecaptchaLogs()`, and `triggerTestAssessment(action)`.

### Layer 4: Settings UI Controls & Risk Telemetry Inspector
#### `app/src/main/java/com/example/ui/screens/SettingsScreen.kt`
* Add a dedicated **reCAPTCHA Enterprise Security** section:
  * Status indicator: Active, Protected Actions (`SIGN_IN`, `CLOUD_SYNC`).
  * Configuration fields: Project ID (`gen-lang-client-0819348356`), Site Key (`6Lfr9vQtAAAAAOPatUeVnYsops31L9VKdftJQq8i`), API Key override.
  * Test assessment button ("Simulate Assessment Event") with instant score display.
  * Live Telemetry Log viewer displaying recent events, scores (0.0 to 1.0 with color coding), reasons, and action tags.

---

## 3. Verification & Safety Plan

* **Compilation:**
  * Run `compile_applet` to verify all models, networking logic, ViewModel bindings, and Compose UI compile with zero errors.
* **Functional Testing:**
  * Verify `Test Assessment` in Settings executes and logs a formatted telemetry entry.
  * Verify Google Sign-In and Cloud Sync trigger the assessment asynchronously without freezing or blocking the UI.
  * Verify low scores (< 0.5) trigger an audit log warning while still allowing operations to proceed as requested.
