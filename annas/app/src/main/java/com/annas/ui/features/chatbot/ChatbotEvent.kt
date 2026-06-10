package com.annas.ui.features.chatbot

sealed interface ChatbotEvent {
    data object OnOpen : ChatbotEvent
    data object OnClose : ChatbotEvent
    data object OnSendMessage : ChatbotEvent

    data class OnInputChange(
        val value: String
    ) : ChatbotEvent
}
