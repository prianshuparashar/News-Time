package com.prianshuparashar.newstime.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prianshuparashar.newstime.data.database.entity.CacheMetadata

@Dao
interface CacheMetadataDao {
    @Query("SELECT * FROM cache_metadata WHERE queryKey = :queryKey")
    suspend fun getMetadata(queryKey: String): CacheMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: CacheMetadata)

    @Query("DELETE FROM cache_metadata WHERE queryKey = :queryKey")
    suspend fun deleteMetadata(queryKey: String)
}