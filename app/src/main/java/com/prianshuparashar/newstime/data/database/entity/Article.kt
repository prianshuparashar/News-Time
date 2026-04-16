package com.prianshuparashar.newstime.data.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.prianshuparashar.newstime.common.constant.Const

@Entity(tableName = "articles", indices = [Index(value = ["queryKey"])])
data class Article(
    @PrimaryKey
    val url: String, // Use URL as unique identifier (deduplicate articles)

    @Embedded
    val source: Source?,

    val author: String?,
    val title: String?,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?,

    // Caching metadata
    val queryKey: String = "", // Composite key for caching (e.g., "top_headlines_us", "search_car")
    val page: Int = Const.INITIAL_PAGE,
    val cachedAt: Long = System.currentTimeMillis(),
    val category: String = "" // "top_headlines", "search", "source"
)