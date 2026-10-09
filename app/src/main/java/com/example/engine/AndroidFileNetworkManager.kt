package com.example.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

data class NetworkStatusInfo(
    val isConnected: Boolean = true,
    val isWifi: Boolean = true,
    val isCellular: Boolean = false,
    val isMetered: Boolean = false,
    val connectionType: String = "WiFi",
    val linkSpeedMbps: Int = 866,
    val isForceOffline: Boolean = false,
    val effectiveState: String = "ONLINE", // "ONLINE", "OFFLINE_FORCED", "OFFLINE_AIRGAP"
    val lastCheckedTimestamp: Long = System.currentTimeMillis()
)

data class StorageStatsInfo(
    val proofCacheSizeBytes: Long = 0L,
    val datasetsSizeBytes: Long = 0L,
    val databaseSizeBytes: Long = 0L,
    val totalUsedStorageKb: Double = 0.0,
    val availableSpaceMb: Long = 0L,
    val filesDirPath: String = "",
    val cacheDirPath: String = ""
)

/**
 * Android System File & Network Management Service.
 * Manages active connectivity states, offline simulation, app-scoped directory exports,
 * and persistent storage telemetry for Proof-of-Thought caches and training datasets.
 */
class AndroidFileNetworkManager(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _networkStatus = MutableStateFlow(NetworkStatusInfo())
    val networkStatus: StateFlow<NetworkStatusInfo> = _networkStatus.asStateFlow()

    private val _storageStats = MutableStateFlow(StorageStatsInfo())
    val storageStats: StateFlow<StorageStatsInfo> = _storageStats.asStateFlow()

    private var forceOfflineMode = false

    init {
        registerNetworkMonitoring()
        refreshStorageStats()
    }

    private fun registerNetworkMonitoring() {
        val cm = connectivityManager ?: return
        try {
            val builder = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            cm.registerNetworkCallback(builder.build(), object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    evaluateNetworkState()
                }

                override fun onLost(network: Network) {
                    evaluateNetworkState()
                }

                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                    evaluateNetworkState()
                }
            })
        } catch (_: SecurityException) {
            // Fallback to one-time evaluation if callback registration fails
        }
        evaluateNetworkState()
    }

    fun evaluateNetworkState() {
        val cm = connectivityManager
        if (cm == null) {
            _networkStatus.value = NetworkStatusInfo(
                isConnected = false,
                effectiveState = if (forceOfflineMode) "OFFLINE_FORCED" else "OFFLINE_AIRGAP"
            )
            return
        }

        if (forceOfflineMode) {
            _networkStatus.value = _networkStatus.value.copy(
                isConnected = false,
                isForceOffline = true,
                effectiveState = "OFFLINE_FORCED",
                lastCheckedTimestamp = System.currentTimeMillis()
            )
            return
        }

        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        val isConnected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
        val isMetered = caps != null && !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        val connType = when {
            isWifi -> "WiFi (High-Speed)"
            isCellular -> "Cellular 5G/LTE"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            isConnected -> "Active Network"
            else -> "Air-Gapped Offline"
        }

        val speed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            caps?.linkDownstreamBandwidthKbps?.div(1000) ?: 100
        } else {
            100
        }

        _networkStatus.value = NetworkStatusInfo(
            isConnected = isConnected,
            isWifi = isWifi,
            isCellular = isCellular,
            isMetered = isMetered,
            connectionType = connType,
            linkSpeedMbps = speed.coerceAtLeast(10),
            isForceOffline = false,
            effectiveState = if (isConnected) "ONLINE" else "OFFLINE_AIRGAP",
            lastCheckedTimestamp = System.currentTimeMillis()
        )
    }

    fun setForceOffline(enabled: Boolean) {
        forceOfflineMode = enabled
        evaluateNetworkState()
    }

    // ==========================================
    // Android File Management for Proofs & Datasets
    // ==========================================

    fun getProofCacheDir(): File {
        val dir = File(context.filesDir, "proof_cache")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getDatasetsDir(): File {
        val dir = File(context.filesDir, "training_datasets")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun exportProofToFile(id: String, title: String, content: String, format: String = "md"): File {
        val safeName = title.lowercase()
            .replace(Regex("[^a-z0-9_-]"), "_")
            .take(32)
        val file = File(getProofCacheDir(), "proof_${safeName}_${id.take(8)}.$format")
        FileOutputStream(file).use { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
        }
        refreshStorageStats()
        return file
    }

    fun exportDatasetToFile(id: String, name: String, content: String, format: String = "jsonl"): File {
        val safeName = name.lowercase()
            .replace(Regex("[^a-z0-9_-]"), "_")
            .take(32)
        val file = File(getDatasetsDir(), "dataset_${safeName}_${id.take(8)}.$format")
        FileOutputStream(file).use { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
        }
        refreshStorageStats()
        return file
    }

    fun deleteExportedFile(file: File): Boolean {
        val deleted = file.delete()
        refreshStorageStats()
        return deleted
    }

    fun refreshStorageStats() {
        val proofDir = getProofCacheDir()
        val datasetDir = getDatasetsDir()

        val proofBytes = proofDir.listFiles()?.sumOf { it.length() } ?: 0L
        val datasetBytes = datasetDir.listFiles()?.sumOf { it.length() } ?: 0L

        val dbFile = context.getDatabasePath("gemini_deepthink_db")
        val dbBytes = if (dbFile.exists()) dbFile.length() else 0L

        val totalBytes = proofBytes + datasetBytes + dbBytes

        val stat = StatFs(context.filesDir.absolutePath)
        val availableMb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)

        _storageStats.value = StorageStatsInfo(
            proofCacheSizeBytes = proofBytes,
            datasetsSizeBytes = datasetBytes,
            databaseSizeBytes = dbBytes,
            totalUsedStorageKb = totalBytes / 1024.0,
            availableSpaceMb = availableMb,
            filesDirPath = context.filesDir.absolutePath,
            cacheDirPath = context.cacheDir.absolutePath
        )
    }
}
