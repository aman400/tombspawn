package com.tombspawn.skeleton.di

import com.tombspawn.skeleton.gradle.CommandExecutor
import com.tombspawn.skeleton.gradle.GradleExecutor
import dagger.Binds
import dagger.Module

@Module
interface ServiceModule {
    @Binds
    fun provideGradleExecutor(gradleExecutor: GradleExecutor): CommandExecutor
}