package com.prianshuparashar.newstime.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.SearchView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.prianshuparashar.newstime.NewsApplication
import com.prianshuparashar.newstime.R
import com.prianshuparashar.newstime.common.dispatcher.DispatcherProvider
import com.prianshuparashar.newstime.common.util.debounce
import com.prianshuparashar.newstime.data.model.ApiArticle
import com.prianshuparashar.newstime.databinding.FragmentSearchBinding
import com.prianshuparashar.newstime.di.module.FragmentModule
import com.prianshuparashar.newstime.ui.adapter.LoadStateAdapter
import com.prianshuparashar.newstime.ui.adapter.NewsAdapter
import com.prianshuparashar.newstime.ui.base.PaginationState
import com.prianshuparashar.newstime.ui.base.UIEvent
import com.prianshuparashar.newstime.ui.util.PaginationScrollListener
import com.prianshuparashar.newstime.ui.viewmodel.NewsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelProvider: ViewModelProvider

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    private lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var loadStateAdapter: LoadStateAdapter
    private lateinit var concatAdapter: ConcatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObservers()
    }

    private fun setupView() {
        newsViewModel = viewModelProvider[NewsViewModel::class.java]

        newsAdapter = NewsAdapter(newsViewModel::onArticleClicked)
        loadStateAdapter = LoadStateAdapter(
            viewLifecycleOwner.lifecycleScope,
            dispatcherProvider,
            newsViewModel::retryLoadMore
        )
        concatAdapter = ConcatAdapter(newsAdapter, loadStateAdapter)

        with(binding) {
            recyclerViewNews.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = newsAdapter
                addOnScrollListener(paginationScrollListener())
            }
            swipeRefreshLayout.setOnRefreshListener {
                val query = searchView.query?.toString()
                if (!query.isNullOrBlank() && query.length >= 2) newsViewModel.forceRefresh()
                else swipeRefreshLayout.isRefreshing = false
            }
        }

        setupSearchFlow()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observePaginationState() }
                launch { observeEvents() }
            }
        }
    }

    private fun paginationScrollListener() = object : PaginationScrollListener(
        binding.recyclerViewNews.layoutManager as LinearLayoutManager,
        dispatcherProvider,
        viewLifecycleOwner.lifecycleScope
    ) {
        override fun loadMoreItems() {
            val query = binding.searchView.query?.toString()
            if (!query.isNullOrBlank() && query.length >= 2)
                newsViewModel.searchNewsWithPagination(query, loadMore = true)
        }

        override fun isLoading() =
            newsViewModel.paginationState.value is PaginationState.LoadingMore

        override fun isLastPage(): Boolean {
            val state = newsViewModel.paginationState.value
            return state is PaginationState.Success && !state.hasMore
        }
    }

    private fun setupSearchFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.searchView.getQueryTextChangeStateFlow()
                    .debounce(300)
                    .filter { isValidSearchQuery(it) }
                    .distinctUntilChanged()
                    .collect { query -> newsViewModel.searchNewsWithPagination(query) }
            }
        }
    }

    private fun isValidSearchQuery(query: String): Boolean {
        val hint = when {
            query.isEmpty()     -> getString(R.string.search_start_typing)
            query.length < 2    -> getString(R.string.search_min_characters)
            else                -> return true
        }
        showHint(hint)
        return false
    }

    private fun showHint(hint: String) = with(binding) {
        textViewHint.text = hint
        applyVisibility(hint = true)
    }

    private suspend fun observePaginationState() {
        newsViewModel.paginationState.collect { state ->
            binding.swipeRefreshLayout.isRefreshing = false
            when (state) {
                is PaginationState.InitialLoading       -> showInitialLoading()
                is PaginationState.LoadingMore          -> showLoadMore()
                is PaginationState.Success<*>           -> handleSuccess(state)
                is PaginationState.Error                -> handleError(state)
                is PaginationState.Idle                 -> showIdle()
            }
        }
    }

    private suspend fun observeEvents() {
        newsViewModel.events.collect { event ->
            when (event) {
                is UIEvent.OpenCustomTabs -> CustomTabsIntent.Builder().build()
                    .launchUrl(requireContext(), event.url.toUri())

                is UIEvent.ShowToast -> Toast.makeText(
                    requireContext(), event.message, Toast.LENGTH_SHORT
                ).show()

                else -> Unit
            }
        }
    }

    private fun showInitialLoading() {
        applyVisibility(progress = true)
        loadStateAdapter.setLoadState(LoadStateAdapter.LoadState.NotLoading)
    }

    private fun showLoadMore() {
        applyVisibility(swipeRefresh = true)
        loadStateAdapter.setLoadState(LoadStateAdapter.LoadState.Loading)
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleSuccess(state: PaginationState.Success<*>) {
        if (state.data.isEmpty()) {
            applyVisibility(empty = true)
        } else {
            applyVisibility(swipeRefresh = true)
            newsAdapter.submitList(state.data as List<ApiArticle>)
            loadStateAdapter.setLoadState(LoadStateAdapter.LoadState.NotLoading)
        }
    }

    private fun handleError(state: PaginationState.Error) {
        if (state.isInitialLoad) {
            applyVisibility(error = true)
            with(binding) {
                applyVisibility(error = true)
                errorState.textErrorTitle.text = state.message
                errorState.buttonRetry.setOnClickListener {
                    val query = searchView.query?.toString()
                    if (!query.isNullOrBlank() && query.length >= 2)
                        newsViewModel.searchNewsWithPagination(query)
                }
            }
        } else loadStateAdapter.setLoadState(LoadStateAdapter.LoadState.Error(state.message))
    }

    private fun showIdle() {
        applyVisibility()
        loadStateAdapter.setLoadState(LoadStateAdapter.LoadState.NotLoading)
    }

    private fun applyVisibility(
        progress: Boolean = false,
        hint: Boolean = false,
        swipeRefresh: Boolean = false,
        empty: Boolean = false,
        error: Boolean = false,
    ) = with(binding) {
        progressBar.isVisible = progress
        textViewHint.isVisible = hint
        swipeRefreshLayout.isVisible = swipeRefresh
        emptyState.root.isVisible = empty
        errorState.root.isVisible = error
    }

    private fun injectDependencies() {
        (requireActivity().application as NewsApplication)
            .applicationComponent
            .getFragmentComponentBuilder()
            .fragmentModule(FragmentModule(this))
            .build()
            .inject(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun SearchView.getQueryTextChangeStateFlow(): StateFlow<String> {
    val query = MutableStateFlow("")
    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            this@getQueryTextChangeStateFlow.clearFocus()
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean {
            query.value = newText
            return true
        }
    })
    return query
}
