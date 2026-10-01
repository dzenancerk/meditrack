package com.example.meditrack.data.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindPodsjetniciRepository(impl: PodsjetniciRepositoryImpl): PodsjetniciRepository

    @Binds
    abstract fun bindParametriRepository(impl: ParametriRepositoryImpl): ParametriRepository

    @Binds
    abstract fun bindKrvniPritisakRepository(impl: KrvniPritisakRepositoryImpl): KrvniPritisakRepository

    @Binds
    abstract fun bindNalaziRepository(impl: NalaziRepositoryImpl): NalaziRepository
}