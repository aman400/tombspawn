package com.tombspawn.skeleton.di.qualifiers

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppDir

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CloneDir