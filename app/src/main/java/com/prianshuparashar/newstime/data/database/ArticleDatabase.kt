package com.prianshuparashar.newstime.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prianshuparashar.newstime.common.constant.Const
import com.prianshuparashar.newstime.data.database.dao.ArticleDao
import com.prianshuparashar.newstime.data.database.dao.CacheMetadataDao
import com.prianshuparashar.newstime.data.database.entity.Article
import com.prianshuparashar.newstime.data.database.entity.CacheMetadata

@Database(
    entities = [Article::class, CacheMetadata::class],
    version = Const.DATABASE_VERSION,
    exportSchema = false
)
abstract class ArticleDatabase : RoomDatabase(), DatabaseService {
    abstract override fun articleDao(): ArticleDao
    abstract override fun cacheMetadataDao(): CacheMetadataDao
}