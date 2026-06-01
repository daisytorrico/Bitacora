package com.catedra.bitacora.features.map.di

import com.catedra.bitacora.features.map.data.repository.MapRepositoryImpl
import com.catedra.bitacora.features.map.domain.repository.MapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MapModule {
    @Binds
    @Singleton
    abstract fun bindMapRepository(
        impl: MapRepositoryImpl
    ): MapRepository
}
