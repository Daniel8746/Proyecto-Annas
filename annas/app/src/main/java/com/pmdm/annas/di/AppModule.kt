package com.pmdm.annas.di

import android.content.Context
import com.pmdm.annas.data.scraper.Scraper
import com.pmdm.annas.data.scraper.WebViewScraper
import com.pmdm.annas.download.SilentDownloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Named("scraperClient")
    @Provides
    @Singleton
    fun provideScraperClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

    @Named("downloadClient")
    @Provides
    @Singleton
    fun provideDownloadClient(): OkHttpClient =
        OkHttpClient.Builder()
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
        @ApplicationContext context: Context
    ): Scraper = Scraper(webViewScraper, okHttpClient, context)

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
}
