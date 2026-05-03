package com.prianshuparashar.newstime.common.constant

object Const {
    const val NETWORK_TIMEOUT = 30L
    const val BASE_URL = "https://newsapi.org/v2/"
    const val API_KEY_PARAM = "apiKey"
    const val DATABASE_NAME = "news_database"
    const val DATABASE_VERSION = 1
    const val DEFAULT_LANGUAGE = "en"
    const val DEFAULT_COUNTRY = "us"
    const val INITIAL_PAGE = 1
    const val SELECTED_POSITION_NONE = -1
    const val PAGE_SIZE = 20
    const val ERROR_UNKNOWN = "An unknown error occurred"
    const val ERROR_NO_INTERNET = "No internet connection"
    const val ERROR_EMPTY_QUERY = "Search query cannot be empty"
    const val PENDING_REFRESH_EXPIRY_MS = 5 * 60 * 1000L // 5 minutes
    const val BUFFER_CAPACITY = 1

    object Cache {
        const val EXPIRY_MINUTES = 30L
    }
}
