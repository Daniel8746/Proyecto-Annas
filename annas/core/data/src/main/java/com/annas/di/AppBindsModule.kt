package com.annas.di

import com.annas.data.sensor.ShakeDetector
import com.annas.data.sensor.ShakeDetectorImpl
import com.annas.data.services.chatbot.ChatBotService
import com.annas.data.services.chatbot.ChatBotServiceImpl
import com.annas.data.services.interceptors.NetworkMonitor
import com.annas.data.services.interceptors.NetworkMonitorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        impl: NetworkMonitorImpl
    ): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindShakeDetector(
        impl: ShakeDetectorImpl
    ): ShakeDetector

    @Binds
    @Singleton
    abstract fun bindChatBotService(
        impl: ChatBotServiceImpl
    ): ChatBotService
}