package com.prianshuparashar.newstime.di.component

import com.prianshuparashar.newstime.di.module.FragmentModule
import com.prianshuparashar.newstime.di.scopes.FragmentScope
import com.prianshuparashar.newstime.ui.fragment.HomeFragment
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [FragmentModule::class])
interface FragmentComponent {

    fun inject(fragment: HomeFragment)

    @Subcomponent.Builder
    interface Builder {
        fun fragmentModule(module: FragmentModule): Builder
        fun build(): FragmentComponent
    }
}