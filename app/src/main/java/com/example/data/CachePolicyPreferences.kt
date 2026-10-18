package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class TtlPreset(
    val id: String,
    val displayName: String,
    val durationMs: Long,
    val description: String
) {
    DAY_1(
        id = "1_day",
        displayName = "1 Tag",
        durationMs = 86_400_000L,
        description = "24 Stunden - Für flüchtige, schnelle Abfragen"
    ),
    DAYS_7(
        id = "7_days",
        displayName = "7 Tage",
        durationMs = 604_800_000L,
        description = "Empfohlen - Ausgewogene Speichernutzung"
    ),
    DAYS_30(
        id = "30_days",
        displayName = "30 Tage",
        durationMs = 2_592_000_000L,
        description = "Langzeit - Für intensive Offline-Nutzung"
    ),
    PERMANENT(
        id = "permanent",
        displayName = "Dauerhaft",
        durationMs = 0L,
        description = "Kein Verfall - Niemals automatisch löschen"
    );

    companion object {
        fun fromId(id: String): TtlPreset = entries.find { it.id == id } ?: DAYS_7
    }
}

data class CachePolicyState(
    val selectedPreset: TtlPreset = TtlPreset.DAYS_7,
    val autoCleanupOnLaunch: Boolean = true,
    val protectTrainingDatasets: Boolean = true,
    val protectPinnedItems: Boolean = true,
    val maxLruEntries: Int = 500,
    val lastCleanupTimestamp: Long = 0L,
    val lastCleanedCount: Int = 0
)

class CachePolicyPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cache_policy_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadCurrentState())
    val state: StateFlow<CachePolicyState> = _state.asStateFlow()

    private fun loadCurrentState(): CachePolicyState {
        val presetId = prefs.getString(KEY_TTL_PRESET, TtlPreset.DAYS_7.id) ?: TtlPreset.DAYS_7.id
        return CachePolicyState(
            selectedPreset = TtlPreset.fromId(presetId),
            autoCleanupOnLaunch = prefs.getBoolean(KEY_AUTO_CLEANUP, true),
            protectTrainingDatasets = prefs.getBoolean(KEY_PROTECT_DATASETS, true),
            protectPinnedItems = prefs.getBoolean(KEY_PROTECT_PINNED, true),
            maxLruEntries = prefs.getInt(KEY_MAX_LRU, 500),
            lastCleanupTimestamp = prefs.getLong(KEY_LAST_CLEANUP_TIME, 0L),
            lastCleanedCount = prefs.getInt(KEY_LAST_CLEANUP_COUNT, 0)
        )
    }

    fun setTtlPreset(preset: TtlPreset) {
        prefs.edit().putString(KEY_TTL_PRESET, preset.id).apply()
        _state.update { it.copy(selectedPreset = preset) }
    }

    fun setAutoCleanupOnLaunch(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CLEANUP, enabled).apply()
        _state.update { it.copy(autoCleanupOnLaunch = enabled) }
    }

    fun setProtectTrainingDatasets(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PROTECT_DATASETS, enabled).apply()
        _state.update { it.copy(protectTrainingDatasets = enabled) }
    }

    fun setProtectPinnedItems(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PROTECT_PINNED, enabled).apply()
        _state.update { it.copy(protectPinnedItems = enabled) }
    }

    fun setMaxLruEntries(limit: Int) {
        prefs.edit().putInt(KEY_MAX_LRU, limit).apply()
        _state.update { it.copy(maxLruEntries = limit) }
    }

    fun recordCleanup(count: Int) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putLong(KEY_LAST_CLEANUP_TIME, now)
            .putInt(KEY_LAST_CLEANUP_COUNT, count)
            .apply()
        _state.update { it.copy(lastCleanupTimestamp = now, lastCleanedCount = count) }
    }

    companion object {
        private const val KEY_TTL_PRESET = "key_ttl_preset"
        private const val KEY_AUTO_CLEANUP = "key_auto_cleanup"
        private const val KEY_PROTECT_DATASETS = "key_protect_datasets"
        private const val KEY_PROTECT_PINNED = "key_protect_pinned"
        private const val KEY_MAX_LRU = "key_max_lru"
        private const val KEY_LAST_CLEANUP_TIME = "key_last_cleanup_time"
        private const val KEY_LAST_CLEANUP_COUNT = "key_last_cleanup_count"
    }
}
