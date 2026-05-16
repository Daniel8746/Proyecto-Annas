package com.annas.di

import android.content.Context
import com.annas.data.cache.MemoryCache
import com.annas.data.network.SilentDownloader
import com.annas.data.notifications.NotificationHelper
import com.annas.data.scraper.Scraper
import com.annas.data.scraper.WebViewScraper
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
object AppModule {

    @Provides
    @Singleton
    fun provideMemoryCache(): MemoryCache {
        return MemoryCache()
    }

    @Named("scraperClient")
    @Provides
    @Singleton
    fun provideScraperClient(
        @ApplicationContext context: Context
    ): OkHttpClient =
        OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, "scraper_http"), 12L * 1024L * 1024L))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

    private val dispatcher = Dispatcher().apply {
        maxRequests = 64
        maxRequestsPerHost = 20
    }

    @Named("downloadClient")
    @Provides
    @Singleton
    fun provideDownloadClient(): OkHttpClient =
        OkHttpClient.Builder()
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
    fun provideScraper(
        webViewScraper: WebViewScraper,
        @Named("scraperClient") okHttpClient: OkHttpClient,
        @ApplicationContext context: Context,
        memoryCache: MemoryCache
    ): Scraper = Scraper(webViewScraper, okHttpClient, context, memoryCache)

    @Provides
    @Singleton
    fun provideWebViewScraper(@ApplicationContext context: Context): WebViewScraper =
        WebViewScraper(context)

    @Provides
    @Singleton
    fun provideSilentDownloader(
        @ApplicationContext context: Context,
        @Named("downloadClient") okHttpClient: OkHttpClient
    ): SilentDownloader = SilentDownloader(context, okHttpClient)

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper =
        NotificationHelper(context)
}
