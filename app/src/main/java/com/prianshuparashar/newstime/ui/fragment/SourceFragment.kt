package com.prianshuparashar.newstime.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prianshuparashar.newstime.NewsApplication
import com.prianshuparashar.newstime.databinding.FragmentSourceBinding
import com.prianshuparashar.newstime.di.module.FragmentModule
import com.prianshuparashar.newstime.ui.adapter.SourceAdapter
import com.prianshuparashar.newstime.ui.base.UIEvent
import com.prianshuparashar.newstime.ui.base.UIState
import com.prianshuparashar.newstime.ui.viewmodel.SourceViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SourceFragment : Fragment() {

    private var _binding: FragmentSourceBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelProvider: ViewModelProvider

    private lateinit var sourceViewModel: SourceViewModel
    private lateinit var sourceAdapter: SourceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSourceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObservers()
    }

    private fun setupView() {
        sourceViewModel = viewModelProvider[SourceViewModel::class.java]
        sourceAdapter = SourceAdapter(sourceViewModel::onSourceClicked)

        with(binding) {
            recyclerViewSources.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewSources.adapter = sourceAdapter
        }

        sourceViewModel.fetchSources()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeSourcesState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeSourcesState() {
        sourceViewModel.sourceState.collect { state ->
            with(binding) {
                when (state) {
                    is UIState.Loading -> {
                        progressBar.isVisible = true
                        errorState.root.isVisible = false
                        cardContainer.isVisible = true
                    }

                    is UIState.Success -> {
                        progressBar.isVisible = false
                        errorState.root.isVisible = false
                        cardContainer.isVisible = true
                        sourceAdapter.submitList(state.data)
                    }

                    is UIState.Error -> {
                        progressBar.isVisible = false
                        cardContainer.isVisible = false
                        errorState.root.isVisible = true
                        errorState.textErrorTitle.text = state.message
                        errorState.buttonRetry.setOnClickListener { sourceViewModel.fetchSources() }
                    }

                    else -> {
                        progressBar.isVisible = false
                        errorState.root.isVisible = false
                    }
                }
            }
        }
    }

    private suspend fun observeEvents() {
        sourceViewModel.events.collect { event ->
            when (event) {
                is UIEvent.NavigateToArticles -> {
                    val action = SourceFragmentDirections.actionSourceToNewsList(
                        sourceId = event.sourceId,
                        country = event.country,
                        language = event.language,
                        query = event.query,
                        title = event.title
                    )
                    findNavController().navigate(action)
                }

                else -> Unit
            }
        }
    }

    private fun injectDependencies() {
        (requireActivity().application as NewsApplication)
            .applicationComponent
            .getFragmentComponentBuilder()
            .fragmentModule(FragmentModule(this))
            .build()
            .inject(this)
    }
}