package com.prianshuparashar.newstime.di.module

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.prianshuparashar.newstime.data.repository.NewsRepository
import com.prianshuparashar.newstime.di.qualifiers.ActivityContext
import com.prianshuparashar.newstime.ui.viewmodel.NewsViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(private val activity: AppCompatActivity) {
    @Provides
    @ActivityContext
    fun provideContext(): Context = activity

    @Provides
    fun provideViewModelFactory(newsRepository: NewsRepository): NewsViewModelFactory =
        NewsViewModelFactory(newsRepository)

    @Provides
    fun provideViewModelProvider(factory: NewsViewModelFactory): ViewModelProvider =
        ViewModelProvider(activity, factory)
}