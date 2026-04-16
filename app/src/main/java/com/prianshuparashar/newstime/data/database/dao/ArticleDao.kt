package com.prianshuparashar.newstime.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prianshuparashar.newstime.data.database.entity.Article

@Dao
interface ArticleDao {
    @Query(
        """
        SELECT * FROM articles
        WHERE queryKey = :queryKey
        ORDER BY page ASC, cachedAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getArticlesPaginated(
        queryKey: String,
        limit: Int,
        offset: Int
    ): List<Article>

    @Query("DELETE FROM articles WHERE queryKey = :queryKey")
    suspend fun deleteByQueryKey(queryKey: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<Article>)
}