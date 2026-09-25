package com.annas.di

import android.content.Context
import com.annas.data.cache.MemoryCache
import com.annas.data.chatbot.setupModel
import com.annas.data.services.interceptors.ConnectVerifierInterceptor
import com.annas.data.services.interceptors.NetworkMonitor
import com.google.firebase.ai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {

    @Provides
    @Singleton
    fun provideMemoryCache(): MemoryCache = MemoryCache()

    val tlsSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
        .build()

    @Provides
    @Singleton
    fun provideBrowserInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                .header("Cache-Control", "max-age=0")
                .build()
            chain.proceed(request)
        }
    }

    @Named("scraperClient")
    @Provides
    @Singleton
    fun provideScraperClient(
        @ApplicationContext context: Context,
        networkMonitor: NetworkMonitor,
        browserInterceptor: Interceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(ConnectVerifierInterceptor(networkMonitor))
            .addInterceptor(browserInterceptor)
            .connectionSpecs(listOf(tlsSpec))
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
        dispatcher: Dispatcher,
        browserInterceptor: Interceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(ConnectVerifierInterceptor(networkMonitor))
            .addInterceptor(browserInterceptor)
            .dispatcher(dispatcher)
            .connectionSpecs(listOf(tlsSpec))
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
    ): GenerativeModel = setupModel(context)
}
