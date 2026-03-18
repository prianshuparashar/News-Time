package com.prianshuparashar.newstime.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "articles", indices = [Index(value = ["queryKey"])])
data class Article(
    @PrimaryKey
    val url: String, // Use URL as unique identifier (deduplicate articles)

    val queryKey: String

    // Additional fields for article data, caching metadata
)