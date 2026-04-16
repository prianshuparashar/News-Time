package com.prianshuparashar.newstime.data.network

import com.prianshuparashar.newstime.common.constant.Const
import com.prianshuparashar.newstime.data.model.News
import com.prianshuparashar.newstime.data.model.Sources
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String? = null,
        @Query("category") category: String? = null,
        @Query("sources") sources: String? = null,
        @Query("language") language: String? = null,
        @Query("q") query: String? = null,
        @Query("page") page: Int = Const.INITIAL_PAGE,
        @Query("pageSize") pageSize: Int = Const.PAGE_SIZE
    ): News

    @GET("everything")
    suspend fun getEverything(
        @Query("q") query: String? = null,
        @Query("sources") sources: String? = null,
        @Query("language") language: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("page") page: Int = Const.INITIAL_PAGE,
        @Query("pageSize") pageSize: Int = Const.PAGE_SIZE
    ): News

    @GET("sources")
    suspend fun getSources(
        @Query("country") country: String? = null,
        @Query("category") category: String? = null,
        @Query("language") language: String? = null
    ): Sources
}