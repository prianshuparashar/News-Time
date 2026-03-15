package com.prianshuparashar.newstime.di.module

import android.content.Context
import com.prianshuparashar.newstime.NewsApplication
import com.prianshuparashar.newstime.di.qualifiers.ApplicationContext
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: NewsApplication) {
    @Provides
    @Singleton
    @ApplicationContext
    fun provideContext(): Context = application
}