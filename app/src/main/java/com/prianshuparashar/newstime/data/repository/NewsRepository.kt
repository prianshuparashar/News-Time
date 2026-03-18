package com.prianshuparashar.newstime.data.repository

import com.prianshuparashar.newstime.common.dispatcher.DispatcherProvider
import com.prianshuparashar.newstime.common.networkhelper.NetworkHelper
import com.prianshuparashar.newstime.data.network.APIService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val apiService: APIService,
    private val networkHelper: NetworkHelper,
    private val dispatcherProvider: DispatcherProvider
) {
    // Repository methods to fetch news data
}