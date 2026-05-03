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
import com.prianshuparashar.newstime.databinding.FragmentCountryBinding
import com.prianshuparashar.newstime.di.module.FragmentModule
import com.prianshuparashar.newstime.ui.adapter.CountryAdapter
import com.prianshuparashar.newstime.ui.base.UIEvent
import com.prianshuparashar.newstime.ui.base.UIState
import com.prianshuparashar.newstime.ui.viewmodel.CountryViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class CountryFragment : Fragment() {

    private var _binding: FragmentCountryBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelProvider: ViewModelProvider
    private lateinit var countryViewModel: CountryViewModel
    private lateinit var countryAdapter: CountryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObservers()
    }

    private fun setupView() {
        countryViewModel = viewModelProvider[CountryViewModel::class.java]
        countryAdapter = CountryAdapter(countryViewModel::onCountrySelected)
        with(binding) {
            recyclerViewCountries.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = countryAdapter
            }
        }
        countryViewModel.loadCountries()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeCountriesState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeCountriesState() {
        countryViewModel.countryState.collect { state ->
            if (state is UIState.Success) countryAdapter.submitList(state.data)
        }
    }

    private suspend fun observeEvents() {
        countryViewModel.events.collect { event ->
            if (event is UIEvent.NavigateToArticles) {
                val action = CountryFragmentDirections.actionCountryToNewsList(
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}