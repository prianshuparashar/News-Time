package com.prianshuparashar.newstime.common.util

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

/**
 * Custom debounce operator for Flow (avoids using preview API).
 * Returns a flow that mirrors the original flow, but filters out values
 * that are followed by newer values before the timeout elapses.
 * Uses channelFlow for thread-safe concurrent emissions.
 */
fun <T> Flow<T>.debounce(timeoutMillis: Long): Flow<T> = channelFlow {
    var debounceJob: Job? = null

    collect { value ->
        debounceJob?.cancel()
        debounceJob = launch {
            delay(timeoutMillis)
            send(value)
        }
    }
}