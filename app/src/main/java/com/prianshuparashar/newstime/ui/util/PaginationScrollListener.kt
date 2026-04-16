package com.prianshuparashar.newstime.ui.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prianshuparashar.newstime.common.constant.Const
import com.prianshuparashar.newstime.common.dispatcher.DispatcherProvider
import com.prianshuparashar.newstime.common.util.debounce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

abstract class PaginationScrollListener(
    private val layoutManager: LinearLayoutManager,
    dispatcherProvider: DispatcherProvider,
    scope: CoroutineScope
) : RecyclerView.OnScrollListener() {

    private val loadMoreTrigger = MutableSharedFlow<Unit>(
        extraBufferCapacity = Const.BUFFER_CAPACITY, // Allow one extra emission without suspension
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        scope.launch {
            loadMoreTrigger.debounce(100)  // Debounce to avoid multiple rapid triggers
                .collect {
                    launch(dispatcherProvider.immediate) {
                        loadMoreItems()
                    }
                }
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (dy <= 0) return  // Only trigger on scroll down

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        val shouldLoadMore = !isLoading() &&
                !isLastPage() &&
                (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3 &&
                firstVisibleItemPosition >= 0

        if (shouldLoadMore) {
            loadMoreTrigger.tryEmit(Unit)
        }
    }

    abstract fun loadMoreItems()
    abstract fun isLoading(): Boolean
    abstract fun isLastPage(): Boolean
}