package com.prianshuparashar.newstime.di.scopes

import javax.inject.Scope

/**
 * Scope for dependencies that should live as long as a Fragment.
 * BINARY retention: Scope annotations are needed only at compile-time for Dagger's code generation.
 * No runtime reflection needed, resulting in better performance and smaller APK size.
 */
@Scope
@Retention(AnnotationRetention.BINARY)
annotation class FragmentScope()
