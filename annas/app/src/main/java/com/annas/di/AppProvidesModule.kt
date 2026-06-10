package com.annas.di

import android.content.Context
import com.annas.data.cache.MemoryCache
import com.annas.data.ia.setupModel
import com.annas.data.services.interceptors.ConnectVerifierInterceptor
import com.annas.data.services.interceptors.NetworkMonitor
import com.google.firebase.ai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {

    @Provides
    @Singleton
    fun provideMemoryCache(): MemoryCache {
        return MemoryCache()
    }

    @Named("scraperClient")
    @Provides
    @Singleton
    fun provideScraperClient(
        @ApplicationContext context: Context,
        networkMonitor: NetworkMonitor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(ConnectVerifierInterceptor(networkMonitor))
            .cache(Cache(File(context.cacheDir, "scraper_http"), 12L * 1024L * 1024L))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

    @Provides
    @Singleton
    fun provideDispatcher(): Dispatcher =
        Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 20
        }

    @Named("downloadClient")
    @Provides
    @Singleton
    fun provideDownloadClient(
        networkMonitor: NetworkMonitor,
        dispatcher: Dispatcher
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(ConnectVerifierInterceptor(networkMonitor))
            .dispatcher(dispatcher)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()

    @Provides
    @Singleton
    fun provideGenerativeModel(
        @ApplicationContext context: Context
    ): GenerativeModel {
        return setupModel(context)
    }
}
