package com.tombspawn.di.qualifiers

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppCacheMap

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApkCacheMap

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionMapKey