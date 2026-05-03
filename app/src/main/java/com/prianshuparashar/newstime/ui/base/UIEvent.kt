package com.prianshuparashar.newstime.ui.base

sealed interface UIEvent {
    // One-off UI Events
    data class OpenCustomTabs(val url: String) : UIEvent

    data class NavigateToArticles(
        val sourceId: String? = null,
        val country: String? = null,
        val language: String? = null,
        val query: String? = null,
        val title: String? = null
    ) : UIEvent

    data class ShowToast(val message: String) : UIEvent
}