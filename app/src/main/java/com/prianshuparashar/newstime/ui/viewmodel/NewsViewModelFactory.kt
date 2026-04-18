package com.prianshuparashar.newstime.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prianshuparashar.newstime.data.repository.NewsRepository

class NewsViewModelFactory(
    private val newsRepository: NewsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(NewsViewModel::class.java)      -> NewsViewModel(newsRepository) as T
        modelClass.isAssignableFrom(SourceViewModel::class.java)    -> SourceViewModel(newsRepository) as T
        modelClass.isAssignableFrom(LanguageViewModel::class.java)  -> LanguageViewModel() as T
        modelClass.isAssignableFrom(CountryViewModel::class.java)   -> CountryViewModel() as T
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}