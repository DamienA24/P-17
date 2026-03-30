package com.openclassrooms.rebonnte.di

import com.openclassrooms.rebonnte.data.repository.AisleRepository
import com.openclassrooms.rebonnte.data.repository.AisleRepositoryImpl
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import com.openclassrooms.rebonnte.data.repository.AuthRepositoryImpl
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.data.repository.MedicineRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindAisleRepository(impl: AisleRepositoryImpl): AisleRepository

    @Binds @Singleton
    abstract fun bindMedicineRepository(impl: MedicineRepositoryImpl): MedicineRepository
}
