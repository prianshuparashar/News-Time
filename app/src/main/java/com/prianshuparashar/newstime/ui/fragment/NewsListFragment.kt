package com.prianshuparashar.newstime.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.prianshuparashar.newstime.NewsApplication
import com.prianshuparashar.newstime.R
import com.prianshuparashar.newstime.common.dispatcher.DispatcherProvider
import com.prianshuparashar.newstime.data.model.ApiArticle
import com.prianshuparashar.newstime.databinding.FragmentNewsListBinding
import com.prianshuparashar.newstime.di.module.FragmentModule
import com.prianshuparashar.newstime.ui.adapter.LoadStateAdapter
import com.prianshuparashar.newstime.ui.adapter.NewsAdapter
import com.prianshuparashar.newstime.ui.base.PaginationState
import com.prianshuparashar.newstime.ui.base.UIEvent
import com.prianshuparashar.newstime.ui.util.PaginationScrollListener
import com.prianshuparashar.newstime.ui.viewmodel.NewsViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewsListFragment : Fragment() {

    private var _binding: FragmentNewsListBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelProvider: ViewModelProvider

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    private lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var loadStateAdapter: LoadStateAdapter
    private lateinit var concatAdapter: ConcatAdapter

    private var contentSnackBar: Snackbar? = null

    // Navigation arguments
    private val args: NewsListFragmentArgs by navArgs()
    private val sourceId get() = args.sourceId
    private val country get() = args.country
    private val language get() = args.language
    private val query get() = args.query
    private val title get() = args.title

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObservers()
    }

    private fun setupView() {
        newsViewModel = viewModelProvider[NewsViewModel::class.java]

        setTitle()
        setAdapters()

        with(binding) {
            recyclerViewNews.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = concatAdapter
                addOnScrollListener(object : PaginationScrollListener(
                    recyclerViewNews.layoutManager as LinearLayoutManager,
                    dispatcherProvider,
                    viewLifecycleOwner.lifecycleScope
                ) {
                    override fun loadMoreItems(): Unit = triggerLoad(true)

                    override fun isLoading(): Boolean =
                        newsViewModel.paginationState.value is PaginationState.LoadingMore

                    override fun isLastPage(): Boolean {
                        val state = newsViewModel.paginationState.value
                        return state is PaginationState.Success && !state.hasMore
                    }
                })
            }
            swipeRefreshLayout.setOnRefreshListener(newsViewModel::forceRefresh)
        }

        triggerLoad()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observePaginationState() }
                launch { observeUIEvents() }
                launch { observeNewContent() }
            }
        }
    }

    private fun setTitle() {
        binding.textTitle.text = title ?: getString(
            when {
                !query.isNullOrBlank() -> R.string.search_results
                !sourceId.isNullOrBlank() -> R.string.source_news
                !language.isNullOrBlank() -> R.string.news_by_language
                !country.isNullOrBlank() -> R.string.news_by_country
                else -> R.string.top_headlines
            }
        )
    }

    private fun setAdapters() {
        newsAdapter = NewsAdapter(newsViewModel::onArticleClicked)
        loadStateAdapter = LoadStateAdapter(
            viewLifecycleOwner.lifecycleScope, dispatcherProvider, newsViewModel::retryLoadMore
        )
        concatAdapter = ConcatAdapter(newsAdapter, loadStateAdapter)
    }

    private fun triggerLoad(loadMore: Boolean = false) {
        when {
            !query.isNullOrBlank() -> newsViewModel.searchNewsWithPagination(
                query = query!!, language = language, loadMore = loadMore
            )

            !sourceId.isNullOrBlank() -> newsViewModel.fetchArticlesWithPagination(
                sourceId = sourceId!!, loadMore = loadMore
            )

            !language.isNullOrBlank() -> newsViewModel.fetchTopHeadlinesWithPagination(
                language = language!!, loadMore = loadMore
            )

            else -> newsViewModel.fetchTopHeadlinesWithPagination(
                country = country, loadMore = loadMore
            )
        }
    }

    private suspend fun observePaginationState() {
        newsViewModel.paginationState.collect { state ->
            binding.swipeRefreshLayout.isRefreshing = false

            when (state) {
                is PaginationState.InitialLoading -> handleInitialLoading()
                is PaginationState.LoadingMore -> handleLoadingMore()
                is PaginationState.Success -> handleSuccess(state)
                is PaginationState.Error -> handleError(state)
                PaginationState.Idle -> handleIdle()
            }
        }
    }

    private fun handleInitialLoading() = with(binding) {
        skeletonLoading.root.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        swipeRefreshLayout.visibility = View.GONE
        emptyState.root.visibility = View.GONE
        errorState.root.visibility = View.GONE
        loadStateAdapter.setLoadState(LoadStateAdapter.LoadState.NotLoading)
    }

    private fun handleLoadingMore() = with(binding) {
        skeletonLoading.root.visibility = View.GONE
        progressBar.visibility = View.GONE
        swipeRefreshLayout.visibility = View.VISIBLE
        errorState.root.visibility = View.GONE
        loadStateAdapter.setLoadState(LoadStateAdapter.LoadState.Loading)
    }

    private fun handleSuccess(state: PaginationState.Success<*>) = with(binding) {
        skeletonLoading.root.visibility = View.GONE
        progressBar.visibility = View.GONE
        errorState.root.visibility = View.GONE

        if (state.data.isEmpty()) {
            swipeRefreshLayout.visibility = View.GONE
            emptyState.root.visibility = View.VISIBLE
        } else {
            swipeRefreshLayout.visibility = View.VISIBLE
            emptyState.root.visibility = View.GONE
            newsAdapter.submitList(state.data.filterIsInstance<ApiArticle>())
        }
    }

    private fun handleIdle() = with(binding) {
        skeletonLoading.root.visibility = View.GONE
        progressBar.visibility = View.GONE
        emptyState.root.visibility = View.GONE
        errorState.root.visibility = View.GONE
        loadStateAdapter.setLoadState(LoadStateAdapter.LoadState.NotLoading)
    }

    private fun handleError(state: PaginationState.Error) {
        if (state.isInitialLoad) {
            with(binding) {
                skeletonLoading.root.visibility = View.GONE
                progressBar.visibility = View.GONE
                swipeRefreshLayout.visibility = View.GONE
                emptyState.root.visibility = View.GONE
                errorState.root.visibility = View.VISIBLE
                errorState.textErrorTitle.text = state.message
                errorState.buttonRetry.setOnClickListener { triggerLoad() }
            }
        } else {
            loadStateAdapter.setLoadState(LoadStateAdapter.LoadState.Error(state.message))
        }
    }

    private suspend fun observeUIEvents() {
        newsViewModel.events.collect { event ->
            when (event) {
                is UIEvent.OpenCustomTabs -> {
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(requireContext(), event.url.toUri())
                }

                is UIEvent.ShowToast -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }
    }

    private suspend fun observeNewContent() {
        newsViewModel.hasNewContent.collect { hasNewContent ->
            if (hasNewContent &&
                _binding != null &&
                viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            ) {
                showNewContentSnackBar()
            }
        }
    }

    private fun showNewContentSnackBar() {
        val currentBinding = _binding ?: return
        contentSnackBar?.dismiss()
        contentSnackBar = Snackbar.make(
            currentBinding.root, R.string.new_content_available, Snackbar.LENGTH_LONG
        ).setAction(R.string.refresh) {
            _binding?.let {
                newsViewModel.applyPendingRefresh()
                it.recyclerViewNews.smoothScrollToPosition(0)
            }
        }.setActionTextColor(requireContext().getColor(R.color.icon_accent))
        contentSnackBar?.show()
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
        contentSnackBar?.dismiss()
        contentSnackBar = null
        newsViewModel.dismissNewContentNotification()
        _binding = null
    }
}
