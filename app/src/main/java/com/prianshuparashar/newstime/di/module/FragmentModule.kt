package com.prianshuparashar.newstime.di.module

import android.content.Context
import androidx.fragment.app.Fragment
import com.prianshuparashar.newstime.di.qualifiers.FragmentContext
import dagger.Module
import dagger.Provides

@Module
class FragmentModule(private val fragment: Fragment) {

    @Provides
    @FragmentContext
    fun provideContext(): Context = fragment.requireContext()
}