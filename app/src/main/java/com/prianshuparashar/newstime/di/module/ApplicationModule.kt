package com.prianshuparashar.newstime.di.module

import android.content.Context
import com.prianshuparashar.newstime.BuildConfig
import com.prianshuparashar.newstime.NewsApplication
import com.prianshuparashar.newstime.common.constant.Const
import com.prianshuparashar.newstime.common.dispatcher.DefaultDispatcherProvider
import com.prianshuparashar.newstime.common.dispatcher.DispatcherProvider
import com.prianshuparashar.newstime.common.networkhelper.NetworkHelper
import com.prianshuparashar.newstime.common.networkhelper.NetworkHelperImpl
import com.prianshuparashar.newstime.data.network.APIKeyInterceptor
import com.prianshuparashar.newstime.data.network.APIService
import com.prianshuparashar.newstime.di.qualifiers.ApplicationContext
import com.prianshuparashar.newstime.di.qualifiers.BaseUrl
import com.prianshuparashar.newstime.di.qualifiers.NetworkApiKey
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: NewsApplication) {
    @Provides
    @Singleton
    @ApplicationContext
    fun provideContext(): Context = application

    @Provides
    @Singleton
    fun provideAPIService(retrofit: Retrofit): APIService {
        return retrofit.create(APIService::class.java)
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient, @BaseUrl baseUrl: String
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor, apiKeyInterceptor: APIKeyInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .connectTimeout(Const.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Const.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Const.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @BaseUrl
    fun provideBaseUrl(): String = Const.BASE_URL

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideAPIKeyInterceptor(@NetworkApiKey apiKey: String): APIKeyInterceptor {
        return APIKeyInterceptor(apiKey)
    }

    @Provides
    @Singleton
    @NetworkApiKey
    fun provideApiKey(): String = BuildConfig.API_KEY

    @Provides
    @Singleton
    fun provideNetworkHelper(networkHelperImpl: NetworkHelperImpl): NetworkHelper {
        return networkHelperImpl
    }

    @Provides
    @Singleton
    fun provideDispatcherProvider(defaultDispatcherProvider: DefaultDispatcherProvider): DispatcherProvider {
        return defaultDispatcherProvider
    }
}