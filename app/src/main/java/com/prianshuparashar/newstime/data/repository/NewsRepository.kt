package com.prianshuparashar.newstime.data.repository

import com.prianshuparashar.newstime.common.constant.Const
import com.prianshuparashar.newstime.common.dispatcher.DispatcherProvider
import com.prianshuparashar.newstime.common.exception.NoInternetException
import com.prianshuparashar.newstime.common.networkhelper.NetworkHelper
import com.prianshuparashar.newstime.data.database.DatabaseService
import com.prianshuparashar.newstime.data.database.entity.CacheMetadata
import com.prianshuparashar.newstime.data.mapper.CacheCategory
import com.prianshuparashar.newstime.data.mapper.generateQueryKey
import com.prianshuparashar.newstime.data.mapper.toApiArticles
import com.prianshuparashar.newstime.data.mapper.toEntities
import com.prianshuparashar.newstime.data.model.ApiArticle
import com.prianshuparashar.newstime.data.model.NewsResult
import com.prianshuparashar.newstime.data.model.Sources
import com.prianshuparashar.newstime.data.network.APIService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val apiService: APIService,
    private val networkHelper: NetworkHelper,
    private val databaseService: DatabaseService,
    private val dispatcherProvider: DispatcherProvider
) {

    private val articleDao get() = databaseService.articleDao()
    private val cacheMetadataDao get() = databaseService.cacheMetadataDao()

    fun getTopHeadlinesOfflineFirst(
        sources: String? = null,
        country: String? = Const.DEFAULT_COUNTRY,
        language: String? = Const.DEFAULT_LANGUAGE,
        category: String? = null,
        page: Int = Const.INITIAL_PAGE,
        pageSize: Int = Const.PAGE_SIZE
    ): Flow<NewsResult> = flow {
        val queryKey = generateQueryKey(
            category = CacheCategory.TOP_HEADLINES,
            country = country,
            language = language,
            categoryFilter = category,
            sourceId = sources
        )

        val cachedArticles = articleDao.getArticlesPaginated(queryKey, page, (page - 1) * pageSize)

        if (cachedArticles.isNotEmpty()) {
            emit(NewsResult(cachedArticles.toApiArticles(), isCachedData = true))
        }

        val shouldFetchFromNetwork = shouldRefreshCache(queryKey, page)

        if (shouldFetchFromNetwork || cachedArticles.isEmpty()) {
            fetchAndCacheTopHeadlines(
                queryKey = queryKey,
                sources = sources,
                country = country,
                language = language,
                category = category,
                page = page,
                pageSize = pageSize,
                hasCachedData = cachedArticles.isNotEmpty()
            )?.let { freshArticles ->
                emit(NewsResult(freshArticles, isCachedData = false, cachedArticles.isNotEmpty()))
            }
        }
    }.flowOn(dispatcherProvider.io)

    private suspend fun shouldRefreshCache(queryKey: String, page: Int): Boolean {
        val metaData = cacheMetadataDao.getMetadata(queryKey) ?: return true
        return System.currentTimeMillis() > metaData.expiresAt || page > metaData.currentPage
    }

    private suspend fun fetchAndCacheTopHeadlines(
        queryKey: String,
        sources: String?,
        country: String?,
        language: String?,
        category: String?,
        page: Int,
        pageSize: Int,
        hasCachedData: Boolean
    ): List<ApiArticle>? {
        if (!networkHelper.isNetworkConnected()) {
            if (!hasCachedData) throw NoInternetException()
            return null
        }

        return try {
            val response = apiService.getTopHeadlines(
                country = country,
                category = category,
                sources = sources,
                language = language,
                query = null,
                page = page,
                pageSize = pageSize
            )

            cacheArticles(
                articles = response.articles,
                queryKey = queryKey,
                category = CacheCategory.TOP_HEADLINES,
                page = page,
                totalResults = response.totalResults,
                clearOldData = page == Const.INITIAL_PAGE
            )

            response.articles
        } catch (e: Exception) {
            if (!hasCachedData) throw e
            null
        }
    }

    private suspend fun cacheArticles(
        articles: List<ApiArticle>,
        queryKey: String,
        category: String,
        page: Int,
        totalResults: Int,
        clearOldData: Boolean
    ) {
        if (clearOldData) articleDao.deleteByQueryKey(queryKey)

        val entities = articles.toEntities(queryKey, page, category)
        articleDao.insertAll(entities)

        val now = System.currentTimeMillis()
        cacheMetadataDao.insertMetadata(
            CacheMetadata(
                queryKey = queryKey,
                lastFetchedAt = now,
                totalResults = totalResults,
                expiresAt = now + (Const.Cache.EXPIRY_MINUTES * 60 * 1000),
                category = category,
                currentPage = page
            )
        )
    }

    fun searchArticlesOfflineFirst(
        query: String? = null,
        sources: String? = null,
        language: String? = Const.DEFAULT_LANGUAGE,
        page: Int = Const.INITIAL_PAGE,
        pageSize: Int
    ): Flow<NewsResult> = flow<NewsResult> {
        val queryKey = generateQueryKey(
            category = CacheCategory.SEARCH,
            query = query,
            sourceId = sources,
            language = language
        )

        val cachedArticles =
            articleDao.getArticlesPaginated(queryKey, pageSize, (page - 1) * pageSize)

        if (cachedArticles.isNotEmpty()) {
            emit(NewsResult(cachedArticles.toApiArticles(), isCachedData = true))
        }

        val shouldFetchFromNetwork = shouldRefreshCache(queryKey, page)

        if (shouldFetchFromNetwork || cachedArticles.isEmpty()) {
            fetchAndCacheSearch(
                queryKey = queryKey,
                query = query,
                sources = sources,
                language = language,
                page = page,
                pageSize = pageSize,
                hasCachedData = cachedArticles.isNotEmpty()
            )?.let { freshArticles ->
                emit(NewsResult(freshArticles, isCachedData = false, cachedArticles.isNotEmpty()))
            }
        }
    }.flowOn(dispatcherProvider.io)

    private suspend fun fetchAndCacheSearch(
        queryKey: String,
        query: String?,
        sources: String?,
        language: String?,
        page: Int,
        pageSize: Int,
        hasCachedData: Boolean
    ): List<ApiArticle>? {
        if (!networkHelper.isNetworkConnected()) {
            if (!hasCachedData) throw NoInternetException()
            return null
        }

        return try {
            val response = apiService.getEverything(query, sources, language, null, page, pageSize)

            cacheArticles(
                articles = response.articles,
                queryKey = queryKey,
                category = CacheCategory.SEARCH,
                page = page,
                totalResults = response.totalResults,
                clearOldData = page == Const.INITIAL_PAGE
            )

            response.articles
        } catch (e: Exception) {
            if (!hasCachedData) throw e
            null
        }
    }

    suspend fun forceRefresh(queryKey: String) {
        articleDao.deleteByQueryKey(queryKey)
        cacheMetadataDao.deleteMetadata(queryKey)
    }

    fun getSources(
        country: String? = null,
        category: String? = null,
        language: String? = null
    ): Flow<Sources> = flow {
        if (!networkHelper.isNetworkConnected()) throw NoInternetException()
        emit(apiService.getSources(country, category, language))
    }.flowOn(dispatcherProvider.io)
}