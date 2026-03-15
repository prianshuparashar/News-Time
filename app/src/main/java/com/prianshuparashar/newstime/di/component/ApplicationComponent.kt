package com.prianshuparashar.newstime.di.component

import com.prianshuparashar.newstime.NewsApplication
import com.prianshuparashar.newstime.di.module.ApplicationModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: NewsApplication)
    fun getActivityComponentBuilder(): ActivityComponent.Builder
    fun getFragmentComponentBuilder(): FragmentComponent.Builder
}