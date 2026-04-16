package com.prianshuparashar.newstime.data.model

data class NewsResult(
    val articles: List<ApiArticle>,
    val isCachedData: Boolean = false,
    val hasNewData: Boolean = false
)
