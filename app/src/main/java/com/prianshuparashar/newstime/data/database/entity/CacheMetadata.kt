package com.prianshuparashar.newstime.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_metadata")
data class CacheMetadata(
    @PrimaryKey
    val queryKey: String, // Unique identifier for query (e.g., "top_headlines_us", "search_bitcoin")

    // Additional fields for cache metadata (e.g., timestamp, totalResults, etc.) as needed
)