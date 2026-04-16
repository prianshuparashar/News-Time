package com.prianshuparashar.newstime.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prianshuparashar.newstime.common.constant.Const

@Entity(tableName = "cache_metadata")
data class CacheMetadata(
    @PrimaryKey val
    queryKey: String, // Unique identifier for query (e.g., "top_headlines_us", "search_bitcoin"

    val expiresAt: Long, // Timestamp in milliseconds when the cache expires
    val currentPage: Int = Const.INITIAL_PAGE, // Last page that was cached for this query

    val lastFetchedAt: Long, // Timestamp of last network fetch
    val totalResults: Int, // Total Results from API
    val category: String // "top_headlines", "search", "source"
)