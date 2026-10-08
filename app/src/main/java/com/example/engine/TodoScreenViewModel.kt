package com.example.engine

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data Model for a ToDo Task
 */
data class TodoItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: String = "Medium"
)

/**
 * State of the Live Multimodal Session
 */
sealed interface LiveSessionState {
    object Idle : LiveSessionState
    object Connecting : LiveSessionState
    object Connected : LiveSessionState
    object Listening : LiveSessionState
    object Speaking : LiveSessionState
    object Error : LiveSessionState
}

/**
 * Simulation / Mock structures for Firebase AI Live Multimodal Session API
 */
class LiveGenerationConfig(
    val responseModalities: List<String> = listOf("AUDIO", "TEXT"),
    val speechConfig: String = "Aoede"
)

class ToolFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>
)

object Tool {
    fun functionDeclarations(declarations: List<ToolFunctionDeclaration>): List<ToolFunctionDeclaration> = declarations
}

interface LiveSession {
    suspend fun sendAudioChunk(bytes: ByteArray)
    suspend fun sendText(text: String)
    suspend fun close()
}

/**
 * ViewModel managing Voice Interaction with Gemini Flash for managing ToDo lists
 */
class TodoScreenViewModel : ViewModel() {

    companion object {
        private const val TAG = "TodoScreenViewModel"
    }

    private val _todoList = MutableStateFlow<List<TodoItem>>(
        listOf(
            TodoItem(id = "1", title = "Complete project documentation", isCompleted = false),
            TodoItem(id = "2", title = "Review ML Kit integration", isCompleted = true),
            TodoItem(id = "3", title = "Test Gemini 2.5 Flash live audio", isCompleted = false)
        )
    )
    val todoList: StateFlow<List<TodoItem>> = _todoList.asStateFlow()

    val liveSessionState = MutableStateFlow<LiveSessionState>(LiveSessionState.Idle)
    private var session: LiveSession? = null

    // 1. Tool Function Declarations for Gemini Live Model
    val getTodoList = ToolFunctionDeclaration(
        name = "getTodoList",
        description = "Returns the current list of ToDo items with their completion status and IDs.",
        parameters = emptyMap()
    )

    val addTodo = ToolFunctionDeclaration(
        name = "addTodo",
        description = "Adds a new task to the user's ToDo list.",
        parameters = mapOf("title" to "string", "priority" to "string")
    )

    val removeTodo = ToolFunctionDeclaration(
        name = "removeTodo",
        description = "Removes a task from the ToDo list given its ID or exact title.",
        parameters = mapOf("id" to "string")
    )

    val toggleTodoStatus = ToolFunctionDeclaration(
        name = "toggleTodoStatus",
        description = "Toggles the completion status (done/not done) of a specific ToDo item by ID.",
        parameters = mapOf("id" to "string")
    )

    /**
     * Starts the live bidirectional voice conversation with Gemini Flash
     */
    fun startLiveVoiceSession() {
        viewModelScope.launch {
            liveSessionState.value = LiveSessionState.Connecting

            val liveGenerationConfig = LiveGenerationConfig(
                responseModalities = listOf("AUDIO", "TEXT"),
                speechConfig = "Aoede"
            )

            val systemInstruction = """
                You are an intelligent voice assistant that manages the user's ToDo list in real time.
                When the user speaks to you, call the appropriate tools (addTodo, removeTodo, toggleTodoStatus, getTodoList)
                to execute their intent, and respond concisely with natural spoken feedback.
            """.trimIndent()

            try {
                // In actual deployment with Firebase AI Vertex Backend:
                // val generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI()).liveModel(
                //     "gemini-2.5-flash-native-audio-preview-12-2025",
                //     generationConfig = liveGenerationConfig,
                //     systemInstruction = systemInstruction,
                //     tools = listOf(
                //         Tool.functionDeclarations(
                //             listOf(getTodoList, addTodo, removeTodo, toggleTodoStatus),
                //         ),
                //     ),
                // )
                // session = generativeModel.connect()

                session = object : LiveSession {
                    override suspend fun sendAudioChunk(bytes: ByteArray) {
                        Log.d(TAG, "Sent audio chunk of size: ${bytes.size}")
                    }

                    override suspend fun sendText(text: String) {
                        Log.d(TAG, "Sent user prompt: $text")
                    }

                    override suspend fun close() {
                        Log.d(TAG, "Session closed")
                    }
                }

                liveSessionState.value = LiveSessionState.Connected
                Log.i(TAG, "Connected to Gemini Live Model session")
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to the model", e)
                liveSessionState.value = LiveSessionState.Error
            }
        }
    }

    /**
     * Executes tool calls requested by Gemini
     */
    fun handleFunctionCall(functionName: String, arguments: Map<String, Any>): Map<String, Any> {
        return when (functionName) {
            "getTodoList" -> {
                mapOf("todos" to _todoList.value)
            }
            "addTodo" -> {
                val title = arguments["title"] as? String ?: "New Task"
                val priority = arguments["priority"] as? String ?: "Medium"
                val newItem = TodoItem(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    isCompleted = false,
                    priority = priority
                )
                _todoList.value = _todoList.value + newItem
                mapOf("success" to true, "addedId" to newItem.id, "title" to newItem.title)
            }
            "removeTodo" -> {
                val id = arguments["id"] as? String ?: ""
                _todoList.value = _todoList.value.filterNot { it.id == id }
                mapOf("success" to true, "removedId" to id)
            }
            "toggleTodoStatus" -> {
                val id = arguments["id"] as? String ?: ""
                _todoList.value = _todoList.value.map {
                    if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
                }
                mapOf("success" to true, "toggledId" to id)
            }
            else -> mapOf("error" to "Unknown function: $functionName")
        }
    }

    fun stopLiveVoiceSession() {
        viewModelScope.launch {
            try {
                session?.close()
                session = null
                liveSessionState.value = LiveSessionState.Idle
            } catch (e: Exception) {
                Log.e(TAG, "Error closing session", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLiveVoiceSession()
    }
}
