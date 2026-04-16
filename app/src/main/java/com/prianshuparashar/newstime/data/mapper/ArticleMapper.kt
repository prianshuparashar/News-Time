package com.prianshuparashar.newstime.data.mapper

import com.prianshuparashar.newstime.data.database.entity.Article
import com.prianshuparashar.newstime.data.database.entity.Source
import com.prianshuparashar.newstime.data.model.ApiArticle
import com.prianshuparashar.newstime.data.model.ApiSource

object CacheCategory {
    const val TOP_HEADLINES = "top_headlines"
    const val SEARCH = "search"
}

fun generateQueryKey(
    category: String? = null,
    country: String? = null,
    language: String? = null,
    query: String? = null,
    sourceId: String? = null,
    categoryFilter: String? = null
): String = buildString {
    append(category)
    country?.let { append("_$it") }
    language?.let { append("_$it") }
    query?.let { append("_$it") }
    sourceId?.let { append("_$it") }
    categoryFilter?.let { append("_$it") }
}

fun List<Article>.toApiArticles(): List<ApiArticle> = map { it.toApiArticle() }

fun Article.toApiArticle(): ApiArticle = ApiArticle(
    url = url,
    source = source?.let {
        ApiSource(
            id = it.id,
            name = it.name,
            description = null,
            url = null,
            category = null,
            language = null,
            country = null
        )
    },
    author = author,
    title = title,
    description = description,
    urlToImage = urlToImage,
    publishedAt = publishedAt,
    content = content
)

fun List<ApiArticle>.toEntities(queryKey: String, page: Int, category: String): List<Article> =
    mapNotNull { article ->
        if (article.url.isNullOrBlank()) null
        else article.toEntity(queryKey, page, category)
    }

fun ApiArticle.toEntity(queryKey: String, page: Int, category: String): Article = Article(
    url = url ?: "",
    source = source?.toEntitySource(),
    author = author,
    title = title ?: "",
    description = description,
    urlToImage = urlToImage,
    publishedAt = publishedAt,
    content = content,
    category = category,
    queryKey = queryKey,
    cachedAt = System.currentTimeMillis(),
    page = page
)

fun ApiSource.toEntitySource(): Source = Source(id = id, name = name)