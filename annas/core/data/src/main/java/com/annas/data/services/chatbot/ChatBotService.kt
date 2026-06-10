package com.annas.data.services.chatbot

import com.annas.model.ChatMessageUi
import kotlinx.collections.immutable.PersistentList

interface ChatBotService {
    suspend fun sendMessage(
        prompt: String,
        history: PersistentList<ChatMessageUi>
    ): String
}