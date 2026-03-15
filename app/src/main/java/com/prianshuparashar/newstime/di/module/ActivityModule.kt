package com.prianshuparashar.newstime.di.module

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.prianshuparashar.newstime.di.qualifiers.ActivityContext
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(private val activity: AppCompatActivity) {
    @Provides
    @ActivityContext
    fun provideContext(): Context = activity
}