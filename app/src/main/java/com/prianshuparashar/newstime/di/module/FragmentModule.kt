package com.prianshuparashar.newstime.di.module

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.prianshuparashar.newstime.data.repository.NewsRepository
import com.prianshuparashar.newstime.di.qualifiers.FragmentContext
import com.prianshuparashar.newstime.ui.viewmodel.NewsViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class FragmentModule(private val fragment: Fragment) {

    @Provides
    @FragmentContext
    fun provideContext(): Context = fragment.requireContext()

    @Provides
    fun provideViewModelFactory(newsRepository: NewsRepository): NewsViewModelFactory =
        NewsViewModelFactory(newsRepository)

    @Provides
    fun provideViewModelProvider(factory: NewsViewModelFactory): ViewModelProvider =
        ViewModelProvider(fragment, factory)
}