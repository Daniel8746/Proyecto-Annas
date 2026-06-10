package com.annas.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

private val InitialChatMessages = persistentListOf(
    ChatMessageUi(
        id = 1L,
        text = "Hola, soy Annas Chat. Â¿En que puedo ayudarte?",
        isFromUser = false
    )
)

@Immutable
data class ChatbotUiState(
    val isOpen: Boolean = false,
    val input: String = "",
    val messages: PersistentList<ChatMessageUi> = InitialChatMessages
)
