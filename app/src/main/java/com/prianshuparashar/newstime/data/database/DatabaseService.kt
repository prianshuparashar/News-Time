package com.prianshuparashar.newstime.data.database

import com.prianshuparashar.newstime.data.database.dao.ArticleDao
import com.prianshuparashar.newstime.data.database.dao.CacheMetadataDao

interface DatabaseService {
    fun articleDao(): ArticleDao
    fun cacheMetadataDao(): CacheMetadataDao
}