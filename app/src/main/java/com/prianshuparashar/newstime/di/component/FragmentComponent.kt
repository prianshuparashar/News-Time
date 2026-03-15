package com.prianshuparashar.newstime.di.component

import com.prianshuparashar.newstime.di.module.FragmentModule
import com.prianshuparashar.newstime.di.scopes.FragmentScope
import com.prianshuparashar.newstime.ui.fragment.CountryFragment
import com.prianshuparashar.newstime.ui.fragment.HomeFragment
import com.prianshuparashar.newstime.ui.fragment.LanguageFragment
import com.prianshuparashar.newstime.ui.fragment.NewsListFragment
import com.prianshuparashar.newstime.ui.fragment.SearchFragment
import com.prianshuparashar.newstime.ui.fragment.SourceFragment
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [FragmentModule::class])
interface FragmentComponent {

    fun inject(fragment: HomeFragment)
    fun inject(fragment: NewsListFragment)
    fun inject(fragment: SourceFragment)
    fun inject(fragment: CountryFragment)
    fun inject(fragment: LanguageFragment)
    fun inject(fragment: SearchFragment)

    @Subcomponent.Builder
    interface Builder {
        fun fragmentModule(module: FragmentModule): Builder
        fun build(): FragmentComponent
    }
}