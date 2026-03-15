package com.pmdm.annas.di

import android.content.Context
import com.pmdm.annas.data.scraper.Scraper
import com.pmdm.annas.data.scraper.WebViewScraper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideScraperInstance(webViewScraper: WebViewScraper): Scraper = Scraper(webViewScraper)

    @Provides
    @Singleton
    fun provideWebViewInstance(@ApplicationContext context: Context): WebViewScraper =
        WebViewScraper(context)
}