package com.prianshuparashar.newstime.di.scopes

import javax.inject.Scope

/**
 * Scope for dependencies that should live as long as an Activity.
 * BINARY retention: Scope annotations are needed only at compile-time for Dagger's code generation.
 * No runtime reflection needed, resulting in better performance and smaller APK size.
 */
@Retention(AnnotationRetention.BINARY)
@Scope
annotation class ActivityScope