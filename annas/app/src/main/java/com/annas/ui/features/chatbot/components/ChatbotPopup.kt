package com.annas.ui.features.chatbot.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.annas.model.ChatbotUiState
import com.annas.ui.features.chatbot.ChatbotEvent
import com.annas.ui.features.chatbot.ChatbotScreen

@Composable
fun ChatbotPopup(
    uiState: ChatbotUiState,
    onChatbotEvent: (ChatbotEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = { onChatbotEvent(ChatbotEvent.OnClose) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            ChatbotScreen(
                messages = uiState.messages,
                input = uiState.input,
                onInputChange = { onChatbotEvent(ChatbotEvent.OnInputChange(it)) },
                onSend = { onChatbotEvent(ChatbotEvent.OnSendMessage) },
                onClose = { onChatbotEvent(ChatbotEvent.OnClose) },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .fillMaxHeight(0.82f)
            )
        }
    }
}
