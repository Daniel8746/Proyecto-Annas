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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideScraperInstance(
        webViewScraper: WebViewScraper,
        okHttpClient: OkHttpClient
    ): Scraper = Scraper(webViewScraper, okHttpClient)

    @Provides
    @Singleton
    fun provideWebViewInstance(@ApplicationContext context: Context): WebViewScraper =
        WebViewScraper(context)
}
