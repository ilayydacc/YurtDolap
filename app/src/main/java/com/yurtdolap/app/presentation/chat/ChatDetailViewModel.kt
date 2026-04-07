package com.yurtdolap.app.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.Message
import com.yurtdolap.app.domain.repository.AuthRepository
import com.yurtdolap.app.domain.repository.ChatRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String? = savedStateHandle["chatId"]

    private val _uiState = MutableStateFlow<UIState<List<Message>>>(UIState.Loading)
    val uiState: StateFlow<UIState<List<Message>>> = _uiState.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    val currentUserId: String = authRepository.currentUserId ?: ""

    init {
        if (chatId.isNullOrBlank()) {
            _uiState.value = UIState.Error("Sohbet bilgisi eksik.")
        } else {
            listenForMessages()
        }
    }

    private fun listenForMessages() {
        val safeChatId = chatId ?: return
        viewModelScope.launch {
            chatRepository.getChatMessages(safeChatId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.value = UIState.Success(resource.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        _uiState.value = UIState.Error(resource.message ?: "Mesajlar alınamadı")
                    }
                    else -> {}
                }
            }
        }
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    fun sendMessage() {
        val safeChatId = chatId ?: return
        val text = _messageText.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _messageText.value = "" // clear input quickly for UX
            val result = chatRepository.sendMessage(safeChatId, text)
            if (result is Resource.Error) {
                // optionally handle error, e.g., show to user
                _messageText.value = text // put back if failed
            }
        }
    }
}
