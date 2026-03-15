package com.prianshuparashar.newstime

import android.app.Application
import com.prianshuparashar.newstime.di.component.ApplicationComponent
import com.prianshuparashar.newstime.di.component.DaggerApplicationComponent
import com.prianshuparashar.newstime.di.module.ApplicationModule

class NewsApplication : Application() {
    lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        injectDependencies()
    }

    private fun injectDependencies() {
        applicationComponent = DaggerApplicationComponent
            .builder()
            .applicationModule(ApplicationModule(this))
            .build()
        applicationComponent.inject(this)
    }
}