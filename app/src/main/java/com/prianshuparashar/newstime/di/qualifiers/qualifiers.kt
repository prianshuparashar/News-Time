package com.prianshuparashar.newstime.di.qualifiers

import javax.inject.Qualifier

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationContext

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ActivityContext

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class FragmentContext

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class BaseUrl

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class NetworkApiKey

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DatabaseName