package com.tagmypet.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.local.TokenManager
import com.tagmypet.data.remote.SocketManager
import com.tagmypet.data.repository.ChatRepository
import com.tagmypet.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Message(
    val id: String,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: String,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val socketManager: SocketManager,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val otherUserId: String = savedStateHandle["chatId"] ?: ""
    private var myUserId: String = ""

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _chatTitle = MutableStateFlow("Chat")
    val chatTitle: StateFlow<String> = _chatTitle.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        socketManager.connect()
        viewModelScope.launch {
            myUserId = tokenManager.getUserId().first() ?: ""
            loadHistory()
        }
        observeSocketMessages()
    }

    private suspend fun loadHistory() {
        when (val result = chatRepository.getMessages(otherUserId)) {
            is Resource.Success -> {
                val dtos = result.data ?: emptyList()
                // Tenta pegar o nome do outro usuário da primeira mensagem recebida
                val otherUser = dtos.firstOrNull { it.sender.id == otherUserId }?.sender
                if (otherUser != null) _chatTitle.value = otherUser.name

                _messages.value = dtos.map { dto ->
                    Message(
                        id = dto._id,
                        text = dto.text,
                        isFromMe = dto.sender.id == myUserId,
                        timestamp = extractTime(dto.createdAt)
                    )
                }
            }

            else -> {}
        }
    }

    private fun observeSocketMessages() {
        viewModelScope.launch {
            socketManager.messageFlow.collect { json ->
                val senderId = json.getJSONObject("sender").getString("_id")
                // Só adiciona se a mensagem vier da pessoa com quem estou falando
                if (senderId == otherUserId) {
                    val newMsg = Message(
                        id = json.getString("_id"),
                        text = json.getString("text"),
                        isFromMe = false,
                        timestamp = extractTime(json.getString("createdAt"))
                    )
                    _messages.value = _messages.value + newMsg
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val result = chatRepository.sendMessage(otherUserId, text)
            if (result is Resource.Success) {
                val dto = result.data!!
                val myMsg = Message(
                    id = dto._id,
                    text = dto.text,
                    isFromMe = true,
                    timestamp = extractTime(dto.createdAt)
                )
                _messages.value = _messages.value + myMsg
            }
        }
    }

    private fun extractTime(iso: String): String = try {
        iso.substring(11, 16)
    } catch (e: Exception) {
        ""
    }
}