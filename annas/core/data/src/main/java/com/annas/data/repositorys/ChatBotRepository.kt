package com.annas.data.repositorys

import com.annas.data.services.chatbot.ChatBotService
import com.annas.model.ChatMessageUi
import kotlinx.collections.immutable.PersistentList
import javax.inject.Inject

class ChatBotRepository @Inject constructor(
    private val service: ChatBotService
) {
    suspend fun sendMessage(
        prompt: String,
        history: PersistentList<ChatMessageUi>
    ): String {
        return service.sendMessage(prompt, history)
    }
}