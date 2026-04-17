package com.prianshuparashar.newstime.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prianshuparashar.newstime.NewsApplication
import com.prianshuparashar.newstime.databinding.FragmentLanguageBinding
import com.prianshuparashar.newstime.di.module.FragmentModule
import com.prianshuparashar.newstime.ui.adapter.LanguageAdapter
import com.prianshuparashar.newstime.ui.base.UIEvent
import com.prianshuparashar.newstime.ui.base.UIState
import com.prianshuparashar.newstime.ui.viewmodel.LanguageViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelProvider: ViewModelProvider
    private lateinit var languageViewModel: LanguageViewModel
    private lateinit var languageAdapter: LanguageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObservers()
    }

    private fun setupView() {
        languageViewModel = viewModelProvider[LanguageViewModel::class.java]
        languageAdapter = LanguageAdapter(languageViewModel::onLanguageSelected)
        with(binding) {
            recyclerViewLanguages.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = languageAdapter
            }
        }
        languageViewModel.loadLanguages()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeLanguageState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeLanguageState() {
        languageViewModel.languagesState.collect { state ->
            if (state is UIState.Success) languageAdapter.submitList(state.data)
        }
    }

    private suspend fun observeEvents() {
        languageViewModel.events.collect { event ->
            if (event is UIEvent.NavigateToArticles) {
                val action = LanguageFragmentDirections.actionLanguageToNewsList(
                    sourceId = event.sourceId,
                    country = event.country,
                    language = event.language,
                    query = event.query,
                    title = event.title
                )
                findNavController().navigate(action)
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