package com.annas.data.services.chatbot

import com.annas.model.ChatMessageUi
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.content
import kotlinx.collections.immutable.PersistentList
import javax.inject.Inject

class ChatBotServiceImpl @Inject constructor(
    private val model: GenerativeModel
) : ChatBotService {

    override suspend fun sendMessage(
        prompt: String,
        history: PersistentList<ChatMessageUi>
    ): String {
        val contents = buildList {

            history.forEach { message ->
                add(
                    content {
                        role = if (message.isFromUser) "user" else "model"
                        text(message.text)
                    }
                )
            }

            add(
                content {
                    role = "user"
                    text(prompt)
                }
            )
        }

        val response = model.generateContent(contents)

        return response.text ?: ""
    }
}