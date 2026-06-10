package com.annas.ui.features.chatbot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.annas.ui.features.chatbot.components.ChatbotLauncher
import com.annas.ui.features.chatbot.components.ChatbotPopup

@Composable
fun ChatbotOverlay(
    modifier: Modifier = Modifier,
    viewModel: ChatbotViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        content()

        ChatbotLauncher(
            onClick = { viewModel.onChatbotEvent(ChatbotEvent.OnOpen) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = 18.dp)
        )

        if (uiState.isOpen) {
            ChatbotPopup(
                uiState = uiState,
                onChatbotEvent = viewModel::onChatbotEvent
            )
        }
    }
}
