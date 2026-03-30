package com.pmdm.annas.di

import android.content.Context
import com.pmdm.annas.data.scraper.Scraper
import com.pmdm.annas.data.scraper.WebViewScraper
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
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

    @Named("downloadClient")
    @Provides
    @Singleton
    fun provideDownloadClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS) // tiempo largo porque los mirrors son lentos
            .writeTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()

    @Provides
    @Singleton
    fun provideScraperInstance(
        webViewScraper: WebViewScraper,
        @Named("scraperClient") okHttpClient: OkHttpClient,
        @ApplicationContext context: Context
    ): Scraper = Scraper(webViewScraper, okHttpClient, context)

    @Provides
    @Singleton
    fun provideWebViewInstance(@ApplicationContext context: Context): WebViewScraper =
        WebViewScraper(context)
}
