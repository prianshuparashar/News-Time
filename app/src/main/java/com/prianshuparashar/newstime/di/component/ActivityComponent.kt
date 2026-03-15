package com.prianshuparashar.newstime.di.component

import com.prianshuparashar.newstime.ui.MainActivity
import com.prianshuparashar.newstime.di.module.ActivityModule
import com.prianshuparashar.newstime.di.scopes.ActivityScope
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {
    fun inject(activity: MainActivity)

    @Subcomponent.Builder
    interface Builder {
        fun activityModule(activityModule: ActivityModule): Builder
        fun build(): ActivityComponent
    }
}