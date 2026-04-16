package com.prianshuparashar.newstime.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prianshuparashar.newstime.common.constant.Const
import com.prianshuparashar.newstime.data.mapper.CacheCategory
import com.prianshuparashar.newstime.data.mapper.generateQueryKey
import com.prianshuparashar.newstime.data.model.ApiArticle
import com.prianshuparashar.newstime.data.model.NewsResult
import com.prianshuparashar.newstime.data.repository.NewsRepository
import com.prianshuparashar.newstime.ui.base.PaginationState
import com.prianshuparashar.newstime.ui.base.UIEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel for news-related screens with offline-first pagination support.
 */
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    // Public State (observed by UI)

    private val _paginationState =
        MutableStateFlow<PaginationState<ApiArticle>>(PaginationState.Idle)
    val paginationState: StateFlow<PaginationState<ApiArticle>> = _paginationState.asStateFlow()

    private val _events = MutableSharedFlow<UIEvent>()
    val events = _events.asSharedFlow()

    private val _hasNewContent = MutableStateFlow(false)
    val hasNewContent: StateFlow<Boolean> = _hasNewContent.asStateFlow()

    // Private Pagination State

    private var currentPage = Const.INITIAL_PAGE
    private var isLoadingMore = false
    private var hasMorePages = true
    private val currentArticles = mutableListOf<ApiArticle>()
    private var lastQuery: NewsQuery? = null


    // Private Smart-Refresh State

    /** Background-fetched articles held until the user chooses to apply or dismiss them. */
    private data class PendingRefresh(
        val articles: List<ApiArticle>,
        val timestamp: Long = System.currentTimeMillis()
    )

    private var pendingRefresh: PendingRefresh? = null
    private var isNotificationShown = false

    // Public API: Data Loading

    fun fetchTopHeadlinesWithPagination(
        country: String? = Const.DEFAULT_COUNTRY,
        language: String? = Const.DEFAULT_LANGUAGE,
        category: String? = null,
        sources: String? = null,
        loadMore: Boolean = false
    ) = executePaginatedFetch(
        NewsQuery.TopHeadlines(country, language, category, sources),
        loadMore
    )

    fun searchNewsWithPagination(
        query: String,
        language: String? = Const.DEFAULT_LANGUAGE,
        loadMore: Boolean = false
    ) {
        if (query.isBlank()) {
            _paginationState.value = PaginationState.Error(Const.ERROR_EMPTY_QUERY, true)
            return
        }
        executePaginatedFetch(NewsQuery.Search(query, language), loadMore)
    }

    fun fetchArticlesWithPagination(
        query: String? = null,
        sourceId: String? = null,
        language: String? = Const.DEFAULT_LANGUAGE,
        loadMore: Boolean = false
    ) = executePaginatedFetch(NewsQuery.Everything(query, sourceId, language), loadMore)

    /** Clears cache and re-fetches from the network, discarding any pending smart-refresh. */
    fun forceRefresh() {
        val query = lastQuery ?: return
        clearNotificationState()
        viewModelScope.launch {
            newsRepository.forceRefresh(buildCacheQueryKey(query))
            executePaginatedFetch(query)
        }
    }

    fun retryLoadMore() {
        lastQuery?.let { executePaginatedFetch(it, loadMore = true) }
    }

    // Public API: User Actions

    fun onArticleClicked(article: ApiArticle) {
        val url = article.url ?: return
        viewModelScope.launch { _events.emit(UIEvent.OpenCustomTabs(url)) }
    }

    /** Replaces the current list with background-fetched data and resets pagination to page 1. */
    fun applyPendingRefresh() {
        pendingRefresh?.let { pending ->
            currentArticles.clear()
            currentArticles.addAll(pending.articles)
            _paginationState.value = PaginationState.Success(
                data = currentArticles.toList(),
                hasMore = pending.articles.size >= Const.PAGE_SIZE,
                currentPage = Const.INITIAL_PAGE,
                totalResults = pending.articles.size
            )
            clearNotificationState()
            currentPage = Const.INITIAL_PAGE
        }
    }

    /** Discards pending background data; user continues viewing the current list. */
    fun dismissNewContentNotification() {
        clearNotificationState()
    }

    // Private: Pagination Pipeline

    /**
     * Common entry-point for all three public fetch methods.
     * Handles guard, reset, page calculation, loading state, and flow collection.
     */
    private fun executePaginatedFetch(query: NewsQuery, loadMore: Boolean = false) {
        if (!canLoadMore(loadMore)) return

        if (!loadMore || lastQuery != query) {
            resetPagination()
            lastQuery = query
        }

        val page = if (loadMore) currentPage + 1 else Const.INITIAL_PAGE

        _paginationState.value = when {
            loadMore -> PaginationState.LoadingMore
            currentArticles.isEmpty() -> PaginationState.InitialLoading
            else -> _paginationState.value  // keep current state while background fetch runs
        }

        isLoadingMore = true

        buildQueryFlow(query, page)
            .onEach { result -> handleOfflineFirstResult(result, page) }
            .catch { exception -> handlePaginationError(exception, !loadMore) }
            .launchIn(viewModelScope)
    }

    /** Returns the repository [Flow] that matches the given [NewsQuery] type. */
    private fun buildQueryFlow(query: NewsQuery, page: Int): Flow<NewsResult> = when (query) {
        is NewsQuery.TopHeadlines -> newsRepository.getTopHeadlinesOfflineFirst(
            country = query.country,
            category = query.category,
            language = query.language,
            sources = query.sources,
            page = page,
            pageSize = Const.PAGE_SIZE
        )

        is NewsQuery.Search -> newsRepository.searchArticlesOfflineFirst(
            query = query.query,
            language = query.language,
            page = page,
            pageSize = Const.PAGE_SIZE
        )

        is NewsQuery.Everything -> newsRepository.searchArticlesOfflineFirst(
            query = query.query,
            sources = query.sourceId,
            language = query.language,
            page = page,
            pageSize = Const.PAGE_SIZE
        )
    }

    private fun handleOfflineFirstResult(result: NewsResult, page: Int) {
        isLoadingMore = false
        currentPage = page

        checkAndDiscardExpiredPendingRefresh()

        // New data arrived while user views cached content — notify instead of silently replacing
        if (result.hasNewData && currentArticles.isNotEmpty() && !isNotificationShown) {
            pendingRefresh = PendingRefresh(result.articles)
            isNotificationShown = true
            _hasNewContent.value = true
            return
        }

        mergeArticles(result.articles, page)
        hasMorePages = result.articles.size == Const.PAGE_SIZE

        _paginationState.value = PaginationState.Success(
            data = currentArticles.toList(),
            hasMore = hasMorePages,
            currentPage = currentPage,
            totalResults = currentArticles.size
        )
    }

    /**
     * Resets and repopulates on page 1; deduplicates by URL on load-more.
     * Uses [HashSet] for O(1) lookups over a potentially large existing list.
     */
    private fun mergeArticles(newArticles: List<ApiArticle>, page: Int) {
        if (page == Const.INITIAL_PAGE) {
            currentArticles.clear()
            currentArticles.addAll(newArticles)
        } else {
            val seen = currentArticles.mapTo(HashSet()) { it.url }
            currentArticles.addAll(newArticles.filter { it.url !in seen })
        }
    }

    private fun handlePaginationError(exception: Throwable, isInitialLoad: Boolean) {
        isLoadingMore = false

        if (currentArticles.isNotEmpty()) {
            _paginationState.value = PaginationState.Success(
                data = currentArticles.toList(),
                hasMore = hasMorePages,
                currentPage = currentPage,
                totalResults = currentArticles.size
            )
            viewModelScope.launch {
                _events.emit(UIEvent.ShowToast(exception.message ?: Const.ERROR_UNKNOWN))
            }
        } else {
            _paginationState.value = PaginationState.Error(
                message = exception.message ?: Const.ERROR_UNKNOWN,
                isInitialLoad = isInitialLoad
            )
        }
    }

    // Private: Utilities

    /** Maps a [NewsQuery] to its deterministic cache key. */
    private fun buildCacheQueryKey(query: NewsQuery): String = when (query) {
        is NewsQuery.TopHeadlines -> generateQueryKey(
            CacheCategory.TOP_HEADLINES,
            query.country, query.language, null, query.sources, query.category
        )

        is NewsQuery.Search -> generateQueryKey(
            CacheCategory.SEARCH,
            null, query.language, query.query
        )

        is NewsQuery.Everything -> generateQueryKey(
            CacheCategory.SEARCH,
            null, query.language, query.query, query.sourceId
        )
    }

    private fun canLoadMore(loadMore: Boolean) = !loadMore || (!isLoadingMore && hasMorePages)

    /** Discards pending refresh state if it has not been acted on within the expiry window. */
    private fun checkAndDiscardExpiredPendingRefresh() {
        val age = System.currentTimeMillis() - (pendingRefresh?.timestamp ?: return)
        if (age > Const.PENDING_REFRESH_EXPIRY_MS) clearNotificationState()
    }

    /** Resets all smart-refresh notification fields to their initial state. */
    private fun clearNotificationState() {
        pendingRefresh = null
        isNotificationShown = false
        _hasNewContent.value = false
    }

    private fun resetPagination() {
        currentPage = Const.INITIAL_PAGE
        isLoadingMore = false
        hasMorePages = true
        currentArticles.clear()
    }
}

sealed class NewsQuery {
    data class TopHeadlines(
        val country: String?,
        val language: String?,
        val category: String?,
        val sources: String?
    ) : NewsQuery()


    data class Search(
        val query: String,
        val language: String?
    ) : NewsQuery()


    data class Everything(
        val query: String?,
        val sourceId: String?,
        val language: String?
    ) : NewsQuery()
}