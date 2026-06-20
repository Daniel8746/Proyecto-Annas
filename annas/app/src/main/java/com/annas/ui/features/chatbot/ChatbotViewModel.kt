package com.annas.ui.features.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annas.data.extensions.updateState
import com.annas.data.repositorys.ChatBotRepository
import com.annas.model.ChatMessageUi
import com.annas.model.ChatbotUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val repository: ChatBotRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState = _uiState.asStateFlow()

    private var nextMessageId = 2L

    fun onChatbotEvent(event: ChatbotEvent) {
        when (event) {
            ChatbotEvent.OnOpen -> _uiState.updateState { copy(isOpen = true) }
            ChatbotEvent.OnClose -> _uiState.updateState { copy(isOpen = false) }

            is ChatbotEvent.OnInputChange -> {
                _uiState.updateState { copy(input = event.value) }
            }

            is ChatbotEvent.OnSendMessage -> {
                viewModelScope.launch {
                    sendCurrentMessage()
                }
            }
        }
    }

    private suspend fun sendCurrentMessage() {
        try {
            val message = _uiState.value.input.trim()
            if (message.isEmpty()) return

            _uiState.updateState {
                copy(
                    input = "",
                    messages = messages.adding(
                        ChatMessageUi(
                            id = nextMessageId++,
                            text = message,
                            isFromUser = true
                        )
                    )
                )
            }

            val botResponse = repository.sendMessage(message, _uiState.value.messages)

            _uiState.updateState {
                copy(
                    messages = messages.adding(
                        ChatMessageUi(
                            id = nextMessageId++,
                            text = botResponse,
                            isFromUser = false
                        )
                    )
                )
            }
        } catch (_ : Exception) {
            _uiState.updateState {
                copy(
                    messages = messages.adding(
                        ChatMessageUi(
                            id = nextMessageId++,
                            text = "Lo siento, no he podido procesar tu solicitud. Inténtalo de nuevo en unos minutos.",
                            isFromUser = false
                        )
                    )
                )
            }
        }
    }
}
