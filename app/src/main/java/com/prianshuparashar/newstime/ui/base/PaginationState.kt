package com.prianshuparashar.newstime.ui.base

sealed class PaginationState<out T> {
    data object Idle : PaginationState<Nothing>()
    data object InitialLoading : PaginationState<Nothing>()
    data object LoadingMore : PaginationState<Nothing>()

    data class Success<T>(
        val data: List<T>,
        val hasMore: Boolean,
        val currentPage: Int,
        val totalResults: Int? = null
    ) : PaginationState<T>()

    data class Error(val message: String, val isInitialLoad: Boolean) : PaginationState<Nothing>()
}