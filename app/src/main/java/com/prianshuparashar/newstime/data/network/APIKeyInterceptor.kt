package com.prianshuparashar.newstime.data.network

import com.prianshuparashar.newstime.common.constant.Const
import okhttp3.Interceptor
import okhttp3.Response

class APIKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // Append the API key as a query parameter to the original URL
        val urlWithApiKey = originalUrl.newBuilder()
            .addQueryParameter(Const.API_KEY_PARAM, apiKey)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(urlWithApiKey)
            .build()

        return chain.proceed(newRequest)
    }
}