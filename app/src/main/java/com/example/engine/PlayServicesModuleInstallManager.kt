package com.example.engine

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Google Play Services ModuleInstall API & Optional Module Client Simulator / Interface.
 * Supports checking availability, downloading optional modules (TFLite, ML Kit, Code Scanner),
 * tracking download progress, and triggering deferred installations.
 */

interface OptionalModuleApi {
    val moduleName: String
}

class TfLiteClient(val context: Context?) : OptionalModuleApi {
    override val moduleName: String = "TensorFlow Lite Runtime (Google Play Services)"
}

object TfLite {
    fun getClient(context: Context?): TfLiteClient = TfLiteClient(context)
}

data class ModuleAvailabilityResponse(
    private val areAvailable: Boolean,
    val availabilityCode: Int = if (areAvailable) 0 else 1
) {
    fun areModulesAvailable(): Boolean = areAvailable
}

data class ModuleInstallStatusUpdate(
    val bytesDownloaded: Long,
    val totalBytesToDownload: Long,
    val installState: Int // 1: Pending, 2: Downloading, 3: Installing, 4: Success, 5: Failed
) {
    val progressPercentage: Float
        get() = if (totalBytesToDownload > 0) (bytesDownloaded.toFloat() / totalBytesToDownload.toFloat()) * 100f else 0f
}

fun interface ModuleInstallProgressListener {
    fun onInstallStatusUpdated(update: ModuleInstallStatusUpdate)
}

class ModuleInstallRequest private constructor(
    val apis: List<OptionalModuleApi>,
    val listener: ModuleInstallProgressListener?
) {
    class Builder {
        private val apis = mutableListOf<OptionalModuleApi>()
        private var listener: ModuleInstallProgressListener? = null

        fun addApi(api: OptionalModuleApi) = apply { apis.add(api) }
        fun setListener(listener: ModuleInstallProgressListener) = apply { this.listener = listener }
        fun build() = ModuleInstallRequest(apis, listener)
    }

    companion object {
        fun newBuilder() = Builder()
    }
}

class ModuleInstallTask<T>(private val resultProducer: suspend () -> T) {
    private var onSuccessCallback: ((T) -> Unit)? = null
    private var onFailureCallback: ((Exception) -> Unit)? = null

    fun addOnSuccessListener(listener: (T) -> Unit) = apply {
        this.onSuccessCallback = listener
    }

    fun addOnFailureListener(listener: (Exception) -> Unit) = apply {
        this.onFailureCallback = listener
    }

    suspend fun await(): T = withContext(Dispatchers.Default) {
        try {
            val res = resultProducer()
            onSuccessCallback?.invoke(res)
            res
        } catch (e: Exception) {
            onFailureCallback?.invoke(e)
            throw e
        }
    }
}

class ModuleInstallClient(val context: Context?) {

    private val _isModuleInstalled = MutableStateFlow(true)
    val isModuleInstalled: StateFlow<Boolean> = _isModuleInstalled.asStateFlow()

    /**
     * Checks if the optional modules (e.g. TfLite) are present on the device.
     */
    fun areModulesAvailable(vararg apis: OptionalModuleApi): ModuleInstallTask<ModuleAvailabilityResponse> {
        return ModuleInstallTask {
            // Simulated check: On modern devices with Play Services, modules can be available or require dynamic download
            ModuleAvailabilityResponse(areAvailable = _isModuleInstalled.value)
        }
    }

    /**
     * Requests immediate installation with real-time progress callbacks.
     */
    fun installModules(request: ModuleInstallRequest): ModuleInstallTask<Boolean> {
        return ModuleInstallTask {
            request.listener?.onInstallStatusUpdated(
                ModuleInstallStatusUpdate(bytesDownloaded = 250_000, totalBytesToDownload = 1_000_000, installState = 2)
            )
            request.listener?.onInstallStatusUpdated(
                ModuleInstallStatusUpdate(bytesDownloaded = 1_000_000, totalBytesToDownload = 1_000_000, installState = 4)
            )
            _isModuleInstalled.value = true
            true
        }
    }

    /**
     * Queues an optional module for deferred background installation when on Wi-Fi and charging.
     */
    fun deferredInstall(vararg apis: OptionalModuleApi): ModuleInstallTask<Void?> {
        return ModuleInstallTask {
            Log.d("ModuleInstall", "Deferred install scheduled for ${apis.joinToString { it.moduleName }}")
            null
        }
    }
}

object ModuleInstall {
    fun getClient(context: Context?): ModuleInstallClient = ModuleInstallClient(context)
}
