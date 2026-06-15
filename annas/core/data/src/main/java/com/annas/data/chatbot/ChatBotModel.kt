package com.annas.data.chatbot

import android.content.Context
import com.annas.data.extensions.loadPrompt
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.HarmBlockThreshold
import com.google.firebase.ai.type.HarmCategory
import com.google.firebase.ai.type.SafetySetting
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.collections.immutable.persistentListOf

fun setupModel(context: Context): GenerativeModel {
    val systemPrompt = context.loadPrompt("prompts/book_assistant_system_prompt.txt")

    val generationConfig = generationConfig {
        temperature = 0.85f
        topK = 40
        topP = 0.92f
        maxOutputTokens = 2048
    }

    val safetySettings = persistentListOf(
        SafetySetting(
            harmCategory = HarmCategory.HARASSMENT,
            threshold = HarmBlockThreshold.ONLY_HIGH
        ),

        SafetySetting(
            harmCategory = HarmCategory.HATE_SPEECH,
            threshold = HarmBlockThreshold.ONLY_HIGH
        ),

        SafetySetting(
            harmCategory = HarmCategory.SEXUALLY_EXPLICIT,
            threshold = HarmBlockThreshold.MEDIUM_AND_ABOVE
        ),

        SafetySetting(
            harmCategory = HarmCategory.DANGEROUS_CONTENT,
            threshold = HarmBlockThreshold.ONLY_HIGH
        )
    )

    return Firebase.ai(
        backend = GenerativeBackend.googleAI()
    ).generativeModel(
        modelName = "gemini-3.5-flash",
        generationConfig = generationConfig,
        safetySettings = safetySettings,
        systemInstruction = content {
            text(systemPrompt)
        }
    )
}